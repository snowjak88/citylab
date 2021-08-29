# jCity

A [libGDX](https://libgdx.com/) tech-demo. Eventually intended to implement a city-building game with an emphasis on logistics.

At present, this gives you:

 - A rudimentary UI
 - An in-game console powered by Groovy
 - An isometric game-screen with pan and zoom
 - The beginnings of a homemade tile-set
 - *Lots* of Groovy scripting.

## Groovy Scripting

A big goal of this project is to enable extensibility.

To that end, I've tried to implement some useful DSLs (Domain-Specific Languages) using [Groovy](http://www.groovy-lang.org/). At present, these allow you to define:

 - Map-generators
 - Tile-sets
 - Modules
 
## Modules

Modules are where the bulk of game functionality is defined.

A Module is, at its most basic, a Groovy script that leverages a [Domain-Specific Language (DSL)](http://docs.groovy-lang.org/docs/latest/html/documentation/core-domain-specific-languages.html) to declare:

 * Entity-processing systems (see [the documentation](https://github.com/libgdx/ashley/wiki/Built-in-Entity-Systems) for the Ashley framework)
 * Rendering-hooks
 * *Input-handlers -- planned*
 * *GUI elements (menus, buttons, windows, map-overlays) -- planned*

### Rendering-Hooks

A rendering-hook is a piece of code that can be inserted into the rendering-loop, and which is called at a certain time each frame.

There are 2 kinds of rendering-hooks:

 1) Cell-rendering hooks -- called for every single visible map-cell.
 1) "Custom" rendering-hooks -- these are given the ability to render to the whole screen. The map-renderer -- that calls all those cell-rendering hooks -- is implemented as a custom rendering-hook.

Both kinds of rendering-hooks can be given an ID, and both can be prioritized relative to other such IDs.

For example: the map-renderer that calls all your cell-rendering hooks has the ID `map`. Knowing that, it's easy to define a custom-renderer that needs to be drawn **after** the map is rendered with:

```
customRenderer 'myRenderer', { deltaTime, batch, shapeDrawer, renderingSupport ->
	// ... render here ...
} after 'map
```
 
### Example Module

The following is part of the definition for the [terrain-fitting Module](https://github.com/snowjak88/jCity/blob/master/assets/data/modules/terrain/terrain.module.groovy). It handles:

 * fitting terrain-tiles to map-cells that require them (via an [IteratingSystem](https://github.com/libgdx/ashley/wiki/Built-in-Entity-Systems)
 * rendering those terrain-tiles when required (via a "cell rendering-hook")

```
import java.lang.Math

id = 'terrain'
description = 'Handles fitting terrain-tiles to the map.'

//
// This module's systems need this Component-class.
class IsTerrainTile implements Component {
	List<Tile> tiles = []
}

// ComponentMappers make us faster at querying and retrieving Components from entities
terrainMapper = ComponentMapper.getFor(IsTerrainTile)

//
// This module declares its entity-processing systems in another file.
// That file is loaded and processed now.
include 'systems.groovy'

//
// Declare a cell-rendering hook into the map-rendering loop.
// This will be called every frame for every on-screen map-cell.
cellRenderHook 'terrainRender', { deltaTime, cellX, cellY, renderingSupport ->
	//
	// Each map-cell should be associated with its own Entity.
	// While each such Entity should be tagged with an IsMapCell component,
	// and such tagged Entities should already be registered in the map-object,
	// we may not have completed all that yet.
	def entity = state.map.getEntity(cellX, cellY)
	if(entity)
		//
		// If this entity is tagged with IsTerrainTile, draw those terrain-tiles now.
		if(terrainMapper.has(entity))
			for(def tile in terrainMapper.get(entity).tiles)
				renderingSupport.renderTile cellX, cellY, tile
}
```
 
## Recent Changes

### 2021-08-29

Module-definitions support:

 * `include` other scripts (to break up your module into multiple script-files)
 * `include` *external JARs* (so your Module can leverage libraries)
 * *relative* dependency ("before this ID", "after these IDs")
 * relatively-prioritized, named rendering-hooks (that will overwrite identically-named rendering-hooks)

The `GameService` interface allows programmatic control over:

 * Module initialization/uninitialization
 * Module re-loading (i.e., you can re-load your Modules from their script-files at run-time, without stopping the game)
 * new-game setup
 * game-loading (*well, not yet*)
 * game-saving (*well, not yet*)

### 2021-08-11

Tile-fitting -- or, more properly, *terrain* tile-fitting -- is carried out in a new entity-processing system, which itself is defined at run-time in our very first Module! The CityMap instance now just carries "flavors" from the map-generator (need to think of a better name for those), and a list of Entities for every cell.

Modules! Modules are here now. At present, you can define the following for each module:

 * entity systems (at present only IteratingSystem is supported, but it's trivial to add).
 * "rendering hooks" -- i.e., a callback that's executed for every map-cell that gets rendered. A chance for your Module to put something on the map, so to speak.

What's yet to be added:
 * UI elements

Defined a new AbstractScriptService (or whatever I called it), which purports to render more efficient the task of serving up those resources we load that are implemented by a Groovy script. Really just cuts down on the boilerplate a bit.
