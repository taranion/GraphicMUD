/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.net.URI;
import java.nio.file.Path;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 *
 */
@Getter
@SuperBuilder
public class MenuItem<T> {
	@NonNull
	private String identifier;
	private T userData;
	/** Label to display to user */
	@NonNull
	private String label;
	private String emoji;
	private Predicate<Object> checkIfSelectable;
	private URI externalLink;
	private Path thumbnailImage;
	
	private boolean isExit;
	
	public String toString() {
		return identifier+"/"+label;
	}

}
