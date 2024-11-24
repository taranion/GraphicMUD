/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import java.util.Locale;
import java.util.function.Predicate;

import com.graphicmud.Localization;
import com.graphicmud.commands.impl.communication.Communication;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.Range;

public enum CommunicationChannel {
    SAY {
//        @Override
//        public Predicate<PlayerCharacter> filterPlayers(PlayerCharacter np) {
//            return p -> !p.equals(np);
//        }

        @Override
        public Range getRangeForChannel() {
            return Range.SURROUNDING;
        }
    },
    SHOUT {
    },
    GOSSIP{
    },
    NEWBIE{
    },
    PRAY{
    },
    GODSAY{
    },
    GROUPSAY{
    },
    GUILDSAY{
    },
    CLASSSAY{
    },
    AUCTION{

    };
    
	public String getName(Locale loc) {
		return Localization.getString("enum.channel."+this.name().toLowerCase(), loc);
	}
    
	public static String[] values(Locale loc) {
		String[] translated = new String[CommunicationChannel.values().length];
		for (int i=0; i<translated.length; i++) {
			translated[i]=CommunicationChannel.values()[i].getName(loc);
		}
		return translated;
	}
    
	public static Object valueOf(Locale loc, String val) {
		for (CommunicationChannel dir : CommunicationChannel.values()) {
			if (dir.getName(loc).equalsIgnoreCase(val))
				return dir;
		}
		return null;
	}

    public boolean isAllowedToUseChannel() {
        return true;
    }
    public Predicate<MUDEntity> filterPlayers(MUDEntity np) {
        return p -> !p.equals(np);
    }

    public String getName() {
        return new Communication().getString(".enum." + this.name());
    }
    
    public Range getRangeForChannel() {
        return Range.EVERYWHERE;
    }
}
