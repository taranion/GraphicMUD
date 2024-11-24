/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.player;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.graphicmud.Identifier;
import com.graphicmud.MUD;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.game.Vital;
import com.graphicmud.game.Vital.VitalType;
import com.graphicmud.world.WorldCenter;

import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.genericrpg.items.CarriedItem;
import de.rpgframework.genericrpg.items.PieceOfGear;
import lombok.Builder;

/**
 *
 */
public class FilePlayerDatabase implements PlayerDatabase {

	private final static Logger logger = System.getLogger("mud.player");

	private Path location;
	private Path charDir;
	private Map<String, PlayerAccount> database;
	private Gson gson;

	//-------------------------------------------------------------------
	@Builder
	public FilePlayerDatabase(Path accountFileLocation, Path characterDirectory) {
		this.location = accountFileLocation;
		this.charDir  = characterDirectory;
		database = new HashMap<String, PlayerAccount>();
		prepareGson();

		if (!Files.exists(accountFileLocation)) {
			try {
				Files.createFile(accountFileLocation);
			} catch (IOException e) {
				logger.log(Level.ERROR, "Failed creating player database at "+accountFileLocation.toAbsolutePath(),e);
			}
		}

		if (!Files.exists(charDir)) {
			try {
				Files.createDirectories( charDir );
			} catch (IOException e) {
				logger.log(Level.ERROR, "Failed creating player database at "+accountFileLocation.toAbsolutePath(),e);
			}
		}

		readDatabase();
		logger.log(Level.INFO, "PlayerDatabase READY");
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
		class IdentifierAdapter extends TypeAdapter<Identifier> {
			@Override
			public void write(JsonWriter out, Identifier value) throws IOException {
		        if (value == null) { out.nullValue(); } else { out.value(value.toString()); }
			}
			@Override
			public Identifier read(JsonReader in) throws IOException {
		        if (in.peek() == JsonToken.NULL) {
		        	in.nextNull();
		            return null;
		        } else {
		            return new Identifier(in.nextString());
		        }
			}
		}

		gson = (new GsonBuilder())
				.setPrettyPrinting()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
				.registerTypeAdapter(Identifier.class, new IdentifierAdapter())
				.registerTypeAdapterFactory(MUDEntityAdapterFactory.INSTANCE)
//				.registerTypeAdapter(EquippedGear.class, new JsonDeserializer<EquippedGear>() {
//		            @Override
//		            public EquippedGear deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//		                Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
//		                return new EquippedGear(new Gson().fromJson(json, mapType));
//		            }
//		        })
				.create();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.player.PlayerDatabase#doesAccountExist(java.lang.String)
	 */
	@Override
	public boolean doesAccountExist(String login) {
		return database.containsKey(login);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.player.PlayerDatabase#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public PlayerAccount authenticate(String login, String secret) {
		PlayerAccount account = database.get(login);
		if (account==null) {
			logger.log(Level.WARNING, "Asked to authenticate a non-existing player {0}", login);
			return null;
		}

		return (account.getSecret().equals(secret))?account:null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.player.PlayerDatabase#getAccount(java.lang.String, java.lang.String)
	 */
	@Override
	public PlayerAccount getAccount(String protcol, String identifier) {
		for (PlayerAccount account : database.values()) {
			if (account.getNetworkIdentifier().containsKey(protcol) && account.getNetworkIdentifier().get(protcol).equals(identifier)) {
				return account;
			}
		}
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.player.PlayerDatabase#createAccount(java.lang.String)
	 */
	@Override
	public PlayerAccount createAccount(String login, String secret) {
		PlayerAccount created = new PlayerAccount();
		created.setName(login);
		created.setSecret(secret);
		created.setCreationDate(LocalDateTime.now());
		created.setLastLogin(created.getCreationDate());

		database.put(login, created);
		writeDatabase();
		return created;
	}

	//-------------------------------------------------------------------
	private void writeDatabase() {
		String backupName = location.getFileName()+".bak";
		Path backup = location.toAbsolutePath().getParent().resolve(backupName);
		// Delete old backup, if it exists
		try {
			Files.deleteIfExists(backup);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed deleting old player database backup",e);
		}

		// Copy current database to old one
		try {
			Files.move(location, backup);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed moving current player DB to backup",e);
		}

		// Write new
		List<PlayerAccount> toWrite = new ArrayList<PlayerAccount>(database.values());
		Collections.sort(toWrite, (o1,o2) -> Collator.getInstance().compare(o1.getName(), o2.getName()));
		String json = gson.toJson(toWrite);
		try {
			Files.write(location, json.getBytes(Charset.defaultCharset()));
			logger.log(Level.DEBUG, "Successfully wrote player database");
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed writing current player DB to disk",e);
		}
	}

	//-------------------------------------------------------------------
	private void readDatabase() {
		logger.log(Level.DEBUG, "Read player accounts from {0}", location.toAbsolutePath());
		try {
			byte[] buf = Files.readAllBytes(location);
			String json = new String(buf, Charset.defaultCharset());
			List<PlayerAccount> toRead = new ArrayList<PlayerAccount>();
			TypeToken<List<PlayerAccount>> typeToken = new TypeToken<List<PlayerAccount>>() {};
			toRead = gson.fromJson(json, typeToken.getType());
			if (toRead==null) {
				logger.log(Level.WARNING, "Player database is empty!");
				return;
			}
			logger.log(Level.WARNING, "Successfully read {0} accounts from database", toRead.size());

			database.clear();
			toRead.forEach(acc -> database.put(acc.getName(), acc));
		} catch (Exception e) {
			logger.log(Level.ERROR, "Failed reading current player DB from disk",e);
		}
	}

	@Override
	public void save(PlayerAccount account) {
		writeDatabase();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.player.PlayerDatabase#doesCharacterExist(java.lang.String)
	 */
	@Override
	public boolean doesCharacterExist(String name) {
		Path charFile = charDir.resolve(name+".json");
		return Files.exists(charFile);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.player.PlayerDatabase#createCharacter(com.graphicmud.player.PlayerAccount, java.lang.String)
	 */
	@Override
	public PlayerCharacter createCharacter(PlayerAccount account, String name) {
		logger.log(Level.INFO, "ENTER: createCharacter");
		PlayerCharacter model = new PlayerCharacter(account.getName(), name);
		// Save a JSON
		Path charFile = charDir.resolve(name+".json").toAbsolutePath();
		String json = gson.toJson(model);
		try {
			Files.write(charFile, json.getBytes(Charset.defaultCharset()), StandardOpenOption.CREATE);
			logger.log(Level.DEBUG, "Successfully wrote character {0}", charFile);
			
			account.getCharacters().add(name);
			save(account);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed writing character to disk",e);
		}
		return model;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.player.PlayerDatabase#saveCharacter(com.graphicmud.player.PlayerAccount, com.graphicmud.player.PlayerCharacter)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void saveCharacter(PlayerAccount account, PlayerCharacter character) {
		logger.log(Level.INFO, "ENTER: saveCharacter");
		// Save a JSON
		Path charFile = charDir.resolve(character.getName()+".json").toAbsolutePath();
		String json = gson.toJson(character);
		try {
			Files.write(charFile, json.getBytes(Charset.defaultCharset()));
			logger.log(Level.DEBUG, "Successfully wrote character {0}", charFile);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed writing character to disk",e);
		}
		// Save rule specific
		RuleSpecificCharacterObject model = character.getRuleObject();
		charFile = charDir.resolve(character.getName()+".cha").toAbsolutePath();
		try {
			byte[] rawData = MUD.getInstance().getRpgConnector().serialize(model);
			Files.write(charFile, rawData);
			logger.log(Level.DEBUG, "Successfully wrote rule specific character {0}", charFile);
		} catch (Exception e) {
			logger.log(Level.ERROR, "Failed writing character to disk",e);
		}
	}

	@Override
	public PlayerCharacter loadCharacter(PlayerAccount account, PlayerCharacter character) {
		logger.log(Level.INFO, "ENTER: loadCharacter");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCharacter(PlayerAccount account, PlayerCharacter character) {
		logger.log(Level.INFO, "ENTER: deleteCharacter");
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	private static MUDEntityTemplate createTemporaryTemplate(CarriedItem ci) {
		MUDEntityTemplate template = new MUDEntityTemplate();
		template.setRuleObject(ci.getResolved());
		template.setRuleDataReference("gear:"+ci.getResolved().getId());
		template.setName(ci.getResolved().getName());
		template.setType(EntityType.ITEM);
		template.setId(new Identifier(ci.getResolved().getId()));
		return template;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.player.PlayerDatabase#getCharacters(com.graphicmud.player.PlayerAccount)
	 */
	@Override
	public List<PlayerCharacter> getCharacters(PlayerAccount account) {
		logger.log(Level.INFO, "ENTER: getCharacters");
		List<PlayerCharacter> ret = new ArrayList<PlayerCharacter>();
		Path charFile = null;
		for (String charName : account.getCharacters()) {
			charFile = charDir.resolve(charName+".json");
			logger.log(Level.INFO, "Loading {0}",charFile);
			try {
				PlayerCharacter charData = gson.fromJson(new String(Files.readAllBytes(charFile), StandardCharsets.UTF_8), PlayerCharacter.class);
				sanityCheck(charData);
				resolve(charData);
				ret.add(charData);
				// Now load ruledata
				Path ruleData = charDir.resolve(charName+".cha");
				RuleSpecificCharacterObject<?,?,?,?> rule = MUD.getInstance().getRpgConnector()
						.deserializeCharacter(charData,Files.readAllBytes(ruleData));
				charData.setRuleObject(rule);
				logger.log(Level.DEBUG, "Successfully loaded character {0} from {1}", charName, account.getName());
				// Connect rule items with MUD entities
				for (CarriedItem ci : rule.getCarriedItems()) {
					logger.log(Level.WARNING, "To convert: "+ci);
					MUDEntity exist = 	(MUDEntity) charData.getFromInventory(ci.getUuid());
					if (exist!=null) {
						logger.log(Level.WARNING, "Connect existing MUDEntity {0} with carried item", ci.getUuid());
						if (exist.getTemplate()==null) {
							exist.setTemplate(createTemporaryTemplate(ci));
						}
						exist.setRuleObject(ci);
					} else {
//						logger.log(Level.WARNING, "No MUDEntity found for UUID "+ci.getUuid()+" in inventory");
						MUDEntityTemplate template = createTemporaryTemplate(ci);
						exist = new ItemEntity(template);
						exist.setRuleObject(ci);
						exist.setRpgReference(ci.getUuid());
						charData.addToInventory(exist);
					}
				}
			} catch (Exception e) {
				try {
					logger.log(Level.ERROR, "Error loading "+charFile.toRealPath(),e);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return ret;
	}

	//-------------------------------------------------------------------
	private void sanityCheck(PlayerCharacter charData) {
		Map<VitalType,Vital> vitals = charData.getVitals();
		if (!vitals.containsKey(VitalType.VITAL1))
			vitals.put(VitalType.VITAL1, new Vital());
		if (!vitals.containsKey(VitalType.VITAL2))
			vitals.put(VitalType.VITAL2, new Vital());
		if (!vitals.containsKey(VitalType.VITAL3))
			vitals.put(VitalType.VITAL3, new Vital());
	}

	//-------------------------------------------------------------------
	private void resolve(PlayerCharacter charData) {
		WorldCenter wc = MUD.getInstance().getWorldCenter();
		RPGConnector<?, ?, ?> rpg = MUD.getInstance().getRpgConnector();
		for (ItemEntity item : charData.getEquippedGear()) {
			Identifier ref = item.getMudReference();
			MUDEntityTemplate temp = wc.getItemTemplate(ref);
			if (temp==null) {
				logger.log(Level.WARNING, "Character {0} has unknown item {1}", charData.getName(), ref);
			} else {
				item.setTemplate(temp);
				logger.log(Level.WARNING, "Character {0} has known item {1} with rules {2}", charData.getName(), ref, temp.getRuleObject());
				if (temp.getRuleObject()==null) {
					logger.log(Level.ERROR, "Template {0} has no associated rule template", ref);
				}
				CarriedItem<?> ci = rpg.instantiateItem((PieceOfGear<?,?,?,?>) temp.getRuleObject()); 
				item.setRpgItem(ci);
			}
		}
	}

}
