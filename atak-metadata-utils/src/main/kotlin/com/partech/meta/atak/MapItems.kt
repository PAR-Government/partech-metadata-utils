package com.partech.meta.atak

import com.partech.meta.*
import com.atakmap.android.maps.MapItem
import java.lang.IllegalArgumentException
import kotlin.reflect.KProperty

/** Helper function to make a metadata field for a map item by supplying a key, a parser, and a serializer. */
fun <M: MapItem, A> makeMapItemMetaField(
    key: String,
    parse: (String) -> A?,
    serialize: (A) -> String
): MetadataField<M, A?> = fromCodec(
    key,
    { mi: M, k: String, v: String? ->
        mi.also { it.setMetaString(k,v) }
    },
    { mi: M, k: String ->
        mi.getMetaString(k, null)
    },
    { parse(it) },
    { serialize(it) }
)

/** Helper function to make a required metadata field for a map item by supplying a key, a parser, and a serializer. */
fun <M: MapItem, A: Any> makeRequiredMapItemMetaField(
    key: String,
    parse: (String) -> A?,
    serialize: (A) -> String
): MetadataField<M, A> = requiredFieldFromCodec(
    key,
    { mi: MapItem, k: String, v: String ->
    mi.also { it.setMetaString(k,v) } },
    { mi: MapItem, k: String ->
        mi.getMetaString(k, null) },
    parse,
    serialize
)

/** Make a MapItem metadata field for the given enum. */
inline fun <reified E : Enum<E>, M : MapItem> makeEnumMetaField(key: String): MetadataField<M, E?> {
    return object: MetadataField<M, E?> {
        override fun set(ctx: M, value: E?) {
            if (value != null) {
                ctx.setMetaInteger(key, value.ordinal)
            } else {
                ctx.removeMetaData(key)
            }
        }

        override fun get(ctx: M): E? {
            return enumValues<E>().getOrNull(ctx.getMetaInteger(key, -1))
        }
    }
}

/** Make an Integer MapItem metadata field. */
fun <M : MapItem> makeIntMetaField(key: String): MetadataField<M, Int?> {
    return object: MetadataField<M, Int?> {
        override fun set(ctx: M, value: Int?) {
            if(value != null) {
                ctx.setMetaInteger(key, value)
            } else {
                ctx.removeMetaData(key)
            }
        }

        override fun get(ctx: M): Int? {
            return if(ctx.hasMetaValue(key)) {
                ctx.getMetaInteger(key, -1)
            } else {
                null
            }
        }
    }
}

/** Make a Float MapItem metadata field. */
fun <M : MapItem> makeFloatMetaField(key: String): MetadataField<M, Float?> {
    return object: MetadataField<M, Float?> {
        override fun set(ctx: M, value: Float?) {
            if(value != null) {
                ctx.setMetaDouble(key, value.toDouble())
            } else {
                ctx.removeMetaData(key)
            }
        }

        override fun get(ctx: M): Float? {
            return if(ctx.hasMetaValue(key)) {
                ctx.getMetaDouble(key, -1.0).toFloat()
            } else {
                null
            }
        }
    }
}

/** Make a Long MapItem metadata field. */
fun <M : MapItem> makeLongMetaField(key: String): MetadataField<M, Long?> {
    return object: MetadataField<M, Long?> {
        override fun set(ctx: M, value: Long?) {
            if(value != null) {
                ctx.setMetaLong(key, value)
            } else {
                ctx.removeMetaData(key)
            }
        }

        override fun get(ctx: M): Long? {
            return if(ctx.hasMetaValue(key)) {
                ctx.getMetaLong(key, -1)
            } else {
                null
            }
        }
    }
}

/** Make a Double MapItem metadata field. */
fun <M : MapItem> makeDoubleMetaField(key: String): MetadataField<M, Double?> {
    return object: MetadataField<M, Double?> {
        override fun set(ctx: M, value: Double?) {
            if(value != null) {
                ctx.setMetaDouble(key, value)
            } else {
                ctx.removeMetaData(key)
            }
        }

        override fun get(ctx: M): Double? {
            return if(ctx.hasMetaValue(key)) {
                ctx.getMetaDouble(key, -1.0)
            } else {
                null
            }
        }
    }
}

/** Make a String MapItem metadata field. */
fun <M : MapItem> makeStringMetaField(key: String): MetadataField<M, String?> {
    return object: MetadataField<M, String?> {
        override fun set(ctx: M, value: String?) {
            if(value != null) {
                ctx.setMetaString(key, value)
            } else {
                ctx.removeMetaData(key)
            }
        }

        override fun get(ctx: M): String? {
            return if(ctx.hasMetaValue(key)) {
                ctx.getMetaString(key, "")
            } else {
                null
            }
        }
    }
}

/** Make a Boolean MapItem metadata field. */
fun <M : MapItem> makeBooleanMetaField(key: String): MetadataField<M, Boolean?> {
    return object: MetadataField<M, Boolean?> {
        override fun set(ctx: M, value: Boolean?) {
            if(value != null) {
                ctx.setMetaBoolean(key, value)
            } else {
                ctx.removeMetaData(key)
            }
        }

        override fun get(ctx: M): Boolean? {
            return if(ctx.hasMetaValue(key)) {
                ctx.getMetaBoolean(key, false)
            } else {
                null
            }
        }
    }
}

/** Takes an optional metadata field, and makes it required by throwing an IllegalArgumentException
 * when trying to access an object without the field set. */
fun <A : Any, M : MapItem> MetadataField<M, A?>.makeRequired(): MetadataField<M, A> {
    val origField = this
    return object: MetadataField<M, A> {
        override fun set(ctx: M, value: A) {
            origField.set(ctx, value)
        }

        override fun get(ctx: M): A {
            return origField.get(ctx)
                ?: throw IllegalArgumentException("Required field is null.")
        }
    }
}

/** Takes an optional metadata field, and makes it required by supplying a default argument
 * that is used whenever the metadata field has not yet been set. */
fun <A : Any, M : MapItem> MetadataField<M, A?>.makeRequiredByDefault(default: A): MetadataField<M, A> {
    val origField = this
    return object: MetadataField<M, A> {
        override fun set(ctx: M, value: A) {
            origField.set(ctx, value)
        }

        override fun get(ctx: M): A {
            return origField.get(ctx)
                ?: default
        }
    }
}

/** Helper function to use a map item metadata field as a delegate. */
fun <M: MapItem, A> MetadataField<M,A>.delegate(): IDelegate<M, A> {
    val field = this
    return object: IDelegate<M, A> {
        override operator fun getValue(ctx: M, property: KProperty<*>): A {
            return ctx.getMetaField(field)
        }

        override operator fun setValue(ctx: M, property: KProperty<*>, value: A) {
            ctx.setMetaField(field, value)
        }
    }
}
