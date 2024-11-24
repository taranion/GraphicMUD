module graphicmud.core {
	exports com.graphicmud.character;
	exports com.graphicmud.commands;
	exports com.graphicmud.dialog;
	exports com.graphicmud.ecs;
	exports com.graphicmud.game;
	exports com.graphicmud.handler;
	exports com.graphicmud.network;
	exports com.graphicmud.network.interaction;
	exports com.graphicmud.map;
	exports com.graphicmud.media;
	exports com.graphicmud.player;
	exports com.graphicmud.symbol;
	exports com.graphicmud.web;
	exports com.graphicmud.world;
	exports com.graphicmud.world.text;
	exports com.graphicmud.world.tile;
	exports com.graphicmud;

	opens com.graphicmud.map to simple.persist;
	opens com.graphicmud.symbol to simple.persist;
	opens com.graphicmud.dialog to simple.persist;
	opens com.graphicmud;

	requires lombok;
	requires simple.persist;
	requires java.xml;
	requires jdk.httpserver;
	requires de.rpgframework.rules;
	requires org.prelle.mudansi;
	requires com.google.gson;
    requires org.prelle.libansi;
	requires java.desktop;
	requires java.scripting;

    uses com.graphicmud.map.SixelEncoder;
}