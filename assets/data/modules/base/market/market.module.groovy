id = 'market'

dependsOn 'commodities'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

//
//
//

marketInterval = preferences.getFloat( 'market-interval', 5f )

//
// Incoming MarketOrders, ready to be picked up by the Market.
//
incomingAsks = new LinkedList<MarketOrder>()
incomingBids = new LinkedList<MarketOrder>()

//
// Statistics by commodity-ID
//
statistics = [:]
provides statistics named 'statistics'

//
//
//

inventoryMapper = ComponentMapper.getFor(HasCommodityInventory)
balanceMapper = ComponentMapper.getFor(HasBankBalance)
pendingOrdersMapper = ComponentMapper.getFor(HasPendingMarketOrders)

include 'orderProcessing.groovy'