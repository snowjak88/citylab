/**
 * 
 */
package org.snowjak.city.console.model

import org.snowjak.city.service.GameAssetService
import org.snowjak.city.service.GameService

/**
 * Presents an interface to the application's data-structures that console-users can interact with.
 * @author snowjak88
 *
 */
class ConsoleModel extends Binding {
	
	private final Set<String> protectedVariableNames = []
	
	public ConsoleModel(final GameAssetService assetService, final GameService gameService) {
		
		super()
		
		addProtectedVariable 'assets', assetService
		addProtectedVariable 'game', gameService
	}
	
	public void addProtectedVariable(String name, Object value) {
		variables[name] = value
		protectedVariableNames << name
	}
	
	@Override
	public void setVariable(String name, Object value) {
		
		if(name in protectedVariableNames)
			return
		
		super.setVariable(name, value)
	}
	
	@Override
	public void setProperty(String property, Object newValue) {
		
		if(property in protectedVariableNames)
			return
		
		super.setProperty(property, newValue)
	}
	
	@Override
	public void removeVariable(String name) {
		
		if(name in protectedVariableNames)
			return
		
		super.removeVariable(name)
	}
}