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

import android.util.Log;

/**
 * Fast trie backed verifier of blocked USB device ids.</br> Instantiated with a list of USB device
 * identifier filters in following formats:
 *
 * <ul>
 *   <li>"vendorId:productId:class:subclass:protocol"
 *   <li>"vendorId:productId:class:subclass"
 *   <li>"vendorId:productId:class"
 *   <li>"vendorId:productId"
 *   <li>"vendorId"
 * </ul>
 *
 * Every identifier is accepted in decimal format, like "123" or hex format like "0x1fa". Also
 * instead of any identifier a "match all" expression "*" or an empty string can be used.</br>
 * Example usage:
 *
 * <pre>
 * String[] filters = new String[] {
 *     "0x1d6b", // - deny all USB devices with vendor ID = 0x1d6b
 *     "0x0f00:0x0b00", // - deny the product 0x0b00 by vendor 0x0f00
 *     "*:*:0x3", // or "::3" - deny all devices of class 3 (HID) regardless of vendor and product
 * };
 * UsbDeviceDenyList denyList = new UsbDeviceDenyList(filters);
 * UsbDevice device; // obtained from UsbManager
 * boolean deviceDenied = denyList.isDenyListed(device.getVendorId(),
 *                  device.getProductId(), device.getDeviceClass(), device.getDeviceSubclass());
 * UsbConfiguration configuration = device.getConfiguration(1);
 * UsbInterface iface = configuration.getInterface(1);
 * boolean interfaceDenied = denyList.isDenyListed(device.getVendorId(),
 *                  device.getProductId(), iface.getInterfaceClass(),
 *                  iface.getInterfaceSubclass(), iface.getProtocol());
 * </pre>
 */
class UsbDeviceDenyList {

    private static final String TAG = UsbDeviceDenyList.class.getSimpleName();

    // We use the value that is out of the range of any USB identifiers for asterisk
    private static final int ID_ASTERISK = 0xffffffff;

    private final AsteriskIntPrefixTree mDenyList = new AsteriskIntPrefixTree(ID_ASTERISK);

    /**
     * Instantiates the deny list with given filters
     *
     * @param deviceFilters filtering entries in format
     *     "vendorId:productId:class:subclass:protocol".
     */
    public UsbDeviceDenyList(String[] deviceFilters) {
        UsbIdFilterEntryParser parser = new UsbIdFilterEntryParser(ID_ASTERISK);
        for (String entry : deviceFilters) {
            try {
                mDenyList.insert(parser.parseFilterEntry(entry));
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.e(TAG, "Failed to parse the deny list filter entry " + entry, e);
            }
        }
    }

    public String toString() {
        return mDenyList.toString();
    }

    /**
     * Checks whether the USB device with given identifiers is deny listed
     *
     * @param vendorId USB vendor id
     * @param productId USB product id
     * @param clazz USB device/interface class
     * @param subclass USB device/interface subclass
     * @param proto USB device/interface protocol
     * @return whether the device is deny listed
     */
    public boolean isDenyListed(int vendorId, int productId, int clazz, int subclass, int proto) {
        return mDenyList.find(new int[] {vendorId, productId, clazz, subclass, proto});
    }
}
