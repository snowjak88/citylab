/**
 * 
 */
package org.snowjak.city.map.tiles;

import static org.snowjak.city.util.Util.clamp;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.snowjak.city.map.tiles.TileSetResourceLoader.TileSetResourceLoaderParameter;
import org.snowjak.city.resources.ScriptedResourceLoader;
import org.snowjak.city.service.GameAssetService;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.czyzby.autumn.annotation.Component;

/**
 * @author snowjak88
 *
 */
@Component
public class TileSetResourceLoader extends ScriptedResourceLoader<TileSet, TileSetResourceLoaderParameter> {
	
	public TileSetResourceLoader(GameAssetService assetService) {
		
		super(assetService);
	}
	
	@Override
	protected void afterLoad(TileSet resource, GameAssetService assetService, boolean isDependencyMode) {
		
		if (!isDependencyMode)
			for (Tile t : resource.getTiles()) {
				
				final Texture texture = assetService.get(t.getFolder().child(t.getFilename()).path(), Texture.class);
				
				//
				// Width/height of 0 == use whatever is in the original texture
				//
				if (t.getWidth() == 0)
					t.setWidth(texture.getWidth());
				if (t.getHeight() == 0)
					t.setHeight(texture.getHeight());
					
				//
				// Clamp the desired texture-region's size to the actual texture-size, after
				// allowing for the origin x/y and padding.
				//
				final int width = clamp(t.getWidth(), 0, texture.getWidth() - t.getPadding() * 2 - t.getX());
				final int height = clamp(t.getHeight(), 0, texture.getHeight() - t.getPadding() * 2 - t.getY());
				
				t.setWidth(width);
				t.setHeight(height);
				
				t.setSprite(new TextureRegion(texture, t.getX(), t.getY(), t.getWidth(), t.getHeight()));
				
			}
		
	}
	
	@Override
	protected CompilerConfiguration getDefaultCompilerConfiguration() {
		
		final CompilerConfiguration config = super.getDefaultCompilerConfiguration();
		
		final ImportCustomizer importCustomizer = new ImportCustomizer();
		importCustomizer.addStaticStars(TileCorner.class.getName());
		
		config.addCompilationCustomizers(importCustomizer);
		
		return config;
	}
	
	@Override
	protected TileSet newInstance() {
		
		return new TileSet();
	}
	
	@Override
	public Class<TileSet> getResourceType() {
		
		return TileSet.class;
	}
	
	public static class TileSetResourceLoaderParameter extends AssetLoaderParameters<TileSet> {
	}
}
