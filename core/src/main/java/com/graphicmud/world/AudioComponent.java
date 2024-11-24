/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import org.prelle.simplepersist.Attribute;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
public class AudioComponent {
	
	@Attribute
	private String file;
	@Attribute
	private boolean music;
	@Attribute
	private boolean loop;
	@Attribute
	private int volume; 

}
