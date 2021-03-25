package fi.thakki.sudokusolver.service.analyzer

typealias AnalyzerFunc = () -> AnalyzeResult

fun runEagerly(vararg analyzers: AnalyzerFunc): AnalyzeResult =
    runEagerly(analyzers.toList())

fun runEagerly(analyzers: List<AnalyzerFunc>): AnalyzeResult =
    analyzers.iterator().let { iterator ->
        while (iterator.hasNext()) {
            iterator.next().invoke().let { analyzeResult ->
                if (analyzeResult != AnalyzeResult.NoChanges) {
                    return analyzeResult
                }
            }
        }
        return AnalyzeResult.NoChanges
    }
