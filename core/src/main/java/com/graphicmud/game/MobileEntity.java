/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.graphicmud.character.EquippedGear;
import com.graphicmud.game.Vital.VitalType;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
public class MobileEntity extends MUDEntity {

	@Setter
	protected Pose pose = Pose.STANDING;
	protected Map<VitalType, Vital> vitals = new HashMap<Vital.VitalType, Vital>();

	@Getter
	private EquippedGear equippedGear;

    //-------------------------------------------------------------------
    /**
     * Empty constructor required for deserialization and initializing lists in this case
     */
    public MobileEntity() {
        super();
        super.type = EntityType.MOBILE;
        vitals = new HashMap<Vital.VitalType, Vital>();
    }

	//-------------------------------------------------------------------
	public MobileEntity(MUDEntityTemplate data) {
		super(data);
        super.type = EntityType.MOBILE;
		if (data != null) {
			if (data.getType() != EntityType.MOBILE && data.getType() != EntityType.PLAYER)
				throw new IllegalArgumentException("ItemEntity requires template of type MOBILE or PLAYER");
		}
		equippedGear = new EquippedGear();
	}
	   
   //-------------------------------------------------------------------
   public Map<VitalType,Vital> getVitals() { 
	   return vitals;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.MUDEntity#pulse()
	 */
	@Override
	public void pulse() {
		if (pose==Pose.DEAD)
			return;
		super.pulse();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.MUDEntity#tick()
	 */
	@Override
	public void tick() {
		if (pose==Pose.DEAD)
			return;
		super.tick();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.MUDEntity#die()
	 */
	@Override
	public void die() {
		pose = Pose.DEAD;
		super.die();
	}

	public List<MUDEntity> getAllFromCompleteInventory(String itemName) {
		if (itemName == null) {	
			return new ArrayList<>();
		}
		List<MUDEntity> completeInventory = new LinkedList<>(getInventory());
		completeInventory.addAll(equippedGear);
		return completeInventory.stream().filter(i -> i.reactsOnKeyword(itemName)).toList();
	}
	
}
