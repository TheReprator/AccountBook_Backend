package dev.reprator.testModule

import java.lang.reflect.GenericArrayType
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.Arrays

class AppReflectionTypes {

    private val EMPTY_TYPE_ARRAY = arrayOf<Type>()

    /**
     * Returns a new parameterized type, applying `typeArguments` to `rawType`.
     */
    fun newParameterizedType(rawType: Type, vararg typeArguments: Type): ParameterizedType {
        return ParameterizedTypeImpl(null, rawType, *typeArguments)
    }

    /**
     * Returns a type that is functionally equal but not necessarily equal according to [ ][Object.equals].
     */
    fun canonicalize(type: Type?): Type? {
        if (type is Class<*>) {
            val c = type as Class<*>?
            return if (c!!.isArray) GenericArrayTypeImpl(canonicalize(c.componentType)) else c
        } else if (type is ParameterizedType) {
            val p = type as ParameterizedType?
            return ParameterizedTypeImpl(
                p!!.ownerType,
                p.rawType, *p.actualTypeArguments
            )
        } else if (type is GenericArrayType) {
            val g = type as GenericArrayType?
            return GenericArrayTypeImpl(g!!.genericComponentType)
        } else if (type is WildcardType) {
            val w = type as WildcardType?
            return WildcardTypeImpl(w!!.upperBounds, w.lowerBounds)
        } else {
            return type // This type is unsupported!
        }
    }

    fun equal(a: Any?, b: Any): Boolean {
        return a === b || a != null && a == b
    }

    /**
     * Returns true if `a` and `b` are equal.
     */
    fun equals(a: Type, b: Type): Boolean {
        if (a === b) {
            return true // Also handles (a == null && b == null).
        } else if (a is Class<*>) {
            return a == b // Class already specifies equals().
        } else if (a is ParameterizedType) {
            if (b !is ParameterizedType) return false
            val aTypeArguments = (a as? ParameterizedTypeImpl)?.typeArguments ?: a.actualTypeArguments
            val bTypeArguments = (b as? ParameterizedTypeImpl)?.typeArguments ?: b.actualTypeArguments
            return (equal(a.ownerType, b.ownerType)
                    && a.rawType == b.rawType
                    && Arrays.equals(aTypeArguments, bTypeArguments))
        } else return if (a is GenericArrayType) {
            if (b !is GenericArrayType) false else equals(a.genericComponentType, b.genericComponentType)
        } else if (a is WildcardType) {
            if (b !is WildcardType) false else Arrays.equals(
                a.upperBounds,
                b.upperBounds
            ) && Arrays.equals(a.lowerBounds, b.lowerBounds)
        } else if (a is TypeVariable<*>) {
            if (b !is TypeVariable<*>) false else a.genericDeclaration === b.genericDeclaration && a.name == b.name
        } else {
            // This isn't a supported type. Could be a generic array type, wildcard type, etc.
            false
        }
    }

    fun hashCodeOrZero(o: Any?): Int {
        return o?.hashCode() ?: 0
    }

    fun typeToString(type: Type?): String {
        return if (type is Class<*>) type.name else type!!.toString()
    }

    fun checkNotPrimitive(type: Type?) {
        if (type is Class<*> && type.isPrimitive) {
            throw IllegalArgumentException()
        }
    }

    private inner class ParameterizedTypeImpl internal constructor(
        ownerType: Type?,
        rawType: Type,
        vararg typeArguments: Type
    ) : ParameterizedType {
        val typeArguments: Array<Type?>
        private val ownerType: Type?
        private val rawType: Type?

        init {
            // require an owner type if the raw type needs it
            if (rawType is Class<*>) {
                val rawTypeAsClass = rawType
                val isStaticOrTopLevelClass =
                    Modifier.isStatic(rawTypeAsClass.modifiers) || rawTypeAsClass.enclosingClass == null
                if (ownerType == null && !isStaticOrTopLevelClass)
                    throw IllegalArgumentException()
            }

            this.ownerType = if (ownerType == null) null else canonicalize(ownerType)
            this.rawType = canonicalize(rawType)
            this.typeArguments = typeArguments.clone() as Array<Type?>
            for (t in this.typeArguments.indices) {
                if (this.typeArguments[t] == null) throw NullPointerException()
                checkNotPrimitive(this.typeArguments[t])
                this.typeArguments[t] = canonicalize(this.typeArguments[t])
            }
        }

        override fun getActualTypeArguments(): Array<Type?> {
            return typeArguments.clone()
        }

        override fun getRawType(): Type? {
            return rawType
        }

        override fun getOwnerType(): Type? {
            return ownerType
        }

        override fun equals(other: Any?): Boolean {
            return other is ParameterizedType && equals(this, other)
        }

        override fun hashCode(): Int {
            return (Arrays.hashCode(typeArguments)
                    xor rawType!!.hashCode()
                    xor hashCodeOrZero(ownerType))
        }

        override fun toString(): String {
            val result = StringBuilder(30 * (typeArguments.size + 1))
            result.append(typeToString(rawType))

            if (typeArguments.size == 0) {
                return result.toString()
            }

            result.append("<").append(typeToString(typeArguments[0]))
            for (i in 1 until typeArguments.size) {
                result.append(", ").append(typeToString(typeArguments[i]))
            }
            return result.append(">").toString()
        }
    }

    private inner class GenericArrayTypeImpl(componentType: Type?) : GenericArrayType {
        private val componentType: Type?

        init {
            this.componentType = canonicalize(componentType)
        }

        override fun getGenericComponentType(): Type? {
            return componentType
        }

        override fun equals(other: Any?): Boolean {
            return other is GenericArrayType && equals(this, other)
        }

        override fun hashCode(): Int {
            return componentType!!.hashCode()
        }

        override fun toString(): String {
            return typeToString(componentType) + "[]"
        }
    }

    /**
     * The WildcardType interface supports multiple upper bounds and multiple lower bounds. We only
     * support what the Java 6 language needs - at most one bound. If a lower bound is set, the upper
     * bound must be Object.class.
     */
    private inner class WildcardTypeImpl(upperBounds: Array<Type>, lowerBounds: Array<Type>) : WildcardType {
        private val upperBound: Type?
        private val lowerBound: Type?

        init {
            if (lowerBounds.size > 1) throw IllegalArgumentException()
            if (upperBounds.size != 1) throw IllegalArgumentException()

            if (lowerBounds.size == 1) {
                checkNotPrimitive(lowerBounds[0])
                if (upperBounds[0] !== Any::class.java) throw IllegalArgumentException()
                this.lowerBound = canonicalize(lowerBounds[0])
                this.upperBound = Any::class.java
            } else {
                checkNotPrimitive(upperBounds[0])
                this.lowerBound = null
                this.upperBound = canonicalize(upperBounds[0])
            }
        }

        override fun getUpperBounds(): Array<Type?> {
            return arrayOf<Type?>(upperBound)
        }

        override fun getLowerBounds(): Array<Type> {
            return lowerBound?.let { arrayOf(it) } ?: EMPTY_TYPE_ARRAY
        }

        override fun equals(other: Any?): Boolean {
            return other is WildcardType && equals(this, other)
        }

        override fun hashCode(): Int {
            // This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
            return (if (lowerBound != null) 31 + lowerBound.hashCode() else 1) xor 31 + upperBound!!.hashCode()
        }

        override fun toString(): String {
            return if (lowerBound != null) {
                "? super " + typeToString(lowerBound)
            } else if (upperBound === Any::class.java) {
                "?"
            } else {
                "? extends " + typeToString(upperBound)
            }
        }
    }
}