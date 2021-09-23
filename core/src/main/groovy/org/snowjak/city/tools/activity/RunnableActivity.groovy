package org.snowjak.city.tools.activity

import org.snowjak.city.tools.Tool

/**
 * Simple Activity that wraps a Runnable.
 * 
 * @author snowjak88
 *
 */
class RunnableActivity extends Activity {
	
	private final Runnable runnable
	
	public RunnableActivity(Tool tool, Runnable runnable) {
		
		super(tool);
		
		this.runnable = runnable
	}
	
	@Override
	public void update() {
		
		runnable?.run()
	}
}
