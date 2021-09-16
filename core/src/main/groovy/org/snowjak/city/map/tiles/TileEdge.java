package org.snowjak.city.map.tiles;

import java.util.Iterator;

import com.google.common.collect.Iterators;

public enum TileEdge implements Iterable<TileEdge> {
	
	WEST(-1, 0, 1), EAST(+1, 0, 0), SOUTH(0, -1, 3), NORTH(0, +1, 2);
	
	private final int dx, dy, oppositeIndex;
	
	private TileEdge(int dx, int dy, int oppositeIndex) {
		
		this.dx = dx;
		this.dy = dy;
		this.oppositeIndex = oppositeIndex;
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
	
	@Override
	public Iterator<TileEdge> iterator() {
		
		return Iterators.forArray(TileEdge.values());
	}
}
