Partech Metadata Utils
----------------------

This repository is home to both the `partech-metadata-utils` and 
`partech-metadata-utils-atak` libraries. As the names suggest, 
the former is a general purpose library targeting the JVM (or, since it is built with Kotlin,
any of Kotlin's other compilation targets), whereas the latter is a library targeting ATAK
(and depends on the ATAK SDK).

Basic Concepts and Motivation
-----------------------------

The core interface in `partech-metadata-utils` is that of the `MetadataField`. It is
defined as follows:

```kotlin
/** Declares a metadata field for the object X, allowing the user to
 * get and set values of type A to this particular field. */
interface MetadataField<in X, A> {
    fun set(ctx: X, value: A)
    fun get(ctx: X): A 
}
```

Having an explicit interface for metadata fields such as this has the advantage of 
allowing such fields to be explicitly documented in an unambiguous way -- the types
 `X` and `A` become the documentation. For instance, compare this with the ad-hoc usage
 keys as often seen with ATAK `MapItem`s or Android `Preference`s. If you have a key `"myKey"`
 that is intended to be used for `Int` fields in ATAK, call-sites might look, in the worst case, like:
 `mi.getInt("myKey", 5)`, or in the best case:

```kotlin
/** An key for MapItems, for an Int property. Has a default value of five. */
const val MY_KEY = "myKey"

/** ... (at call-sites) **/
mi.getInt(MY_KEY, 5)
```

However, even with this best-case scenario where everything is documented properly in a docstring,
there is nothing preventing us from using `MY_KEY` wrong:

```kotlin
/** Somewhere else in the code-base. Compiler doesn't complain: */
mi.getFloat(MY_KEY, 7)
```

This is a fragile API! As alluded to above, `partech-metadata-utils` also solves the problem
 of "default values" (where relevant) not being documented, by providing some additional
 interfaces:

```kotlin
interface Default<in X, out A> {
    fun X.getDefaultValue(): A
}

/** Declares a metadata field with a default value for the object X, allowing the user to
 * get and set values of type A to this particular field. */
interface MetadataFieldWithDefault<in X,A>: MetadataField<X,A>, Default<X, A>
```

The library then provides alternative APIs given a `MetadataField`, which cannot possibly
 be called incorrectly. An example usage (using some of the utilities for constructing metadata we will see in the next section)
 might look like:

```kotlin
object MyMetadataField: MetadataFieldWithDefault<MapItem, Int>
by makeIntMetadataField(
     key = "myKey",
     defaultValue = 5
)

val i: Int = MyMetadataField.getOrDefault(mi)
```

The old fragile APIs can then, if desired, be prohibited/discouraged by making use of a
 static analyzer such as [detekt](https://detekt.github.io/detekt/).

Building MetadataFields
-----------------------

As seen above, `partech-metadata-utils` offers utilities for building `MetadataField`s. 
Some of the basic ones provided by `partech-metadata-utils` itself are 
`requiredDefaultFieldFromCodec`, `requiredFieldFromCodec` and `fromCodec`, which all
construct various types of `MetadataField`s by supplying a `key` together with a codec
(that is, a joint serialzier/deserializer pair for the type in question).

However, most users (if they will not be building their own custom metadata utilities)
 will likely instead use the more specific utilities provided by `partech-metadata-utils-atak`,
 which provides utilities for constructing metadata fields on sub-classes of `MapItem`.

Usage as Delegates
------------------

For an even more convenient API for making use of `MetadataField`s, Kotlin extension 
 properties and delegation can be utilized. For example:

```kotlin
/** Defining the field */
val MapItem.myField: Int 
by makeIntMetaField<MapItem>("myKey")
    .makeRequiredByDefault(5)
    .delegate()

/** At use-sites */
val i: Int = mi.myField
mi.myField = 42
```

Developer Contact
-----------------

Nathan Bedell  
Software Engineer
PAR Government / ISR Engineering Services  
nathan_bedell@partech.com

