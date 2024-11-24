/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.player;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;

/**
 * 
 */
public class MUDEntityAdapterFactory implements TypeAdapterFactory {
	
	public static final MUDEntityAdapterFactory INSTANCE = new MUDEntityAdapterFactory();

	//-------------------------------------------------------------------
	public MUDEntityAdapterFactory() {}

	//-------------------------------------------------------------------
	/**
	 * @see com.google.gson.TypeAdapterFactory#create(com.google.gson.Gson, com.google.gson.reflect.TypeToken)
	 */
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		// Only handle IndexedPojo and subclasses
	    if (!MUDEntity.class.isAssignableFrom(type.getRawType())) {
	      return null;
	    }

	    // Get the default adapter as delegate
	    // Cast is safe due to `type` check at method start
	    @SuppressWarnings("unchecked")
	    TypeAdapter<MUDEntity> genericDelegate = (TypeAdapter<MUDEntity>) gson.getDelegateAdapter(this, type);
	    TypeAdapter<ItemEntity> itemEntityDelegate = (TypeAdapter<ItemEntity>) gson.getDelegateAdapter(this, TypeToken.get(ItemEntity.class));
	    TypeAdapter<PlayerCharacter> pcDelegate = (TypeAdapter<PlayerCharacter>) gson.getDelegateAdapter(this, TypeToken.get(PlayerCharacter.class));
	    TypeAdapter<MobileEntity> mobileEntityDelegate = (TypeAdapter<MobileEntity>) gson.getDelegateAdapter(this, TypeToken.get(MobileEntity.class));
	    // Cast is safe because `T` is IndexedPojo or subclass (due to `type` check at method start)
	    @SuppressWarnings("unchecked")
	    TypeAdapter<T> adapter = (TypeAdapter<T>) new TypeAdapter<MUDEntity>() {
	      @Override
	      public void write(JsonWriter out, MUDEntity value) throws IOException {
	    	  genericDelegate.write(out, value);
	      }

	      @Override
	      public MUDEntity read(JsonReader in) throws IOException {
	        // Read JsonObject from JsonReader to be able to pass it to `IndexedPojo.setKeySet(...)`
	        // afterwards
	        // Note: JsonParser automatically parses in lenient mode, which cannot be disabled
	        // Note: Might have to add handling for JSON null values
	        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
	        JsonElement type = jsonObject.get("type");
	        if (type!=null &&EntityType.ITEM.name().equals(type.getAsString())) {
	        	return itemEntityDelegate.fromJsonTree(jsonObject);
	        } else if (type!=null &&EntityType.MOBILE.name().equals(type.getAsString())) {
	        	return mobileEntityDelegate.fromJsonTree(jsonObject);
	        } else if (type!=null &&EntityType.PLAYER.name().equals(type.getAsString())) {
	        	return pcDelegate.fromJsonTree(jsonObject);
	        } else
	        	return genericDelegate.fromJsonTree(jsonObject);
	      }	    
	    };

	    return adapter;
	 }

}
