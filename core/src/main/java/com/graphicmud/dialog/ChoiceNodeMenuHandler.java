package com.graphicmud.dialog;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.graphicmud.Localization;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.interaction.ActionMenuItem;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuHandler;
import com.graphicmud.player.PlayerCharacter;

public class ChoiceNodeMenuHandler extends MenuHandler {
	
	private final static Logger logger = System.getLogger(ChoiceNodeMenuHandler.class.getName());
	
	private ActionNode toPerform;
	private boolean backIsQuit;
	private MUDEntity initiator;
	private List<MUDEntity> talkers;

	//-------------------------------------------------------------------
	public ChoiceNodeMenuHandler(ActionNode toPerform, boolean backIsQuit, MUDEntity initiator, List<MUDEntity> talkers) {
		super(null, null);
		this.toPerform = toPerform;
		this.backIsQuit= backIsQuit;
		this.initiator = initiator;
		this.talkers   = talkers;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#enter(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void enter(ClientConnection con) {
		createAsMenu(con.getLocale(), toPerform, backIsQuit, con);
		perform(toPerform.getIntroActions());
		
		con.presentMenu(menu);
	}
	
	//-------------------------------------------------------------------
	private void createAsMenu(Locale loc, ActionNode toPerform, boolean backIsQuit, ClientConnection con) {
		menu = new Menu(Localization.getString("menu.choicenode.title"));
		for (ChoiceNode choice : toPerform.getChoices()) {
			menu.add( 
					ActionMenuItem.builder()
					.identifier(UUID.randomUUID().toString())
					.label(choice.getOption())
					.onActionPerform( (mi,userData) -> choiceSelected(con, initiator, choice))
					.build() 
					);
		}
		// Return from here
		if (backIsQuit) {
			menu.add(ActionMenuItem.builder().label(Localization.getString("menu.choicenode.leave"))
					.identifier("quit")
					.onActionPerform( (mi,userData) -> leaveDialog(con, toPerform))
					.build());
		} else {
			menu.add(ActionMenuItem.builder().label(Localization.getString("menu.choicenode.back"))
					.identifier("quit")
					.onActionPerform( (mi,userData) -> leaveDialog(con, toPerform))
					.build());
		}			
	}

	//-------------------------------------------------------------------
	private void choiceSelected(ClientConnection con, MUDEntity initiator, ChoiceNode choice) {
		logger.log(Level.INFO, "Choice selected: "+choice);
		
		String youSay = (choice.getYouSay()!=null)?choice.getYouSay():choice.getOption();
		con.sendShortText(Priority.IMMEDIATE, "You say \""+youSay+"\"");
		logger.log(Level.INFO, "You say = "+youSay);
		logger.log(Level.INFO, "  answer= "+choice.getAnswer());
		
		if (choice.getAnswer()!=null) {
			if (choice.getAnswer().getChoices()!=null && !choice.getAnswer().getChoices().isEmpty()) {
				// Choice has choices again
				logger.log(Level.INFO, "1");
				ChoiceNodeMenuHandler next = new ChoiceNodeMenuHandler(choice.getAnswer(), false, initiator, talkers);
				logger.log(Level.INFO, "2");
				con.pushConnectionListener(next);
			} else {
				perform(choice.getAnswer().getIntroActions());
				perform(choice.getAnswer().getOutroActions());
				logger.log(Level.INFO, "3");
				if (choice.isReturnAfterChoice()) {
					logger.log(Level.INFO, "3a");
					con.popConnectionListener(null);					
				} else {
					logger.log(Level.INFO, "3b");
					con.presentMenu(menu);
				}
				logger.log(Level.INFO, "4");
			}
		} else {
			// Strange, a choice without an answer
			logger.log(Level.INFO, "5");
			con.presentMenu(menu);
		}
		
		logger.log(Level.INFO, "6");

	}
	
	//-------------------------------------------------------------------
	private void perform(List<DialogAction> actions) {
		for (DialogAction action : actions) {
			int speaker = (action.getSpeaker()<=talkers.size())?action.getSpeaker():0;
			MUDEntity you = talkers.get(speaker);
			switch (action) {
			case SayDialogAction say -> {
				String line = you.getName()+" says \""+say.getText()+"\"";
				((PlayerCharacter)initiator).getConnection().sendShortText(Priority.IMMEDIATE, line);
			}
			case EmoteDialogAction emote -> {
				String line = you.getName()+" "+emote.getText();
				((PlayerCharacter)initiator).getConnection().sendShortText(Priority.IMMEDIATE, line);
			}
			default -> {
				logger.log(Level.ERROR, "Don't know how to perform {0} action",action.getClass().getSimpleName());
			}
			}
		}

	}
	
	//-------------------------------------------------------------------
	private void leaveDialog(ClientConnection con, ActionNode tree) {
		logger.log(Level.INFO, "Leave dialog");
		
		perform(tree.getOutroActions());
		con.popConnectionListener(null);
	}

}
