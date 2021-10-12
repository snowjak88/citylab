id = 'network'

i18n.addBundle 'i18n'

title = i18n.get('title')
description = i18n.get('description')

//
//
//

networkTypes = new LinkedHashSet<>()

registerNetworkType = { Class<? extends IsNetworkNode> type ->
	networkTypes << type
}

provides networkTypes named 'networkTypes'
provides registerNetworkType named 'registerNetworkType'

//
//
//

include 'pathfinding.groovy'
include 'networkMapMode.groovy'