package fi.thakki.sudokusolver.model

import kotlinx.serialization.Serializable

@Serializable
class Region(
    override val cells: Set<Cell>
) : CellCollection(), Set<Cell> by cells
