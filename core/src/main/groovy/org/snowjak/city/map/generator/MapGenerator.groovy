/**
 * 
 */
package org.snowjak.city.map.generator

import static org.snowjak.city.map.MapDomain.TERRAIN

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.snowjak.city.CityGame
import org.snowjak.city.map.BoundedMap
import org.snowjak.city.map.MapDomain
import org.snowjak.city.map.TileSet
import org.snowjak.city.map.generator.support.MapGeneratorConfigurationDsl

import com.badlogic.gdx.files.FileHandle
import com.github.czyzby.autumn.mvc.component.asset.AssetService
import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleBasisFunction.InterpolationType
import com.sudoplay.joise.module.ModuleFractal.FractalType
import com.sudoplay.joise.module.ModuleFunctionGradient.FunctionGradientAxis

/**
 * @author snowjak88
 *
 */
class MapGenerator {
	
	public org.snowjak.city.map.Map generateBounded(int width, int height, FileHandle scriptFile, AssetService assetService) {
		if(scriptFile == null)
			throw new NullPointerException()
		
		if(!scriptFile.exists())
			throw new FileNotFoundException()
		
		def customImports = new ImportCustomizer()
		customImports.addStaticStars( BasisType.class.name, InterpolationType.class.name, FractalType.class.name, FunctionGradientAxis.class.name )
			
		def config = new CompilerConfiguration()
		config.scriptBaseClass = MapGeneratorConfigurationDsl.class.name
		config.addCompilationCustomizers(customImports)
		
		def shell = new GroovyShell(this.class.classLoader, new Binding(), config)
		def script = shell.parse(scriptFile.file()) as MapGeneratorConfigurationDsl
		
		try {
			script.run()
		} catch(Throwable t) {
			throw new RuntimeException("Cannot execute map-generation script \"" + scriptFile.path() + "\" -- unexpected exception.", t)
		}
		
		if(script.altitude == null)
			throw new RuntimeException("Map-generation script \"" + scriptFile.path() + "\" is incomplete: does not set \"altitude\".")
		if(script.tiles == null)
			throw new RuntimeException("Map-generation script \"" + scriptFile.path() + "\" is incomplete: does not set \"tiles\".")
		
		final Module altitudeProducer = script.altitude
		def tilesProducer = script.tiles
		
		assetService.load CityGame.DEFAULT_TILESET_PATH, TileSet.class
		assetService.finishLoading CityGame.DEFAULT_TILESET_PATH, TileSet.class
		final TileSet terrainTileset = assetService.get(CityGame.DEFAULT_TILESET_PATH, TileSet.class)
		
		final org.snowjak.city.map.Map map = new BoundedMap(width, height)
		map.setTileSetFor MapDomain.TERRAIN, terrainTileset
		
		for(int x in 0..width-1)
			for(int y in 0..height-1) {
				
				def tileName = tilesProducer.doCall(x, y)
				
				final float altitude = altitudeProducer.get(x, y)
				
				def tileHashcode = terrainTileset.getTile(tileName).id
				map.setCell x, y, TERRAIN, tileHashcode
				map.setCell x, y, org.snowjak.city.map.Map.DIMENSION_ALTITUDE, altitude
			}
		
		println "Generated a new map from ${scriptFile.path()}"
		for(int y in 0..height-1) {
			print "[ "
			for(int x in 0..width-1) {
				def alt = map.getCellFloat(x, y, org.snowjak.city.map.Map.DIMENSION_ALTITUDE) as int
				print "$alt "
			}
			println "]"
		}
		
		map
	}
}
