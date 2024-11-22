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

import android.util.SparseArray;

/**
 * A prefix tree that supports "match all" asterisk values.<br>
 * Since at every tree depth there may be up to two nodes that match the search criteria:
 *
 * <ul>
 *   <li>asterisk
 *   <li>matching value
 * </ul>
 *
 * the worst case search scenario is equivalent to binary tree traversal O(2^d), where d is the
 * depth of the tree.<br>
 * This is particularly efficient for short sequences with a big character set.
 */
public class AsteriskIntPrefixTree {
    private final Node root;
    private final int asteriskValue;

    /**
     * Instantiate the data structure.
     *
     * @param asteriskValue the value to be treated as "match all" asterisk
     */
    public AsteriskIntPrefixTree(int asteriskValue) {
        this.asteriskValue = asteriskValue;
        root = new Node();
    }

    /**
     * Inserts a sequence of integers into the prefix tree.
     *
     * @param values the sequence of integers to be inserted
     */
    public void insert(int[] values) {
        Node current = root;
        for (int key : values) {
            SparseArray<Node> children = current.children;
            if (children.indexOfKey(key) < 0) {
                children.put(key, new Node());
            }
            current = children.get(key);
        }
        current.isFinalNode = true;
    }

    /**
     * Checks if a given sequence of integers is present in the prefix tree.
     *
     * @param values the sequence of integers to be checked
     * @return true if the sequence is present, false otherwise
     */
    public boolean find(int[] values) {
        return find(values, 0, root);
    }

    private boolean find(int[] values, int position, Node root) {
        if (root == null) {
            return false;
        }
        if (position == values.length) {
            return root.isFinalNode;
        }
        int curValue = values[position];
        return find(values, position + 1, root.children.get(curValue))
                || find(values, position + 1, root.children.get(asteriskValue));
    }

    /** Prints the prefix tree in human-readable format. */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        printNode(root, sb, 0);
        return sb.toString();
    }

    private void printNode(Node node, StringBuilder sb, int depth) {
        for (int i = 0; i < node.children.size(); i++) {
            int key = node.children.keyAt(i);
            Node subNode = node.children.get(key);
            for (int j = 0; j < depth; j++) {
                sb.append("  ");
            }
            if (key == asteriskValue) {
                sb.append("*");
            } else {
                sb.append(String.format("0x%x", key));
            }
            sb.append(subNode.isFinalNode ? "| " : ": ");
            if (subNode.children.size() > 0) {
                sb.append("\n");
                printNode(subNode, sb, depth + 1);
            } else {
                sb.append("\n");
            }
        }
    }

    static class Node {
        final SparseArray<Node> children = new SparseArray<>();
        boolean isFinalNode;
    }
}
