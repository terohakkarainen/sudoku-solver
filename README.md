# SudokuSolver

Shell application for people loving to solve sudokus.

## Table of contents
* [General information](#general-information)
* [Technologies](#technologies)
* [Building and launching](#building-and-launching)
* [Application usage](#application-usage)
* [Sudoku file format](#sudoku-file-format)

## General information
SudokuSolver is a shell application for making it easier to solve sudokus than
the classic pen-and-paper method. The application can (as the name suggests) also
solve any sudoku, while still preserving the user the joy of solving it herself.

## Technologies
* Kotlin 1.4+
* Gradle
  * ShadowJar plugin (https://github.com/johnrengelman/shadow)
  * BuildConfig plugin (https://github.com/gmazzo/gradle-buildconfig-plugin)
* SnakeYml
* JUnit 5
* AssertK
* Detekt
* ArchUnit

## Building and launching
Build the application from shell with command
```
gradle shadowJar
```
which will create the JAR file into `build/libs` directory.

Launch the application with command
```
java -jar build/libs/sudoku-solver-0.0.1-SNAPSHOT.jar src/test/resources/sudoku.yml
```
which will also pass a sample sudoku file `src/test/resources/sudoku.yml` as command line argument.

## Application usage
SudokuSolver shows the user a prompt looking like this
```
? > help | R:1, 35% | Enter command:
```
in which `R:1` stands for revision 1 and `35%` the completeness of the sudoku (percentage of cells
with either given or set value).

Typing `?` and pressing Enter shows a short help text. All commands follow the same
convention
```
<command type as single character> <command-specific arguments, if any>
```
For example, command `p` _prints_ the current sudoku to screen and command `s 0,0 3` _sets_ value
_3_ to cell in coordinates _(0,0)_. Command `q` _quits_ the application. 

## Sudoku file format
Sudoku is described as a YAML file with following structure:
* _dimension_ (**mandatory**): width/height of the sudoku grid
* _symbols_ (**mandatory**): list of one-character symbols used in the sudoku
* _givens_ (**mandatory**): values initially given in sudoku, presented graphically
* _regions_ (**optional**): custom regions in sudoku, if not a standard sudoku.

For an example of standard sudoku file, open `src/test/resources/sudoku.yml` in text editor.
For an example of custom sudoku file, open `src/test/resources/7x7.yml` in text editor.
