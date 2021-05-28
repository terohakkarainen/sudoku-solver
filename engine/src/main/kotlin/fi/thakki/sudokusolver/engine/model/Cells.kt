package fi.thakki.sudokusolver.engine.model

import kotlinx.serialization.Serializable

@Serializable
class Cells(
    override val cells: Set<Cell>
) : CellCollection(), Set<Cell> by cells
