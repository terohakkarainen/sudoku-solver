package fi.thakki.sudokusolver.model

class Cells(
    override val cells: Set<Cell>
) : CellCollection(), Set<Cell> by cells
