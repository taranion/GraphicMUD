module graphicmud.tiles.jfx {
	exports com.graphicmud.symbol.swing.sixel;
	exports com.graphicmud.symbol.swing;

	provides com.graphicmud.map.SixelEncoder with com.graphicmud.symbol.swing.sixel.HQSixelEncoder;

	requires java.desktop;
	requires lombok;
	requires graphicmud.core;
}