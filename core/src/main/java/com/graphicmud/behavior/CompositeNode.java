/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.graphicmud.game.MUDEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public abstract class CompositeNode implements MUDAction {
	
	protected final static Logger logger = System.getLogger(CompositeNode.class.getPackageName());

	@Getter
	@Setter
	protected String id;
	protected List<BiFunction<MUDEntity, Context, TreeResult>> children = new ArrayList<BiFunction<MUDEntity,Context,TreeResult>>();
	
	//-------------------------------------------------------------------
	public <E extends CompositeNode> E add(BiFunction<MUDEntity, Context, TreeResult> action) {
		this.children.add(action);
		return (E)this;
	}

}
