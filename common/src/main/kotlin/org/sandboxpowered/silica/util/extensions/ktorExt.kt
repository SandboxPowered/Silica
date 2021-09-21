package org.sandboxpowered.silica.util.extensions

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

suspend fun HttpClient.downloadFile(file: File, url: String): Boolean {
    val call: HttpResponse = request {
        url(url)
        method = HttpMethod.Get
    }
    if (!call.status.isSuccess()) {
        return false
    }
    call.content.copyAndClose(file.writeChannel())
    return true
}