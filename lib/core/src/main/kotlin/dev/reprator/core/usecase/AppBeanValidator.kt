package dev.reprator.core.usecase

public interface AppEntityValidator<T> {
    fun validate(): T
}
