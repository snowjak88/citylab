import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

class HasTerrainType implements Component, Poolable {
	String type
	
	void reset() {
		type = null
	}
}