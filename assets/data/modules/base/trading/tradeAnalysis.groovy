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
		if(ask.analyzedPostSale)
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
		
		ask.analyzedPostSale = true
	}
	
	orders.bids.each { commodityID, bid ->
		
		if(!bid.complete)
			return
		if(bid.analyzedPostSale)
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
		
		bid.analyzedPostSale = true
	}
	
	orders.asks.removeAll { commodityID, order -> order == null || ( order.complete && order.analyzedPostSale && order.delivered ) }
	orders.bids.removeAll { commodityID, order -> order == null || ( order.complete && order.analyzedPostSale && order.delivered ) }
	
	if(orders.asks.isEmpty() && orders.bids.isEmpty())
		entity.remove HasPendingMarketOrders
}