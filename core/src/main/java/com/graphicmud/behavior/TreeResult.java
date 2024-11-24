/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.util.List;
import java.util.Optional;

import com.graphicmud.behavior.TestBasedAction.ITestResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 
 */
@Getter
@Builder
@AllArgsConstructor
public class TreeResult {
	
	public static enum Result {
		SUCCESS,
		FAILURE,
		RUNNING,
	}
	
	private Result value;
	private List<Object> path;
	/** Message intended for the MUD itself */
	private String internalErrorMessage;
	/** Message intended for the entity executing the command */
	private String errorMessage;
	/** 
	 * May contain some information about what is missing.
	 * The format needs to be specified yet.
	 */
	private Optional<Object> failedCondition;
	private ITestResult testResult;

	//-------------------------------------------------------------------
	public TreeResult(Result type) {
		this.value = type;
	}

	//-------------------------------------------------------------------
	public TreeResult(boolean success) {
		this.value = success?Result.SUCCESS:Result.RUNNING;
	}

	//-------------------------------------------------------------------
	public TreeResult(String msg) {
		this.value = Result.FAILURE;
		this.internalErrorMessage = msg;
	}

	//-------------------------------------------------------------------
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(value.name());
		return buf.toString();
	}

}
