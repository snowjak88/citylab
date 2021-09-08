/**
 * 
 */
package org.snowjak.city.util

public class PrioritizationFailedException extends RuntimeException {
	
	private static final long serialVersionUID = 7065095667124956884L
	
	public PrioritizationFailedException() {
		
		super();
	}
	
	public PrioritizationFailedException(String message, Throwable cause) {
		
		super(message, cause);
	}
	
	public PrioritizationFailedException(String message) {
		
		super(message);
	}
	
	public PrioritizationFailedException(Throwable cause) {
		
		super(cause);
	}
}