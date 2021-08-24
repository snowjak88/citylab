package org.snowjak.city.resources


/**
 * A version of {@link DelegatingScript} that automatically catches all
 * missing-property and -method exceptions and converts them to "dummy"
 * invocations.
 */
public abstract class AutoProxyingPrintlessDelegatingScript extends DelegatingScript {
	
	
	
	@Override
	public void println() {
	}
	
	@Override
	public void print(Object value) {
	}
	
	@Override
	public void println(Object value) {
	}
	
	@Override
	public void printf(String format, Object value) {
	}
	
	@Override
	public void printf(String format, Object[] values) {
	}
	
	@Override
	public Object invokeMethod(String name, Object args) {
		
		try {
			return super.invokeMethod(name, args)
		}
		catch(MissingMethodException e) {
			return getInterceptingObject(name)
		}
	}
	
	@Override
	public Object getProperty(String property) {
		
		try {
			return super.getProperty(property)
		} catch(MissingPropertyException e) {
			return getInterceptingObject(property)
		}
	}
	
	@Override
	public void setProperty(String property, Object newValue) {
		
		try {
			super.setProperty(property, newValue)
		} catch(MissingPropertyException e) {
		}
	}
	
	private Object getInterceptingObject(String name) {
		[
			'invokeMethod': { n, a ->
				return n
			},
			'getProperty': { p ->
				return p
			},
			'setProperty': { p, v ->
			}
		] as Object
	}
}
