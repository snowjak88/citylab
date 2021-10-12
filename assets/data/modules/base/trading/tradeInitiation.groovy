intervalIteratingSystem 'traderCommodityOrderCreationSystem', Family.all( HasCommodityInventory, HasBankBalance, IsCommodityTransmutator, CanTradeCommodities ).exclude( HasPendingMarketOrders ).get(), tradeInterval, { entity, deltaTime ->
	
	//
	// Compute required quantities of transmutation reagents.
	// Compute produced quantities of transmutation products.
	final requiredQuantities = [:]
	final producedQuantities = [:]
	
	final transmutator = isCommodityTransmutatorMapper.get(entity)
	transmutator.transmutations.each { id, transmutation ->
		transmutation.reagents.each { commodityID, quantity ->
			requiredQuantities[commodityID] = quantity + ( requiredQuantities[commodityID] ?: 0f )
		}
		transmutation.products.each { commodityID, quantity ->
			producedQuantities[commodityID] = quantity + ( producedQuantities[commodityID] ?: 0f )
		}
	}
	
	final inventory = inventoryMapper.get(entity)
	final canTrade = tradeMapper.get(entity)
	final balance = balanceMapper.get(entity)
	
	//
	// Inspect each required-quantity in turn, to see how much we want to buy of each.
	requiredQuantities.each { commodityID, explicitRequiredQuantity ->
		
		//
		// The *actually*-required quantity is equal to our supposed required quantity, minus the
		// amount we produce "at home".
		final requiredQuantity = Util.max( 0f, explicitRequiredQuantity - ( producedQuantities[commodityID] ?: 0f ) )
		
		def marketOrders = pendingOrdersMapper.get(entity)
		final existingBid = marketOrders?.bids?.get(commodityID)
		//
		// If we have an existing bid that was completed -- get rid of it. We don't need it anymore
		if(existingBid?.complete) {
			marketOrders?.bids[commodityID]?.remove existingBid
			Pools.free existingBid
		}
		//
		// If we have an existing bid that's still pending -- then don't submit a new bid just yet.
		if(existingBid && !existingBid.complete)
			return
		
		//
		// How much do we have on-hand?
		final onHandQuantity = inventory.inventory[commodityID] ?: 0f
		
		//
		// How much would we like to buy
		final desiredQuantity = Util.max( 0, canTrade.timeHorizon * requiredQuantity - onHandQuantity )
		if(desiredQuantity <= 0)
			return
		
		//
		// Given our price-beliefs about this commodity, pick a price.
		if(!canTrade.priceBeliefs[commodityID])
			canTrade.priceBeliefs[commodityID] = [ commodityID: commodityID, min: 0.01, max: 100.0 ] as CanTradeCommodities.PriceBelief
		final minPriceBelief = canTrade.priceBeliefs[commodityID].min
		final maxPriceBelief = canTrade.priceBeliefs[commodityID].max
		final bidPrice = state.RND.nextDouble() * ( maxPriceBelief - minPriceBelief ) + minPriceBelief
		
		//
		// How favorable should we regard buying conditions just now?
		def float favorability = 1f
		
		if(canTrade.observedPrices[commodityID] && modules['market'].statistics[commodityID]) {
			final observed = canTrade.observedPrices[commodityID]
			final statistics = modules['market'].statistics[commodityID]
			favorability = 1f - ( statistics.mean - observed.low ) / ( observed.high - observed.low )
		}
		
		//
		// How much do we actually want to bid for, given favorability and our current bank-balance?
		final bidQuantity = Util.min( desiredQuantity * favorability, balance.balance / bidPrice )
		
		final bidOrder = Pools.obtain( MarketOrder )
		bidOrder.owner = entity
		bidOrder.commodityID = commodityID
		bidOrder.originalQuantity = bidQuantity
		bidOrder.quantity = bidQuantity
		bidOrder.price = bidPrice
		
		if(!marketOrders)
			marketOrders = entity.addAndReturn( state.engine.createComponent( HasPendingMarketOrders ) )
		marketOrders.bids[commodityID] = bidOrder
	}
	
	//
	// Inspect each produced-quantity in turn, to see how much we want to sell of each.
	producedQuantities.each { commodityID, explicitProducedQuantity ->
		
		//
		// The *actual* produced quantity equals the explicitly-given produced-quantity,
		// minus the amount we consume "at home".
		final producedQuantity = Util.max( 0f, explicitProducedQuantity - canTrade.timeHorizon * ( requiredQuantities[commodityID] ?: 0f ) )
		
		def marketOrders = pendingOrdersMapper.get(entity)
		final existingAsk = marketOrders?.asks?.get(commodityID)
		//
		// If we have an existing ask that was completed -- get rid of it. We don't need it anymore
		if(existingAsk?.complete) {
			marketOrders?.asks[commodityID]?.remove existingAsk
			Pools.free existingAsk
		}
		//
		// If we have an existing ask that's still pending -- then don't submit a new ask just yet.
		if(existingAsk && !existingAsk.complete)
			return
		
		//
		// How much do we have in the inventory?
		final onHandQuantity = ( inventory.inventory[commodityID] ?: 0f )
		
		//
		// How much would we like to sell?
		final desiredQuantity = Util.max( 0f, onHandQuantity - producedQuantity )
		if(desiredQuantity <= 0)
			return
		
		//
		// Given our price-beliefs about this commodity, pick a price.
		if(!canTrade.priceBeliefs[commodityID])
			canTrade.priceBeliefs[commodityID] = [ commodityID: commodityID, min: 0.01, max: 100.0 ] as CanTradeCommodities.PriceBelief
		final minPriceBelief = canTrade.priceBeliefs[commodityID].min
		final maxPriceBelief = canTrade.priceBeliefs[commodityID].max
		final askPrice = state.RND.nextDouble() * ( maxPriceBelief - minPriceBelief ) + minPriceBelief
		
		//
		// How favorable should we regard buying conditions just now?
		def float favorability = 1f
		
		if(canTrade.observedPrices[commodityID] && modules['market'].statistics[commodityID]) {
			final observed = canTrade.observedPrices[commodityID]
			final statistics = modules['market'].statistics[commodityID]
			favorability = ( statistics.mean - observed.low ) / ( observed.high - observed.low )
		}
		
		final askQuantity = desiredQuantity * favorability
		
		final askOrder = Pools.obtain( MarketOrder )
		askOrder.owner = entity
		askOrder.commodityID = commodityID
		askOrder.originalQuantity = askQuantity
		askOrder.quantity = askQuantity
		askOrder.price = askPrice
		
		if(!marketOrders)
			marketOrders = entity.addAndReturn( state.engine.createComponent( HasPendingMarketOrders ) )
		marketOrders.asks[commodityID] = askOrder
	}
}