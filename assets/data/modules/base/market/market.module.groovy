id = 'market'

dependsOn 'commodities'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

//
//
//

//
// Incoming MarketOrders, ready to be picked up by the Market.
//
incomingAsks = new LinkedList<MarketOrder>()
incomingBids = new LinkedList<MarketOrder>()

provides incomingAsks named 'asks'
provides incomingBids named 'bids'

//
// Last transacted price by commodity-ID
//
lastPrices = [:]
provides lastPrices named 'lastPrices'

//
//
//

//
// Current set of asks and bids.
// A Map correlating commodity-IDs with TreeSets of MarketOrders (automatically sorted by price (ascending))
asks = [:]
bids = [:]

inventoryMapper = ComponentMapper.getFor(HasCommodityInventory)

//
// Market-processing system.
// Matches bid and ask orders.
// Updates published statistics.
//
intervalSystem 'marketProcessingSystem', 1, { deltaTime ->
	
	//
	// First: empty the ask and bid queues
	//
	while(!incomingAsks.isEmpty()) {
		final incomingAsk = incomingAsks.pop()
		
		if(!incomingAsk.commodityID)
			continue
		if(!incomingAsk.owner)
			continue
		if(incomingAsk.quantity <= 0)
			continue
		
		asks.computeIfAbsent(incomingAsk.commodityID, { cid -> new TreeSet()}) << incomingAsk
	}
	
	while(!incomingBids.isEmpty()) {
		final incomingBid = incomingBids.pop()
		
		if(!incomingBid.commodityID)
			continue
		if(!incomingBid.owner)
			continue
		if(incomingBid.quantity <= 0)
			continue
		
		bids.computeIfAbsent(incomingBid.commodityID, { cid -> new TreeSet()}) << incomingBid
	}
	
	//
	// Second: fulfill all ask orders we can.
	//
	// Process each commodity in order ...
	for(String commodityID : asks.keySet()) {
		
		//
		// Continue processing orders on this commodity until we can't process any more
		def canFulfillOrder = true
		while(canFulfillOrder) {
			
			//
			// What's the lowest-price ask for this commodity?
			final cheapestAsk = asks[commodityID].first()
			
			//
			// What about the highest-price bid?
			final biggestBid = bids[commodityID]?.last()
			
			//
			// Is this a match we can make?
			canFulfillOrder = ( cheapestAsk && biggestBid && cheapestAsk.price <= biggestBid.price && cheapestAsk.quantity > 0 && biggestBid.quantity > 0 )
			if(canFulfillOrder) {
				
				//
				// How much is actually being exchanged?
				final quantityExchanged = Util.min( biggestBid.quantity, cheapestAsk.quantity )
				
				//
				// Does the asker have sufficient inventory to cover this exchange?
				final askerInventory = inventoryMapper.get(cheapestAsk.owner)
				if(!askerInventory || askerInventory.inventory[commodityID] < quantityExchanged) {
					
					//
					// If not -- remove the asker's order, and continue to process orders for this commodity
					asks.remove cheapestAsk
					
					continue
				}
				
				//
				// If the bidder doesn't have an inventory yet (for whatever reason),
				// create one.
				def bidderInventory = inventoryMapper.get(biggestBid.owner)
				if(!bidderInventory)
					bidderInventory = biggestBid.owner.addAndReturn( state.engine.createComponent(HasCommodityInventory) )
				
				//
				// Update the bid/ask orders. Remove if they've been completely filled.
				biggestBid.quantity -= quantityExchanged
				if(biggestBid.quantity <= 0)
					bids.remove biggestBid
				
				cheapestAsk.quantity -= quantityExchanged
				if(cheapestAsk.quantity <= 0)
					asks.remove cheapestAsk
				
				//
				// Update the bidder's and asker's inventories
				askerInventory[commodityID] -= quantityExchanged
				bidderInventory[commodityID] += quantityExchanged
				
				//
				// Finally, update reported statistics
				lastPrices[commodityID] = cheapestAsk.price
				
			}
			
		}
	}
}