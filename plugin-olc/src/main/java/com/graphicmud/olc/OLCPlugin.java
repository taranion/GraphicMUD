package com.graphicmud.olc;

import com.graphicmud.GraphicMUDPlugin;
import com.graphicmud.commands.CommandSyntaxParser;
import com.graphicmud.olc.commands.EditType;

public class OLCPlugin implements GraphicMUDPlugin {

	//-------------------------------------------------------------------
	public OLCPlugin() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.GraphicMUDPlugin#initialize()
	 */
	@Override
	public void initialize() {
		CommandSyntaxParser.registerLocalizedOptionProvider("EDITTYPE",
                EditType::values,
                EditType::valueOf
				);
		
	}

}
