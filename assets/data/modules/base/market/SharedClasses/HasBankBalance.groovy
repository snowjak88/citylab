import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

class HasBankBalance implements Component, Poolable {
	
	float balance = 0.0
	
	void reset() {
		balance = 0
	}
}