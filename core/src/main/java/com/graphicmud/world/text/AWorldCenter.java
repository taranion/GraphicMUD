/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.text;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.graphicmud.MUDException;
import com.graphicmud.MUDException.Reason;
import com.graphicmud.world.Location;
import com.graphicmud.world.LocationFactory;
import com.graphicmud.world.Position;
import com.graphicmud.world.PositionFactory;
import com.graphicmud.world.World;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.Zone;
import com.graphicmud.world.impl.WorldImpl;

/**
 *
 */
public abstract class AWorldCenter implements WorldCenter {

	protected final static Logger logger = System.getLogger("mud.world");

	protected Map<Integer,World> knownWorlds;
	private LocationFactory<?> roomFactory;
	private PositionFactory<?> positionFactory;
	
	protected Gson gson;

	//-------------------------------------------------------------------
	public AWorldCenter(LocationFactory<?> roomFactory, PositionFactory<?> positionFactory) {
		knownWorlds = new HashMap<>();
		this.roomFactory  = roomFactory;
		this.positionFactory = positionFactory;
		logger.log(Level.DEBUG, "PositionFactory: "+positionFactory.getClass().getSimpleName());
		logger.log(Level.DEBUG, "LocationFactory: "+roomFactory.getClass().getSimpleName());
		prepareGson();
	}
	
	//-------------------------------------------------------------------
	private void prepareGson() {
		class LocalDateAdapter extends TypeAdapter<LocalDateTime> {
		    @Override
		    public void write(final JsonWriter jsonWriter, final LocalDateTime localDate) throws IOException {
		        if (localDate == null) {
		            jsonWriter.nullValue();
		        } else {
		            jsonWriter.value(localDate.toString());
		        }
		    }

		    @Override
		    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
		        if (jsonReader.peek() == JsonToken.NULL) {
		            jsonReader.nextNull();
		            return null;
		        } else {
		            return LocalDateTime.parse(jsonReader.nextString());
		        }
		    }
		}

		gson = (new GsonBuilder())
				.setPrettyPrinting()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
				.create();
	}

	//---------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public <L extends Location> L createLocation(World w, Zone z) {
		logger.log(Level.TRACE, "createLocation({0}, {1})", w, z);
		return (L) roomFactory.apply(w, z);
	}

	//---------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public <P extends Position> P createPosition() {
		P ret = (P) positionFactory.get();
		logger.log(Level.TRACE, "createPosition() returns a {0}", ret.getClass());
		return ret;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#createWorld(java.lang.String)
	 */
	@Override
	public World createWorld(String name) throws MUDException {
		// Find a free world number
		int number = -1;
		for (int i=1; i<100; i++) {
			Integer key = i;
			if (!knownWorlds.keySet().contains(key)) {
				number = key;
			}
		}
		if (number==-1) {
			logger.log(Level.INFO, "Could not find a free world number");
			throw new MUDException(Reason.NO_FREE_IDENTIFIER);

		}

		WorldImpl world = new WorldImpl(number, name);
		knownWorlds.put(number, world);
		logger.log(Level.INFO, "World {0} ''"+name+"'' created");

		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#pulse()
	 */
	@Override
	public void pulse() {
		logger.log(Level.TRACE, "pulse");
		for (World world : knownWorlds.values()) {
			long before=System.currentTimeMillis();
			world.pulse();
			long diff=System.currentTimeMillis()-before;			
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#tick()
	 */
	@Override
	public void tick() {
		logger.log(Level.INFO, "TICK");
		for (World world : knownWorlds.values()) {
			long before=System.currentTimeMillis();
			world.tick();
			long diff=System.currentTimeMillis()-before;			
		}
	}

}
