/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.socials.commands;

import java.util.Locale;

import com.graphicmud.Localization;

public enum SocialType {
    BOW,
    BLOW,
    CURTSEY,
    COUGH,
    GIGGLE,
    GROWL,
    LAUGH,
    POINT,
    SMILE,
    WAVE,
	YAWN;

    public String getName(Locale loc) {
        return Localization.getString("command.social.enum."+this.name().toLowerCase(), loc);
    }
    public static String[] values(Locale loc) {
        String[] translated = new String[SocialType.values().length];
        for (int i=0; i<translated.length; i++) {
            translated[i]= SocialType.values()[i].getName(loc);
        }
        return translated;
    }
    public static Object valueOf(Locale loc, String val) {
        for (SocialType dir : SocialType.values()) {
            if (dir.getName(loc).equalsIgnoreCase(val))
                return dir;
        }
        return null;
    }
}
