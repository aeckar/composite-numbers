package io.github.aeckar.kent

// TODO might delete this later

//import io.github.aeckar.kent.utils.high
//import io.github.aeckar.kent.utils.low
//
//// TODO implement kotlinx.serialization support for all classes
//// TODO implement
//
//class MutableFloat128 : Float128 {
//
//}
//
///**
// * TODO
// *
// */
//open class Float128 : CompositeNumber<Float128> {
//    override var sign: Int
//        protected set
//    var exponent: Int
//        protected set
//    var mantissaHigh: Long
//        protected set
//    var mantissaLow: Long
//        protected set
//
//    override val isNegative get() = sign == -1
//    override val isPositive get() = sign == 1
//
//    override fun immutable(): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun mutable(): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun uniqueMutable(): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun signum(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun unaryMinus(): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun pow(power: Int): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun hashCode(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun isLong(): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun toRational(): Rational {
//        TODO("Not yet implemented")
//    }
//
//    override fun toInt128(): Int128 {
//        TODO("Not yet implemented")
//    }
//
//    /**
//     * TODO
//     *
//     * For a two's complement representation of this value,
//     * call [bitsToInt128]`().twosComplement()`.
//     */
//    override fun toString(): String {
//        TODO("Not yet implemented")
//    }
//
//    override fun toDouble(): Double {
//        TODO("Not yet implemented")
//    }
//
//    override fun toInt(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun toLong(): Long {
//        TODO("Not yet implemented")
//    }
//
//    override fun stateEquals(other: Float128): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun rem(other: Float128): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun div(other: Float128): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun times(other: Float128): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun plus(other: Float128): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun valueOf(other: Float128): Float128 {
//        TODO("Not yet implemented")
//    }
//
//    override fun compareTo(other: Float128): Int {
//        TODO("Not yet implemented")
//    }
//
//    constructor() {
//
//    }
//
//    fun isNaN() {
//    // todo
//    }
//
//    fun isInfinite() {
//        // todo
//    }
//
//    fun isFinite() {
//        // todo
//    }
//
//    fun bitsToInt128(): Int128 {
//        val sign = if (sign == -1) Int.MIN_VALUE else 0
//        val signAndExponent = sign or (exponent shl (Int.SIZE_BITS - EXPONENT_SIZE_BITS - 1))
//        return Int128(signAndExponent or mantissaHigh.high, mantissaHigh.low, mantissaLow.high, mantissaLow.low)
//    }
//
//    // TODO create separate objects for exponent and mantissa limits if needed
//
//    companion object {
//        const val EXPONENT_SIZE_BITS = 15
//        const val MANTISSA_SIZE_BITS = 112
//
//        private const val NAN_HASH = -441827835 // Value has no significance
//        private const val POSITIVE_INFINITY_HASH = Int.MAX_VALUE
//        private const val NEGATIVE_INFINITY_HASH = Int.MIN_VALUE
//    }
//}