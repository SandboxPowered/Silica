package org.sandboxpowered.silica.util.extensions

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File

suspend fun HttpClient.downloadFile(file: File, url: String): Boolean {
    val call: HttpResponse = request {
        url(url)
        method = HttpMethod.Get
    }
    if (!call.status.isSuccess()) {
        return false
    }
    call.bodyAsChannel().copyAndClose(file.writeChannel())
    return true
}