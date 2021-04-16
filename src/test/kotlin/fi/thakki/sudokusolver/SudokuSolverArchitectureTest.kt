package fi.thakki.sudokusolver

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import fi.thakki.sudokusolver.application.SudokuSolverConsoleApplication
import org.junit.jupiter.api.Test

internal class SudokuSolverArchitectureTest {

    private val importedClasses: JavaClasses =
        ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE)

    @Test
    fun `no cyclic package dependencies`() {
        slices()
            .matching("$BASE_PACKAGE.(**)..")
            .should()
            .beFreeOfCycles()
            .check(importedClasses)
    }

    @Test
    fun `model package does not reference service package`() {
        noClasses()
            .that()
            .resideInAPackage("$MODEL_PACKAGE..")
            .should()
            .accessClassesThat()
            .resideInAnyPackage("$SERVICE_PACKAGE..")
            .check(importedClasses)
    }

    @Test
    fun `Architecture layers are ok`() {
        // @formatter:off
        layeredArchitecture()
            .layer(PRESENTATION_LAYER)
                .definedBy("$APPLICATION_PACKAGE..")
                .ignoreDependency(SudokuSolverMain::class.java, SudokuSolverConsoleApplication::class.java)
            .layer(SERVICE_LAYER)
                .definedBy("$SERVICE_PACKAGE..")
            .layer(MODEL_LAYER)
                .definedBy("$MODEL_PACKAGE..")
            .whereLayer(PRESENTATION_LAYER)
                .mayOnlyBeAccessedByLayers(SERVICE_LAYER)
            .whereLayer(SERVICE_LAYER)
                .mayOnlyBeAccessedByLayers(PRESENTATION_LAYER)
            .whereLayer(MODEL_LAYER)
                .mayOnlyBeAccessedByLayers(PRESENTATION_LAYER, SERVICE_LAYER)
            .check(importedClasses)
        // @formatter:on
    }

    companion object {
        private const val PRESENTATION_LAYER = "Presentation"
        private const val SERVICE_LAYER = "Service"
        private const val MODEL_LAYER = "Model"
        private val BASE_PACKAGE = SudokuSolverMain::class.java.`package`.name
        private val APPLICATION_PACKAGE = "$BASE_PACKAGE.application"
        private val MODEL_PACKAGE = "$BASE_PACKAGE.model"
        private val SERVICE_PACKAGE = "$BASE_PACKAGE.service"
        private val UTIL_PACKAGE = "$BASE_PACKAGE.util"
    }
}
