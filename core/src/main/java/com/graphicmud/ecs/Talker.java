/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.ElementList;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.world.Range;

/**
 * Have a few lines to say
 */
public class Talker extends Component {

    private final static Logger logger = System.getLogger(Talker.class.getName());

    private final static String WAIT = "WAIT";

    private static Random random = new Random();

    @Attribute(name = "prob")
    private int probability = 20;
    @Attribute(name = "wait")
    private int waitBetween = 30;
    @ElementList(entry = "line", type = String.class, inline = true)
    private List<String> lines = new ArrayList<String>();

    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.ecs.Component#pulse(com.graphicmud.game.MUDEntity)
     */
    @Override
    public void pulse(MUDEntity entity) {
        Integer needWait = entity.getComponentData(this, WAIT);
        if (needWait != null && needWait > 0) {
            entity.storeComponentData(this, WAIT, needWait - 1);
            return;
        }

        int r = random.nextInt(1000);
        if (r > probability)
            return;
        String line = lines.get(random.nextInt(lines.size()));
        line = line.trim();
        String say = entity.getName() + " " + Localization.getString("command.communication.enum.say.prefixOthers") + ", \"" + line + "\"";
        MUD.getInstance().getWorldCenter().getPlayersInRangeExceptSelf(entity, Range.SURROUNDING).forEach(rcv -> {
            rcv.getConnection().sendShortText(Priority.UNIMPORTANT, say);
        });


        entity.storeComponentData(this, WAIT, waitBetween);

    }

    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.ecs.Component#tick(com.graphicmud.game.MUDEntity)
     */
    @Override
    public void tick(MUDEntity entity) {
        // TODO Auto-generated method stub

    }

}
