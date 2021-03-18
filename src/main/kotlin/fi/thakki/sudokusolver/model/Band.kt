package fi.thakki.sudokusolver.model

class Band(
    override val cells: List<Cell>
) : CellCollection(), List<Cell> by cells
