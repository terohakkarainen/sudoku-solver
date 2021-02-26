package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.domain.Coordinates
import fi.thakki.sudokusolver.util.PuzzleBuilder
import kotlin.random.Random

fun main(args: Array<String>) {

    val puzzle = PuzzleBuilder(layout = PuzzleBuilder.Layout.STANDARD_4X4)
        .withGiven("3", Coordinates(0, 1))
        .withGiven("2", Coordinates(1, 0))
        .withGiven("2", Coordinates(2, 1))
        .withGiven("3", Coordinates(3, 0))
        .withGiven("4", Coordinates(0, 2))
        .withGiven("1", Coordinates(1, 3))
        .withGiven("1", Coordinates(2, 2))
        .withGiven("4", Coordinates(3, 3))
        .build()

    puzzle.bands.reversed().forEach { band ->
        print("| ")
        band.forEach { cell ->
            print("${cell.value ?: "."} | ")
        }
        println()
    }

    println()

    val regionTags = puzzle.regions.map {
        it to Random.nextInt(9)
    }.toMap()

    puzzle.bands.reversed().forEach { band ->
        print("| ")
        band.forEach { cell ->
            print("${regionTags[puzzle.regionOf(cell)]} | ")
        }
        println()
    }
}
