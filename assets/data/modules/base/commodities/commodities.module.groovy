id = 'commodities'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

commodityUpdateInterval = preferences.getFloat( 'update-interval', 1f )
commodityMarketInterval = preferences.getFloat( 'market-interval', 5f )

//
//
//

hasCommodityInventoryMapper = ComponentMapper.getFor( HasCommodityInventory )
isCommodityTransmutatorMapper = ComponentMapper.getFor( IsCommodityTransmutator )

sellSurplusMapper = ComponentMapper.getFor( CanSellSurplusCommodities )

//
//
//

intervalIteratingSystem 'commodityTransmutationSystem', Family.all(IsCommodityTransmutator, HasCommodityInventory).get(), commodityUpdateInterval, { entity, deltaTime ->
	
	final transmutator = isCommodityTransmutatorMapper.get(entity)
	final inventory = hasCommodityInventoryMapper.get(entity)
	
	//
	// Try to run each specified transmutation in turn.
	transmutator.transmutations.each { id, transmutations ->
		
		//
		// Ensure we have all reagents in the required quantities
		if(transmutations.reagents.any { reagentID, unitQuantity ->
					final trueQuantity = unitQuantity * deltaTime
					inventory.inventory[reagentID] < trueQuantity
				})
			return
		
		//
		// Debit the reagents from the inventory
		transmutations.reagents.each { reagentID, unitQuantity ->
			final trueQuantity = unitQuantity * deltaTime
			inventory.inventory[reagentID] -= trueQuantity
		}
		
		//
		// Credit the products to the inventory
		transmutations.products.each { productID, unitQuantity ->
			final trueQuantity = unitQuantity * deltaTime
			inventory.inventory[productID] = ( inventory.inventory[productID] ?: 0 ) + trueQuantity
		}
	}
}

intervalIteratingSystem 'commoditySurplusSellingSystem', Family.all(HasCommodityInventory, CanSellSurplusCommodities).get(), commodityMarketInterval, { entity, deltaTime ->
	
	final sellSurplus = sellSurplusMapper.get(entity)
	final inventory = hasCommodityInventoryMapper.get(entity)
	final transmutator = isCommodityTransmutatorMapper.get(entity)
	
	//
	// Look over each item of inventory
	inventory.inventory.each { commodityID, quantity ->
		
		//
		// is this commodity a reagent? a product? or something else?
		final isReagent = transmutator.transmutations.any { id, transmutations -> transmutations.reagents.containsKey(commodityID) }
		
		//
		// How much unit-quantity of this commodity do all our registered transmutations require?
		def requiredUnitQuantity = 0f
		
		if(isReagent)
			requiredUnitQuantity = transmutator.transmutations.values().sum( 0f, { it.reagents[commodityID] ?: 0f } )
		else
			requiredUnitQuantity = transmutator.transmutations.values().sum( 0f, { it.products[commodityID] ?: 0f } )
		
		//
		// What's the actual total quantity required, given our sell-surplus time-horizon?
		final requiredQuantity = requiredUnitQuantity * sellSurplus.timeHorizon
		
		//
		// Do we have at least that much in inventory?
		// If so, we can create a sell order, priced by:
		//
		//  sell-price == last-sale-price + 10% - ( 50 * excessInventory / requiredQuantity )%
		//
		// We will always try to sell by 10% more than the last sale-price.
		// But that sell-price is affected by how much excess inventory we have. If we have
		// 2x the required quantity, then we sell at -100% markup (i.e., at a cost of 0.0)
		//
		// If last-sale-price is not known, use 10 (why not?)
		// If requiredQuantity is 0, then don't do the mark-down at all
		//
		final excessInventory = Util.max( 0, quantity - requiredQuantity )
		
		final float lastSalePrice = modules['market'].lastPrices[commodityID] ?: 10f
		final float markdown = (requiredQuantity > 0) ? ( 0.5f * excessInventory / requiredQuantity ) : 0f
		final float sellPrice = ( lastSalePrice * 1.1f ) - ( lastSalePrice * Util.clamp( markdown, 0f, 1f ) )
		final float sellQuantity = Util.max( excessInventory * 0.25f, requiredQuantity * 0.25f )
		
		modules['market'].asks << ( [
			owner: entity,
			commodityID: commodityID,
			quantity: sellQuantity,
			price: sellPrice
		] as MarketOrder )
	}
}