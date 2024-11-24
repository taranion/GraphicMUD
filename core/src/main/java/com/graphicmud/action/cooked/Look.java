/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.graphicmud.action.RoomHelper;
import com.graphicmud.action.raw.Echo;
import com.graphicmud.action.raw.RawAction;
import com.graphicmud.action.raw.SendSurrounding;
import com.graphicmud.behavior.Context;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.io.text.TextUtil;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;

/**
 * 
 */
public class Look {
	
	private final static Logger logger = System.getLogger(Look.class.getPackageName());

	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	public static class LookAround implements CookedAction {
		//-------------------------------------------------------------------
		public String getId() { return "LookAround";}
		//-------------------------------------------------------------------
		@Override
		public CookedActionResult apply(MUDEntity performedBy, Context context) {
			return new CookedActionResult("look",List.of(SendSurrounding::sendMapAndText),null);
		}
	}

	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	public static class LookDirection implements CookedAction {
		private Direction direction;
		//-------------------------------------------------------------------
		public String getId() { return "LookDirection("+direction+")";}
		//-------------------------------------------------------------------
		public LookDirection(Direction dir) {
			this.direction = dir;
		}
		//-------------------------------------------------------------------
		@Override
		public CookedActionResult apply(MUDEntity performedBy, Context context) {
			context.put(ParameterType.DIRECTION, direction);
			CookedActionResult test = RoomHelper.getRoomAndExitByPosition(performedBy, context);
			if (!test.isSuccessful()) return test;
			
			CookedActionResult raw = new CookedActionResult();
			Exit exit = context.get(ParameterType.EXIT);
            String specialDescription = exit.getDescription();
            if (specialDescription != null && !specialDescription.isBlank()) {
            	raw.add( (new Echo("mess.self.exit", TextUtil.addFullStop(exit.getDescription())))::sendSelf);
            } else {
     	        Location loc = context.get(ParameterType.ROOM_TARGET);
            	String title = loc.getRoomComponent().get().getTitle();
            	raw.add( (new Echo("mess.self.exitwithoutdescription", direction.getName(performedBy.getLocale()), TextUtil.firstToLower(title)))::sendSelf);
            }
			return raw;
		}
	}

	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	public static class LookEntity implements CookedAction {
		private String target;
		//-------------------------------------------------------------------
		public String getId() { return "LookEntity("+target+")";}
		//-------------------------------------------------------------------
		public LookEntity(String target) {
			this.target = target;
		}
		//-------------------------------------------------------------------
		@Override
		public CookedActionResult apply(MUDEntity performedBy, Context context) {
			logger.log(Level.DEBUG, "LookEntity.apply "+context);
			// Test first
	        CommandUtil.IndexedEntity indexedItem = CommandUtil.createIndexEntity(target);
	        // Im Inventory anschauen
	        List<MUDEntity> bestMatchingEntities;
	        try {
	            bestMatchingEntities = new LinkedList<>(CommandUtil.getAllEntitiesFromPosition(indexedItem.getItemName(), performedBy.getPosition()));
	            if (bestMatchingEntities.isEmpty() || bestMatchingEntities.size() -1 <indexedItem.getIndex()) {
	            	return new CookedActionResult("mess.nosuchtarget", target);
	            }
	            MUDEntity bestMatchingEntity = bestMatchingEntities.get(indexedItem.getIndex());
	            if (bestMatchingEntity == performedBy) {
	            	return new CookedActionResult("mess.selftargeting");
	            }
	            context.put(ParameterType.TARGET, bestMatchingEntity);
	        } catch (NoSuchPositionException npe) {
				return new CookedActionResult(npe.toString());
	        }

			// Now real execution
			List<RawAction> raw = new ArrayList<>();
			
			MUDEntity bestMatchingEntity = context.get(ParameterType.TARGET);
			logger.log(Level.DEBUG, "bestMatchingEntity {0}", bestMatchingEntity);
			raw.add( (new Echo("command.look.mess.self.at", bestMatchingEntity.getName()))::sendSelf);
			if (bestMatchingEntity.getDescription()!=null) {
				raw.add( (new Echo(bestMatchingEntity.getDescription()))::sendSelf);
			}
        	raw.add( (new Echo("command.look.mess.other.at", bestMatchingEntity.getName()))::sendOthers);
        	raw.add( (new Echo("command.look.mess.target.at", bestMatchingEntity.getName()))::sendTarget);
			return new CookedActionResult("lookAt",raw,bestMatchingEntity.getName());
		}
	}

	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	public static CookedActionResult lookAround(MUDEntity actor, Context context) {
		return new CookedActionResult("look",List.of(SendSurrounding::sendMapAndText),null);
	}
	
}
