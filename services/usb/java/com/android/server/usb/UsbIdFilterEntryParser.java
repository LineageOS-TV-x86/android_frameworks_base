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
 * See the License for the specific language governing permissions an
 * limitations under the License.
 */

package com.android.server.usb;

class UsbIdFilterEntryParser {

    private static final int MASK_TWO_BYTES = 0x0000ffff;
    private static final int MASK_ONE_BYTE = 0x000000ff;

    private final int asteriskId;

    UsbIdFilterEntryParser(int asteriskId) {
        this.asteriskId = asteriskId;
    }

    int[] parseFilterEntry(String entry) {
        if (entry == null || entry.isEmpty()) {
            throw new IllegalArgumentException("Invalid USB filter descriptor: it's blank");
        }
        String[] split = entry.split(":");
        if (split.length > 5 || split.length < 1) {
            throw new IllegalArgumentException(
                    "Invalid USB filter descriptor: "
                            + entry
                            + ", it has "
                            + split.length
                            + " entries");
        }
        int vid = parseUsbIdEntry(split[0], MASK_TWO_BYTES);
        int pid = split.length > 1 ? parseUsbIdEntry(split[1], MASK_TWO_BYTES) : asteriskId;
        int cls = split.length > 2 ? parseUsbIdEntry(split[2], MASK_ONE_BYTE) : asteriskId;
        int sub = split.length > 3 ? parseUsbIdEntry(split[3], MASK_ONE_BYTE) : asteriskId;
        int proto = split.length > 4 ? parseUsbIdEntry(split[4], MASK_ONE_BYTE) : asteriskId;
        return new int[] {vid, pid, cls, sub, proto};
    }

    private int parseUsbIdEntry(String s, int mask) {
        int value;
        if (s.equals("*") || s.isEmpty()) {
            return asteriskId;
        }
        if (s.startsWith("0x")) {
            // skip the "0x" and parse the rest
            value = Integer.parseInt(s.substring(2).trim(), 16);
        } else {
            value = Integer.parseInt(s.trim(), 10);
        }
        if ((value & ~mask) != 0) {
            throw new NumberFormatException(
                    String.format("Value 0x%X does not match the mask 0x%x", value, mask));
        }
        return value;
    }
}
