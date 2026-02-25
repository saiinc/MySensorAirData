package com.saionji.mysensor.shared.network.service

import io.ktor.client.HttpClient

expect object HttpClientFactory {
    fun createHttpClient(): HttpClient
}
