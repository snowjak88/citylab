id = 'commodities'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

transmutateUpdateInterval = preferences.getFloat( 'transmutate-interval', 1f )

//
//
//

hasCommodityInventoryMapper = ComponentMapper.getFor( HasCommodityInventory )
isCommodityTransmutatorMapper = ComponentMapper.getFor( IsCommodityTransmutator )

//
//
//

intervalIteratingSystem 'commodityTransmutationSystem', Family.all(IsCommodityTransmutator, HasCommodityInventory).get(), transmutateUpdateInterval, { entity, deltaTime ->
	
	final transmutator = isCommodityTransmutatorMapper.get(entity)
	final inventory = hasCommodityInventoryMapper.get(entity)
	
	//
	// Try to run each specified transmutation in turn.
	transmutator.transmutations.each { id, transmutations ->
		
		//
		// Ensure we have all reagents in the required quantities
		if(transmutations.reagents.any { reagentID, unitQuantity ->
					final trueQuantity = unitQuantity * deltaTime
					( inventory.inventory[reagentID] ?: 0 ) < trueQuantity
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