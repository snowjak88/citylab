id = 'clouds'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

//
// Declare a "custom" rendering hook into the map-rendering loop.
// This is called only once per frame.
//
// As with cell-rendering hooks, custom-rendering hooks have IDs, too, which also
// are susceptible of being overwritten by other custom-rendering hooks.
//
// Note how we prioritize this renderer, relative to the map-renderer (which has the ID "map"),
// which executes all those cell-render-hooks.
//
// You indicate priorities using both "before" and "after", and you can prioritize both
// custom- and cell-rendering hooks.
//
dependsOn 'cloud.png', Texture
cloudTexture = assets.get( 'cloud.png', Texture )

clouds = null
onActivate {
	->
	clouds = new boolean[Util.max(3, state.map.width / 12)][Util.max(state.map.height / 12, 3)]
	for(def x=0; x<clouds.length; x++)
		for(def y=0; y<clouds[x].length; y++)
			clouds[x][y] = state.rnd.nextInt(10) <= 3
}

cloudOffsetX = 0f
cloudOffsetY = 0f
cloudsIndexStartX = 0
cloudsIndexStartY = 0

cloudPosition = new Vector2()

renderHook 'clouds', { delta, batch, shapeDrawer, support ->
	
	if(clouds == null)
		return
	
	def viewBounds = support.viewportWorldBounds
	
	final float cloudWidth = 4
	final float cloudHeight = cloudWidth * (cloudTexture.height / cloudTexture.width)
	
	final cloudSpacingX = state.map.width / ( clouds.length - 2 )
	final cloudSpacingY = state.map.height / ( clouds[0].length - 2 )
	
	cloudOffsetX += delta * 7 / 2
	cloudOffsetY += delta * 3 / 2
	
	if(cloudOffsetX >= cloudSpacingX) {
		cloudOffsetX -= cloudSpacingX
		cloudsIndexStartX = Util.wrap( cloudsIndexStartX - 1, 0, clouds.length-1)
	}
	if(cloudOffsetY >= cloudSpacingY) {
		cloudOffsetY -= cloudSpacingY
		cloudsIndexStartY = Util.wrap( cloudsIndexStartY - 1, 0, clouds[0].length-1)
	}
	
	float originX = cloudOffsetX
	float originY = cloudOffsetY
	
	def x = originX - cloudSpacingX
	for(def i=0; i<clouds.length; i++) {
		
		def y = state.map.height + cloudSpacingY - originY
		for(def j=0; j<clouds[i].length; j++) {
			
			if( clouds[Util.wrap( i+cloudsIndexStartX, 0, clouds.length-1 )][Util.wrap( j+cloudsIndexStartY, 0, clouds[i].length-1 )] ) {
				
				float alpha = 1
				if(x < 0 || y < 0 || x > state.map.width || y > state.map.width) {
					def xd = -1
					def yd = -1
					if(x < 0 || x > state.map.width)
						xd = 1 - Util.clamp( (x<0) ? (-x / cloudSpacingX) : ((x-state.map.width) / cloudSpacingX), 0, 1 )
					if(y < 0 || y > state.map.height)
						yd = 1 - Util.clamp( (y<0) ? (-y / cloudSpacingY) : ((y-state.map.height) / cloudSpacingY), 0, 1 )
					
					
					if(xd < 0)
						xd = yd
					if(yd < 0)
						yd = xd
					alpha = ( xd + yd ) / 2
				}
				
				if(state.camera) {
					final newAlpha = alpha * ( state.camera.zoom / 4f )
					alpha = newAlpha
				}
				
				if(alpha < 1)
					batch.setColor new Color(1f,1f,1f,alpha)
				else
					batch.setColor Color.WHITE
				
				
				cloudPosition.set( (float)x, (float)y )
				support.mapToViewport cloudPosition
				
				batch.draw cloudTexture, cloudPosition.x, cloudPosition.y, (float) cloudWidth, (float) cloudHeight
			}
			
			y -= cloudSpacingY
		}
		
		x += cloudSpacingX
		
		batch.setColor Color.WHITE
	}
	
} after 'map'

mapModes['default'].renderingHooks << 'clouds'