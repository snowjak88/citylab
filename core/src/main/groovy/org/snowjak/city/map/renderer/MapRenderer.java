/**
 * 
 */
package org.snowjak.city.map.renderer;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

import org.snowjak.city.GameState;
import org.snowjak.city.ecs.components.HasMapLayers;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.renderer.hooks.AbstractRenderingHook;
import org.snowjak.city.map.tiles.Tile;
import org.snowjak.city.map.tiles.TileCorner;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.util.PrioritizationFailedException;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.github.czyzby.kiwi.log.Logger;

import space.earlygrey.shapedrawer.ShapeDrawer;

public class MapRenderer implements RenderingSupport, Disposable {
	
	public static final String DEFAULT_MAP_MODE_ID = "default";
	
	private static final Logger LOG = LoggerService.forClass(MapRenderer.class);
	
	private static final int NUM_VERTICES = 20;
	
	private static final float LOGICAL_TILE_WIDTH = 1f;
	private static final float LOGICAL_TILE_HEIGHT = 0.5f;
	private static final float LOGICAL_ALTITUDE_MUTIPLIER = 0.25f;
	
	private static final float HALF_TILE_WIDTH = LOGICAL_TILE_WIDTH * 0.5f;
	private static final float HALF_TILE_HEIGHT = LOGICAL_TILE_HEIGHT * 0.5f;
	
	public static final float WORLD_GRID_UNIT_SIZE = 64f;
	private static final float WORLD_GRID_WIDTH = WORLD_GRID_UNIT_SIZE * LOGICAL_TILE_WIDTH;
	private static final float WORLD_GRID_HEIGHT = WORLD_GRID_UNIT_SIZE * LOGICAL_TILE_HEIGHT;
	private static final float WORLD_ALTITUDE_MULTIPLIER = WORLD_GRID_UNIT_SIZE * LOGICAL_ALTITUDE_MUTIPLIER;
	
	private Matrix4 isoTransform;
	private Matrix4 invIsotransform;
	private Vector3 scratchV3 = new Vector3();
	
	private Vector2 topRight = new Vector2();
	private Vector2 bottomLeft = new Vector2();
	private Vector2 topLeft = new Vector2();
	private Vector2 bottomRight = new Vector2();
	
	private Rectangle viewBounds = new Rectangle();
	private Rectangle worldBounds = new Rectangle();
	private Rectangle visibleMapBounds = new Rectangle();
	
	private final float layerOffsetX = 0, layerOffsetY = -0;
	private int mapVisibleMinX = 0, mapVisibleMinY = 0, mapVisibleMaxX = 0, mapVisibleMaxY = 0;
	
	/**
	 * Scratch packed float[] array for delivering vertex-data to OpenGL
	 */
	private final float[] vertices = new float[NUM_VERTICES];
	
	/**
	 * Scratch Vector2[4] array for preparing cell-vertices
	 */
	private final Vector2[] cellVertices = new Vector2[4];
	
	/**
	 * Scratch Vector2 used for computing cell-vertices.
	 */
	private final Vector2 cellVertex = new Vector2();
	
	private final GameState state;
	private final ShaderProgram maskingShaderProgram = new ShaderProgram(
			Gdx.files.internal("mapTileVertexShader.vert").readString(),
			Gdx.files.internal("mapTileFragmentShader.frag").readString());
	private SpriteBatch batch;
	private ShapeDrawer shapeDrawer;
	
	/**
	 * The rendering-hook that actually executes the map-renderer. In effect, this
	 * MapRenderer hooks into itself, with id = "map". This enables the MapRenderer
	 * to allow other rendering-hooks to execute prior to the map being rendered.
	 */
	public final AbstractRenderingHook MAP_RENDERING_HOOK = new AbstractRenderingHook("map") {
		
		private final ComponentMapper<HasMapLayers> layerMapper = ComponentMapper.getFor(HasMapLayers.class);
		
		@Override
		public void render(float delta, Batch batch, ShapeDrawer shapeDrawer, RenderingSupport support) {
			
			//
			// This main rendering-hook handles rendering the actual map.
			//
			if (state == null || state.getMap() == null)
				return;
			
			final CityMap map = state.getMap();
			final List<MapLayer> prioritizedMapLayers = state.getRenderingHookRegistry().getPrioritizedMapLayers();
			if (prioritizedMapLayers.isEmpty())
				return;
			
			final ListIterator<MapLayer> listerator = prioritizedMapLayers.listIterator(prioritizedMapLayers.size());
			
			for (int cellY = mapVisibleMaxY; cellY >= mapVisibleMinY; cellY--)
				for (int cellX = mapVisibleMinX; cellX <= mapVisibleMaxX; cellX++)
					if (map.isValidCell(cellX, cellY)) {
						
						final Entity entity = map.getEntity(cellX, cellY);
						if (entity == null)
							continue;
						if (!layerMapper.has(entity))
							continue;
							
						//
						// Reset the MapLayer iterator back to the top
						//
						while (listerator.hasNext())
							listerator.next();
						
						final HasMapLayers hasLayers = layerMapper.get(entity);
						
						//
						// Scan downward until previous() yields a non-transparent Tile
						// (or we get to the beginning of the list)
						//
						while (listerator.hasPrevious()) {
							final String layerID = listerator.previous().getId();
							final Tile tile = hasLayers.getTiles().get(layerID);
							if (tile == null)
								continue;
							if (!tile.isTransparent())
								break;
						}
						
						//
						// Now render all these tiles in order.
						while (listerator.hasNext()) {
							final String layerID = listerator.next().getId();
							final Tile tile = hasLayers.getTiles().get(layerID);
							final Color tint = hasLayers.getTints().get(layerID);
							final Integer altitudeOverride = hasLayers.getAltitudeOverrides().get(layerID);
							
							renderTile(cellX, cellY, tile, tint, (altitudeOverride == null) ? -1 : altitudeOverride);
						}
					}
		}
		
	};
	
	public MapRenderer(GameState state) {
		
		this.state = state;
		setupBatch();
		init();
	}
	
	private void init() {
		
		for (int i = 0; i < cellVertices.length; i++)
			cellVertices[i] = new Vector2();
		
		for (int i = 0; i < viewportToMapVertices.length; i++)
			viewportToMapVertices[i] = new Vector2();
		
		// create the isometric transform
		isoTransform = new Matrix4();
		isoTransform.idt();
		
		isoTransform.scale((float) (Math.sqrt(2.0) / 2.0), (float) (Math.sqrt(2.0) / 4.0), 1.0f);
		isoTransform.rotate(0.0f, 0.0f, 1.0f, -45);
		
		// ... and the inverse matrix
		invIsotransform = new Matrix4(isoTransform);
		invIsotransform.inv();
		
		try {
			
			state.getRenderingHookRegistry().addRenderingHook(MAP_RENDERING_HOOK);
			
			if (state.getMapModes().containsKey(DEFAULT_MAP_MODE_ID))
				state.getMapModes().get(DEFAULT_MAP_MODE_ID).getRenderingHooks().add(MAP_RENDERING_HOOK.getId());
			
		} catch (PrioritizationFailedException e) {
			throw new RuntimeException("Cannot initialize main map-renderer!", e);
		}
	}
	
	private void setupBatch() {
		
		this.batch = new SpriteBatch(2048);
		
		this.batch.disableBlending();
		this.batch.setShader(maskingShaderProgram);
		
		final Pixmap shapeDrawerPixmap = new Pixmap(1, 1, Format.RGB888);
		shapeDrawerPixmap.setColor(Color.WHITE);
		shapeDrawerPixmap.fill();
		final Texture shapeDrawerTexture = new Texture(shapeDrawerPixmap);
		final TextureRegion shapeDrawerRegion = new TextureRegion(shapeDrawerTexture);
		
		this.shapeDrawer = new ShapeDrawer(this.batch, shapeDrawerRegion);
		this.shapeDrawer.setDefaultLineWidth(1f / WORLD_GRID_WIDTH);
	}
	
	/**
	 * Updates the MapRenderer's viewport-bounds based on the given camera. The
	 * MapRenderer should only render map-cells that are visible to the Camera, so
	 * it's important that you keep the MapRenderer updated every time your Camera
	 * changes one of its attributes.
	 * 
	 * @param camera
	 */
	public void setView(OrthographicCamera camera) {
		
		batch.setProjectionMatrix(camera.combined);
		
		//
		// the viewport's width and height scale per camera's zoom-level
		//
		// We'll pad this width and height just a bit (with that constant multiplier)
		float width = camera.viewportWidth * camera.zoom * 1.25f;
		float height = camera.viewportHeight * camera.zoom * 1.25f;
		//
		// Project the width- and height-vectors onto the camera's up vector
		float w = width * Math.abs(camera.up.y) + height * Math.abs(camera.up.x);
		float h = height * Math.abs(camera.up.y) + width * Math.abs(camera.up.x);
		
		//
		// given the Camera's position defines the middle of the viewport,
		// our viewing bounds are easy to derive
		viewBounds.set(camera.position.x - w / 2, camera.position.y - h / 2, w, h);
		
		updateViewportBounds();
	}
	
	@Override
	public Rectangle getViewportWorldBounds() {
		
		return viewBounds;
	}
	
	@Override
	public Rectangle getVisibleMapCells() {
		
		return visibleMapBounds;
	}
	
	private void updateViewportBounds() {
		
		// setting up the viewport bounds
		// COL1
		topRight.set(viewBounds.x + viewBounds.width - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY);
		// COL2
		bottomLeft.set(viewBounds.x - layerOffsetX, viewBounds.y - layerOffsetY);
		// ROW1
		topLeft.set(viewBounds.x - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY);
		// ROW2
		bottomRight.set(viewBounds.x + viewBounds.width - layerOffsetX, viewBounds.y - layerOffsetY);
		
		// transforming screen coordinates to iso coordinates (so we don't render more
		// than we need to)
		final int overlap = 0;
		// mapVisibleMinY = (int) (viewportToMap(topLeft, true).y) - overlap;
		// mapVisibleMaxY = (int) (viewportToMap(bottomRight, true).y) + overlap;
		//
		// mapVisibleMinX = (int) (viewportToMap(bottomLeft, true).x) - overlap;
		// mapVisibleMaxX = (int) (viewportToMap(topRight, true).x) + overlap;
		
		mapVisibleMinY = (int) (viewportToMap(bottomLeft, true).y) - overlap;
		mapVisibleMaxY = (int) (viewportToMap(topRight, true).y) + overlap;
		
		mapVisibleMinX = (int) (viewportToMap(topLeft, true).x) - overlap;
		mapVisibleMaxX = (int) (viewportToMap(bottomRight, true).x) + overlap;
		
		visibleMapBounds.set(mapVisibleMinX, mapVisibleMinY, (mapVisibleMaxX - mapVisibleMinX + 1),
				(mapVisibleMaxY - mapVisibleMinY + 1));
	}
	
	public void render(float delta) {
		
		if (state == null || state.getMap() == null)
			return;
		
		final MapMode currentMapMode;
		if (state.getActiveMapMode() != null)
			currentMapMode = state.getActiveMapMode();
		else
			currentMapMode = state.getMapModes().get(DEFAULT_MAP_MODE_ID);
		if (currentMapMode == null)
			return;
		
		batch.begin();
		
		renderMapMode(delta, currentMapMode);
		
		batch.end();
	}
	
	private void renderMapMode(float delta, MapMode mapMode) {
		
		for (String includedMapModeName : mapMode.getIncludes()) {
			final MapMode includedMapMode = state.getMapModes().get(includedMapModeName);
			if (includedMapMode != null)
				renderMapMode(delta, includedMapMode);
		}
		
		for (AbstractRenderingHook hook : state.getRenderingHookRegistry().getPrioritizedRenderingHooks())
			if (hook.isEnabled())
				if (mapMode.getRenderingHooks().contains(hook.getId()))
					hook.render(delta, batch, shapeDrawer, this);
	}
	
	@Override
	public boolean isPointVisible(int viewportX, int viewportY) {
		
		return isPointVisible(viewportX, viewportY);
	}
	
	@Override
	public boolean isPointVisible(float viewportX, float viewportY) {
		
		return viewBounds.contains(viewportX, viewportY);
	}
	
	@Override
	public boolean isCellVisible(int cellX, int cellY) {
		
		if (state == null || state.getMap() == null)
			return false;
		
		final CityMap map = state.getMap();
		if (!map.isValidCell(cellX, cellY))
			return false;
		
		return (cellX >= mapVisibleMinX && cellX <= mapVisibleMaxX && cellY >= mapVisibleMinY
				&& cellY <= mapVisibleMaxY);
	}
	
	public void renderTile(int col, int row, Tile tile, Color tint, int altitudeOverride) {
		
		if (state == null || state.getMap() == null)
			return;
		
		final CityMap map = state.getMap();
		if (!map.isValidCell(col, row))
			return;
		if (!isCellVisible(col, row))
			return;
		
		if (tile == null || tile.getSprite() == null)
			return;
		
		final float color;
		
		if (tint == null)
			color = Color.toFloatBits(batch.getColor().r, batch.getColor().g, batch.getColor().b, batch.getColor().a);
		else
			color = Color.toFloatBits(tint.r, tint.g, tint.b, tint.a);
		
		final float tileScale = 1f / (float) tile.getGridWidth();
		final TileCorner base = tile.getBase();
		getCellVertices(col, row, cellVertices, base, altitudeOverride);
		
		float x = cellVertices[0].x, y = cellVertices[0].y;
		for (int i = 1; i < 4; i++) {
			if (x > cellVertices[i].x)
				x = cellVertices[i].x;
			if (y > cellVertices[i].y)
				y = cellVertices[i].y;
		}
		
		final boolean flipX = false;// cell.getFlipHorizontally();
		final boolean flipY = false;// cell.getFlipVertically();
		final int rotations = 0;// cell.getRotation();
		
		TextureRegion region = tile.getSprite();
		
		final float x1 = x;
		final float y1 = y - ((float) tile.getSurfaceOffset() * tileScale);
		final float x2 = x1 + region.getRegionWidth() * tileScale;
		final float y2 = y1 + region.getRegionHeight() * tileScale;
		
		final float u1 = region.getU();
		final float v1 = region.getV2();
		final float u2 = region.getU2();
		final float v2 = region.getV();
		
		vertices[X1] = x1;
		vertices[Y1] = y1;
		vertices[C1] = color;
		vertices[U1] = u1;
		vertices[V1] = v1;
		
		vertices[X2] = x1;
		vertices[Y2] = y2;
		vertices[C2] = color;
		vertices[U2] = u1;
		vertices[V2] = v2;
		
		vertices[X3] = x2;
		vertices[Y3] = y2;
		vertices[C3] = color;
		vertices[U3] = u2;
		vertices[V3] = v2;
		
		vertices[X4] = x2;
		vertices[Y4] = y1;
		vertices[C4] = color;
		vertices[U4] = u2;
		vertices[V4] = v1;
		
		if (flipX) {
			float temp = vertices[U1];
			vertices[U1] = vertices[U3];
			vertices[U3] = temp;
			temp = vertices[U2];
			vertices[U2] = vertices[U4];
			vertices[U4] = temp;
		}
		if (flipY) {
			float temp = vertices[V1];
			vertices[V1] = vertices[V3];
			vertices[V3] = temp;
			temp = vertices[V2];
			vertices[V2] = vertices[V4];
			vertices[V4] = temp;
		}
		if (rotations != 0) {
			switch (rotations) {
			case Cell.ROTATE_90: {
				float tempV = vertices[V1];
				vertices[V1] = vertices[V2];
				vertices[V2] = vertices[V3];
				vertices[V3] = vertices[V4];
				vertices[V4] = tempV;
				
				float tempU = vertices[U1];
				vertices[U1] = vertices[U2];
				vertices[U2] = vertices[U3];
				vertices[U3] = vertices[U4];
				vertices[U4] = tempU;
				break;
			}
			case Cell.ROTATE_180: {
				float tempU = vertices[U1];
				vertices[U1] = vertices[U3];
				vertices[U3] = tempU;
				tempU = vertices[U2];
				vertices[U2] = vertices[U4];
				vertices[U4] = tempU;
				float tempV = vertices[V1];
				vertices[V1] = vertices[V3];
				vertices[V3] = tempV;
				tempV = vertices[V2];
				vertices[V2] = vertices[V4];
				vertices[V4] = tempV;
				break;
			}
			case Cell.ROTATE_270: {
				float tempV = vertices[V1];
				vertices[V1] = vertices[V4];
				vertices[V4] = vertices[V3];
				vertices[V3] = vertices[V2];
				vertices[V2] = tempV;
				
				float tempU = vertices[U1];
				vertices[U1] = vertices[U4];
				vertices[U4] = vertices[U3];
				vertices[U3] = vertices[U2];
				vertices[U2] = tempU;
				break;
			}
			}
		}
		
		batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
	}
	
	@Override
	public Vector2[] getCellVertices(int col, int row, TileCorner base) {
		
		getCellVertices(col, row, cellVertices, base, -1);
		return cellVertices;
	}
	
	/**
	 * Given a map-cell (identified with {@code x,y}), compute its 4 vertices in
	 * terms of the viewport's coordinate system.
	 * <p>
	 * Vertices are given clockwise from (-x,-y):
	 * 
	 * <pre>
	 * x1,y1
	 * x1,y2
	 * x2,y2
	 * x2,y1
	 * </pre>
	 * </p>
	 * 
	 * @param col
	 * @param row
	 * @param vertices
	 * @param base
	 *            use TileCorner as the basis for altitude calculations, or
	 *            {@code null} to use altitude at each vertex
	 * @param altitudeOverride
	 *            ignore the stored altitude and use this value instead; if
	 *            {@code <0}, then do not override
	 * @return
	 * @throw {@link IndexOutOfBoundsException} if {@code col} or {@code row} fall
	 *        outside of the map
	 */
	private void getCellVertices(int col, int row, Vector2[] vertices, TileCorner base, int altitudeOverride) {
		
		if (state == null || state.getMap() == null)
			return;
		
		final CityMap map = state.getMap();
		
		int index = 0;
		for (TileCorner corner : Arrays.asList(TileCorner.LEFT, TileCorner.TOP, TileCorner.RIGHT, TileCorner.BOTTOM)) {
			
			final int altitude;
			if (altitudeOverride >= 0)
				altitude = altitudeOverride;
			else if (base == null || base == corner)
				altitude = map.getCellAltitude(col, row, corner);
			else
				altitude = map.getCellAltitude(col, row, base);
			
			vertices[index++].set(computeCellVertexX(col + corner.getOffsetX(), row + corner.getOffsetY()),
					computeCellVertexY(col + corner.getOffsetX(), row + corner.getOffsetY(), altitude));
		}
	}
	
	@Override
	public Vector2 getVertex(int vertexX, int vertexY) {
		
		if (state == null || state.getMap() == null)
			return null;
		final int altitude = (state.getMap().isValidVertex(vertexX, vertexY))
				? state.getMap().getVertexAltitude(vertexX, vertexY)
				: 0;
		cellVertex.set(computeCellVertexX(vertexX, vertexY), computeCellVertexY(vertexX, vertexY, altitude));
		return cellVertex;
	}
	
	private float computeCellVertexX(int vertexX, int vertexY) {
		
		return (vertexX * HALF_TILE_WIDTH) + (vertexY * HALF_TILE_WIDTH);
	}
	
	private float computeCellVertexY(int vertexX, int vertexY, int altitude) {
		
		return (vertexY * HALF_TILE_HEIGHT) - (vertexX * HALF_TILE_HEIGHT)
				+ ((float) altitude * LOGICAL_ALTITUDE_MUTIPLIER);
	}
	
	@Override
	public Vector2 mapToViewport(Vector2 iso) {
		
		scratchV3.set(iso.x, iso.y, 0);
		scratchV3.mul(isoTransform);
		
		iso.set(scratchV3.x, scratchV3.y);
		return iso;
	}
	
	/**
	 * Unproject the given viewport-location to the map (taking vertex-altitude into
	 * account).
	 * 
	 * @param viewportCoordinates
	 * @return the mutated {@code viewportCoordinates}
	 */
	public Vector2 viewportToMap(Vector2 viewportCoordinates) {
		
		return viewportToMap(viewportCoordinates, false);
	}
	
	private Vector3 viewportToMapScratchV3 = new Vector3();
	private Vector2[] viewportToMapVertices = new Vector2[4];
	
	/**
	 * Unproject the given viewport-location to the map (taking vertex-altitude into
	 * account).
	 * <p>
	 * This method is thread-safe. You may call it from multiple threads at once.
	 * </p>
	 * 
	 * @param viewportCoordinates
	 * @param ignoreAltitude
	 * @return
	 */
	public Vector2 viewportToMap(Vector2 viewportCoordinates, boolean ignoreAltitude) {
		
		if (state == null || state.getMap() == null)
			return viewportCoordinates;
		
		final CityMap map = state.getMap();
		
		synchronized (viewportToMapScratchV3) {
			//
			// Figure out which (viewport-)vertical column to scan through.
			//
			// If the map were flat, this would be the cell we're hitting ...
			viewportToMapScratchV3.set(viewportCoordinates.x, viewportCoordinates.y, 0);
			viewportToMapScratchV3.mul(invIsotransform);
			final float flatMapCellX = viewportToMapScratchV3.x, flatMapCellY = viewportToMapScratchV3.y;
			
			if (ignoreAltitude)
				return new Vector2(flatMapCellX, flatMapCellY);
				
			//
			// this is the column we need to scan ...
			final int column = Math.round(flatMapCellX + flatMapCellY);
			
			//
			// Now iterate up the column -- starting at cell [column,0] and continuing by
			// [-1,+1] until we find a cell whose viewport-projected vertices contain the
			// given viewport-coordinate.
			//
			final Predicate<Vector2> isWithinQuadrilaterial = (v) -> (Intersector.isPointInTriangle(v.x, v.y,
					viewportToMapVertices[0].x, viewportToMapVertices[0].y, viewportToMapVertices[1].x,
					viewportToMapVertices[1].y, viewportToMapVertices[3].x, viewportToMapVertices[3].y)
					|| Intersector.isPointInTriangle(v.x, v.y, viewportToMapVertices[1].x, viewportToMapVertices[1].y,
							viewportToMapVertices[2].x, viewportToMapVertices[2].y, viewportToMapVertices[3].x,
							viewportToMapVertices[3].y));
			
			int cellX = column, cellY = 0;
			//
			// We will alternate back and forth between decrementing X and incrementing Y.
			// This field governs whether we do one or the other.
			boolean justDidX = true;
			
			//
			// Note that we might just start off with cellX being *way* outside the map,
			// if "column" is greater than map.getWidth().
			//
			// Accordingly, we might have to iterate for a bit until we get on the map.
			//
			while (cellX > 0 && cellY < map.getHeight() && !map.isValidCell(cellX, cellY)) {
				if (justDidX)
					cellX--;
				else
					cellY++;
				justDidX = !justDidX;
			}
			
			//
			// OK -- so long as we're still on the map, search.
			while (map.isValidCell(cellX, cellY)) {
				
				getCellVertices(cellX, cellY, viewportToMapVertices, null, -1);
				
				if (isWithinQuadrilaterial.test(viewportCoordinates))
					return new Vector2(cellX, cellY);
				
				if (justDidX)
					cellX--;
				else
					cellY++;
				justDidX = !justDidX;
			}
			
			//
			// Eh. Return our best guess.
			return new Vector2(flatMapCellX, flatMapCellY);
		}
	}
	
	@Override
	public void dispose() {
		
		if (batch != null) {
			if (batch.isDrawing())
				batch.end();
			
			batch.dispose();
			maskingShaderProgram.dispose();
		}
	}
}
