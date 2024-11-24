/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.util.ArrayList;
import java.util.List;

import com.graphicmud.action.MoveInRoom;
import com.graphicmud.action.MoveToRoom;
import com.graphicmud.action.RestAction;
import com.graphicmud.action.cooked.Communicate;
import com.graphicmud.action.cooked.DoorsAction;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;

public class Demo {
	
	public static void main(String[] args) {
		int bed = 0;
//		ActionContext entity = new ActionContext();
//		Consumer<ActionContext> goToBed = new MoveRoomAction(10302)
//				.andThen(DoorsAction::lockAll)
//				.andThen((new MoveInRoom(bed))::find)
//				.andThen(RestAction::sleep);		
//		goToBed.accept(entity);
		
		
		List<MUDEntity> mobs = new ArrayList<MUDEntity>();
//		mobs.stream()			
//			.forEach(Social::yawn)
//			;
		
		
		SequenceNode goToBed = new SequenceNode()
				.add((new MoveToRoom(10302))::find)
				.add(DoorsAction::lockAll)
				.add((new MoveInRoom(bed))::find)
				.add(RestAction::sleep);

		ItemEntity langschwert = null;
		RandomNode node = new RandomNode()
				.add((new Communicate("Hello"))::say)
//				.add((new InteractWithItem(langschwert))::get)
//				.add(Social::yawn)
				.add(goToBed);
	}
	
	/**
	 * <random>
	 *   <communicate method="say">
	 *     <text>Hello</text>
	 *   </communicate>
	 *   <social method="yawn"/>
	 *   <sequence id="goToBed">
	 *     <moveToRoom method="find" param="10302"/>
	 *     <doors method="lockAll"/
	 *     <moveInRoom method="find" param="bed"/>
	 *     <rest method="sleep"/>
	 *   </sequence>
	 *   <sequence id="follow">
	 *     <select method="target"/>
	 *     <target method="follow"/> <!-- Registriert Event Listener -->
	 *   </sequence>
	 * </random>
	 */

}
