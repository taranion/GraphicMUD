/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.handler;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionListener;
import com.graphicmud.network.ClientConnectionState;
import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.interaction.Form;
import com.graphicmud.player.PlayerAccount;
import com.graphicmud.player.PlayerDatabase;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@Builder(setterPrefix = "with")
public class LoginHandler implements ClientConnectionListener, ConnectionVariables {

	static enum State {
		ENTER_LOGIN,
		ENTER_PASSWORD,
		NEW_ACCOUNT_PASSWORD,
		CREATE_VERIFY_PASSWORD,
		LOGGED_IN
	}


	private final static Logger logger = System.getLogger("network.login");

	private final static String FORM_NEW_ACCOUNT = "newAccount";
	private final static String FORM_LOGIN_ACCOUNT = "login";
	private final static String VAR_STATE = "state";

	@Getter
	@Setter
	private List<String> welcomeScreensANSI;
	private PlayerDatabase authenticator;
	@Builder.Default
	private transient ClientConnectionListener chainedHandler = new AccountHandler();

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#initialize(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void enter(ClientConnection con) {
		logger.log(Level.DEBUG, "ENTER enter() from connector {1}", con, con.getConnector().getProtocolIdentifier());
		if (logger.isLoggable(Level.DEBUG)) {
			con.getVariableNames().forEach(key -> logger.log(Level.DEBUG, String.format("%10s = %s", key, con.getVariable(key))));
		}
		if (authenticator==null)
			authenticator=MUD.getInstance().getPlayerDatabase();
		
		// Randomize a welcome screen
		logger.log(Level.WARNING, "TODO: send start screen");
		if (welcomeScreensANSI!=null && !welcomeScreensANSI.isEmpty() && con.getCapabilities().charset==StandardCharsets.UTF_8) {
			Random random = new Random();
			int index = random.nextInt(welcomeScreensANSI.size());
			String screen = welcomeScreensANSI.get(index);
			con.sendScreen(screen);
//			for (String screen : welcomeScreensANSI)
//				con.sendScreen(screen);
		}

		logger.log(Level.INFO, "Soundclient = "+con.getSoundClient());
		logger.log(Level.INFO, "caps = "+con.getProtocolCapabilities());
		if (con.getSoundClient()!=null) {
			con.getSoundClient().playMusic("audio/02 Britannic Lands.mp3", 100, 0, true);
		}

		if (con.getPlayer()!=null) {
			logger.log(Level.INFO, "Already logged in");
			enterState(con, State.LOGGED_IN);
			return;
		}
		
		/*
		 * Some connectors (e.g. Discord, Telegram) come with a unique identification,
		 * others like Telnet require giving login and password
		 */
		String connectID = con.getVariable(VAR_CONNECTION_ID);
		String login     = con.getVariable(VAR_LOGIN);
		if (connectID!=null) {
			String protocol  = con.getConnector().getProtocolIdentifier();
			PlayerAccount account = authenticator.getAccount(protocol, connectID);

			//initializeWithConnectionIdentifier(con, connectID);
			enterState(con, State.LOGGED_IN);
		} else {
			if (login==null) {
				enterState(con, State.ENTER_LOGIN);
			} else {
				enterState(con, State.ENTER_PASSWORD);
			}
		}
		
		logger.log(Level.DEBUG, "LEAVE enter()");
	}

	//-------------------------------------------------------------------
	private void enterState(ClientConnection con, State state) {
		String login     = con.getVariable(VAR_LOGIN);
		con.setListenerVariable(this, VAR_STATE, state);
		switch (state) {
		case ENTER_LOGIN:
			con.sendPrompt(Localization.getString("loginhandler.mess.enter_login", con.getLocale()));
			break;
		case ENTER_PASSWORD:
			con.sendPrompt(Localization.fillString("loginhandler.mess.enter_password", con.getLocale(),login));
			break;
		case NEW_ACCOUNT_PASSWORD:
			con.sendPrompt(Localization.fillString("loginhandler.mess.new_account_password", con.getLocale(),login));
			break;
		case CREATE_VERIFY_PASSWORD:
			con.sendPrompt(Localization.fillString("loginhandler.mess.confirm_password", con.getLocale(),login));
			break;
		case LOGGED_IN:	
			goToNextHandler(con);
		}
	}

//	//-------------------------------------------------------------------
//	private void initializeWithConnectionIdentifier(ClientConnection con, String connectID) {
//		logger.log(Level.INFO, "initializeWithConnectionIdentifier");
//		String login     = con.getVariable(VAR_LOGIN);
//		String protocol  = con.getConnector().getProtocolIdentifier();
//		/*
//		 * If an account can be found using the connector authenticator, use it -
//		 * otherwise create one
//		 */
//		PlayerAccount account = authenticator.getAccount(protocol, connectID);
//		logger.log(Level.DEBUG, "Account = {0}", account);
//		if (account==null) {
//			// We cannot find an existing account for that connection identifier.
//			// Let the user decide if he wants to create a new account or enter
//			// the login for an existing account to connect
//			con.setState(ClientConnectionState.NEW_OR_OLD);
//			enterStateNewOrOld(con);
//			return;
//		} else {
//			con.setPlayer(account);
//			con.sendShortText("Welcome "+account.getName());
//			con.setState(ClientConnectionState.LOGGED_IN);
//			con.setCharacter(MUD.getInstance().getPlayerDatabase().createCharacter(account, "Dummy"));
//			con.pushConnectionListener(chainedHandler);
//		}
//	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#performScreenRefresh(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void performScreenRefresh(ClientConnection con) {
	}

//	//-------------------------------------------------------------------
//	private void enterStateNewOrOld(ClientConnection con) {
//		String username = con.getVariable(ConnectionVariables.VAR_LOGIN);
//		String greeting = String.format("Welcome %s!\r\nYour user is not yet connected with an account in this game.\r\n", username);
//		greeting+="If you already have an existing account, you can log in to use it.\r\n";
//		greeting+="Or you can decide to create a totally new account.\r\n";
//		Menu menu = new Menu(greeting)
//				.add(OPT_NEW_ACCOUNT)
//				.add(OPT_CONNECT_ACCOUNT)
//				;
//
//		MenuHandler handler = new MenuHandler(this, menu);
//		con.pushConnectionListener(handler);
//	}

	//-------------------------------------------------------------------
	/**
	 * Some clients - like TinTin - treat the terminal answers to some
	 * requests like "Device Attributes" as if they are user input.
	 * In such cases we try to remove that
	 */
	private String eventuallyFixInput(String input) {
		int index = input.indexOf("\\e[");
		if (index>0) {
			int next = input.indexOf("c", index);
			// Found response to an escape sequence in PRINTABLE
			logger.log(Level.WARNING, "Client prepended Control Sequence response as login");
			return input.substring(next+1);
		} else if (input.indexOf(";1c")>0) {
			// Found response to an escape sequence in PRINTABLE
			logger.log(Level.WARNING, "Client prepended Control Sequence response as login");
			return input.substring(input.indexOf(";1c")+3);
		}
		return input;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedInput(com.graphicmud.network.ClientConnection, java.lang.String)
	 */
	@Override
	public void receivedInput(ClientConnection con, String input) {
		State state = con.getListenerVariable(this, VAR_STATE);		
		logger.log(Level.DEBUG, "receivedInput {0} in state {1}",input, state);
		String login     = con.getVariable(VAR_LOGIN);
		if (login!=null) login=login.toLowerCase();
		String password1 = con.getVariable(VAR_PASSWORD);

		input = eventuallyFixInput(input);
		input = input.trim();
		if (input.isBlank())
			return;

		switch (state) {
		case ENTER_LOGIN:
			// Treat input as login
			logger.log(Level.DEBUG, "Login entered: {0}", input);
			if (input.length()<5) {
				con.sendPrompt(Localization.getString("loginhandler.mess.invalid_login"));
				enterState(con, state);
				return;
			}
			input = input.toLowerCase();
			con.setVariable(VAR_LOGIN, input);

			if (authenticator!=null) {
				boolean exists = authenticator.doesAccountExist(input);
				if (exists) {
					enterState(con, State.ENTER_PASSWORD);
				} else {
					enterState(con, State.NEW_ACCOUNT_PASSWORD);
				}
			} else {
				goToNextHandler(con);
			}
			break;
		case ENTER_PASSWORD:
			// Treat input as password for login
			logger.log(Level.DEBUG, "Password entered");
			
			PlayerAccount account = authenticator.authenticate(login, input);
			if (account==null) {
				con.sendShortText(Priority.IMMEDIATE, Localization.getString("loginhandler.mess.wrong_password", con.getLocale()));
				con.clearVariable(VAR_LOGIN);
				con.clearVariable(VAR_PASSWORD);
				enterState(con,State.ENTER_LOGIN);
			} else {
				con.setVariable(VAR_PASSWORD, input);
				con.setPlayer(account);
				goToNextHandler(con);
			}
			break;
		case NEW_ACCOUNT_PASSWORD:
			// Treat input as password (1) for new account
			if (input.length()<8) {
				con.sendShortText(Priority.IMMEDIATE, "Password is too short (8 characters minimum)\r\n");
				enterState(con, State.NEW_ACCOUNT_PASSWORD);
				return;
			}
			con.setVariable(VAR_PASSWORD, input);
			enterState(con, State.CREATE_VERIFY_PASSWORD);
			break;
		case CREATE_VERIFY_PASSWORD:
			// Treat input as verification password for new account
			if (password1.equals(input)) {
				// Passwords match - create account
				account = authenticator.createAccount(login, password1);
				con.setPlayer(account);
				goToNextHandler(con);
				break;
			} else {
				// Password mismatch
				con.sendPrompt(Localization.getString("loginhandler.mess.password_mismatch", con.getLocale()));
				enterState(con, State.ENTER_LOGIN);
			}
			con.suppressEcho(false);
			break;
		default:
			logger.log(Level.WARNING, "Unhandled state {0}", con.getState());
		}
	}

	//-------------------------------------------------------------------
	/**
	 * Enter StartMenuHandler
	 */
	private void goToNextHandler(ClientConnection con) {
		con.setState(ClientConnectionState.LOGGED_IN);

		// Rule based character objects are expected to be created in the next handler
		// and will be stored in the AccountHandler
//		logger.log(Level.WARNING, "TODO: connect a correct character");
//		logger.log(Level.INFO, "Calling {0} to create a rule-based character object", MUD.getInstance().getCharacterFactory());
//		RuleSpecificCharacterObject<?, ?, ?, ?> ruleBased = MUD.getInstance().getCharacterFactory().createRuleBasedCharacterObject("Dummy");
//		PlayerCharacter playerChar = MUD.getInstance().getPlayerDatabase().createCharacter(con.getPlayer(), "Dummy");
////		PlayerCharacter playerChar = new PlayerCharacter();
//		playerChar.setRuleObject(ruleBased);
//		con.setCharacter(playerChar);


		if (chainedHandler!=null) {
			logger.log(Level.DEBUG, "Give control to {0}",chainedHandler);
			con.pushConnectionListener(chainedHandler);
		} else {
			logger.log(Level.ERROR, "Missing chained handler for successful logins");
		}
	}

//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.mud.network.ClientConnectionListener#onVariableChange(org.prelle.mud.network.ClientConnection, java.lang.String)
//	 */
//	@Override
//	public void onVariableChange(ClientConnection con, String variable) {
//		if (con.getState()==ClientConnectionState.ENTER_LOGIN && "USERNAME".equals(variable)) {
//			String username = (String)con.getVariable(variable);
//			logger.log(Level.INFO, "Login is known now");
//			if (authenticator!=null) {
//				boolean exists = authenticator.doesAccountExist(username);
//				if (exists) {
//					logger.log(Level.INFO, "Account exists - go to password input");
//					con.setState(ClientConnectionState.UNTRUSTED_ENTER_PASSWORD);
//				}
////			} else {
////				goToNextHandler(con);
//			}
//		}
//	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedKeyCode(com.graphicmud.network.ClientConnection, int)
	 */
	@Override
	public void receivedKeyCode(ClientConnection con, int code, List<Integer> arguments) {
		logger.log(Level.DEBUG, "Received key code {0}",code);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#returnFromHandler(com.graphicmud.network.ClientConnection, com.graphicmud.network.ClientConnectionListener, java.lang.Object)
	 */
	@Override
	public void reenter(ClientConnection con, Object result) {
//		ClientConnectionListener handler = con.popConnectionListener();
		logger.log(Level.DEBUG, "return from handler with {0}", result);

//		switch (con.getState()) {
//		case NEW_OR_OLD:
//			if (result==OPT_NEW_ACCOUNT) {
//				logger.log(Level.DEBUG, "Create new account");
//
//				TextField tfUser = new TextField("login", askLoginString);
//				TextField tfPass = new TextField("pass1", askPasswordString);
//				TextField tfPass2= new TextField("pass2", confirmPWString);
//				Form form = new Form(FORM_NEW_ACCOUNT, "Create an account").add(tfUser, tfPass, tfPass2);
//
//				con.presentForm(form);
////				con.setState(ClientConnectionState.CREATE_LOGIN);
//			} else {
//				logger.log(Level.DEBUG, "Log-in to existing account");
//				TextField tfUser = new TextField("login", askLoginString);
//				TextField tfPass = new TextField("pass", askPasswordString);
//				Form form = new Form(FORM_LOGIN_ACCOUNT, "Log-in for existing account").add(tfUser, tfPass);
//
//				con.presentForm(form);
////				con.setState(ClientConnectionState.ENTER_LOGIN);
//			}
//			break;
//		default:
//			logger.log(Level.WARNING, "Unhandled state "+con.getState());
//		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedFormResponse(com.graphicmud.network.ClientConnection, com.graphicmud.network.interaction.Form, java.util.Map)
	 */
	@Override
	public void receivedFormResponse(ClientConnection con, Form form, java.util.Map<String, String> answers) {
		logger.log(Level.DEBUG, "receivedFormResponse {0} with {1}", form.getId(), answers);

//		switch (con.getState()) {
//		case NEW_OR_OLD:
//			switch (form.getId()) {
//			case FORM_NEW_ACCOUNT:
//				// Requirement 1: Login may not yet exist
//				String login = answers.get("login");
//				if (authenticator.doesAccountExist(login)) {
//					con.sendShortText("Login already exists!");
//					logger.log(Level.WARNING, "Someone tried to recreate account {0}", login);
//					enterStateNewOrOld(con);
//					return;
//				}
//				// Requirement 2: Password must be sufficient
//				String p1 = answers.get("pass1");
//				String p2 = answers.get("pass2");
//				if (p1.length()<8) {
//					con.sendShortText("Password too short!");
//					enterStateNewOrOld(con);
//					return;
//				}
//				// Requirement 3: Passwords must be identical
//				if (!p1.equals(p2)) {
//					con.sendShortText("Passwords do not match!");
//					enterStateNewOrOld(con);
//					return;
//				}
//				// All good - create account
//				PlayerAccount account = authenticator.createAccount(login, p1);
//				con.getVariableNames().forEach(key -> logger.log(Level.DEBUG, String.format("%10s = %s", key, con.getVariable(key))));
//				String proto = con.getConnector().getProtocolIdentifier();
//				account.getNetworkIdentifier().put(proto, con.getVariable(ConnectionVariables.VAR_CONNECTION_ID));
//				authenticator.save(account);
//				logger.log(Level.INFO, "A new account {0} has been created via {1}", login, con.getConnector().getName());
//				con.pushConnectionListener(chainedHandler);
//				con.setPlayer(account);
//				break;
//			}
//			break;
//		default:
//			logger.log(Level.ERROR, "Unprocessed form {0} in state {1}", form.getId(), con.getState());
//		}

	}

}
