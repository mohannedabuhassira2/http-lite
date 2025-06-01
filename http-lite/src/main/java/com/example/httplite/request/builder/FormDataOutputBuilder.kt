package com.example.httplite.request.builder

import com.google.gson.Gson
import com.example.httplite.model.MediaData
import java.io.ByteArrayOutputStream
import java.util.UUID

internal class FormDataOutputBuilder(
    private val boundary: String = UUID.randomUUID().toString(),
    private val gson: Gson
) {
    private val output = ByteArrayOutputStream()

    fun writeMediaPart(
        fieldName: String,
        media: MediaData
    ): FormDataOutputBuilder = apply {
        writeBoundary()
        writeHeader("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"${media.file.name}\"")
        writeHeader("Content-Type: ${media.contentType}")
        writeLine()
        output.write(media.file.readBytes())
        writeLine()
    }

    fun writeJsonPart(
        fieldName: String,
        value: Any
    ): FormDataOutputBuilder = apply {
        writeBoundary()
        writeHeader("Content-Disposition: form-data; name=\"$fieldName\"")
        writeLine()
        writeString(gson.toJson(value))
        writeLine()
    }

    fun writeEndBoundary(): FormDataOutputBuilder = apply {
        writeString("--$boundary--$NEW_LINE")
    }

    fun toByteArray(): ByteArray = output.toByteArray()

    private fun writeBoundary() {
        writeString("--$boundary$NEW_LINE")
    }

    private fun writeHeader(header: String) {
        writeString(header + NEW_LINE)
    }

    private fun writeLine() {
        writeString(NEW_LINE)
    }

    private fun writeString(text: String) {
        output.write(text.toByteArray())
    }

    private companion object {
        const val NEW_LINE = "\r\n"
    }
}
