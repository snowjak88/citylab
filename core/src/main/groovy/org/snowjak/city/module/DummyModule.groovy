package org.snowjak.city.module

/**
 * A Module will be imported twice, the first time in "dependency-checking mode". At that time,
 * other Modules are unavailable to it (in its [modules] variable). However, that Module
 * may still make references to various published values and methods from other Modules.
 *
 * <p>
 * To allow a Module to make a legal reference to something that doesn't exist yet -- for example,
 * a method published by an upstream Module -- we need to provide a dummy Module that can simulate
 * anything. Variable-references return the referenced variable-name as a value,
 * and method-references return the referenced method-name.
 * </p>
 * @author snowjak88
 *
 */
class DummyModule {
	
	@Override
	public Object getProperty(String name) {
		name
	}
	
	@Override
	public void setProperty(String name, Object value) {
		
	}
	
	@Override
	public Object invokeMethod(String name, Object args) {
		names
	}
}
