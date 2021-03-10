package fi.thakki.sudokusolver.model

class Stack(
    override val cells: List<Cell>
) : CellCollection(), List<Cell> by cells
