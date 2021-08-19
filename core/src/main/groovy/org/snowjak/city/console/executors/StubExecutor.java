/**
 * 
 */
package org.snowjak.city.console.executors;

import org.snowjak.city.console.Console;

import com.badlogic.gdx.maps.MapRenderer;

/**
 * Stub executor. No matter what you provide, it spits out the same message.
 * 
 * @author snowjak88
 *
 */
public class StubExecutor extends AbstractConsoleExecutor {
	
	private Object[] objects = MapRenderer.class.getDeclaredMethods();
	private int i = 0;
	
	/**
	 * @param console
	 */
	public StubExecutor(Console console) {
		
		super(console);
	}
	
	@Override
	public void execute(String command) {
		
		getConsole().print("And the " + i + "th type is:");
		getConsole().print(objects[i]);
		
		i++;
		if (i >= objects.length)
			i = 0;
	}
}
