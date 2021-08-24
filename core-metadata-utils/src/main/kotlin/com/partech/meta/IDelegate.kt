package com.partech.meta

import kotlin.reflect.KProperty

/** Helper interface for defining custom delegates. */
interface IDelegate<X, A> {
    operator fun getValue(value: X, property: KProperty<*>): A
    operator fun setValue(ctx: X, property: KProperty<*>, value: A)
}