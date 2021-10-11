//
// Gather HasPendingMarketOrders from entities into the asks, bids lists
//
intervalIteratingSystem 'markedOrderGatheringSystem', Family.all(HasPendingMarketOrders).get(), marketInterval, { entity, deltaTime ->
	
	final pending = pendingOrdersMapper.get(entity)
	
	pending.asks.each { commodityID, order ->
		if(!order.complete)
			incomingAsks << order
	}
	
	pending.bids.each { commodityID, order ->
		if(!order.complete)
			incomingBids << order
	}
}

//
// Market-processing system.
// Matches bid and ask orders.
// Updates published statistics.
//
intervalSystem 'marketProcessingSystem', marketInterval, { deltaTime ->
	
	final asks = [:]
	final bids = [:]
	
	final commodities = new LinkedHashSet()
	//
	// First: empty the ask and bid queues
	//
	
	statistics.each{ c, s -> s?.reset() }
	
	while(!incomingAsks.isEmpty()) {
		final incomingAsk = incomingAsks.pop()
		
		if(!incomingAsk.commodityID || !incomingAsk.owner || incomingAsk.quantity <= 0 || incomingAsk.price < 0) {
			incomingAsk.complete = true
			continue
		}
		
		asks.computeIfAbsent(incomingAsk.commodityID, { cid -> new LinkedList()}) << incomingAsk
		commodities << incomingAsk.commodityID
		
		if(!statistics[incomingAsk.commodityID])
			statistics[incomingAsk.commodityID] = [ commodityID: incomingAsk.commodityID ] as CommodityStatistics
		statistics[incomingAsk.commodityID].addDemand incomingAsk.quantity
	}
	
	while(!incomingBids.isEmpty()) {
		final incomingBid = incomingBids.pop()
		
		if(!incomingBid.commodityID || !incomingBid.owner || incomingBid.quantity <= 0 || incomingBid.price < 0) {
			incomingBid.complete = true
			continue
		}
		
		bids.computeIfAbsent(incomingBid.commodityID, { cid -> new LinkedList()}) << incomingBid
		commodities << incomingBid.commodityID
		
		if(!statistics[incomingBid.commodityID])
			statistics[incomingBid.commodityID] = [ commodityID: incomingBid.commodityID ] as CommodityStatistics
		statistics[incomingBid.commodityID].addSupply incomingBid.quantity
	}
	
	println "-=-=-=-=-=-=- MARKET OPEN -=-=-=-=-=-=-"
	
	//
	// Process each commodity in turn.
	//
	for(String commodityID : commodities) {
		
		//
		// Shuffle the bids and asks, and then sort by price.
		//
		if(bids[commodityID]) {
			bids[commodityID].shuffle state.RND
			bids[commodityID].sort()
		}
		if(asks[commodityID]) {
			asks[commodityID].shuffle state.RND
			asks[commodityID].sort()
		}
		
		if(!statistics[commodityID])
			statistics[commodityID] = [ commodityID: commodityID ] as CommodityStatistics
		
		//
		// Continue processing bids/asks until there are no more of either.
		//
		def canProcessOrders = true
		while(canProcessOrders) {
			
			//
			// Pick the cheapest ask and the biggest bid
			final cheapestAsk = asks[commodityID]?.peekFirst()
			final biggestBid = bids[commodityID]?.peekLast()
			
			if(!cheapestAsk || !biggestBid) {
				canProcessOrders = false
				continue
			}
			
			//
			// Is the "ask" valid?
			if(cheapestAsk.quantity < 0 || cheapestAsk.price < 0) {
				cheapestAsk.complete = true
				asks[commodityID].remove cheapestAsk
				continue
			}
			
			//
			// Is the "bid" valid?
			if(biggestBid.quantity < 0 || biggestBid.price < 0) {
				biggestBid.complete = true
				bids[commodityID].remove biggestBid
				continue
			}
			
			//
			// The buyer- and seller-prices will not be the same.
			// The transfer-cost per unit commodity is computed separately,
			// and affects the price the buyer ultimately pays. (The buyer always
			// pays for shipping, in this model.)
			//
			final float transferCost = 0f
			
			final float quantityExchanged = Util.min( cheapestAsk.quantity, biggestBid.quantity )
			final float averagePrice = ( cheapestAsk.price + biggestBid.price ) / 2f
			
			final float buyerPrice = averagePrice + transferCost
			final float sellerPrice = averagePrice
			
			final float totalBuyerPrice = buyerPrice * quantityExchanged
			final float totalSellerPrice = sellerPrice * quantityExchanged
			
			//
			// Now -- does the asker have the inventory to cover this order?
			final askerInventory = inventoryMapper.get( cheapestAsk.owner )
			if(!askerInventory || !askerInventory.inventory[commodityID] || askerInventory.inventory[commodityID] < quantityExchanged) {
				cheapestAsk.complete = true
				asks[commodityID].remove cheapestAsk
				continue
			}
			
			//
			// Does the bidder have the cash to cover this order?
			final bidderBalance = balanceMapper.get( biggestBid.owner )
			if(!bidderBalance || bidderBalance.balance < totalBuyerPrice) {
				biggestBid.complete = true
				bids[commodityID].remove biggestBid
			}
			
			//
			// OK -- everything seems to check out so far --
			//
			// Update the bid and ask orders
			cheapestAsk.quantity -= quantityExchanged
			cheapestAsk.addFulfilled sellerPrice, quantityExchanged
			
			if(cheapestAsk.quantity <= 0) {
				cheapestAsk.complete = true
				asks[commodityID].remove cheapestAsk
			}
			
			biggestBid.quantity -= quantityExchanged
			biggestBid.addFulfilled buyerPrice, quantityExchanged
			
			if(biggestBid.quantity <= 0) {
				biggestBid.complete = true
				bids[commodityID].remove biggestBid
			}
			
			//
			// Transfer inventory from asker to bidder
			askerInventory.inventory[commodityID] -= quantityExchanged
			
			def bidderInventory = inventoryMapper.get( biggestBid.owner )
			if(!bidderInventory)
				bidderInventory = biggestBid.owner.addAndReturn( state.engine.createComponent( HasCommodityInventory ) )
			bidderInventory.inventory[commodityID] = quantityExchanged + ( bidderInventory.inventory[commodityID] ?: 0f )
			
			//
			// Transfer currency from bidder to asker
			bidderBalance.balance -= totalBuyerPrice
			
			def askerBalance = balanceMapper.get( cheapestAsk.owner )
			if(!askerBalance)
				askerBalance = cheapestAsk.owner.addAndReturn( state.engine.createComponent( HasBankBalance ) )
			askerBalance.balance += totalSellerPrice
			
			//
			// Update statistics
			statistics[commodityID].addPrice sellerPrice, buyerPrice
			statistics[commodityID].addVolume quantityExchanged
			
			println "Transaction: $commodityID -- $quantityExchanged @ $averagePrice"
			
		}
		
		//
		// Mark all remaining bids/asks as rejected
		//
		if(bids[commodityID])
			bids[commodityID].each { o ->
				if(o.complete)
					return
				o.complete = true
			}
		
		if(asks[commodityID])
			asks[commodityID].each { o ->
				if(o.complete)
					return
				o.complete = true
			}
	}
	
	println "-=-=-=-=-=-=- MARKET CLOSE -=-=-=-=-=-=-"
	
	for(String commodityID : commodities) {
		final stat = statistics[commodityID]
		println "    $commodityID: ${stat?.sellerMean}\t[ ${stat?.sellerLow}, ${stat?.sellerHigh} ]\t${stat.recentTotalVolume} / ${stat.averageVolume}"
	}
	
	println "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
}