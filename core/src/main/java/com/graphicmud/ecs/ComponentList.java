/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.util.ArrayList;

import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.ElementListUnion;

/**
 * 
 */
@SuppressWarnings("serial")
@ElementListUnion({
	@ElementList(entry = "aggro", type = AggroComponent.class),
	@ElementList(entry = "behavior", type = BehaviorComponent.class),
	@ElementList(entry = "dialog", type = Dialog.class),
	@ElementList(entry = "follower", type = FollowerComponent.class),
	@ElementList(entry = "knowledge", type = KnowledgeComponent.class),
	@ElementList(entry = "roamer", type = Roamer.class),
	@ElementList(entry = "talker", type = Talker.class),
	@ElementList(entry = "shopkeeper", type = Shopkeeper.class)
}
)
public class ComponentList extends ArrayList<Component> {

}
