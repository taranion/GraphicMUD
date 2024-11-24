/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
public interface CommandCenter {
	
	@AllArgsConstructor
	@Getter
	public static class CommandName {
		public String name;
		public CommandGroup type;
	}

//	public static class ParsedCommand {
//		String asInput;
//		Map<String,Object> parameterByName = new LinkedHashMap<>();
//		Command commandToExecute;
//		public ParsedCommand(String asInput, Command comm) {
//			this.asInput = asInput;
//			this.commandToExecute = comm;
//		}
//		public String toString() {
//			return asInput+"=>"+commandToExecute+" with "+parameterByName;
//		}
//	}


	public CommandCenter registerCommand(Command value);
	public List<CommandName> getAllCommands(Locale loc);

//	public List<ParsedCommand> getPossibleCompletions(ClientConnection con, String input);

	public void parse(ClientConnection con, String input);
	@Deprecated public void parse(MUDEntity executedBy, String input);
	public void execute(Class<? extends Command> comCls, MUDEntity executedBy, Map<String,Object> variables);

}
