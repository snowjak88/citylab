# jCity

A [libGDX](https://libgdx.com/) tech-demo. Eventually intended to implement a city-building game with an emphasis on logistics.

At present, this gives you:

 - A rudimentary UI
 - An isometric game-screen with pan and zoom
 - The beginnings of a homemade tile-set
 - *Lots* of Groovy scripting.

## Groovy Scripting

A big goal of this project is to enable extensibility.

To that end, I've tried to implement some useful DSLs (Domain-Specific Languages) using [Groovy](http://www.groovy-lang.org/). At present, these allow you to define:

 - Map-generators
 - Tile-sets
 - Modules
 
## Recent Changes

Tile-fitting -- or, more properly, *terrain* tile-fitting -- is carried out in a new entity-processing system, which itself is defined at run-time in our very first Module! The CityMap instance now just carries "flavors" from the map-generator (need to think of a better name for those), and a list of Entities for every cell.

Modules! Modules are here now. At present, you can define the following for each module:

 * entity systems (at present only IteratingSystem is supported, but it's trivial to add).
 * "rendering hooks" -- i.e., a callback that's executed for every map-cell that gets rendered. A chance for your Module to put something on the map, so to speak.

What's yet to be added:
 * UI elements

Defined a new AbstractScriptService (or whatever I called it), which purports to render more efficient the task of serving up those resources we load that are implemented by a Groovy script. Really just cuts down on the boilerplate a bit.
