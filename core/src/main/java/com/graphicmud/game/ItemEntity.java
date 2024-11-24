/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import com.graphicmud.character.EquipmentPosition;

import de.rpgframework.genericrpg.items.CarriedItem;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class ItemEntity extends MUDEntity {
	
    protected transient CarriedItem<?> rpgItem;
    @Setter
    @Getter
    protected EquipmentPosition equippedAt;

    //-------------------------------------------------------------------
    /**
     * Empty constructor required for deserialization and initializing lists in this case
     */
    public ItemEntity() {
        super();
        super.type = EntityType.ITEM;
    }

	//-------------------------------------------------------------------
	public ItemEntity(MUDEntityTemplate data) {
		super(data);
        super.type = EntityType.ITEM;
		if (data.getType()!=EntityType.ITEM)
			throw new IllegalArgumentException("ItemEntity requires template of type ITEM");
	}

	//-------------------------------------------------------------------
	public String toString() {
		return "ITEM("+getMudReference()+")";
	}

	//-------------------------------------------------------------------
	public void setRpgItem(CarriedItem<?> value) {
		this.rpgItem = value;
		super.ruleData = value;
	}
	
	//-------------------------------------------------------------------
	public int getCount() {
		if (rpgItem!=null) return rpgItem.getCount();
		return 1;
	}

}
