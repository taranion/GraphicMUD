/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.graphicmud.Localization;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;

import lombok.Data;

public class EquippedGear extends ArrayList<ItemEntity> {
	
	private static Map<EquipmentPosition, Integer> SLOTS = Map.of(
			EquipmentPosition.HEAD, 1,
			EquipmentPosition.FINGER, 4,
			EquipmentPosition.BODY, 1,
			EquipmentPosition.BACK, 1,
			EquipmentPosition.LEGS, 1,
			EquipmentPosition.WEAPON_PRIMARY, 1,
			EquipmentPosition.WEAPON_SECONDARY, 1
			);

	public List<EquipmentPosition> getSlots() {
		return List.of(
				EquipmentPosition.HEAD, 
				EquipmentPosition.FINGER, 
				EquipmentPosition.BODY,
				EquipmentPosition.BACK, 
				EquipmentPosition.LEGS, 
				EquipmentPosition.WEAPON_PRIMARY, 
				EquipmentPosition.WEAPON_SECONDARY);
	}
	
	private Map<Integer, MUDEntity> createSlotMap(int x) {
        Map<Integer, MUDEntity> map = new HashMap<>();
        IntStream.rangeClosed(1, x).forEach(i -> map.put(i, null));
        return map;
    }

    public static String getSlotName(EquipmentPosition position, int i) {
        String prefix = "enum.equipmentposition.";
        String enumName = position.name().toLowerCase();
        return Localization.getString(prefix + enumName + "_" + i);
    }

    //-------------------------------------------------------------------
    public List<ItemEntity> getItemsAt(EquipmentPosition position) {
    	List<ItemEntity> ret = new ArrayList<>();
        ret.addAll( this.stream().filter(item -> item.getEquippedAt()==position).toList() );
        int emptySlots = SLOTS.containsKey(position)?(SLOTS.get(position)-ret.size()):0;
        // Fill with empty slots where needed
        if (emptySlots>0) {
        	for (int i=0; i<emptySlots; i++) 
        		ret.add(null);
        }
        return ret;
    }

    //-------------------------------------------------------------------
    private List<ItemEntity> getRawItemsAt(EquipmentPosition position) {
    	return this.stream().filter(item -> item.getEquippedAt()==position).toList();
    }

    //-------------------------------------------------------------------
    public boolean isSlotUsed(EquipmentPosition value) {
    	return !getRawItemsAt(value).isEmpty();
    }

    //-------------------------------------------------------------------
    public boolean isSlotFree(EquipmentPosition value) {
    	return getRawItemsAt(value).isEmpty();
    }

    //-------------------------------------------------------------------
    public int getNumEmptySlots(EquipmentPosition position) {
        int maxSlots = SLOTS.containsKey(position)?SLOTS.get(position):0;
    	List<ItemEntity> ret = new ArrayList<>();
        ret.addAll( this.stream().filter(item -> item.getEquippedAt()==position).toList() );
        return maxSlots - ret.size();
    }

//    //-------------------------------------------------------------------
//    public WearResult wear(ItemEntity itemToWear) {
//        EquipmentPosition equipmentSlot = itemToWear.getTemplate().getEquipmentSlot();
//        if (equipmentSlot == EquipmentPosition.NONE) {
//            return new WearResult(false,equipmentSlot, EQUIP_CANNOT_WEAR, itemToWear.getName());
//        }
//        // Should not already be added
//        if (this.contains(itemToWear)) {
//            itemToWear.setEquippedAt(equipmentSlot);
//            return new WearResult(true,equipmentSlot, EQUIP_ALREADY_EQUIPPED, itemToWear.getName());
//        }
//        // Check if there is a free slot
//        int free = getNumEmptySlots(equipmentSlot);
//        if (free<1)
//            return new WearResult(false,equipmentSlot, EQUIP_SLOT_USED, itemToWear.getName());
//        // Enough free slots
//        itemToWear.setEquippedAt(equipmentSlot);
//        this.add(itemToWear);
//        int slotNum = (int) this.stream().filter(item -> item.getEquippedAt()==equipmentSlot).count();
//        return new WearResult(true,equipmentSlot, "equippedgear.mess." + equipmentSlot.name().toLowerCase(), itemToWear.getName(), getSlotName(equipmentSlot, slotNum));
//    }

    //-------------------------------------------------------------------
    public RemoveResult unequip(String itemName) {
        int numberInPos = 0;
        for (ItemEntity item : this) {
        	if (item.reactsOnKeyword(itemName)) {
        		EquipmentPosition pos = item.getEquippedAt();
                numberInPos = (int) stream().filter(i -> i.getEquippedAt()==pos).count();
                item.setEquippedAt(null);
                this.remove(item);
//                numberInPos = innerMapEntry.getKey();
                return new RemoveResult(true, Localization.fillString("equippedgear.mess.remove", item.getName(), getSlotName(pos, numberInPos)), item);
        	}
        }
        
//        for (Map.Entry<EquipmentPosition, Map<Integer, MUDEntity>> entry : this.entrySet()) {
//            Map<Integer, MUDEntity> integerMUDEntityMap = entry.getValue();
//            for (Map.Entry<Integer, MUDEntity> innerMapEntry : integerMUDEntityMap.entrySet()) {
//                MUDEntity item = innerMapEntry.getValue();
//                if (item != null && item.reactsOnKeyword(itemName)) {
//                    itemToRemove = item;
//                    pos = entry.getKey();
//                    numberInPos = innerMapEntry.getKey();
//                    integerMUDEntityMap.put(numberInPos, null);
//                    return new RemoveResult(true, Localization.fillString("equippedgear.mess.remove", itemToRemove.getName(), getSlotName(pos, numberInPos)), itemToRemove);
//                }
//            }
//        }
        return new RemoveResult(false, Localization.fillString("equippedgear.mess.notequipped", itemName), null);
    }

    @Data
    public static class WearResult {
        private final boolean ok;
        private final EquipmentPosition position;
        private final String i18nKey;
        private final Object[] data;
        public WearResult(boolean ok, EquipmentPosition pos, String i18nKey, Object...data) {
        	this.ok = ok;
        	this.position = pos;
        	this.i18nKey = i18nKey;
        	this.data    = data;
        }
    }

    @Data
    public static class RemoveResult {
        private final boolean ok;
        private final String msg;
        private final ItemEntity itemToRemove;
    }
}
