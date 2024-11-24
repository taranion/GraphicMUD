/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.io.text;

public class TextUtil {


    public static String capitalize(String str) {
        if (str == null || str.isBlank()) {
            return str; 
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String firstToLower(String str) {
        if (str == null || str.isBlank()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String addFullStop(String str) {
        if (str == null || str.isBlank() || str.endsWith(".")) {
            return str;
        }
        return str + ".";
    }
}
