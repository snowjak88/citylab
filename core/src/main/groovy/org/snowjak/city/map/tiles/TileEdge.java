package org.snowjak.city.map.tiles;

import static org.snowjak.city.map.tiles.TileCorner.BOTTOM;
import static org.snowjak.city.map.tiles.TileCorner.LEFT;
import static org.snowjak.city.map.tiles.TileCorner.RIGHT;
import static org.snowjak.city.map.tiles.TileCorner.TOP;

import java.util.Iterator;

import com.google.common.collect.Iterators;

public enum TileEdge implements Iterable<TileEdge> {
	
	WEST(-1, 0, 1, TOP, LEFT), EAST(+1, 0, 0, BOTTOM, RIGHT), SOUTH(0, -1, 3, LEFT, BOTTOM), NORTH(0, +1, 2, TOP,
			RIGHT);
	
	private final int dx, dy, oppositeIndex;
	private final TileCorner[] corners;
	
	private TileEdge(int dx, int dy, int oppositeIndex, TileCorner... corners) {
		
		this.dx = dx;
		this.dy = dy;
		this.oppositeIndex = oppositeIndex;
		this.corners = corners;
	}
	
	public int getDx() {
		
		return dx;
	}
	
	public int getDy() {
		
		return dy;
	}
	
	public TileEdge getOpposite() {
		
		return TileEdge.values()[oppositeIndex];
	}
	
	public TileCorner[] getCorners() {
		
		return corners;
	}
	
	@Override
	public Iterator<TileEdge> iterator() {
		
		return Iterators.forArray(TileEdge.values());
	}
	
	public static TileEdge fromDelta(int dx, int dy) {
		
		for (TileEdge e : TileEdge.values())
			if (e.dx == dx && e.dy == dy)
				return e;
		return null;
	}
}
