/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.usb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.function.Function;

@RunWith(RobolectricTestRunner.class)
public class AsteriskIntPrefixTreeTest {

    private static final int ASTERISK = -1;
    private static final int MAX_TEST_LEN = 4;
    private AsteriskIntPrefixTree tree;

    @Before
    public void setUp() {
        tree = new AsteriskIntPrefixTree(ASTERISK);
    }

    @Test
    public void testEmptyTree() {
        assertNoneMatch(MAX_TEST_LEN);
        assertAsteriskOnlyEntriesWontMatch(MAX_TEST_LEN);
    }

    @Test
    public void testInsertAndFindSingleDigitValue() {
        int[] value = new int[] {0};
        tree.insert(value);
        assertNoneExceptOneMatch(value, MAX_TEST_LEN);
        assertAsteriskOnlyEntriesWontMatch(MAX_TEST_LEN);
    }

    @Test
    public void testInsertAndFindTripleDigitValue() {
        int[] value = new int[] {0, 1, 2};
        tree.insert(value);
        assertNoneExceptOneMatch(value, MAX_TEST_LEN);
        assertAsteriskOnlyEntriesWontMatch(MAX_TEST_LEN);
    }

    @Test
    public void testInsertAndFindSingleDigitAsteriskValue() {
        tree.insert(new int[] {ASTERISK});

        Function<int[], Boolean> shouldFind = (int[] ints) -> ints.length == 1; // [ * ]

        assertAllCombinations(shouldFind, MAX_TEST_LEN);
        assertTrue(tree.find(new int[] {ASTERISK}));
        assertAsteriskOnlyEntriesWontMatch(2, MAX_TEST_LEN);
    }

    @Test
    public void testInsertAndFindTripleDigitAsteriskValues() {
        tree.insert(new int[] {1, 2, ASTERISK});

        Function<int[], Boolean> shouldFind =
                (int[] ints) -> ints.length == 3 && ints[0] == 1 && ints[1] == 2; // [ 1, 2, * ]

        assertAllCombinations(shouldFind, MAX_TEST_LEN);
        assertTrue(tree.find(new int[] {1, 2, ASTERISK}));
        assertAsteriskOnlyEntriesWontMatch(MAX_TEST_LEN);
    }

    @Test
    public void testInsertAndFindMultipleVariableLengthValues() {
        tree.insert(new int[] {1, 2, ASTERISK});
        tree.insert(new int[] {ASTERISK, 3, 4});
        tree.insert(new int[] {5, 6, 7, 8});

        Function<int[], Boolean> shouldFind =
                (int[] ar) ->
                        (ar.length == 3 && ar[0] == 1 && ar[1] == 2) // [ 1, 2, * ]
                                || (ar.length == 3 && ar[1] == 3 && ar[2] == 4) // [ *, 3, 4 ]
                                || (Arrays.equals(ar, new int[] {5, 6, 7, 8}));

        assertAllCombinations(shouldFind, MAX_TEST_LEN);
        assertAsteriskOnlyEntriesWontMatch(MAX_TEST_LEN);
    }

    private void assertAsteriskOnlyEntriesWontMatch(int maxLength) {
        assertAsteriskOnlyEntriesWontMatch(0, maxLength);
    }

    private void assertNoneExceptOneMatch(int[] shouldFind, int maxLength) {
        assertAllCombinations((ints -> Arrays.equals(ints, shouldFind)), maxLength);
    }

    private void assertNoneMatch(int maxLength) {
        assertAllCombinations(null, maxLength);
    }

    /**
     * Exhaustively tries all combinations of length in range 0..maxLength against shouldFind value
     *
     * @param shouldFind whether given combination should be found in the tree
     * @param maxLength maximal test sequence length
     */
    private void assertAllCombinations(Function<int[], Boolean> shouldFind, int maxLength) {
        for (int length = 1; length <= maxLength; ++length) {
            int[] arr = new int[length];
            assertAllCombinations(shouldFind, arr, 0);
        }
    }

    /**
     * Exhaustively tries all the possible combinations of given length and tests against shouldFind
     * value
     *
     * @param shouldFind whether given combination should be found in the tree
     * @param values array for storing the current test value
     * @param idx index of the element to modify
     */
    private void assertAllCombinations(Function<int[], Boolean> shouldFind, int[] values, int idx) {
        for (int i = 0; i < 10; ++i) {
            if (idx < values.length) {
                values[idx] = i;
                assertAllCombinations(shouldFind, values, idx + 1);
            } else if (shouldFind != null && shouldFind.apply(values)) {
                assertTrue("Should find " + Arrays.toString(values), tree.find(values));
            } else {
                assertFalse("Should NOT find " + Arrays.toString(values), tree.find(values));
            }
        }
    }

    /**
     * Tests that asterisk only sequences in given length range are not found
     *
     * @param minLength minimal test sequence length
     * @param maxLength maximal test sequence length
     */
    private void assertAsteriskOnlyEntriesWontMatch(int minLength, int maxLength) {
        for (int length = minLength; length <= maxLength; length++) {
            int[] arr = new int[length];
            Arrays.fill(arr, ASTERISK);
            assertFalse("Should NOT find " + Arrays.toString(arr), tree.find(arr));
        }
    }
}
