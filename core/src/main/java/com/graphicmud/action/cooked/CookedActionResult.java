/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.util.ArrayList;
import java.util.List;

import com.graphicmud.action.raw.Echo;
import com.graphicmud.action.raw.RawAction;

import lombok.Getter;

/**
 * 
 */
@Getter
public class CookedActionResult extends ArrayList<RawAction> {
	
	private boolean successful;
	private String actionID;
	private String parameter;

	//-------------------------------------------------------------------
	public CookedActionResult(RawAction error) {
		this.successful = false;
		this.add(error);
	}

	//-------------------------------------------------------------------
	public CookedActionResult(String actionID, List<RawAction> actions, String parameter) {
		this.successful = true;
		this.actionID   = actionID;
		this.addAll(actions);
		this.parameter  = parameter;
	}

	//-------------------------------------------------------------------
	public CookedActionResult(String error) {
		this.successful = false;
		this.add((new Echo(error))::sendSelf);
	}

	//-------------------------------------------------------------------
	public CookedActionResult(String error, Object data) {
		this.successful = false;
		this.add((new Echo(error, data))::sendSelf);
	}

	//-------------------------------------------------------------------
	public CookedActionResult(String error, Object... data) {
		this.successful = false;
		this.add((new Echo(error, data))::sendSelf);
	}

	//-------------------------------------------------------------------
	public CookedActionResult() {
		this.successful = true;
	}

	//-------------------------------------------------------------------
	public CookedActionResult(boolean success, String actionID, String param) {
		this.successful = success;
		this.actionID   = actionID;
		this.parameter  = param;
	}

}
