/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.util.Objects;

/**
 *
 */
@FunctionalInterface
public interface TriConsumer<A,B,C> {

	public abstract void accept(A arg0, B arg1, C arg2);

	default TriConsumer<A, B, C> andThen(TriConsumer<? super A, ? super B, ? super C> after) {
			Objects.requireNonNull(after);
			return (A a, B b, C c) -> {
				accept(a,b,c);
				after.accept(a, b, c);
			};
	}
}
