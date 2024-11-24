package com.graphicmud.socials;

import com.graphicmud.GraphicMUDPlugin;
import com.graphicmud.commands.CommandSyntaxParser;
import com.graphicmud.socials.commands.SocialType;

/**
 * 
 */
public class SocialsPlugin implements GraphicMUDPlugin {

	//-------------------------------------------------------------------
	/**
	 */
	public SocialsPlugin() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.GraphicMUDPlugin#initialize()
	 */
	@Override
	public void initialize() {
		CommandSyntaxParser.registerLocalizedOptionProvider("SOCIALTYPE",
                SocialType::values,
                SocialType::valueOf
		);
		
	}

}
