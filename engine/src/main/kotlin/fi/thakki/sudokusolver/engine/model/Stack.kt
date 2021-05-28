package fi.thakki.sudokusolver.engine.model

class Stack(
    override val cells: List<Cell>
) : CellCollection(), List<Cell> by cells
