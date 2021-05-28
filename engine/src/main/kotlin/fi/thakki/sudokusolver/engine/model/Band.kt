package fi.thakki.sudokusolver.engine.model

class Band(
    override val cells: List<Cell>
) : CellCollection(), List<Cell> by cells
