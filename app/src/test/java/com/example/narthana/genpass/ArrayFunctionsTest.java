package com.example.narthana.genpass;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by narthana on 31/10/16.
 */

public class ArrayFunctionsTest
{
    @Test
    public void compressArray()
    {
        int[] array = new int[] {0, 1, 2, 3, 4, 6, 8, 9, 10};
        int[] compressed = Utility.compressWithRanges(array);
        int[] expected = new int[] {9, 0, 4, -6, 8, 10};
        assertTrue(Arrays.toString(compressed), Arrays.equals(compressed, expected));
    }

    @Test
    public void compressThenExpandArray()
    {
        int[] array = new int[] {1, 3, 4, 6, 8, 9, 10, 14};
        int[] compressed = Utility.compressWithRanges(array);
        int[] expanded = Utility.expandFromRanges(compressed);
        assertTrue(Arrays.toString(expanded), Arrays.equals(array, expanded));
    }
}
