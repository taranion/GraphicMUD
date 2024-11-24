/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.util.Arrays;
import java.util.Locale;

import com.graphicmud.MUD;
import com.graphicmud.action.raw.Echo;
import com.graphicmud.behavior.Context;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.world.Location;
import com.graphicmud.world.Range;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;

/**
 * 
 */
public class CookedActionHelper {

	//-------------------------------------------------------------------
	public static CookedActionResult validateExit(MUDEntity actor, Context context, Direction dir) {
		Location room = context.get(ParameterType.ROOM_CURRENT);
		// Find the exit for the configured direction
		Exit exit = room.getRoomComponent().get().getExit(dir);
		if (exit==null) {
			return new CookedActionResult( (new Echo("action.error.no_exit_in_direction", dir)::sendSelf));
		}
		context.put(ParameterType.EXIT, exit);
		return new CookedActionResult();
	}

	//-------------------------------------------------------------------
	public static CookedActionResult validateTargetMobile(MUDEntity actor, Context context) {
		String search = context.get(ParameterType.TARGET_NAME);
		if (search==null)
			return new CookedActionResult();
		
		for (MUDEntity ent : MUD.getInstance().getWorldCenter().getLifeformsInRangeExceptSelf(actor, Range.SURROUNDING)) {
			if (ent.getName().toLowerCase().contains(search)) {
				context.put(ParameterType.TARGET, ent);
				if (ent instanceof MobileEntity)
					context.put(ParameterType.MOBILE, ent);
				else if (ent instanceof ItemEntity)
					context.put(ParameterType.ITEM, ent);
				return new CookedActionResult();
			}
		}
		return new CookedActionResult( (new Echo("action.error.invalid_target", search)::sendSelf));
	}

	//-------------------------------------------------------------------
	public static Direction getDirectionByName(Locale loc, String target) {
        String[] values = Direction.values(loc);
        String directionString = Arrays.stream(values).filter(v -> v.toLowerCase().startsWith(target)).findFirst().orElse(null);
        return Direction.valueOf(loc, directionString);
	}
	
}
