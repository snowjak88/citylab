# city-lab

At present, it's little more than a [libGDX](https://libgdx.com/)-based isometric tech-demo. Eventually intended to implement a city-building game with an emphasis on logistics and minimizing transportation-costs.

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
 * Tools
 * Map-Modes
 * "Visual parameters"
 * *GUI elements (menus, buttons, windows, map-overlays) -- planned*

### Map Modes

A map-mode is a collection of rendering-hooks and tools. The `default` map-mode contains, at a minimum, the terrain-renderer and its associated tools. Map-modes are intended to be used to implement overlays and displays for supplemental information. Only one map-mode may be active at any time.

### Rendering-Hooks

A rendering-hook is a piece of code that can be inserted into the rendering-loop, and which may be called once per frame. A rendering-hook is always associated with at least one map-mode.

The first idea was to have *all* Module-based rendering be performed via discrete render-hooks. The `terrain` module, for instance, would register a cell-rendering hook that would handle drawing terrain-tiles (ordered relative to other such hooks).

*However*, this was eventually abandoned in favor of a different system: Modules may register their tiles with a certain entity-system Component. This resulted in a significant boost in rendering efficiency, because:

* the rendering-system can "see" all required tiles up-front, and so ignore any bottom tiles that would be completely occluded by covering tiles
* Modules need only define their tiles when changes are required, instead of every single frame

### Visual Parameters

A "visual parameter" defines a field that should be included in the game-setup screen. For example, the `water` module registers one such field to allow the "sea-level" to be configured.

### Tools

A tool is a bundle of functionality focused on acting upon the game-state.

A tool may have one or more activation-methods:

* a tool-bar button (with an associated button-group)
* a hotkey

Tools may listen for modifier-keys -- Shift, Control, Alt -- while activated.

Triggering any activation-state activates a tool's functionality, which may be implemented in one or more handlers:

* map-hover (called when the mouse is moved over a different map-cell with no buttons pressed)
* map-click (a mouse-button is depressed and released without changing its position)
* map-drag (divided into drag-start, -update, and -end events)
* simple update (called on every frame)
 
## Recent Changes

### 2021-09-27

**Visual parameters**. A Module may register a "visual parameter" that is automatically included in the game-setup screen.

We have (basic) road-tiles now. The `roads` module provides basic road-planning functionality. Roads may be laid out in one of three modes (straight, L-bend, or A*-pathfound).

The `water` module is the barest of bare-bones. It only marks sea-level map-vertices as "watery", and only when the map is first loaded. It doesn't do any "flow" at present.

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
