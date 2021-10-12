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
pendingTradeMapper = ComponentMapper.getFor( IsPendingTrade )

//
//
//

intervalIteratingSystem 'traderBankruptcyIdentifyingSystem', Family.all( HasBankBalance ).exclude( HasPendingMarketOrders ).get(), tradeInterval, { entity, deltaTime ->
	entity.add state.engine.createComponent( Bankruptcy )
}

eventComponent Bankruptcy, { entity, deltaTime ->
	
	entity.remove CanTradeCommodities
	entity.remove HasPendingMarketOrders
	
}

include 'tradeInitiation.groovy'
include 'tradeAnalysis.groovy'
include 'tradeExecution.groovy'