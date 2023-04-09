package dev.reprator.core

public interface Validator<T> {
    fun validate(): T
}
