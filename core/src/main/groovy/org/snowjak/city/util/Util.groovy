/**
 * 
 */
package org.snowjak.city.util


/**
 * @author snowjak88
 *
 */
class Util {
	
	public static int min(int...values) {
		def minValue = Integer.MAX_VALUE
		for(def v : values)
			minValue = (minValue > v) ? v : minValue;
		minValue
	}
	
	public static float min(float...values) {
		def minValue = Float.MAX_VALUE
		for(def v : values)
			minValue = (minValue > v) ? v : minValue;
		minValue
	}
	
	public static int max(int...values) {
		def minValue = Integer.MIN_VALUE
		for(def v : values)
			minValue = (minValue < v) ? v : minValue;
		minValue
	}
	
	public static float max(float...values) {
		def minValue = -Float.MAX_VALUE
		for(def v : values)
			minValue = (minValue < v) ? v : minValue;
		minValue
	}
	
	public static int clamp(int value, int min, int max) {
		if(value < min)
			return min
		if(value > max)
			return max
		value
	}
	
	public static float clamp(float value, float min, float max) {
		if(value < min)
			return min
		if(value > max)
			return max
		value
	}
	
	public static int wrap(int v, int min, int max) {
		final int range = (max+1) - min
		while (v < min) v += range
		while (v > max) v -= range
		v
	}
}
