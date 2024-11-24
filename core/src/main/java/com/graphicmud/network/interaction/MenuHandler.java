package com.graphicmud.network.interaction;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.ClientConnectionListener;

/**
 *
 */
public class MenuHandler implements ClientConnectionListener {

	private final static Logger logger = System.getLogger("network.interaction");

	protected Menu menu;
	protected ClientConnectionListener returnTo;
	protected VisualizedMenu visualMenu;

	//-------------------------------------------------------------------
	public MenuHandler(ClientConnectionListener returnTo, Menu menu) {
//		if (returnTo==null)
//			throw new NullPointerException();
		this.returnTo = returnTo;
		this.menu = menu;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#enter(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void enter(ClientConnection con) {
		visualMenu = con.presentMenu(menu);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedInput(com.graphicmud.network.ClientConnection, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void receivedInput(ClientConnection con, String input) {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "INPUT "+input);
		logger.log(Level.INFO, "Will be recognized as "+visualMenu.getMenuItemForInput(input));

		MenuItem<?> item = visualMenu.getMenuItemForInput(input);
		if (item==null) {
			con.sendShortText(Priority.IMMEDIATE, "Invalid input");
			return;
		}
		
		// Verify that the option is possible
		if (item.getCheckIfSelectable()!=null && !item.getCheckIfSelectable().test( (Object)item.getUserData())) {
			logger.log(Level.WARNING, "User selected invalid option {0}", item.getIdentifier());
			con.sendShortText(Priority.IMMEDIATE, "Invalid choice\r\n");
			return;
		}

		switch (item) {
		case ToggleMenuItem toggle -> {
			logger.log(Level.DEBUG, "Menu item is a Toggle");
			// Is there something to deselect
			Object oldSelection = con.retrieveMenuItemData(menu, item);
			if (oldSelection!=null) {
				// Deselect
				if (toggle.getOnDeselectPerform()!=null) {
					toggle.getOnDeselectPerform().accept(con, toggle, oldSelection);
				}
				con.removeMenuItem(menu, item);
			} else {
				// Select
				if (toggle.getOnSelectPerform()!=null) {
					toggle.getOnSelectPerform().accept(con, toggle, item.getUserData());
				}
			}
			con.presentMenu(menu);
		}
		case ActionMenuItem action -> {
			logger.log(Level.DEBUG, "Menu item is an action");
			if (action.getOnActionPerform()!=null) {
				action.getOnActionPerform().accept(action, item.getUserData());
			}
			if (action.getFinallyGoTo()!=null) {
				logger.log(Level.DEBUG, "User chose option ''{0}'' - use chained handler {1}", item.getLabel(), action.getFinallyGoTo().getClass().getSimpleName());
				con.pushConnectionListener(action.getFinallyGoTo());
			}
		}
		default -> {
			logger.log(Level.WARNING, "Handling {0} menu items not implemented", item.getClass().getSimpleName());
		}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedKeyCode(com.graphicmud.network.ClientConnection, int)
	 */
	@Override
	public void receivedKeyCode(ClientConnection con, int code, List<Integer> arguments) {
		logger.log(Level.INFO, "KEY "+code);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#reenter(com.graphicmud.network.ClientConnection, java.lang.Object)
	 */
	@Override
	public void reenter(ClientConnection con, Object result) {
		logger.log(Level.INFO, "returnFromHandler with {0}", result);
		con.presentMenu(menu);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedFormResponse(com.graphicmud.network.ClientConnection, org.prelle.mud.network.interaction.Form, java.util.Map)
	 */
	@Override
	public void receivedFormResponse(ClientConnection con, Form form, java.util.Map<String, String> answers) {
		logger.log(Level.DEBUG, "receivedFormResponse {0} with {1}", form.getId(), answers);


	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#performScreenRefresh(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void performScreenRefresh(ClientConnection con) {
		logger.log(Level.WARNING, "TODO: performScreenRefresh");
	}

}
