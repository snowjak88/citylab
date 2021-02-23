/**
 * 
 */
package org.snowjak.city.service;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.EnumMap;
import java.util.Map;

import org.snowjak.city.configuration.preferences.LambdaPreference;
import org.snowjak.city.map.tileset.TileDescriptor;
import org.snowjak.city.map.tileset.TileDescriptor.TileCorner;
import org.snowjak.city.map.tileset.TileDescriptor.TileCornerDescriptor;
import org.snowjak.city.map.tileset.TilesetDescriptor;
import org.snowjak.city.map.tileset.TilesetException;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.IntSet.IntSetIterator;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.component.preferences.PreferencesService;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
import com.github.czyzby.lml.util.LmlUtilities;

/**
 * @author snowjak88
 *
 */
@Component
public class TilesetService {
	
	/**
	 * All tile-sets are stored within the given base directory.
	 */
	public static final String PATHNAME_TILESETS_BASE = "images/tilesets/";
	
	private Map<TilesetDomain, TilesetDescriptor> tilesetDescriptors = new EnumMap<>(TilesetDomain.class);
	private Map<TilesetDomain, TiledMapTileSet> tilesets = new EnumMap<>(TilesetDomain.class);
	
	@Inject
	private AssetService assets;
	
	private static final Json json = new Json(OutputType.javascript);
	{
		json.setSerializer(TilesetDescriptor.class, new TilesetDescriptor.Serializer());
	}
	
	/**
	 * Rebuilds the current tileset belonging to the given {@link TilesetDomain
	 * domain}.
	 * 
	 * <ol>
	 * <li>Retrieve the configured base-path and tile-set name from the given
	 * domain</li>
	 * <li>Scan the \tilesets\{domain-base}\ directory for the given tileset
	 * subdirectory</li>
	 * <li>Import each tile listed in the tileset's descriptor</li>
	 * <li>Insert each tile into the given {@code tileset}</li>
	 * <li>Remove any tiles from {@code tileset} not included in the import</li>
	 * </ol>
	 * <p>
	 * This method will <b>block</b> while loading its assets. This is a
	 * quick-and-dirty method which will have to be replaced at some point in the
	 * future.
	 * </p>
	 * 
	 * TODO build a more event-driven tile-loading system
	 * 
	 * @param tileset
	 * @throws TilesetException
	 */
	public void rebuildTileset(TilesetDomain domain) throws TilesetException {
		
		final Logger log = LoggerService.forClass(TilesetService.class);
		
		log.debug("Rebuilding {0} tile-set ...", domain.name());
		
		final String tilesetName = domain.getTilesetName();
		if (tilesetName == null || tilesetName.isEmpty()) {
			log.error("Cannot rebuild tile-set -- current tile-set-name is empty!");
			throw new TilesetException(
					"Selected tile-set domain (" + domain.name() + ") has no configured tile-set name!");
		}
		
		final TiledMapTileSet tileset = tilesets.computeIfAbsent(domain, (d) -> new TiledMapTileSet());
		
		//
		// Does the given tile-set directory exist?
		final String tilesetBasePath = PATHNAME_TILESETS_BASE + domain.getDirectory();
		final FileHandle tilesetDirectory = Gdx.files.internal(tilesetBasePath + tilesetName + "/");
		if (!(tilesetDirectory.exists() || Gdx.app.getType() == ApplicationType.Android)) {
			log.error("Cannot rebuild {0} tile-set -- selected tile-set directory (\"{1}\") does not exist.",
					domain.name(), tilesetDirectory.toString());
			throw new TilesetException(
					"Selected tile-set directory \"" + tilesetDirectory.toString() + "\" does not exist.");
		}
		
		//
		// Does the given tile-set descriptor exist?
		final FileHandle tilesetDescriptorFile = tilesetDirectory.child("tileset.json");
		if (!(tilesetDescriptorFile.exists())) {
			log.error("Cannot rebuild {0} tile-set -- selected tile-set (\"{1}\") has no descriptor.", domain.name(),
					tilesetName);
			throw new TilesetException("Selected tile-set \"" + tilesetName + "\" has no descriptor.");
		}
		
		//
		// Import the tile-set descriptor.
		final TilesetDescriptor tilesetDescriptor = json.fromJson(TilesetDescriptor.class, tilesetDescriptorFile);
		if (tilesetDescriptor == null) {
			log.error("Could not import tile-set descriptor.");
			throw new TilesetException("Could not import tile-set descriptor.");
		}
		
		tilesetDescriptors.put(domain, tilesetDescriptor);
		
		//
		// Finish initializing the TilesetDescriptor.
		for (TileDescriptor tile : tilesetDescriptor.tiles.values()) {
			
			//
			// Compute the tile's hash
			tile.hash = (domain.name() + tilesetName + tile.stringID).hashCode();
			
			//
			// Verify tile images can be loaded
			final FileHandle tileImageFile = tilesetDirectory.child(tile.fileName);
			if (!(tileImageFile.exists())) {
				log.error(
						"The imported tile-set \"{0}\" has a tile (id=\"{1}\") whose assigned image-file (\"{2}\") is unavailable.",
						tilesetDescriptor.title, tile.stringID, tile.fileName);
				throw new TilesetException("Selected tile-set has a tile which refers to an unavailable image-file.");
			}
			tile.file = tileImageFile;
			if (!assets.isLoaded(tileImageFile.path())) {
				log.debug("Initiating load for tile image-file \"{0}\"", tile.fileName);
				assets.load(tileImageFile.path(), Texture.class);
			}
			
			//
			// Convert String- to object-references for tile corners
			for (TileCorner corner : TileCorner.values()) {
				final TileCornerDescriptor cornerDescriptor = tile.corners.get(corner);
				final String referencedTileID = cornerDescriptor.id;
				if (!tilesetDescriptor.tiles.containsKey(referencedTileID)) {
					log.error(
							"The imported tile-set \"{0}\" has a tile (id=\"{1}\"), of which the {2}th corner references an unrecognized tile-ID (\"{3}\")",
							tilesetDescriptor.title, tile.stringID, corner.getIndex(), cornerDescriptor.id);
					throw new TilesetException("Selected tile-set has a tile which refers to an unrecognized tile.");
				}
				
				cornerDescriptor.ref = tilesetDescriptor.tiles.get(referencedTileID);
			}
		}
		
		//
		// Now we can initialize the tile-set.
		//
		// Keep track of which tile-IDs we *aren't* updating, so we can remove them
		// later.
		final IntSet updatedIdHashes = new IntSet(tilesetDescriptor.tiles.size());
		tileset.forEach(t -> updatedIdHashes.add(t.getId()));
		
		for (TileDescriptor td : tilesetDescriptor.tiles.values()) {
			
			assets.finishLoading(td.file.path(), Texture.class);
			log.debug("Loading tile image-file \"{0}\"", td.fileName);
			final Texture tileTexture = assets.get(td.file.path(), Texture.class);
			
			final int padding = max(tilesetDescriptor.padding, 0);
			final int startX = max(td.startX, 0) + padding;
			final int startY = max(td.startY, 0) + padding;
			
			if (td.width < 0 && td.height < 0) {
				td.width = tileTexture.getWidth();
				td.height = tileTexture.getHeight();
			}
			
			final int width = min(max(td.width, 0), tileTexture.getWidth() - padding - 1);
			final int height = min(max(td.height, 0), tileTexture.getHeight() - padding - 1);
			
			final TextureRegion tileTextureRegion = new TextureRegion(tileTexture, startX, startY, width, height);
			
			final TiledMapTile tile = new StaticTiledMapTile(tileTextureRegion);
			tileset.putTile(td.hash, tile);
			updatedIdHashes.remove(td.hash);
		}
		
		log.debug("Imported {0} tiles.", tilesetDescriptor.tiles.size());
		
		if (!updatedIdHashes.isEmpty()) {
			log.debug("Removing {0} tiles from the updated tile-set as not contained within the import.",
					updatedIdHashes.size);
			final IntSetIterator nonUpdatedIterator = updatedIdHashes.iterator();
			while (nonUpdatedIterator.hasNext)
				tileset.removeTile(nonUpdatedIterator.next());
		}
		
		log.debug("Tile-set import complete.");
	}
	
	public TiledMapTileSet getTilesetFor(TilesetDomain domain) {
		
		return tilesets.computeIfAbsent(domain, (d) -> new TiledMapTileSet());
	}
	
	public TilesetDescriptor getDescriptorFor(TilesetDomain domain) {
		
		return tilesetDescriptors.computeIfAbsent(domain, (d) -> new TilesetDescriptor());
	}
	
	/**
	 * Identifies the several domains for which you can specify a tileset. Also
	 * stores each domain's properties (e.g., currently-configured tile-set name).
	 * 
	 * @author snowjak88
	 *
	 */
	public enum TilesetDomain {
		
		WORLD("world/", "tilesets-world", "default");
		
		private final String directory, preferenceName, defaultValue;
		private String tilesetName = "default";
		
		/**
		 * @param directory
		 * @param preferenceName
		 * @param defaultValue
		 */
		private TilesetDomain(String directory, String preferenceName, String defaultValue) {
			
			this.directory = directory;
			this.preferenceName = preferenceName;
			this.defaultValue = defaultValue;
		}
		
		public String getDirectory() {
			
			return directory;
		}
		
		public String getPreferenceName() {
			
			return preferenceName;
		}
		
		public String getTilesetName() {
			
			return tilesetName;
		}
		
		public String getDefaultValue() {
			
			return defaultValue;
		}
		
		public void setTilesetName(String tilesetName) {
			
			this.tilesetName = tilesetName;
		}
		
	}
	
	@Component
	public static class TilesetsPropertiesInitializer {
		
		@Inject
		private PreferencesService preferences;
		
		@Initiate(priority = AutumnActionPriority.VERY_HIGH_PRIORITY)
		public void initiateTilesetProperties() {
			
			for (TilesetDomain domain : TilesetDomain.values()) {
				final TilesetDomain d = domain;
				preferences.addPreference(domain.getPreferenceName(), "default", new LambdaPreference<String>(
						() -> d.getDefaultValue(), (a) -> LmlUtilities.getActorId(a), (s) -> s, (s) -> s));
			}
		}
	}
}
