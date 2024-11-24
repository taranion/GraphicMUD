/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game.impl;

import java.util.ArrayList;

import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.Root;

import com.graphicmud.game.MUDEntityTemplate;

/**
 * 
 */
@SuppressWarnings("serial")
@Root(name = "entities")
@ElementList(entry="entity",type=MUDEntityTemplate.class)
public class MUDEntityTemplateList extends ArrayList<MUDEntityTemplate> {

}
