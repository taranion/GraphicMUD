package com.graphicmud.telnet;

import java.util.ArrayList;
import java.util.List;

public class CharLoginDefault {

	private List<String> type = new ArrayList<>();

	public CharLoginDefault() {
		// TODO Auto-generated constructor stub
	}

	public void add(String value) { type.add(value); }
	public List<String> getTypes() { return type; }

}
