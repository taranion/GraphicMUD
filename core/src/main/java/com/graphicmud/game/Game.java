/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.graphicmud.MUD;
import com.graphicmud.game.Vital.VitalType;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.player.RPGConnector;

import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.genericrpg.data.ASkillGroup;
import de.rpgframework.genericrpg.data.ASkillGroupValue;
import de.rpgframework.genericrpg.data.ASkillValue;
import de.rpgframework.genericrpg.data.Lifeform;
import de.rpgframework.genericrpg.items.CarriedItem;
import de.rpgframework.genericrpg.items.PieceOfGear;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
public class Game {

	private final static Logger logger = System.getLogger("mud.game");

	private List<PlayerCharacter> players;
	private Map<VitalType,String[]> vitalNames;
	
	/** 
	 * If skills of characters have groups with values, this method returns them 
	 */
	@Getter @Setter
	private Function<RuleSpecificCharacterObject<?, ?, ?, ?>, List<ASkillGroupValue<?>>> rpgSkillGroupProvider;
	/**
	 * In absence of a rpgSkillGroupProvider, this method returns named categories
	 */
	@Getter @Setter
	private Function<RuleSpecificCharacterObject<?, ?, ?, ?>, List<String>> rpgSkillCategoriesProvider;
	@Getter @Setter
	private BiFunction<RuleSpecificCharacterObject<?, ?, ?, ?>, ASkillGroup, List<? extends ASkillValue>> rpgSkillsByGroupProvider;
	@Getter @Setter
	private BiFunction<RuleSpecificCharacterObject<?, ?, ?, ?>, String, List<? extends ASkillValue>> rpgSkillsByCategoryProvider;

	//-------------------------------------------------------------------
	public Game() {
		players = new ArrayList<>();
		vitalNames = new HashMap<Vital.VitalType, String[]>();
		vitalNames.put(VitalType.VITAL1, new String[]{"Hit Points","HP"});
		vitalNames.put(VitalType.VITAL2, new String[]{"Mana","M"});
		vitalNames.put(VitalType.VITAL3, new String[]{"Movement","Mv"});
	}

	//-------------------------------------------------------------------
	public void addPlayer(PlayerCharacter value) {
		if (!players.contains(value)) {
			players.add(value);
		}
	}

	//-------------------------------------------------------------------
	public void removePlayer(PlayerCharacter value) {
		players.remove(value);
	}

	//-------------------------------------------------------------------
	public List<PlayerCharacter> getPlayers() {
		List<PlayerCharacter> ret = new ArrayList<>(players);
		return ret;
	}

	//-------------------------------------------------------------------
	/**
	 * Create an instance of the given template
	 * @param template
	 * @return
	 */
	public MUDEntity instantiate(MUDEntityTemplate template) {
		if (template==null) {
			logger.log(Level.ERROR, "Received Null pointer to instantiate");
			return null;
		}
		MUDEntity entity = ((template.getType()==EntityType.MOBILE))
				?(new MobileEntity(template))
				:(new ItemEntity(template));
		if (template.getRuleObject()!=null) {
			RPGConnector connector = MUD.getInstance().getRpgConnector();
			if (entity instanceof ItemEntity) {
				CarriedItem<?> ci = connector.instantiateItem( (PieceOfGear)template.getRuleObject());
				entity.setRuleObject( ci);
				((ItemEntity)entity).setRpgReference(ci.getUuid());
			} else {
				entity.setRuleObject( connector.instantiateMobile((Lifeform) template.getRuleObject()));
			}
		}
		entity.prepare(template.getRoom(), template.getZoneDir());
		MUD.getInstance().getRpgConnector().calculate(entity);
		return entity;
	}

	//-------------------------------------------------------------------
	public void setNameVitals(VitalType type, String longName, String shortName) {
		vitalNames.put(type, new String[] {longName, shortName});
	}

	//-------------------------------------------------------------------
	public String[] getVitalName(VitalType index) {
		return vitalNames.get(index);
	}

}

	