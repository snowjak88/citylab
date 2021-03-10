/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.snowjak.city.map.tiles.TileSetDescriptor

/**
 * @author snowjak88
 *
 */
class TileSetDescriptorScript {
	
	private final CompilerConfiguration config;
	
	public TileSetDescriptorScript() {
		final ImportCustomizer customImports = new ImportCustomizer();
		customImports.addStaticStars();
		
		config = new CompilerConfiguration();
		//config.setScriptBaseClass(TileSetDescriptorSpec.class.getName());
		config.addCompilationCustomizers(customImports);
	}
	
	public TileSetDescriptor load(File file) {
		
		if (file == null)
			throw new NullPointerException();
		
		if (!file.exists())
			throw new RuntimeException(new FileNotFoundException());
		
		def tileSetDescriptorSpec = new TileSetDescriptorSpec()
		
		try {
			final GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), new Binding(), config)
			def script = shell.parse(file)
			
			Closure closure = script()
			closure.delegate = tileSetDescriptorSpec
			closure.resolveStrategy = Closure.DELEGATE_ONLY
			closure.owner = tileSetDescriptorSpec
			closure()
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		tileSetDescriptorSpec.build()
	}
}
