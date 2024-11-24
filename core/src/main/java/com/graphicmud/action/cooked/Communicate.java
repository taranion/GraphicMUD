/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Locale;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.CData;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.ActionUtil;
import com.graphicmud.action.raw.Echo;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.world.Range;
import com.graphicmud.world.WorldCenter;

/**
 * 
 */
public class Communicate {

	private final static Logger logger = System.getLogger(Communicate.class.getPackageName());
	
	public static class Say extends Communicate implements CookedAction {
		@Attribute
		private String to;
		@CData
		private String text;
		//-------------------------------------------------------------------
		public Say() { super("bla"); }
		//-------------------------------------------------------------------
		public String getId() { return "Say("+text+")";}
		//-------------------------------------------------------------------
		@Override
		public CookedActionResult apply(MUDEntity performedBy, Context context) {
			String textToSay = (context.containsKey(ParameterType.TEXT))?context.get(ParameterType.TEXT):text;
			if ("src".equals(to)) {
				context.put(ParameterType.TARGET, context.get(ParameterType.SOURCE));
			} else {
				context.remove(ParameterType.TARGET);
			}
			logger.log(Level.DEBUG,"Communicate.say "+textToSay+" in context "+context);

			CookedActionResult raw = new CookedActionResult("say", textToSay);
			MUDEntity target = context.get(ParameterType.TARGET);
			if (target==null) {
				raw.add( (new Echo("action.communicate.say.self",textToSay))::sendSelf);
				raw.add( (new Echo("action.communicate.say.others",textToSay, performedBy.getName()))::sendOthers);
			} else {
				raw.add( (new Echo("action.communicate.say.target.self",textToSay, target.getName()))::sendSelf);
				raw.add( (new Echo("action.communicate.say.target",textToSay, target.getName()))::sendTarget);
				raw.add( (new Echo("action.communicate.say.target.others",textToSay, performedBy.getName()))::sendOthersExceptTarget);
			}
			return raw;
		}
	}
	
	@CData
	protected String text = "not set";
	
	//-------------------------------------------------------------------
	public Communicate(String text) {
		this.text = text;
	}
	
	//-------------------------------------------------------------------
	public TreeResult say(MUDEntity performedBy, Context context) {
		String textToSay = (context.containsKey(ParameterType.TEXT))?context.get(ParameterType.TEXT):text;
		logger.log(Level.DEBUG,"Communicate.say "+textToSay+" in context "+context);
		
		MUDEntity target = context.get(ParameterType.TARGET);
		WorldCenter wCenter = MUD.getInstance().getWorldCenter();
		for (MobileEntity other : wCenter.getPlayersInRangeExceptSelf(performedBy, Range.SURROUNDING)) {
			Locale loc = ActionUtil.getLocale(other);
			if (other==target) {
				String output = Localization.fillString("action.communicate.say.target", 
						loc, performedBy.getName(), textToSay);
				other.sendShortText(Priority.PERSONAL, output);
			} else {
				if (target==null) {
					String output = Localization.fillString("action.communicate.say", 
							loc, performedBy.getName(), textToSay);
					other.sendShortText(Priority.UNIMPORTANT, output);
				} else {
					String output = Localization.fillString("action.communicate.say.otherWithTarget", 
							loc, performedBy.getName(), target.getName(), textToSay);
					other.sendShortText(Priority.UNIMPORTANT, output);
				}
			}
		}
		return new TreeResult(Result.SUCCESS);
	}

}

