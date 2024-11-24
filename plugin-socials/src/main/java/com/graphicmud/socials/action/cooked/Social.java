/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.socials.action.cooked;

import static com.graphicmud.io.text.TextUtil.capitalize;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import com.graphicmud.action.RoomHelper;
import com.graphicmud.action.cooked.CookedAction;
import com.graphicmud.action.cooked.CookedActionHelper;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.CookedActionResult;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.action.raw.Echo;
import com.graphicmud.action.raw.FireEntityEvent;
import com.graphicmud.action.raw.FireRoomEvent;
import com.graphicmud.action.raw.RawAction;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.game.EntityState;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.game.MUDEvent.Type;
import com.graphicmud.socials.commands.SocialType;
import com.graphicmud.world.Location;

public class Social implements CookedAction {
	
	private final static Logger logger = System.getLogger(Social.class.getPackageName());
	
	private final static String NOT_WHILE_FIGHTING = "action.social.error.not_while_fighting";

	
	private SocialType social;
	
	//-------------------------------------------------------------------
	public Social(SocialType type) {
		this.social = type;
	}
	
	//-------------------------------------------------------------------
	public List<RawAction> perform(MUDEntity performedBy, Context context) {
		return performSocial(performedBy, context, social);
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult selectedSocial(MUDEntity performedBy, Context context) {
//		SocialType selection = context.get(ParameterType.SOCIAL);
		MobileEntity target = context.get(ParameterType.MOBILE);
		throw new RuntimeException("Social must be repaired");
//		if (target==null)
//			return performSocial(performedBy, context, selection);
//		else
//			return performTargetedSocial(performedBy, context, selection, target);
	}
	
	//-------------------------------------------------------------------
	private static CookedActionResult performSocial(MUDEntity performedBy, Context context, SocialType social) {
		logger.log(Level.INFO, "ENTER: performSocial");
		try {
			CookedActionResult errors = test(performedBy, context);
			if (!errors.isEmpty())
				return errors;
			
			Location room = context.get(ParameterType.ROOM_CURRENT);
			String toLoc = "action.social." + social.name().toLowerCase();
			List<RawAction> raw = new ArrayList<>();
			// Send to self
			raw.add( new Echo(toLoc+".self")::sendSelf );
			raw.add( new Echo(toLoc+".other", performedBy.getName())::sendOthers );
			raw.add( new FireRoomEvent(room, new MUDEvent(Type.SOCIAL_PERFORMED, performedBy, social)));
			return new CookedActionResult("social",raw, social.name());
		} finally {
			logger.log(Level.DEBUG, "LEAVE: performSocial");
		}
	}
	
	//-------------------------------------------------------------------
	private static CookedActionResult performTargetedSocial(MUDEntity performedBy, Context context, SocialType social, MobileEntity target) {
		logger.log(Level.DEBUG, "ENTER: performTargetedSocial");
		try {
			CookedActionResult errors = test(performedBy, context);
			if (!errors.isEmpty())
				return errors;
			Location room = context.get(ParameterType.ROOM_CURRENT);
			String toLoc = "action.social." + social.name().toLowerCase();
	        
			CookedActionResult raw = new CookedActionResult();
			// Send to self
			raw.add( new Echo(toLoc+".selfWithTarget", target.getName())::sendSelf );
			raw.add( new Echo(toLoc+".target", target.getName())::sendTarget );
			raw.add( new Echo(toLoc+".otherWithTarget", performedBy.getName(), target.getName())::sendOthersExceptTarget );
			raw.add( new FireRoomEvent(room, new MUDEvent(Type.SOCIAL_PERFORMED, performedBy, social)));
			raw.add( new FireEntityEvent(target, new MUDEvent(Type.RCV_SOCIAL, performedBy, social)));
			return raw;
		} finally {
			logger.log(Level.DEBUG, "LEAVE performTargetedSocial");
		}
	}
	
	//-------------------------------------------------------------------
	public static List<RawAction> wave(MUDEntity performedBy, Context context) {
		return performSocial(performedBy, context, SocialType.WAVE);
	}
	public static void wave(MUDEntity performedBy) {
		performSocial(performedBy, new Context(), SocialType.WAVE);		
	}
	
	//-------------------------------------------------------------------
	public static TreeResult yawn(MUDEntity performedBy, Context context) {
		return CookedActionProcessor.performAsTreeResult( performSocial(performedBy, context, SocialType.YAWN), performedBy, context);
	}
	public static void yawn(MUDEntity performedBy) {
		performSocial(performedBy, new Context(), SocialType.YAWN);		
	}

	//-------------------------------------------------------------------
	private static CookedActionResult test(MUDEntity performedBy, Context context) {
		CookedActionResult res = RoomHelper.getRoomByPosition(performedBy, context);
		if (!res.isEmpty()) return res;
		res = CookedActionHelper.validateTargetMobile(performedBy, context);
		if (!res.isEmpty()) return res;
		
		// Some socials may be used while fighting, others don't
		if (performedBy.getState()==EntityState.FIGHTING) {
			res.add( (new Echo(NOT_WHILE_FIGHTING))::sendSelf );
		}
		
		return res;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.action.cooked.CookedAction#apply(com.graphicmud.game.MUDEntity, com.graphicmud.behavior.Context)
	 */
	@Override
	public CookedActionResult apply(MUDEntity performedBy, Context context) {
		logger.log(Level.INFO, "ENTER: Social.apply\n{0}",context);
		try {
			CookedActionResult errors = test(performedBy, context);
			if (!errors.isEmpty())
				return errors;

//			context.put(ParameterType.SOCIAL, social);
			MobileEntity target = context.get(ParameterType.MOBILE);
			if (target==null)
				return performSocial(performedBy, context, social);
			else
				return performTargetedSocial(performedBy, context, social, target);
		} finally {
			logger.log(Level.INFO, "LEAVE: Social.apply");
		}
//		CookedActionResult test = test(performedBy, context);
//		if (!test.isSuccessful()) return test;
//		
//		List<RawAction> raw = new ArrayList<>();
//		Location room = context.get(ParameterType.ROOM_CURRENT);
//		
//		String toLoc = "action.social." + social.name().toLowerCase();
//        MUDEvent evTarget = new MUDEvent(Type.RCV_SOCIAL, performedBy, social);
//		raw.add( (new Echo(toLoc+".self"))::sendSelf );
//		raw.add( (new Echo(toLoc+".other", capitalize(performedBy.getName()), evTarget))::sendOthers );
//		raw.add(new FireRoomEvent(room, new MUDEvent(Type.SOCIAL_PERFORMED, performedBy, social)));
//		return new CookedActionResult(raw);
	}

}
