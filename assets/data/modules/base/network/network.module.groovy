id = 'network'

i18n.addBundle 'i18n'

title = i18n.get('title')
description = i18n.get('description')

//
//
//

networkTypes = new LinkedHashSet<Class<? extends IsNetworkNode>>()

provides networkTypes named 'networkTypes'

//
// Recursively flood a given network-ID to a network-node and all its connections.

isCellMapper = ComponentMapper.getFor( IsMapCell )
import java.util.UUID
floodNetworkID = { Entity entity, Class<? extends IsNetworkNode> type, String networkID ->
	
	final componentType = ComponentType.getFor( type )
	final node = entity.getComponent( componentType )
	if(!node)
		return
	//
	// Update this node if needed.
	node.networkID = networkID
	
	//
	// Scan connections to see where we need to flood.
	for( def connection : node.connections ) {
		final neighborNode = connection?.getComponent( componentType )
		if(neighborNode?.networkID != networkID)
			floodNetworkID connection, type, networkID
	}
}

//
// Check a network-node's connections. Assign this network-node a network-ID that
// matches its connections. If this node's connections have more than one network-ID,
// then pick one and flood it out to all other connections.
//
checkNetworkID = { Entity entity, Class<? extends IsNetworkNode> type ->
	final componentType = ComponentType.getFor( type )
	final node = entity.getComponent( componentType )
	//
	// Scan node's connections. See how many distinct network-IDs we have among them.
	final Set<String> networkIDs = []
	for( def connection : node.connections ) {
		
		final neighborNode = connection.getComponent( componentType )
		if(neighborNode?.networkID)
			networkIDs << neighborNode.networkID
		
	}
	
	//
	// If we have *no* unique network-IDs out there, we'll just assign a new one here.
	if(networkIDs.isEmpty())
		node.networkID = UUID.randomUUID().toString()
	//
	// If there's only 1 unique network-ID out there, we copy it into the new node.
	else if(networkIDs.size() == 1)
		node.networkID = networkIDs[0]
	//
	// If there's more than 1 unique network-ID, then we need to pick one and flood it out.
	else {
		final chosenNetworkID = networkIDs[0]
		floodNetworkID entity, type, chosenNetworkID
	}
}

provides checkNetworkID named 'checkNetworkID'

registerNetworkType = { Class<? extends IsNetworkNode> type ->
	networkTypes << type
	
	listeningSystem "${type.simpleName}NetworkNodeAdditionSystem", Family.all( type ).get(), { entity, deltaTime ->
		
		checkNetworkID entity, type
		
	}, { entity, deltaTime ->
		
	}
}

provides registerNetworkType named 'registerNetworkType'

//
//
//

include 'pathfinding.groovy'
include 'networkMapMode.groovy'