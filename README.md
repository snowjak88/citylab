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
 
 The tile-set DSL, especially, I've put a lot of work into.
 
When I get around to it, the plan is to enable entire gameplay modules to be loaded that way as well -- new map-overlays, new buildings, new mechanics.

### Notes for the future

I've been feeling that the Tile-Set DSL is too cluttered. That, and it doesn't do everything that I want it to do -- e.g.:

 - allow you to easily include tile-set definitions in sub-folders that inherit their super-definition's properties.
 - support run-time tile properties -- e.g., enabling water-tiles to re-draw themselves at a higher or lower altitude as need be

## Gradle

This project uses [Gradle](http://gradle.org/) to manage dependencies. Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands. Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.