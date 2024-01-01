package dev.reprator.core.util.dbConfiguration

interface DatabaseFactory {
    fun connect()
    fun close()
}
