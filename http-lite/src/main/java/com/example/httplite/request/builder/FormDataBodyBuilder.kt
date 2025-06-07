package com.example.httplite.request.builder

import com.google.gson.Gson
import com.example.httplite.request.builder.model.MediaData
import java.io.ByteArrayOutputStream
import java.util.UUID

internal class FormDataBodyBuilder(
    private val data: Map<String, Any>,
    private val dataKey: String,
    private val boundary: String = UUID.randomUUID().toString(),
    private val gson: Gson
) {
    private val output = ByteArrayOutputStream()

    fun buildFormDataBody(): ByteArray {
        data.forEach { (key, value) ->
            val fieldName = "$dataKey[$key]"
            when (value) {
                is MediaData -> writeMediaPart(fieldName, value)
                else -> writeJsonPart(fieldName, value)
            }
        }

        return writeEndBoundary().toByteArray()
    }

    private fun writeMediaPart(
        fieldName: String,
        media: MediaData
    ): FormDataBodyBuilder = apply {
        writeStartBoundary()
        writeHeader("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"${media.file.name}\"")
        writeHeader("Content-Type: ${media.contentType}")
        writeLine()
        output.write(media.file.readBytes())
        writeLine()
    }

    private fun writeJsonPart(
        fieldName: String,
        value: Any
    ): FormDataBodyBuilder = apply {
        writeStartBoundary()
        writeHeader("Content-Disposition: form-data; name=\"$fieldName\"")
        writeLine()
        writeString(gson.toJson(value))
        writeLine()
    }

    private fun writeStartBoundary() {
        writeString("--$boundary$NEW_LINE")
    }

    private fun writeEndBoundary(): FormDataBodyBuilder = apply {
        writeString("--$boundary--$NEW_LINE")
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

    private fun toByteArray(): ByteArray = output.toByteArray()

    private companion object {
        const val NEW_LINE = "\r\n"
    }
}
