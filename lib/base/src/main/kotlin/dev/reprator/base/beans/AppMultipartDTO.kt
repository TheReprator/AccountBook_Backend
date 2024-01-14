package dev.reprator.base.beans

class AppMultipartDTO<T>(
    val data: T,
    val image: ByteArray? = null
)