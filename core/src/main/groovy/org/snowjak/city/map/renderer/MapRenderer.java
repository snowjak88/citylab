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

import java.util.Map;

import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.tiles.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Largely copies the logic of {@link IsometricTiledMapRenderer}, modified to
 * enable altitude-sensitive rendering of a {@link Map} instance.
 * 
 * @author snowjak88
 *
 */
public class MapRenderer {
	
	private static final int NUM_VERTICES = 20;
	private static final float DEFAULT_TILE_GRID_WIDTH = 32f, DEFAULT_TILE_GRID_HEIGHT = 16f;
	
	private Matrix4 isoTransform;
	private Matrix4 invIsotransform;
	private Vector3 scratchV3 = new Vector3();
	
	private Vector2 topRight = new Vector2();
	private Vector2 bottomLeft = new Vector2();
	private Vector2 topLeft = new Vector2();
	private Vector2 bottomRight = new Vector2();
	
	private Rectangle viewBounds = new Rectangle();
	private Rectangle imageBounds = new Rectangle();
	
	private final float[] vertices = new float[NUM_VERTICES];
	
	private CityMap map;
	private float unitScale = 1f / DEFAULT_TILE_GRID_WIDTH;
	private Batch batch;
	private boolean ownsBatch = false;
	
	public MapRenderer() {
		
		this(null, null);
	}
	
	public MapRenderer(CityMap map) {
		
		this(map, null);
	}
	
	public MapRenderer(Batch batch) {
		
		this(null, batch);
	}
	
	public MapRenderer(CityMap map, Batch batch) {
		
		setMap(map);
		setBatch(batch);
		init();
	}
	
	private void init() {
		
		// create the isometric transform
		isoTransform = new Matrix4();
		isoTransform.idt();
		
		// isoTransform.translate(0, 32, 0);
		isoTransform.scale((float) (Math.sqrt(2.0) / 2.0), (float) (Math.sqrt(2.0) / 4.0), 1.0f);
		isoTransform.rotate(0.0f, 0.0f, 1.0f, -45);
		
		// ... and the inverse matrix
		invIsotransform = new Matrix4(isoTransform);
		invIsotransform.inv();
	}
	
	public void setMap(CityMap map) {
		
		this.map = map;
	}
	
	public void setBatch(Batch batch) {
		
		this.batch = (batch != null) ? batch : new SpriteBatch();
		ownsBatch = (batch == null);
	}
	
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
	}
	
	public void setView(Matrix4 projection, float x, float y, float width, float height) {
		
		batch.setProjectionMatrix(projection);
		viewBounds.set(x, y, width, height);
	}
	
	public void render() {
		
		if (map == null)
			return;
		
		if (ownsBatch) {
			batch.enableBlending();
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		batch.begin();
		
		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a);
		
		final float tileWidth = DEFAULT_TILE_GRID_WIDTH * unitScale;
		final float tileHeight = DEFAULT_TILE_GRID_HEIGHT * unitScale;
		
		final float layerOffsetX = 0;
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -0;
		
		final float halfTileWidth = tileWidth * 0.5f;
		final float halfTileHeight = tileHeight * 0.5f;
		
		// setting up the screen points
		// COL1
		topRight.set(viewBounds.x + viewBounds.width - layerOffsetX, viewBounds.y - layerOffsetY);
		// COL2
		bottomLeft.set(viewBounds.x - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY);
		// ROW1
		topLeft.set(viewBounds.x - layerOffsetX, viewBounds.y - layerOffsetY);
		// ROW2
		bottomRight.set(viewBounds.x + viewBounds.width - layerOffsetX,
				viewBounds.y + viewBounds.height - layerOffsetY);
		
		// transforming screen coordinates to iso coordinates (so we don't render more
		// than we need to)
		int row1 = (int) (translateScreenToIso(topLeft).y / tileWidth) - 2;
		int row2 = (int) (translateScreenToIso(bottomRight).y / tileWidth) + 2;
		
		int col1 = (int) (translateScreenToIso(bottomLeft).x / tileWidth) - 2;
		int col2 = (int) (translateScreenToIso(topRight).x / tileWidth) + 2;
		
		for (int row = row2; row >= row1; row--) {
			for (int col = col1; col <= col2; col++) {
				
				if (!map.isValidCell(col, row))
					continue;
				
				for (Tile tile : map.getTiles(col, row)) {
					
					final int altitude = map.getTileAltitude(col, row, tile.getBase());
					
					final float tileScale = DEFAULT_TILE_GRID_WIDTH / (float) tile.getGridWidth();
					
					final float altitudeFactor = ((float) altitude * tile.getAltitudeOffset() * unitScale * tileScale);
					final float surfaceFactor = ((float) tile.getSurfaceOffset() * unitScale * tileScale);
					
					final float x = (col * halfTileWidth) + (row * halfTileWidth);
					final float y = (row * halfTileHeight) - (col * halfTileHeight) + altitudeFactor - surfaceFactor;
					
					final boolean flipX = false;// cell.getFlipHorizontally();
					final boolean flipY = false;// cell.getFlipVertically();
					final int rotations = 0;// cell.getRotation();
					
					TextureRegion region = tile.getSprite();
					
					final float x1 = x + layerOffsetX;
					final float y1 = y + layerOffsetY;
					final float x2 = x1 + region.getRegionWidth() * unitScale * tileScale;
					final float y2 = y1 + region.getRegionHeight() * unitScale * tileScale;
					
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
			}
		}
		
		batch.end();
	}
	
	public Vector3 translateIsoToScreen(Vector2 iso) {
		
		scratchV3.set(iso.x, iso.y, 0);
		scratchV3.mul(isoTransform);
		
		return scratchV3;
	}
	
	public Vector3 translateScreenToIso(Vector2 vec) {
		
		scratchV3.set(vec.x, vec.y, 0);
		scratchV3.mul(invIsotransform);
		
		return scratchV3;
	}
}
