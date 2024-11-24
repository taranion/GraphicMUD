/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@Getter
@Setter
public class PlayerAccount {

	private String name;
	private String secret;
	private Map<String,String> networkIdentifier = new HashMap<>();
	private LocalDateTime creationDate;
	private LocalDateTime lastLogin;
	private List<String> characters = new ArrayList<String>();

}
