/*
Copyright (C) 2021 PAR Government

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

For more information, please email nathan_bedell@partech.com
*/

package com.partech.meta

import java.lang.IllegalArgumentException

/** Declares a metadata field for the object X, allowing the user to
 * get and set values of type A to this particular field. */
interface MetadataField<in X, A> {
    fun set(ctx: X, value: A)
    fun get(ctx: X): A
}

/** An interface for objects providing a key for a metadata field. */
interface Keyed {
    val key: String
}

/** An interface for objects providing a default value of type A that
 * can be extracte in context X. */
interface Default<in X, out A> {
    fun X.getDefaultValue(): A
}

/** Declares a metadata field with a default value for the object X, allowing the user to
 * get and set values of type A to this particular field. */
interface MetadataFieldWithDefault<in X,A>: MetadataField<X,A>, Default<X, A>

interface KeyedMetadataFieldWithDefault<in X,A>: MetadataFieldWithDefault<X,A>, Keyed

fun <X,A> MetadataFieldWithDefault<X,A>.getOrDefault(ctx: X): A {
    return get(ctx) ?: ctx.getDefaultValue()
}

/** Set the given metadata field to the given value on this object. */
fun <X,A> X.setMetaField(field: MetadataField<X, A>, value: A) {
    field.set(this, value)
}

/** Get the given metadata field for this object. Returns null if the metadata field
 * has not been set. */
fun <X,A> X.getMetaField(field: MetadataField<X, A>): A {
    return field.get(this)
}

fun <Y,X,A> MetadataField<X,A>.imap(f: (Y) -> X): MetadataField<Y,A> {
    return object: MetadataField<Y,A> {
        override fun set(ctx: Y, value: A) = set(f(ctx),value)
        override fun get(ctx: Y): A = get(f(ctx))
    }
}

fun <X,A,B> MetadataField<X,A>.omap(f: (A) -> B, g: (B) -> A): MetadataField<X,B> {
    val origField = this
    return object: MetadataField<X,B> {
        override fun set(ctx: X, value: B) = origField.set(ctx,g(value))
        override fun get(ctx: X): B = origField.get(ctx).let { f(it) }
    }
}

/** Helper function for constructing metadata fields making use of the common case of
 * a metadata field that is backed by a map from string keys to strings. */
fun <X,A> fromCodec(
    key: String,
    writeKeyValue: (X,String,String?) -> X,
    getKeyValue: (X,String) -> String?,
    parse: X.(String) -> A?, serialize: X.(A) -> String
): MetadataField<X, A?> {
    return object: MetadataField<X, A?> {
        override fun set(ctx: X, value: A?) {
            writeKeyValue(ctx, key, value?.let { ctx.serialize(it) })
        }
        override fun get(ctx: X): A? =
            getKeyValue(ctx,key)?.let { ctx.parse(it) }
    }
}

/** Helper function for constructing required metadata fields making use of the common case of
 * a metadata field that is backed by a map from string keys to strings. */
fun <X,A: Any> requiredFieldFromCodec(
    key: String,
    writeKeyValue: (X,String,String) -> X,
    getKeyValue: (X,String) -> String,
    parse: (String) -> A?,
    serialize: (A) -> String
): MetadataField<X, A> {
    return object: MetadataField<X, A> {
        override fun set(ctx: X, value: A) {
            writeKeyValue(ctx, key, serialize(value))
        }
        override fun get(ctx: X): A =
            parse(getKeyValue(ctx,key))
                ?: throw IllegalArgumentException("Malformed field: $key")
    }
}

/** Helper function for constructing metadata fields making use of the common case of
 * a metadata field that is backed by a map from string keys to strings. */
fun <X,A> fromCodec(
    key: String,
    getDefaultValue: X.() -> A,
    writeKeyValue: (X,String,String?) -> X,
    setKeyToNull : (X, String) -> X,
    getKeyValue: (X,String) -> String?,
    parse: X.(String) -> A?, serialize: X.(A) -> String
): KeyedMetadataFieldWithDefault<X, A?> {
    return object: MetadataFieldWithDefault<X, A?>, KeyedMetadataFieldWithDefault<X, A?> {
        override fun set(ctx: X, value: A?) {
            if (value != null) {
                writeKeyValue(ctx, key, ctx.serialize(value))
            } else {
                setKeyToNull(ctx, key)
            }
        }
        override fun get(ctx: X): A? =
            getKeyValue(ctx,key)?.let { ctx.parse(it) }

        override fun X.getDefaultValue() = getDefaultValue()
        override val key: String
            get() = key
    }
}

/** Helper function for constructing metadata fields making use of the common case of
 * a metadata field that is backed by a map from string keys to strings. */
fun <X,A> requiredDefaultFieldFromCodec(
    key: String,
    getDefaultValue: X.() -> A,
    writeKeyValue: (X,String,String) -> X,
    getKeyValue: (X,String) -> String?,
    parse: X.(String) -> A?,
    serialize: X.(A) -> String
): KeyedMetadataFieldWithDefault<X, A> {
    return object: MetadataFieldWithDefault<X, A>, KeyedMetadataFieldWithDefault<X, A> {
        override fun set(ctx: X, value: A) {
            writeKeyValue(ctx, key, ctx.serialize(value!!))
        }
        override fun get(ctx: X): A =
            getKeyValue(ctx,key)?.let { ctx.parse(it) } ?: ctx.getDefaultValue()

        override fun X.getDefaultValue() = getDefaultValue()
        override val key: String
            get() = key
    }
}