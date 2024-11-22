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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UsbFilterEntryParserInvalidValuesTest {

    private static final int ASTERISK = -1;
    private UsbIdFilterEntryParser parser;

    @Before
    public void setUp() {
        parser = new UsbIdFilterEntryParser(ASTERISK);
    }

    @Test(expected = NumberFormatException.class)
    public void invalidNumericValue() {
        parser.parseFilterEntry("ef");
    }

    @Test(expected = NumberFormatException.class)
    public void invalidVidValue() {
        parser.parseFilterEntry("0xfffff");
    }

    @Test(expected = NumberFormatException.class)
    public void invalidPidValue() {
        parser.parseFilterEntry("0x1234:0xfffff");
    }

    @Test(expected = NumberFormatException.class)
    public void invalidClassValue() {
        parser.parseFilterEntry("0x1234:0x5678:0xfff");
    }

    @Test(expected = NumberFormatException.class)
    public void invalidSubClassValue() {
        parser.parseFilterEntry("0x1234:0x5678:0xab:0xfff");
    }

    @Test(expected = NumberFormatException.class)
    public void invalidProtocolValue() {
        parser.parseFilterEntry("0x1234:0x5678:0xab:0xcd:0xfff");
    }

    @Test(expected = IllegalArgumentException.class)
    public void outOfBoundsEntriesValue() {
        parser.parseFilterEntry("0x1234:0x5678:0xab:0xcd:0xef:0x0e");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidAllColons() {
        parser.parseFilterEntry(":::::");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidValueEmptyString() {
        parser.parseFilterEntry("");
    }
}
