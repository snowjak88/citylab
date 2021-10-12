package market

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable

import java.time.Instant

class IsPendingTrade implements Component, Poolable {
	
	Instant created
	Entity from, to
	String commodityID
	float quantity
	boolean instant
	
	void reset() {
		created = null
		from = null
		to = null
		commodityID = null
		quantity = 0
		instant = false
	}
}