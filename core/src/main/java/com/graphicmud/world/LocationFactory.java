/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import java.util.function.BiFunction;

/**
 *
 */
public interface LocationFactory<L extends Location> extends BiFunction<World, Zone, L> {

}
