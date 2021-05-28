package fi.thakki.sudokusolver.engine.model

class Region(
    override val cells: Set<Cell>
) : CellCollection(), Set<Cell> by cells
