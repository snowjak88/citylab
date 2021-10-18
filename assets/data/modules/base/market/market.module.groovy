id = 'market'

dependsOn 'commodities'
dependsOn 'network'

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
// Mapping from network-ID to market-ID.
// (May not have your network-ID; market-IDs are
// only created when market-orders are received.)
//
networkToMarket = [:]
provides networkToMarket named 'networkToMarket'

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