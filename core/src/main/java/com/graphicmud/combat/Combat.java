/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphicmud.game.EntityState;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.Location;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 
 */
@SuperBuilder(setterPrefix = "with")
public abstract class Combat {
	
	protected CombatListener listener;
	
	@Getter
	private List<MUDEntity> combatantsSideA;
	@Getter
	private List<MUDEntity> combatantsSideB;
	@Getter
	private Location location;
	
	private Map<MUDEntity, Object> perCombatantState;
	
	@Getter @Setter
	private boolean combatFinished;
	
	//-------------------------------------------------------------------
	public void prepare() {
		perCombatantState = new HashMap<MUDEntity, Object>();
		listener.prepareCombatants(this);
		getAllCombatants().forEach(e -> {
			e.setState(EntityState.FIGHTING);
			e.setCurrentCombat(this);
			});
	}
	
	//-------------------------------------------------------------------
	public abstract void next();
	
	//-------------------------------------------------------------------
	public List<MUDEntity> getAllCombatants() {
		List<MUDEntity> ret = new ArrayList<MUDEntity>();
		ret.addAll(combatantsSideA);
		ret.addAll(combatantsSideB);
		return ret;
	}
	
	//-------------------------------------------------------------------
	public void storeState(MUDEntity combatant, Object state) {
		perCombatantState.put(combatant, state);
	}
	
	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public <E> E retrieveState(MUDEntity combatant) {
		return (E)perCombatantState.get(combatant);
	}
	
	//-------------------------------------------------------------------
	public List<MUDEntity> getOppositeParty(MUDEntity combatant) {
		if (combatantsSideA.contains(combatant))
			return combatantsSideB;
		if (combatantsSideB.contains(combatant))
			return combatantsSideA;
		return List.of();
	}

	//-------------------------------------------------------------------
	public void setCombatFinished(boolean finished) {
		this.combatFinished=finished;
		getAllCombatants().forEach(e -> e.setState(finished?EntityState.IDLE:EntityState.FIGHTING));
		if (finished)
			getAllCombatants().forEach(e -> e.setCurrentCombat(null));
	}
}
