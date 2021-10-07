
//
// Provides network-wide pathfinding.
// Clients requesting pathfinding must configure an instance of
// NetworkPathfinderRequest, and then wait for that ...Request
// to be marked as [done] = true
//

include '../gdx-ai-1.8.2.jar'

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.Graph
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.PathFinderRequest
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph

import com.badlogic.gdx.utils.TimeUtils
final float pathfindTimeSliceSeconds = 1f / 60f
final long pathfindTimeSliceMillis = 1000l * pathfindTimeSliceSeconds
final long pathfindTimeSliceNanos = TimeUtils.millisToNanos(pathfindTimeSliceMillis)

import org.snowjak.city.GameState

class FilteringIndexedEntityGraph implements IndexedGraph<Entity> {
	
	final isCellMapper = ComponentMapper.getFor(IsMapCell)
	final isNetworkNodeMapper = ComponentMapper.getFor(IsNetworkNode)
	
	final GameState state
	final Closure filter
	
	FilteringIndexedEntityGraph(Closure filter, GameState state) {
		this.filter = filter
		this.state = state
	}
	
	public Array<Connection<Entity>> getConnections(Entity fromEntity) {
		if(!isNetworkNodeMapper.has(fromEntity))
			return new Array(1)
		
		final networkNode = isNetworkNodeMapper.get(fromEntity)
		
		final result = new Array(networkNode.connections.size())
		
		for(def connection : networkNode.connections) {
			
			if(!isNetworkNodeMapper.has(connection))
				continue
			
			if(filter(fromEntity, connection))
				result.add new DefaultConnection( fromEntity, connection )
			
		}
		
		result
	}
	
	public int getIndex(Entity entity) {
		final node = isCellMapper.get(entity)
		( node.cellX * state.map.width + node.cellY )
	}
	
	public int getNodeCount() {
		state.map.width * state.map.height
	}
}

//
// Component denoting that an Entity has an active PathFinderRequest.
class OngoingPathfinderRequest implements Component, Poolable {
	PathFinderRequest pfr
	FilteringIndexedEntityGraph graph
	
	void reset() {
		pfr = null
		graph = null
	}
}

//
// Pathfinder heuristic.
// Really only works if both Entities have IsMapCell components (thus assigning
// them "physical" locations in the world).
pathfindingHeuristic = new Heuristic<Entity>() {
			public float estimate(Entity from, Entity to) {
				
				final fromCell = isCellMapper.get(from)
				final toCell = isCellMapper.get(to)
				
				if(fromCell && toCell)
					return Math.abs(fromCell.cellX - toCell.cellX) + Math.abs(fromCell.cellY - toCell.cellY)
					
				return 1
			}
		}

//
// Pathfinder instances by Graph
pathfindersByGraph = [:]

isCellMapper = ComponentMapper.getFor(IsMapCell)
isNetworkNodeMapper = ComponentMapper.getFor(IsNetworkNode)
pathfinderRequestMapper = ComponentMapper.getFor(NetworkPathfinderRequest)
ongoingRequestMapper = ComponentMapper.getFor(OngoingPathfinderRequest)

//
// When we have a NetworkPathfinderRequest without a corresponding OngoingPathfinderRequest,
// we need to create that OngoingPathfinderRequest so we can work on it.
iteratingSystem 'incomingNetworkPathfinderSystem', Family.all(IsNetworkNode, NetworkPathfinderRequest).exclude(OngoingPathfinderRequest).get(), { entity, deltaTime ->
	
	final request = pathfinderRequestMapper.get(entity)
	
	if(request.done)
		return
	
	final ongoing = entity.addAndReturn( state.engine.createComponent(OngoingPathfinderRequest) )
	
	ongoing.pfr = new PathFinderRequest(request.start, request.end, pathfindingHeuristic, new DefaultGraphPath() )
	ongoing.graph = new FilteringIndexedEntityGraph(request.filter ?: { _ -> true }, state )
	
	ongoing.changeStatus( PathFinderRequest.SEARCH_INITIALIZED )
	
	request.started = true
	request.done = false
	request.success = false
	request.path.clear()
}

//
// Service pathfinding-requests. Take no more than 1/60th of a second per cycle.
timeSliceSystem 'networkPathfinder', Family.all(IsNetworkNode, NetworkPathfinderRequest, OngoingPathfinderRequest).get(), pathfindTimeSliceSeconds, { entity, deltaTime ->
	
	final request = pathfinderRequestMapper.get(entity)
	final ongoing = ongoingRequestMapper.get(entity)
	
	//
	// Is the request finished yet?
	if(!request.done) {
		
		//
		// Not done yet?
		// Get the pathfinder that's configured for use with the supplied graph-instance,
		// and continue the search.
		//
		final pathfinder = pathfindersByGraph.computeIfAbsent(ongoing.graph, { g -> new IndexedAStarPathFinder(g) } )
		request.done = pathfinder.search( ongoing.pfr, pathfindTimeSliceNanos )
		
	}
	
	//
	// Is the request finished yet?
	if(request.done) {
		
		//
		// Well, was the request successful?
		request.success = ongoing.pfr.pathFound
		
		//
		// If it was successful, copy the found path into the original request-component,
		// and remove the Ongoing component.
		if(request.success) {
			for(def pathEntry : ongoing.resultPath)
				request.path << pathEntry
			
			entity.remove OngoingPathfinderRequest
		}
	}
}

//
// If NetworkPathfinderRequest falls off an entity, remove its OngoingPathfinderRequest
listeningSystem 'trimOngoingNetworkPathfinderRequestSystem', Family.all(NetworkPathfinderRequest, OngoingPathfinderRequest).get(), { entity, deltaTime ->
	//
	// do nothing when both components are on an entity
}, { entity, deltaTime ->
	
	//
	// One of the two Components fell off this entity.
	// Don't know which. Just try to remove both.
	entity.remove NetworkPathfinderRequest
	entity.remove OngoingPathfinderRequest
}