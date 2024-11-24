/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

public class ShopException extends Throwable {
    public ShopException(String message) {
        super(message);
    }
}
