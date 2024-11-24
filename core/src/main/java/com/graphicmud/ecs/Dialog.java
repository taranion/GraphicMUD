/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Persister;

import com.graphicmud.dialog.ChoiceNodeMenuHandler;
import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.MUDEvent.Type;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;

import lombok.Setter;

/**
 * Holds full conversation with player
 */
public class Dialog extends Component {

	private final static Logger logger = System.getLogger(Dialog.class.getName());
	private final static Persister persister = new Persister();

	@Attribute(name="file")
	private String filename;

	@Setter
	private transient Path dialogFile;

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.ecs.Component#prepare(java.nio.file.Path)
	 */
	@Override
	public void prepare(Path zoneDir) {
		dialogFile = zoneDir.resolve(filename);
		if (!Files.exists(dialogFile)) {
			logger.log(Level.ERROR, "Dialog file {0} is missing", dialogFile);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.ecs.Component#handleEvent(com.graphicmud.game.MUDEvent)
	 */
	@Override
	public void handleEvent(MUDEntity you, MUDEvent event) {
		logger.log(Level.DEBUG, "RCV event "+event);
		// Only react to TALK
		if (event.getType()!=Type.TALK_REQUEST) return;

		MUDEntity initiator = event.getSource();
		Map<String, String> res = new HashMap<String,String>();
		res.put("player", initiator.getName());
		res.put("name", you.getName());

		if (dialogFile==null) {
			logger.log(Level.ERROR, "Dialog file ''{0}'' not loaded for {1}", filename, you.getName());
			return;
		}

		ClientConnection con = ((PlayerCharacter)initiator).getConnection();
		DialogueTree tree = null;
		try {
			tree = persister.read(DialogueTree.class, new FileReader(dialogFile.toFile()));
		} catch (IOException e) {
			logger.log(Level.ERROR, "Error loading dialog tree "+dialogFile,e);
			((PlayerCharacter)initiator).getConnection().sendShortText(Priority.IMMEDIATE, "ERROR! Dialog is broken.");
			return;
		}

		((PlayerCharacter)initiator).getConnection().setDoNotDisturb(true);

		logger.log(Level.WARNING, "Calling presentDialog");
		con.presentDialog(tree, you.getTemplate().getImageFilename());
		logger.log(Level.WARNING, "Now start ChoiceNodeMenuHandler");
//		ChoiceNodeMenuHandler handler = new ChoiceNodeMenuHandler(tree, true, initiator, List.of(you));
//		con.pushConnectionListener(handler);
	}

}
