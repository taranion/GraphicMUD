package com.graphicmud.commands;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.graphicmud.MUD;
import com.graphicmud.commands.CommandSyntaxParser.CommandElement;
import com.graphicmud.commands.CommandSyntaxParser.FixWord;
import com.graphicmud.commands.CommandSyntaxParser.RestOfLine;
import com.graphicmud.commands.CommandSyntaxParser.Variable;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;

/**
 * 
 */
public class NewCommandCenter implements CommandCenter {
	
	static class CommandNode {
		CommandElement element;
		List<CommandNode> children = new ArrayList<NewCommandCenter.CommandNode>();
		List<Command> belongsTo = new ArrayList<Command>();
		public String variableName;
		public Object origValue;
		public CommandNode(CommandElement element) {
			this.element = element;
		}
		public String toString() {
			StringBuilder buf = new StringBuilder();
			if (element!=null) buf.append(element.toString());
			if (variableName!=null) buf.append(" --> "+variableName);
			if (belongsTo!=null) buf.append("  executePossible");
			return buf.toString();
		}
		public String dump() {
			StringBuilder builder = new StringBuilder();
			dump(builder,0);
			return builder.toString();
		}
		private void dump(StringBuilder builder, int depth) {
			for (CommandNode child : children) {
				for (int i=0; i<depth; i++) builder.append("  ");
				builder.append(child.toString()+"\n");
				child.dump(builder, depth+1);
			}
		}
		public boolean matches(String input) {
			if (element instanceof FixWord) {
				return ((FixWord)element).getWord().toLowerCase().startsWith(input.toLowerCase());
			}
			if (element instanceof Variable) {
				Variable var = (Variable)element;
			}
			return false;
		}
		public CommandGroup getBestCommandGroup() {
			CommandGroup old = null;
			for (Command tmp : belongsTo) {
				if (old==null || tmp.getCommandGroup().ordinal()<old.ordinal()) {
					old=tmp.getCommandGroup();
				}
			};
			return old;
		}
		public CommandNode getFirstChildMatching(String key) {
			List<CommandNode> list = children.stream().filter(cn -> cn.matches(key)).toList();
			Collections.sort(list, new Comparator<CommandNode>() {
				public int compare(CommandNode o1, CommandNode o2) {
					if (o1.belongsTo==null) return -1;
					if (o2.belongsTo==null) return 1;
					int cmp = Integer.compare(o1.getBestCommandGroup().ordinal(), o2.getBestCommandGroup().ordinal());
					if (cmp!=0) return cmp;
					return 0;
				}});
			if (list.isEmpty()) return null;
			return list.getFirst();
		}
		public boolean isOptional() {
			if (element==null) return false;
			return element.isOptional();
		}
	}

	static class CommandPath {
		List<CommandNode> pathNodes = new ArrayList<CommandNode>();
		Map<CommandNode, String> inputPerNode = new HashMap<CommandNode, String>();
		
		private transient String asInput;
		private transient Command command;
		private transient Map<String,Object> variables;
		
		public CommandPath(CommandNode node, String token) {
			this.pathNodes.add(node);
			inputPerNode.put(node, token);
		}
		public String toString() {
			StringBuilder buf = new StringBuilder();
			for (CommandNode node : pathNodes) {
				buf.append(node.toString()+"  =?= input was " +inputPerNode.get(node));
				if (node.variableName!=null)
					buf.append(" (VAR "+node.variableName+" = "+node.origValue+")");
				buf.append("\n");
			}
			buf.append(String.valueOf(getCommand()));
			return buf.toString();
		}
		private void finalizeResults() {
			variables = new HashMap<String, Object>();
			List<String> tokens = new ArrayList<String>();
			for (CommandNode node : pathNodes) {
				if (node.belongsTo!=null) {
					if (!node.belongsTo.isEmpty()) {
						command=node.belongsTo.getFirst();
					} else {
						logger.log(Level.ERROR, "No command associated with node "+node);
					}
				}
				String input = inputPerNode.get(node);
				if (input==null)
					continue;
				if (node.variableName==null)
					continue;
				tokens.add(input);
				if (node.origValue!=null) {
					variables.put(node.variableName, node.origValue);
				} else {
					variables.put(node.variableName, input);
				}
			}
			asInput = String.join(" ", tokens);	
		}
		public Map<String,Object> getVariables() {
			return variables;
//			Map<String,Object> vars = new HashMap<String, Object>();
//			for (CommandNode node : pathNodes) {
//				String input = inputPerNode.get(node);
//				if (input==null)
//					continue;
//				if (node.variableName==null)
//					continue;
//				if (node.origValue!=null)
//					vars.put(node.variableName, node.origValue);
//				else
//					vars.put(node.variableName, input);
//			}
//			return vars;
		}
		public Command getCommand() {
			return command;
//			Command ret = null;
//			for (CommandNode cp : pathNodes)
//				if (cp.belongsTo!=null) 
//					ret=cp.belongsTo;			
//			return ret;
		}
		public String asInput() {
			return asInput;
		}
		
	}
	
	private final static Logger logger = System.getLogger(NewCommandCenter.class.getPackageName());
	
	private static List<Locale> locales = new ArrayList<Locale>();
	private static List<Command> registered = new ArrayList<Command>();
	private static Map<Locale, CommandNode> rootNodes = new HashMap<Locale, NewCommandCenter.CommandNode>();
	
	//-------------------------------------------------------------------
	static {
		addSupportedLocale(Locale.ENGLISH);
	}

	//-------------------------------------------------------------------
	static void unitTestReset() {
		rootNodes.clear();
		locales.clear();
		addSupportedLocale(Locale.ENGLISH);
	}
	
	//-------------------------------------------------------------------
	public static void addSupportedLocale(Locale loc) {
		if (!locales.contains(loc)) 
			locales.add(loc);
		if (!rootNodes.containsKey(loc))
			rootNodes.put(loc, new CommandNode(null));
	}
	
	//-------------------------------------------------------------------
	/**
	 * Let the engine understand a new command
	 */
	public CommandCenter registerCommand(Command com) {
		logger.log(Level.WARNING, "Register command "+com.getId());
		registered.add(com);
		String syntaxKey = "command."+com.getId()+".syntax";
		// Add the command to each locales command tree
		for (Locale loc : locales) {
			// Find the command syntax for this locale
			String syntax = com.getProperties().getString(syntaxKey, loc);
			try {
				List<CommandElement> elements = CommandSyntaxParser.parse(syntax);
				extendTree(loc, elements, com);
			} catch (ParseException e) {
				logger.log(Level.ERROR, "Syntax error for command ''{0}'': {1}", com.getId(), e.getMessage());
			} catch (Exception e) {
				logger.log(Level.ERROR, "Syntax error for command ''{0}'': {1}", com.getId(), e.toString());
				e.printStackTrace();
			}
		}
		return this;
	}

	//-------------------------------------------------------------------
	private static void extendTree(Locale loc, List<CommandElement> elements, Command com) {
		logger.log(Level.TRACE, "Extend tree for locale {0} with {1}", loc, elements);
		
		CommandNode root = rootNodes.get(loc);
		extendTree(loc, List.of(root), elements, com);
	}

	//-------------------------------------------------------------------
	/**
	 * @param parents The parent node or in case of optional parent nodes 
	 * 		all parents up to the last non-optional
	 */
	private static void extendTree(Locale loc, List<CommandNode> parents, List<CommandElement> elements, Command com) {
		if (elements.isEmpty())
			return;
		CommandElement elem = elements.getFirst();
		CommandNode toAdd = new CommandNode(elem);
		CommandNode parent = parents.getLast();
		// Is there already a matching child
		for (CommandNode child : parent.children) {
			
		}
		
		// No
		
		// recurseParents is the list of parents to enter recursion with
		List<CommandNode> recurseParents = new ArrayList<NewCommandCenter.CommandNode>();
		List<CommandElement> tail = elements.subList(1, elements.size());
		// Add node to all parents
//		logger.log(Level.DEBUG, "Parents to add "+toAdd+" to "+parents);
		String varName = (elem instanceof Variable)?((Variable)elem).getName():null;		
		if (toAdd.element instanceof Variable && ((Variable)toAdd.element).getOptions()!=null) {
			String[] names = ((Variable)toAdd.element).getOptions().apply(loc);
			logger.log(Level.TRACE, "Replace "+toAdd.element+" with "+Arrays.toString(names)+" for locale "+loc);
			for (String name : names) {
				CommandNode toAdd2 = new CommandNode(new FixWord(name));
				toAdd2.variableName = varName;	
				toAdd2.belongsTo.add(com);
				toAdd2.origValue = ((Variable)toAdd.element).getOptionBackResolver().apply(loc, name);
				for (CommandNode tmp : parents) {
					tmp.children.add(toAdd2);
					recurseParents.add(toAdd2);
				}
				if (tail.isEmpty())
					toAdd.belongsTo.add(com);
			}
		} else {
			toAdd.variableName = varName;
			if (elem.isOptional()) {
				recurseParents.addAll(parents);				
			} 
			recurseParents.add(toAdd);
			for (CommandNode tmp : parents) {
				tmp.children.add(toAdd);
			}
			toAdd.belongsTo.add(com);
		}
		
		if (elem instanceof RestOfLine) {
//			logger.log(Level.DEBUG, "Don't recurse after RestOfLine");
			return;
		}
		
		// Recurse
		if (!tail.isEmpty()) {
			extendTree( loc,  recurseParents, tail, com );
		}
		
	}
	
	//-------------------------------------------------------------------
	static CommandNode getTreeRoot(Locale loc) {
		return rootNodes.get(loc);
	}

	@Override
	public List<CommandName> getAllCommands(Locale loc) {
		logger.log(Level.DEBUG, "getAllCommands({0})",loc);
		List<CommandName> ret = new ArrayList<CommandName>();
		CommandNode tree = getTreeRoot(loc);
		for (CommandNode node : tree.children) {
			if (node.element instanceof FixWord) {
				FixWord word = (FixWord) node.element;
				// TODO: Umgang damit wenn das Kommandowort von Kommandos aus mehreren Kategorien verwendet wird
				ret.add(new CommandName(word.getWord(), node.belongsTo.get(0).getCommandGroup()));
			}
		}
		return ret;
	}

//	@Override
//	public List<ParsedCommand> getPossibleCompletions(ClientConnection con, String input) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	public List<CommandPath> getPossiblePathes(MUDEntity issuer, String input) {
		return getPossiblePathes(issuer, input, Locale.ENGLISH);
	}
	public List<CommandPath> getPossiblePathes(MUDEntity issuer, String input, Locale loc) {
		int readUntil=0;		
		CommandNode current = getTreeRoot(loc);	
		if (current==null) {
			logger.log(Level.ERROR, "Did not find command tree for locale "+loc);
			return List.of();
		}
		
		List<CommandPath> ret = getPossiblePathes(issuer, input, loc, current, readUntil);
		ret.forEach(cp -> cp.finalizeResults());
		return ret;
	}
	private List<CommandPath> getPossiblePathes(MUDEntity issuer, String input, Locale loc, CommandNode current, int readPos) {
		logger.log(Level.DEBUG, "--getPossiblePathes: parse input ''{0}''", input);
		if (input.length()==0)
			return new ArrayList<CommandPath>();
		
		String origInput = input;
		StringTokenizer tok = new StringTokenizer(input);
		String token = tok.nextToken();
		// Find out how far in the input we have read
		int readUntil =  token.length() +1;
		logger.log(Level.DEBUG, "Token is ''{0}'' and read position now {1}", token, readUntil);
			
		List<CommandPath> ret = new ArrayList<CommandPath>();
		// Find a matching child of the current element
		for (CommandNode child : current.children) {
			switch (child.element) {
			case FixWord fix -> {
				if (fix.getWord().startsWith(token.toLowerCase())) {
					// Match
					logger.log(Level.DEBUG, "Token ''{0}'' matches {1}", token, fix);
					if (!checkPermission(issuer, child))
						continue;
					
					// Depth first
					String tailInput = "";
					if (readUntil<input.length())
						tailInput = input.substring(readUntil);
					List<CommandPath> sub = getPossiblePathes(issuer, tailInput, loc, child, readUntil);
					logger.log(Level.DEBUG, "sub = "+sub);
					if (sub.isEmpty()) {
						CommandPath path = new CommandPath(child, fix.getWord());
						path.command = child.belongsTo.get(0);
						ret.add(path);
						
						logger.log(Level.DEBUG, "Create and add command path for "+token);
					} else {
						sub.forEach( cp -> {
							logger.log(Level.DEBUG, "Prepend "+token+" to "+cp.pathNodes);
							cp.pathNodes.add(0, child);
							cp.inputPerNode.put(child, fix.getWord());
							ret.add(cp);
							logger.log(Level.DEBUG, "  resulting in "+cp.pathNodes);
							});							
						}
				} // if
			} // FixWord
			case RestOfLine rest -> {
				CommandPath cp = new CommandPath(child, origInput);
				cp.inputPerNode.put(child, origInput);
				logger.log(Level.DEBUG, "Added ''{0}'' as {1}", origInput, child.variableName);
				ret.add(cp);				
			}
			case Variable var -> {
				// Match
				logger.log(Level.DEBUG, "Token ''{0}'' matches variable {1}", token, var);
				if (!checkPermission(issuer, child))
					continue;
				
				// Depth first
				String tailInput = "";
				if (readUntil<input.length())
					tailInput = input.substring(readUntil);
				List<CommandPath> sub = getPossiblePathes(issuer, tailInput, loc, child, readUntil);
				logger.log(Level.DEBUG, "sub = "+sub);
				if (sub.isEmpty()) {
					ret.add(new CommandPath(child, token));
					logger.log(Level.DEBUG, "Create and add command path for "+token);
				} else {
					sub.forEach( cp -> {
						logger.log(Level.DEBUG, "Prepend "+token+" to "+cp.pathNodes);
						cp.pathNodes.add(0, child);
						cp.inputPerNode.put(child, token);
						ret.add(cp);
						logger.log(Level.DEBUG, "  resulting in "+cp.pathNodes);
						});							
				}
			}
			default -> logger.log(Level.WARNING, "Nothing to do for "+child);
				
			} // switch
		} // for			
		
		return ret;
	}
	private boolean checkPermission(MUDEntity issuer, CommandNode node) {
		return true;
	}

	

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.CommandCenter#parse(com.graphicmud.network.ClientConnection, java.lang.String)
	 */
	@Override
	public void parse(ClientConnection con, String input) {
		logger.log(Level.WARNING, "parse ''{0}''", input);
		List<CommandPath>  options = getPossiblePathes(con.getCharacter(), input, con.getLocale());
		// Sort options by command group priority
		Collections.sort(options, new Comparator<CommandPath>() {
			@Override
			public int compare(CommandPath c1, CommandPath c2) {
				if (c1.command==null && c2.command!=null) return 1;
				if (c1.command!=null && c2.command==null) return -1;
				
				int base = Integer.compare(c1.getCommand().getCommandGroup().ordinal(), c2.getCommand().getCommandGroup().ordinal());
				logger.log(Level.DEBUG, "Comparing "+c1.getCommand()+"/"+c1.command.getCommandGroup()+" with "+c2.command+"/"+c2.command.getCommandGroup()+" resulted in "+base);
				if (base!=0)
					return base;
				return c1.asInput().compareTo(c2.asInput());
			}
		});
		for (CommandPath entry : options) {
			logger.log(Level.WARNING, "Sorted Option: "+entry+" with variables "+entry.getVariables());
		}
		// Execute the first
		if (!options.isEmpty()) {
			Command com = options.get(0).getCommand(); 
			logger.log(Level.WARNING, "Execute {0}",com.getClass());
			com.execute(con.getCharacter(), options.get(0).getVariables());
			// Inform RPG connector
			switch (com.getCommandGroup()) {
			case EXAMINE:
			case INTERACT:
				MUD.getInstance().getRpgConnector().commandHook(com, con.getCharacter(), options.get(0).getVariables());
			}
			con.sendPrompt("");
		} else {
			con.sendShortText(Priority.IMMEDIATE, "Huh?\r\n");
			con.sendPrompt("");
		}
	}
	

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.CommandCenter#parse(com.graphicmud.network.ClientConnection, java.lang.String)
	 */
	@Override
	public void parse(MUDEntity executedBy, String input) {
		logger.log(Level.WARNING, "parse ''{0}''", input);
		List<CommandPath>  options = getPossiblePathes(executedBy, input, Locale.getDefault());
		// Sort options by command group priority
		Collections.sort(options, new Comparator<CommandPath>() {
			@Override
			public int compare(CommandPath c1, CommandPath c2) {
				if (c1.command==null && c2.command!=null) return 1;
				if (c1.command!=null && c2.command==null) return -1;
				
				int base = Integer.compare(c1.getCommand().getCommandGroup().ordinal(), c2.getCommand().getCommandGroup().ordinal());
				logger.log(Level.INFO, "Comparing "+c1.getCommand()+"/"+c1.command.getCommandGroup()+" with "+c2.command+"/"+c2.command.getCommandGroup()+" resulted in "+base);
				if (base!=0)
					return base;
				return c1.asInput().compareTo(c2.asInput());
			}
		});
		for (CommandPath entry : options) {
			logger.log(Level.INFO, "Sorted Option: "+entry);
		}
		// Execute the first
		if (!options.isEmpty()) {
			options.get(0).getCommand().execute(executedBy, options.get(0).getVariables());			
		} else {
			logger.log(Level.ERROR, "Mobile {0} executed unknown command ''{1}''", executedBy, input);
		}
	}

	@Override
	public void execute(Class<? extends Command> comCls, MUDEntity executedBy, Map<String,Object> variables) {
		Command com = null;
		for (Command tmp : registered) {
			if (tmp.getClass()==comCls) {
				com=tmp;
				break;
			}
		}
		if (com==null) {
			logger.log(Level.ERROR, "Command "+comCls+" not registered");
			return;
		}
		
		com.execute(executedBy, variables);			
	}
}
