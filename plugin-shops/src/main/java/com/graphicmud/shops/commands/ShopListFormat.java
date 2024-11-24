/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

import com.graphicmud.game.MUDEntity;

public interface ShopListFormat {
    
    String getFormattedOutput(MUDEntity player, MUDEntity shopkeeperMobile);
}
