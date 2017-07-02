package com.example.android.genpass

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by narthana on 31/10/16.
 */

class ArrayFunctionsTest {
    @Test
    fun compressArray() {
        val array = intArrayOf(0, 1, 2, 3, 4, 6, 8, 9, 10)
        val compressed = compressWithRanges(array)
        val expected = intArrayOf(0, 4, -6, 8, 10)
        assertTrue(
                compressed.joinToString(prefix = "[", postfix = "]"),
                compressed contentEquals expected
        )
    }

    @Test
    fun compressThenExpandArray() {
        val array = intArrayOf(1, 3, 4, 6, 8, 9, 10, 14)
        val compressed = compressWithRanges(array)
        val expanded = expandFromRanges(compressed)
        assertTrue(
                expanded.joinToString(prefix = "[", postfix = "]"),
                expanded.toSet() == array.toSet()
        )
    }
}
