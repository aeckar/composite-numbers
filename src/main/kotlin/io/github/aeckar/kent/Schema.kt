package io.github.aeckar.kent

import io.github.aeckar.hass.Schema
import io.github.aeckar.hass.schema

// TODO move to jvmMain

/**
 * *Hass* serialization schema for [composite numbers][CompositeNumber] and [tables][Table].
 */
public val schema: Schema = schema {
    define<Int128> {
        read { Int128(readInt(), readInt(), readInt(), readInt()) }
        static write {
            writeInt(it.q1)
            writeInt(it.q2)
            writeInt(it.q3)
            writeInt(it.q4)
        }
    }

    define<Rational> {
        read { Rational(readLong(), readLong(), readInt(), readInt()) }
        static write {
            writeLong(it.numer)
            writeLong(it.denom)
            writeInt(it.scale)
            writeInt(it.sign)
        }
    }

    define<Table<Any>> {    // Works for matrices too
        read { Table(read()) }
        static write { write(it.backingArray) }
    }
}