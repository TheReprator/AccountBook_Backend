package dev.reprator.base.action

interface AppDatabaseFactory {
    fun connect()
    fun close()
}
