/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.handler;

import com.graphicmud.Localization;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionListener;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.interaction.ActionMenuItem;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuHandler;
import com.graphicmud.world.Location;

public class OLCEditRoomHandler extends MenuHandler {

    private final static System.Logger logger = System.getLogger(OLCEditRoomHandler.class.getPackageName());
    private final Location room;


    private ClientConnection con;

    private static final String VAR_STATE = "State";

    enum State {
        MENU,
        TITLE,
        DESCRIPTION,
        EXITS,
        FLAGS
    }

    public OLCEditRoomHandler(ClientConnectionListener returnTo, Location room) {
        super(returnTo, null);
        this.room = room;
    }

    /**
     * @see com.graphicmud.network.interaction.MenuHandler#enter(com.graphicmud.network.ClientConnection)
     */
    @Override
    public void enter(ClientConnection con) {
        this.con = con;
        con.setListenerVariable(this, VAR_STATE, OLCEditRoomHandler.State.MENU);

        menu = new Menu(Localization.fillString("menu.editroom.title", room.getNr()));

        menu.add(ActionMenuItem.builder()
                .identifier("title")
                .label(Localization.getString("menu.editroom.edittitle"))
                .onActionPerform((o, v) -> enterState(OLCEditRoomHandler.State.TITLE))
                .build());
        menu.add(ActionMenuItem.builder()
                .identifier("title")
                .label(Localization.getString("menu.editroom.description"))
                .onActionPerform((o, v) -> enterState(State.DESCRIPTION))
                .build());
        // Exit MUD
        menu.add(ActionMenuItem.builder()
                .identifier("leave")
                .label(Localization.getString("menu.selectchar.back"))
                .emoji("🔙")
                .onActionPerform((a, b) -> con.popConnectionListener(null))
                .build()
        );
        con.presentMenu(menu);
    }

    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.network.ClientConnectionListener#reenter(com.graphicmud.network.ClientConnection, java.lang.Object)
     */
    @Override
    public void reenter(ClientConnection con, Object result) {
        logger.log(System.Logger.Level.DEBUG, "reenter with {0}", result);
        con.presentMenu(menu);
    }

    //-------------------------------------------------------------------
    private void enterState(State newState) {
        OLCEditRoomHandler.State state = con.getListenerVariable(this, VAR_STATE);
        logger.log(System.Logger.Level.DEBUG, "Change from state {0} to {1}", state, newState);
        con.setListenerVariable(this, VAR_STATE, newState);
        switch (newState) {
            case MENU:
                con.presentMenu(menu);
                break;
            case TITLE:
                con.sendShortText(Priority.IMMEDIATE, "Old title: " + room.getRoomComponent().get().getTitle());
                con.sendPrompt("New title: ");
                break;
            case DESCRIPTION:
                con.sendShortText(Priority.IMMEDIATE, "Old description: " + room.getDescription());
                con.sendPrompt("New description: ");
                break;
            default:
                logger.log(System.Logger.Level.WARNING, "Don't know how to handle " + newState);
        }
    }


    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.network.ClientConnectionListener#receivedInput(com.graphicmud.network.ClientConnection, java.lang.String)
     */
    @Override
    public void receivedInput(ClientConnection con, String input) {
        State state = con.getListenerVariable(this, VAR_STATE);
        if (state == OLCEditRoomHandler.State.MENU) {
            super.receivedInput(con, input);
            return;
        }


        logger.log(System.Logger.Level.INFO, "TODO: process " + input + " in state " + state);
        switch (state) {
            case TITLE -> {
                room.getRoomComponent().get().setTitle(input);
                con.sendShortText(Priority.IMMEDIATE, "Okidoki");
                con.sendShortText(Priority.IMMEDIATE, "Aber glaub ja nicht, dass das schon gespeichert wird.");
                enterState(OLCEditRoomHandler.State.MENU);
            }
            case DESCRIPTION -> {
            }
            case EXITS -> {
            }
            case FLAGS -> {
            }
        }

    }

}
