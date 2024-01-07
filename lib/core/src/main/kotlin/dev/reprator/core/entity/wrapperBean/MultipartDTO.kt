package dev.reprator.core.entity.wrapperBean

class MultipartDTO<T>(
    val data: T,
    val image: ByteArray? = null
)