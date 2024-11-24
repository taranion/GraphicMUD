/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.player;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.graphicmud.character.EquipmentPosition;
import com.graphicmud.combat.CombatListener;
import com.graphicmud.commands.Command;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.MobileEntity;

import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.genericrpg.data.Lifeform;
import de.rpgframework.genericrpg.items.CarriedItem;
import de.rpgframework.genericrpg.items.PieceOfGear;

/**
 * Implement this class to provide an character object that is specific to
 * an RPG / your rules.
 */
@SuppressWarnings("rawtypes")
public interface RPGConnector<T extends RuleSpecificCharacterObject, L extends Lifeform, G extends PieceOfGear> {

	//-------------------------------------------------------------------
	/**
	 * @return The TTRPG representation of the character
	 */
	public T createRuleBasedCharacterObject(String name);

	//-------------------------------------------------------------------
	/**
	 * Convert a rule-based character to raw data for saving
	 */
	public byte[] serialize(RuleSpecificCharacterObject character) throws IOException;

	//-------------------------------------------------------------------
	/**
	 * Read a rule-based character from raw data
	 */
	public T deserializeCharacter(PlayerCharacter entity, byte[] data) throws IOException;
	
	//-------------------------------------------------------------------
	public Object getByReference(String reference);
	
	//-------------------------------------------------------------------
	public L deserializeLifeform(byte[] data) throws IOException;
	
	//-------------------------------------------------------------------
	public G deserializeItem(byte[] data) throws IOException;
	
	//-------------------------------------------------------------------
	public void calculate(MUDEntity entity);
	
	//-------------------------------------------------------------------
	public CombatListener createNewCombatHandler();
	
	//-------------------------------------------------------------------
	/**
	 * E.g. when a player character had some significant changes, like new gear
	 */
	public void processEvent(MUDEntity receiver, MUDEvent event);

	//-------------------------------------------------------------------
	public String convertCurrencyToString(long money);

	//-------------------------------------------------------------------
	public int getMonetaryValue(MUDEntity entity);

	//-------------------------------------------------------------------
	public int getMonetaryValue(MUDEntityTemplate entity);

	//-------------------------------------------------------------------
	/**
	 * @param container Optional container where the item should be added
	 * @return An error text or NULL if everything was okay
	 */
	public String addToInventory(MobileEntity actor, ItemEntity toAdd, UUID container);

	//-------------------------------------------------------------------
	/**
	 * @param container Optional container where the item should be removed from
	 * @return An error text or NULL if everything was okay
	 */
	public String removeFromInventory(MobileEntity actor, ItemEntity toRemove, UUID container);

	//-------------------------------------------------------------------
	/**
	 * @return An error text or NULL if everything was okay
	 */
	public String equip(MobileEntity actor, ItemEntity itemToEquip, EquipmentPosition position);

	//-------------------------------------------------------------------
	/**
	 * @return An error text or NULL if everything was okay
	 */
	public String unequip(MobileEntity actor, ItemEntity itemToUnequip);

	//-------------------------------------------------------------------
	public CarriedItem<G> instantiateItem(PieceOfGear<?, ?, ?, ?> ruleObject);

	//-------------------------------------------------------------------
	public Object instantiateMobile(Lifeform ruleObject);

	//-------------------------------------------------------------------
	/**
	 * Called when commands of the INTERACT or EXAMINE category have been executed,
	 * so that the RPG may add custom additions 
	 */
	public default void commandHook(Command com, PlayerCharacter character, Map<String, Object> variables) {}
	
}
