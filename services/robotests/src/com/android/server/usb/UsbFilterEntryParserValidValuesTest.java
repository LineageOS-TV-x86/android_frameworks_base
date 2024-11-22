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

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import java.util.Arrays;
import java.util.Collection;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class UsbFilterEntryParserValidValuesTest {

    private static final int ASTERISK = -1;
    private UsbIdFilterEntryParser parser;

    private final String originalValue;
    private final int[] parsedValue;

    public UsbFilterEntryParserValidValuesTest(String originalValue, int[] parsedValue) {
        this.originalValue = originalValue;
        this.parsedValue = parsedValue;
    }

    @Before
    public void setUp() {
        parser = new UsbIdFilterEntryParser(ASTERISK);
    }

    @Test
    public void parse() {
        assertArrayEquals(parsedValue, parser.parseFilterEntry(originalValue));
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "USB filter entry: {0} + {1}")
    public static Collection getTestData() {
        Object[][] data = {
            {"0x1234", new int[] {0x1234, ASTERISK, ASTERISK, ASTERISK, ASTERISK}},
            {"1234", new int[] {1234, ASTERISK, ASTERISK, ASTERISK, ASTERISK}},
            {"0x1234:*:*:*:*", new int[] {0x1234, ASTERISK, ASTERISK, ASTERISK, ASTERISK}},
            {"0x1234::::", new int[] {0x1234, ASTERISK, ASTERISK, ASTERISK, ASTERISK}},
            {"1234:*:*:*:*", new int[] {1234, ASTERISK, ASTERISK, ASTERISK, ASTERISK}},
            {"0x1234:0x5678", new int[] {0x1234, 0x5678, ASTERISK, ASTERISK, ASTERISK}},
            {"0x1234:567", new int[] {0x1234, 567, ASTERISK, ASTERISK, ASTERISK}},
            {"0x1234:0x5678:*:*:*", new int[] {0x1234, 0x5678, ASTERISK, ASTERISK, ASTERISK}},
            {"0x1234:0x5678:::", new int[] {0x1234, 0x5678, ASTERISK, ASTERISK, ASTERISK}},
            {"0x1234:0x5678:0xab", new int[] {0x1234, 0x5678, 0xab, ASTERISK, ASTERISK}},
            {"0x1234:0x5678:0xab:*:*", new int[] {0x1234, 0x5678, 0xab, ASTERISK, ASTERISK}},
            {"0x1234:0x5678:0xab::", new int[] {0x1234, 0x5678, 0xab, ASTERISK, ASTERISK}},
            {"0x1234:0x5678:0xab:0xcd", new int[] {0x1234, 0x5678, 0xab, 0xcd, ASTERISK}},
            {"0x1234:0x5678:0xab:0xcd:*", new int[] {0x1234, 0x5678, 0xab, 0xcd, ASTERISK}},
            {"0x1234:0x5678:0xab:0xcd:", new int[] {0x1234, 0x5678, 0xab, 0xcd, ASTERISK}},
            {"0x1234:0x5678:0xab:0xcd:0xef", new int[] {0x1234, 0x5678, 0xab, 0xcd, 0xef}},
            {"*:0x5678:0xab:0xcd:0xef", new int[] {ASTERISK, 0x5678, 0xab, 0xcd, 0xef}},
            {":0x5678:0xab:0xcd:0xef", new int[] {ASTERISK, 0x5678, 0xab, 0xcd, 0xef}},
            {"*:*:0xab:0xcd:0xef", new int[] {ASTERISK, ASTERISK, 0xab, 0xcd, 0xef}},
            {"::0xab:0xcd:0xef", new int[] {ASTERISK, ASTERISK, 0xab, 0xcd, 0xef}},
            {"*:*:*:0xcd:0xef", new int[] {ASTERISK, ASTERISK, ASTERISK, 0xcd, 0xef}},
            {":::0xcd:0xef", new int[] {ASTERISK, ASTERISK, ASTERISK, 0xcd, 0xef}},
            {"*:*:*:*:0xef", new int[] {ASTERISK, ASTERISK, ASTERISK, ASTERISK, 0xef}},
            {"::::0xef", new int[] {ASTERISK, ASTERISK, ASTERISK, ASTERISK, 0xef}},
            {"*:*:*:*:*", new int[] {ASTERISK, ASTERISK, ASTERISK, ASTERISK, ASTERISK}},
            {"::::*", new int[] {ASTERISK, ASTERISK, ASTERISK, ASTERISK, ASTERISK}},
            {"0x1234::::0xef", new int[] {0x1234, ASTERISK, ASTERISK, ASTERISK, 0xef}},
            {"0x1234:*:*:*:0xef", new int[] {0x1234, ASTERISK, ASTERISK, ASTERISK, 0xef}},
        };
        return Arrays.asList(data);
    }
}
