package fi.thakki.sudokusolver.model

class Region(
    override val cells: Set<Cell>
) : CellCollection(), Set<Cell> by cells
