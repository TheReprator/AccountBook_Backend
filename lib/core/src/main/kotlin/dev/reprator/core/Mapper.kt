package dev.reprator.core

interface Mapper<in InputModal, out OutputModal> {
    suspend fun map(from: InputModal): OutputModal
}


interface MapperToFrom<InputModal, OutputModal> {
    suspend fun mapTo(from: InputModal): OutputModal
    suspend fun mapFrom(from: OutputModal): InputModal
}