package fi.thakki.sudokusolver.model

import kotlinx.serialization.Serializable

@Serializable
class Stack(
    override val cells: List<Cell>
) : CellCollection(), List<Cell> by cells
