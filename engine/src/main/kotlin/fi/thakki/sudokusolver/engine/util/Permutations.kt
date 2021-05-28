package fi.thakki.sudokusolver.engine.util

fun <T : Any> permutations(
    elements: Set<T>,
    tupleSize: Int
): Set<Set<T>> {
    require(tupleSize >= 0) { "Tuple size must be positive integer" }
    return accumulatingPermutations(elements, tupleSize, emptySet())
}

private fun <T : Any> accumulatingPermutations(
    elements: Set<T>,
    tupleSize: Int,
    acc: Set<T>
): Set<Set<T>> =
    if (acc.size == tupleSize - 1) {
        elements.mapNotNull { element ->
            if (!acc.contains(element)) acc.plus(element) else null
        }.toSet()
    } else {
        elements.flatMap { element ->
            accumulatingPermutations(elements.drop(1).toSet(), tupleSize, acc.plus(element))
        }.toSet()
    }
