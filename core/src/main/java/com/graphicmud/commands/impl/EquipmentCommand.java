/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.Equipment;
import com.graphicmud.action.cooked.Inventory;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.character.EquipmentPosition;
import com.graphicmud.character.EquippedGear;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.TableColumn;
import com.graphicmud.player.PlayerCharacter;

import lombok.Data;

public class EquipmentCommand extends ACommand {
	
    public EquipmentCommand() {
        super(CommandGroup.INTERACT, "equipment", Localization.getI18N());
    }

    //-------------------------------------------------------------------
    /**
     * @see com.graphicmud.commands.Command#execute(com.graphicmud.game.MUDEntity, java.util.Map)
     */
    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        if (!(np instanceof MobileEntity mobile)) {
            return;
        }
        String itemName = (String) params.get("item");
        if (itemName != null && !itemName.isBlank()) {
            equip(mobile, itemName);
        } else {
            showEquipment((PlayerCharacter) np, mobile);
        }
    }

    //-------------------------------------------------------------------
    private static void equip(MobileEntity mobile, String itemName) {
//        List<MUDEntity> items = mobile.getAllFromInventory(itemName);
//        if (items.isEmpty()) {
//            mobile.sendShortText(ClientConnection.Priority.IMMEDIATE, Localization.fillString("command.wear.mess.nosuchitem", itemName));
//            return ;
//        }
//        ItemEntity itemToWear = (ItemEntity) items.getFirst();
        
        Context context = new Context();
        context.put(ParameterType.TARGET_NAME, itemName);
        CookedActionProcessor.perform(Equipment::equip, mobile, context);
//        EquippedGear bodyMap = mobile.getEquippedGear();
//        EquippedGear.WearResult wear = bodyMap.wear(itemToWear);
//        if (wear.isOk()) {
//        	// Let the RPG try the same
//           String error = MUD.getInstance().getRpgConnector().equip(mobile, (ItemEntity) itemToWear, wear.getPosition());
//           if (error!=null) {
//        	   logger.log(Level.WARNING, "MUD was fine, but RPG rejected equipping: "+error);
//        	   bodyMap.unequip(itemName);
//        	   mobile.sendShortText(ClientConnection.Priority.IMMEDIATE, error);
//        	   return true;
//           }
//           mobile.removeFromInventory(itemToWear);
//           mobile.sendShortText(ClientConnection.Priority.IMMEDIATE, wear.getI18nKey(), wear.getData());
//            //  sendOther(entity, itemname);
//         } else {
//            mobile.sendShortText(ClientConnection.Priority.IMMEDIATE, wear.getMsg());
//        }
//        return false;
    }

    //-------------------------------------------------------------------
   private void showEquipment(PlayerCharacter np, MobileEntity mobile) {
        List<EqLine> eq = getEqList(mobile);
        Table<EqLine> table = new Table<>();
        table.setTitle("Your equipment");
        table.addColumn(new TableColumn<EqLine, String>("Position")
                .setValueProvider((t, item) -> item.getPos())
        );
        table.addColumn(new TableColumn<EqLine, String>("Name")
                .setValueProvider((t, item) -> item.getItem())
                .setMaxWidth(55)
        );
        table.setData(eq);
        np.getConnection().presentTable(table);
    }

   //-------------------------------------------------------------------
   private List<EqLine> getEqList(MobileEntity mobile) {
        List<EqLine> result = new ArrayList<>();
        EquippedGear equippedGear = mobile.getEquippedGear();
        for (EquipmentPosition equipmentPosition : equippedGear.getSlots()) {
        	int i=0;
        	for (ItemEntity item : equippedGear.getItemsAt(equipmentPosition)) {
                String name = item != null ? item.getName() : " ";                
                result.add(new EqLine(equippedGear.getSlotName(equipmentPosition, ++i), name));
        	}
        }
        
//        for (EquipmentPosition equipmentPosition : equippedGear.keySet()) {
//            Map<Integer, MUDEntity> internalMap = equippedGear.get(equipmentPosition);
//            for (Integer i : internalMap.keySet()) {
//                MUDEntity mudEntity = internalMap.get(i);
//                String name = mudEntity != null ? mudEntity.getName() : " ";                
//                result.add(new EqLine(equippedGear.getSlotName(equipmentPosition, i), name));
//            }
//        }
        return result;
    }

    @Data
    private static class EqLine {
        private final String pos;
        private final String item;
    }
}
