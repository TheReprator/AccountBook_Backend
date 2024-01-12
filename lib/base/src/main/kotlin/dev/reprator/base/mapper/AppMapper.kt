package dev.reprator.base.mapper

interface AppMarkerMapper

fun interface AppMapper<in InputModal, out OutputModal> {
    suspend fun map(from: InputModal): OutputModal
}


interface AppMapperToFrom<InputModal, OutputModal> {
    suspend fun mapTo(from: InputModal): OutputModal
    suspend fun mapFrom(from: OutputModal): InputModal
}