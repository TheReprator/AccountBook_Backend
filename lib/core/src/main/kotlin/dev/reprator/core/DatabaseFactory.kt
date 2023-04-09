package dev.reprator.core

interface DatabaseFactory {
    fun connect()
    fun close()
}
