package fi.thakki.sudokusolver.application

import fi.thakki.sudokusolver.model.RevisionInformation
import fi.thakki.sudokusolver.util.DateConversions

object RevisionMessages {

    fun newRevisionStored(revisionInfo: RevisionInformation): String =
        "Stored new revision ${revisionInfo.number} at " +
                "${DateConversions.toPrintable(revisionInfo.createdAt)}: \"${revisionInfo.description}\""

    fun switchedToRevision(revisionInfo: RevisionInformation): String =
        "Switched to revision ${revisionInfo.number} created at " +
                "${DateConversions.toPrintable(revisionInfo.createdAt)}: \"${revisionInfo.description}\""
}
