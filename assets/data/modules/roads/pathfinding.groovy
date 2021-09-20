
//
// We need to be able to pathfind across the map when drawing a road from one spot to another.
//

include 'gdx-ai-1.8.2.jar'

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.Graph
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph

import java.util.concurrent.LinkedBlockingQueue

class PathfindRequest implements Poolable {
	final List<IsMapCell> checkpoints = []
	final List<IsMapCell> result = []
	boolean success, done
	
	void reset() {
		checkpoints.clear()
		result.clear()
		success = false
		done = false
	}
}

mapGraph = null
pathfinder = null
pathfindingHeuristic = null
requestQueue = new LinkedBlockingQueue<PathfindRequest>()
keepPathfinderTaskRunning = true

onActivate {
	->
	mapGraph = new IndexedGraph<IsMapCell>() {
				
				public Array<Connection<IsMapCell>> getConnections(IsMapCell fromNode) {
					final result = new Array(4)
					
					final int cx = fromNode.cellX
					final int cy = fromNode.cellY
					
					if(!state.map.isValidCell(cx,cy))
						return result
					
					for(def edge : TileEdge) {
						
						final int nx = cx + edge.dx
						final int ny = cy + edge.dy
						if(!state.map.isValidCell(nx,ny))
							continue
						
						final neighbor = state.map.getEntity(nx,ny)
						if(!isCellMapper.has(neighbor))
							throw new RuntimeException("The map-cell at ($nx,$ny) has an entity, but that entity isn't configured as a map-cell -- something is wrong ...")
						
						if(!isValidRoadCell(nx,ny))
							continue
						if(!isValidRoadConnection(cx, cy, nx, ny))
							continue
						
						result.add new DefaultConnection( fromNode, isCellMapper.get(neighbor) )
					}
					
					result
				}
				
				public int getIndex(IsMapCell node) {
					( node.cellX * state.map.width + node.cellY )
				}
				
				public int getNodeCount() {
					state.map.width * state.map.height
				}
			}
	
	pathfindingHeuristic = new Heuristic<IsMapCell>() {
				public float estimate(IsMapCell node, IsMapCell endNode) {
					
					final simpleDistance = Math.abs(node.cellX - endNode.cellX) + Math.abs(node.cellY - endNode.cellY)
					def costMultiplier = 1
					
					final corner = ((int)node.cellX != (int)endNode.cellX && (int)node.cellY != (int)endNode.cellY)
					if(roadPlan.penalizeCorners && corner)
						costMultiplier += 10
					
					simpleDistance * costMultiplier
				}
			}
	
	pathfinder = new IndexedAStarPathFinder<IsMapCell>(mapGraph)
	
	//
	// The pathfinder will run continuously in the background, operating
	// on one request at a time.
	keepPathfinderTaskRunning = true
	submitTask {
		->
		while(keepPathfinderTaskRunning) {
			final currentRequest = requestQueue.take()
			final graphPath = new DefaultGraphPath<IsMapCell>()
			
			synchronized(currentRequest) {
				def success = false
				def startNode = currentRequest.checkpoints[0]
				for(endNode : currentRequest.checkpoints) {
					if(endNode === startNode)
					continue
					
					success |= pathfinder.searchNodePath( startNode, endNode, pathfindingHeuristic, graphPath )
					
					if(success)
					for( def node : graphPath )
					currentRequest.result << node
				}
				
				currentRequest.success = success
				currentRequest.done = true
			}
		}
	}
}

onDeactivate {
	->
	keepPathfinderTaskRunning = false
}

submitPathfindRequest = { PathfindRequest request ->
	requestQueue.offer request
}