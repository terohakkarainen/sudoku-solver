package fi.thakki.sudokusolver.engine.util

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object StringCompressor {

    fun compress(s: String): ByteArray =
        ByteArrayOutputStream().use { bos ->
            GZIPOutputStream(bos).use { gos ->
                gos.bufferedWriter(Charsets.UTF_8).use { it.write(s) }
            }
            bos.toByteArray()
        }

    fun decompress(data: ByteArray): String =
        GZIPInputStream(data.inputStream()).use { gis ->
            gis.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
}
