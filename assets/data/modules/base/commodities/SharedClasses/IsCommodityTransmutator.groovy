import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

class IsCommodityTransmutator implements Component, Poolable {
	
	/**
	 * Transmutations by ID
	 */
	final Map<String, Transmutation> transmutations = [:]
	
	void reset() {
		transmutations.clear()
	}
	
	public static class Transmutation {
		final Map<String, Float> reagents = [:]
		final Map<String, Float> products = [:]
	}
}