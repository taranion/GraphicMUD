/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import java.lang.System.Logger;
import java.text.MessageFormat;
import java.util.Locale;

import org.prelle.simplepersist.Attribute;

import com.graphicmud.player.PlayerCharacter;

import de.rpgframework.MultiLanguageResourceBundle;

/**
 * Command.java
 *
 * @author Stefan Prelle  
 */

public abstract class ACommand implements Command {

	protected final static Logger logger = System.getLogger("command");
	protected final static Logger syslog = System.getLogger("syslog");

	private MultiLanguageResourceBundle i18n;

	@Attribute
	protected CommandGroup type;
	@Attribute
	protected String id;
	protected String description;
	protected String quickHelp;

	private final String prefix;

	//---------------------------------------------------------------
	public ACommand(CommandGroup type, String name, MultiLanguageResourceBundle i18n) {
		this.type = type;
		this.id = name;
		prefix = "command."+id;
		setPropertyResource(i18n);
	}

	//-------------------------------------------------------------------
	private void setPropertyResource(MultiLanguageResourceBundle i18n) {
		this.i18n = i18n;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#getI18N(java.lang.String)
	 */
	@Override
	public String getI18N(String key) {
		return i18n.getString(key);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#getI18N(java.lang.String, java.util.Locale)
	 */
	@Override
	public String getI18N(String key, Locale loc) {
		return i18n.getString(key, loc);
	}

	//-------------------------------------------------------------------
	public String getString(String suffix) {
		return i18n.getString(prefix+"."+suffix);
	}

	//-------------------------------------------------------------------
	public String getString(String suffix, Locale loc) {
		return i18n.getString(prefix+"."+suffix, loc);
	}

	//-------------------------------------------------------------------
	public String fillString(String suffix, Locale loc, Object ...data) {
		String pattern = i18n.getString(prefix+"."+suffix, loc);
		return MessageFormat.format(pattern, data);
	}

	//-------------------------------------------------------------------
	public String fillString(String suffix, Object ...data) {
		String pattern = i18n.getString(prefix+"."+suffix);
		return MessageFormat.format(pattern, data);
	}

    //-------------------------------------------------------------------
   /**
    * Returns a unique, untranslated identifier - not shown to players but to
    * distinguish commands internally
    * @return
    */
	@Override
	public String getId() {
	   return id;
	}


	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#getProperties()
	 */
	public MultiLanguageResourceBundle getProperties() {
		return i18n;
	}

	//---------------------------------------------------------------
	public String toString() {
		return id;
	}

	//------------------------------------------------
	/*
	 * @see org.prelle.mud.commands.Command#getCommandGroup()
	 */
	public CommandGroup    getCommandGroup() {return type;}
	//------------------------------------------------
	/*
	 * @see org.prelle.mud.commands.Command#getDescription()
	 */
	public String getDescription()  {return description;}

	//-------------------------------------------------------------------------
	/*
	 * @see org.prelle.mud.commands.Command#canBeExecutedBy(org.prelle.mud.life.Player)
	 */
	public boolean canBeExecutedBy(PlayerCharacter s) {
//		for (CommandVariant variant : variants) {
//			if (variant.canBeExecutedBy(s))
//				return true;
//		}
		return false;
	}

	//-----------------------------------------------------------------
	/**
	 * @return
	 */
	public String dump() {
		StringBuffer buf = new StringBuffer();
		buf.append("Command(type="+type+", name="+id+", descr="+description+"\n");
		buf.append("  quickhelp  ="+quickHelp+"\n");
		buf.append("  description="+description+"\n");
//		buf.append("  variants:");
//		for (CommandVariant var : variants) {
//			buf.append("\n  "+((CommandVariantImpl)var).dump());
//		}
		return buf.toString();
	}



} // Command
