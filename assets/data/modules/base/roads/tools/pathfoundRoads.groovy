//
// Attempts to plan a road connecting two or more points using pathfinding.
//

include 'gdx-ai-1.8.2.jar'

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.Graph
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.PathFinderRequest
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph

//
// Pathfinder-objects. These are initialized on activation (see the bottom of this file).
//
mapGraph = null
pathfinder = null
pathfindingHeuristic = null

graphPath = null

currentPathfinderRequest = null

import com.badlogic.gdx.utils.TimeUtils
final long pathfindTimeSliceMillis = 1000 / 50
final long pathfindTimeSliceNanos = TimeUtils.millisToNanos(pathfindTimeSliceMillis)

planPathfoundRoad = {
	->
	
	//
	// Plan a path for the first pair of checkpoints.
	//
	if(roadPlan.checkpoints.size() < 2)
		return
	
	//
	// All we'll do here is set up the currentPathfinderRequest object.
	// The actual path-finding is done with every tick, via updatePathfinder()
	//
	def startEntity = roadPlan.checkpoints[0]
	def endEntity = roadPlan.checkpoints[1]
	graphPath = new DefaultGraphPath<Entity>()
	
	currentPathfinderRequest = new PathFinderRequest(startEntity, endEntity, pathfindingHeuristic, graphPath)
	currentPathfinderRequest.changeStatus PathFinderRequest.SEARCH_INITIALIZED
	
	roadPlan.pathDone = false
	roadPlan.pathSuccess = false
}

updatePathfinder = {
	->
	//
	// If we have a currentPathfinderRequest, attempt to update it.
	if(!currentPathfinderRequest)
		return
	
	if(!roadPlan.pathDone)
		roadPlan.pathDone = pathfinder.search(currentPathfinderRequest, pathfindTimeSliceNanos)
	
	if(roadPlan.pathDone) {
		
		roadPlan.pathSuccess = currentPathfinderRequest.pathFound
		
		if(roadPlan.pathSuccess) {
			
			for(def entity : graphPath)
				roadPlan.pathEntities << entity
			roadPlan.pathEntities.each { it.add state.engine.createComponent(IsSelected) }
		}
	}
}

//
// When this Module is activated, configure these big objects.
// We can't configure them up-front, because they depend on the
// game-map being initialized.
//
onActivate {
	->
	mapGraph = new IndexedGraph<Entity>() {
				
				public Array<Connection<Entity>> getConnections(Entity fromEntity) {
					final result = new Array(4)
					
					if(!isCellMapper.has(fromEntity))
						return result
					
					final fromNode = isCellMapper.get(fromEntity)
					final int cx = fromNode.cellX
					final int cy = fromNode.cellY
					
					if(!isValidRoadCell(cx,cy))
						return result
					
					for(def edge : TileEdge) {
						
						final int nx = cx + edge.dx
						final int ny = cy + edge.dy
						if(!state.map.isValidCell(nx,ny))
							continue
						
						final neighbor = state.map.getEntity(nx,ny)
						
						if(!isValidRoadCell(nx,ny))
							continue
						if(!isValidRoadConnection(cx, cy, nx, ny))
							continue
						
						result.add new DefaultConnection( fromEntity, neighbor )
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
	
	pathfindingHeuristic = new Heuristic<Entity>() {
				public float estimate(Entity from, Entity to) {
					
					final node = isCellMapper.get(from)
					final endNode = isCellMapper.get(to)
					
					Math.abs(node.cellX - endNode.cellX) + Math.abs(node.cellY - endNode.cellY)
				}
			}
	
	pathfinder = new IndexedAStarPathFinder<Entity>(mapGraph)
}
