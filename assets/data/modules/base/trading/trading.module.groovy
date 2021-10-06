id = 'trading'

dependsOn 'market'
dependsOn 'commodities'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

tradeInterval = preferences.getFloat( 'trade-interval', 5f )

//
//
//

inventoryMapper = ComponentMapper.getFor( HasCommodityInventory )
balanceMapper = ComponentMapper.getFor( HasBankBalance )
isCommodityTransmutatorMapper = ComponentMapper.getFor( IsCommodityTransmutator )

tradeMapper = ComponentMapper.getFor( CanTradeCommodities )
pendingOrdersMapper = ComponentMapper.getFor( HasPendingMarketOrders )

//
//
//

onActivate { ->
	final e1 = state.engine.createEntity()
	def inventory = e1.addAndReturn( state.engine.createComponent( HasCommodityInventory ) )
	inventory.inventory['water'] = 50
	inventory.inventory['corn'] = 0
	def balance = e1.addAndReturn( state.engine.createComponent( HasBankBalance ) )
	balance.balance = 1000
	def transmutations = e1.addAndReturn( state.engine.createComponent( IsCommodityTransmutator ) )
	def transmutation = new IsCommodityTransmutator.Transmutation()
	transmutation.reagents['water'] = 1
	transmutation.products['corn'] = 1
	transmutations.transmutations['e1'] = transmutation
	e1.add state.engine.createComponent( CanTradeCommodities )
	state.engine.addEntity e1
	
	final e2 = state.engine.createEntity()
	inventory = e2.addAndReturn( state.engine.createComponent( HasCommodityInventory ) )
	inventory.inventory['water'] = 0
	inventory.inventory['corn'] = 50
	balance = e2.addAndReturn( state.engine.createComponent( HasBankBalance ) )
	balance.balance = 1000
	transmutations = e2.addAndReturn( state.engine.createComponent( IsCommodityTransmutator ) )
	transmutation = new IsCommodityTransmutator.Transmutation()
	transmutation.reagents['corn'] = 1
	transmutation.products['water'] = 1
	transmutations.transmutations['e2'] = transmutation
	e2.add state.engine.createComponent( CanTradeCommodities )
	state.engine.addEntity e2
}

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
		final desiredQuantity = canTrade.timeHorizon * ( (onHandQuantity >= 1) ? (requiredQuantity*requiredQuantity / onHandQuantity) : requiredQuantity )
		
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
		
		final bidOrder = Pools.obtain(HasPendingMarketOrders.MarketOrder)
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
		final producedQuantity = Util.max( 0f, explicitProducedQuantity - ( requiredQuantities[commodityID] ?: 0f ) )
		
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
		
		final askOrder = Pools.obtain(HasPendingMarketOrders.MarketOrder)
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

intervalIteratingSystem 'traderCommodityOrderProcessingSystem', Family.all( CanTradeCommodities, HasPendingMarketOrders, IsCommodityTransmutator ).get(), tradeInterval, { entity, deltaTime ->
	
	//
	// Process each market-order in turn. If it's complete, then incorporate the reported price-information
	// back into this entity's beliefs.
	//
	final canTrade = tradeMapper.get(entity)
	final orders = pendingOrdersMapper.get(entity)
	final inventory = inventoryMapper.get(entity)
	
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
	
	orders.asks.each { commodityID, ask ->
		
		if(!ask.complete)
			return
		
		final statistics = modules['market'].statistics[commodityID]
		
		//
		// "Play-back" all the fulfillments we recorded.
		ask.fulfilled.each { f ->
			final weight = ask.quantity / ask.originalQuantity
			final displacement = weight * ( canTrade.priceBeliefs[commodityID].max + canTrade.priceBeliefs[commodityID].min ) / 2f
			
			//
			// If 0 quantity sold ...
			if(f.quantity <= 0) {
				//
				// Translate price-belief range downward by 1/6 of "displacement"
				final shift = displacement / 6f
				canTrade.priceBeliefs[commodityID].max = Util.max( 0.01, canTrade.priceBeliefs[commodityID].max - shift )
				canTrade.priceBeliefs[commodityID].min = Util.max( 0.01, canTrade.priceBeliefs[commodityID].min - shift )
				
			}
			//
			// If market-share is less than 75% ...
			else if (f.quantity / statistics.recentTotalVolume < 0.75f) {
				//
				// Translate price-belief range downward by 1/7 of "displacement"
				final shift = displacement / 7f
				canTrade.priceBeliefs[commodityID].max = Util.max( 0.01, canTrade.priceBeliefs[commodityID].max - shift )
				canTrade.priceBeliefs[commodityID].min = Util.max( 0.01, canTrade.priceBeliefs[commodityID].min - shift )
			}
			//
			// If offer-price < trade-price
			else if (ask.price < f.price) {
				//
				// Translate belief-range upward by 120% of weight * overbid
				final overbid = f.price - ask.price
				final shift = 1.2f * weight * overbid
				canTrade.priceBeliefs[commodityID].max = canTrade.priceBeliefs[commodityID].max + shift
				canTrade.priceBeliefs[commodityID].min = canTrade.priceBeliefs[commodityID].min + shift
			}
			//
			// If demand > supply
			else if (statistics.demand > statistics.supply) {
				//
				// Translate belief-range upward by 1/5 of the historical mean price
				final shift = statistics.mean / 5f
				canTrade.priceBeliefs[commodityID].max = canTrade.priceBeliefs[commodityID].max + shift
				canTrade.priceBeliefs[commodityID].min = canTrade.priceBeliefs[commodityID].min + shift
				
			} else {
				//
				// Translate belief range downward by 1/5 of the historical mean price
				final shift = statistics.mean / 5f
				canTrade.priceBeliefs[commodityID].max = Util.max( 0.01, canTrade.priceBeliefs[commodityID].max - shift )
				canTrade.priceBeliefs[commodityID].min = Util.max( 0.01, canTrade.priceBeliefs[commodityID].min - shift )
			}
		}
	}
	
	orders.bids.each { commodityID, bid ->
		
		if(!bid.complete)
			return
		
		final statistics = modules['market'].statistics[commodityID]
		
		//
		// "Play-back" all the fulfillments we recorded.
		bid.fulfilled.each { f ->
			//
			// If at least 50% of offer filled
			if(f.quantity / bid.originalQuantity >= 0.5f) {
				//
				// Move price-beliefs inward by 10% of upper limit
				final shift = canTrade.priceBeliefs[commodityID].max / 10f
				canTrade.priceBeliefs[commodityID].min = canTrade.priceBeliefs[commodityID].min + shift
				canTrade.priceBeliefs[commodityID].max = Util.max( canTrade.priceBeliefs[commodityID].min, canTrade.priceBeliefs[commodityID].max - shift )
				
			} else {
				//
				// Increase upper price-belief by 10%
				final shift = canTrade.priceBeliefs[commodityID].max / 10f
				canTrade.priceBeliefs[commodityID].max = canTrade.priceBeliefs[commodityID].max + shift
			}
			
			final requiredQuantity = requiredQuantities[commodityID] ?: 0f
			final onHandQuantity = inventory.inventory[commodityID] ?: 0f
			
			//
			// If less than full market-share and inventory < 1/4 desired-quantity ...
			if(f.quantity < statistics.recentTotalVolume && onHandQuantity / requiredQuantity < 0.25f) {
				//
				// Translate price-beliefs upward by "displacement"
				final displacement = statistics.mean - ( canTrade.priceBeliefs[commodityID].min + canTrade.priceBeliefs[commodityID].max ) / 2f
				canTrade.priceBeliefs[commodityID].min += displacement
				canTrade.priceBeliefs[commodityID].max += displacement
			}
			//
			// If offer-price > trade-price
			else if(bid.price > f.price) {
				//
				// Translate belief-range downward by 110% of "overbid"
				final overbid = bid.price - f.price
				final shift = overbid * 1.1f
				canTrade.priceBeliefs[commodityID].max = Util.max( 0.01, canTrade.priceBeliefs[commodityID].max - shift )
				canTrade.priceBeliefs[commodityID].min = Util.max( 0.01, canTrade.priceBeliefs[commodityID].min - shift )
			}
			//
			// If supply > demand and offer > historical-mean
			else if (statistics.supply > statistics.demand && bid.price > statistics.mean) {
				//
				// Translate belief-range downward by 110% of "overbid"
				final overbid = bid.price - statistics.mean
				final shift = overbid * 1.1f
				canTrade.priceBeliefs[commodityID].max = Util.max( 0.01, canTrade.priceBeliefs[commodityID].max - shift )
				canTrade.priceBeliefs[commodityID].min = Util.max( 0.01, canTrade.priceBeliefs[commodityID].min - shift )
			}
			//
			// If demand > supply
			else if (statistics.demand > statistics.supply) {
				//
				// Translate belief-range upward by 1/5 of historical-mean
				final shift = statistics.mean / 5f
				canTrade.priceBeliefs[commodityID].min += shift
				canTrade.priceBeliefs[commodityID].max += shift
				
			} else {
				//
				// Translate belief-range downward by 1/5 of historical-mean
				final shift = statistics.mean / 5f
				canTrade.priceBeliefs[commodityID].max = Util.max( 0.01, canTrade.priceBeliefs[commodityID].max - shift )
				canTrade.priceBeliefs[commodityID].min = Util.max( 0.01, canTrade.priceBeliefs[commodityID].min - shift )
			}
		}
	}
	
	orders.asks.removeAll { commodityID, order -> order == null || order.complete }
	orders.bids.removeAll { commodityID, order -> order == null || order.complete }
	
	if(orders.asks.isEmpty() && orders.bids.isEmpty())
		entity.remove HasPendingMarketOrders
}