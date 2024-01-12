package dev.reprator.base.usecase

public interface AppEntityValidator<T> {
    fun validate(): T
}
