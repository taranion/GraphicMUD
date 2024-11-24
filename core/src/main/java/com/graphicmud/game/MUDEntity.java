/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.prelle.simplepersist.AttribConvert;
import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;

import com.graphicmud.Identifier;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.action.script.OnEvent;
import com.graphicmud.behavior.Context;
import com.graphicmud.combat.Combat;
import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.ecs.Component;
import com.graphicmud.ecs.ComponentList;
import com.graphicmud.game.Affliction.AfflictionType;
import com.graphicmud.game.Vital.VitalType;
import com.graphicmud.io.IdentifierConverter;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.text.RoomPosition;

import lombok.Getter;
import lombok.Setter;

/**
 * This represents an instance of an entity during runtime
 */
@Getter
//@Setter
public class MUDEntity implements ReactsOnTime, Customizable<MUDEntity> {

    private final static Logger logger = System.getLogger(MUDEntity.class.getPackageName());

    /*
     * Stuff that will be persisted, if a player character is saved
     */
	@AttribConvert(value = IdentifierConverter.class)
    protected Identifier mudReference;
	@Attribute(required = true)
	protected EntityType type;
	@Element
    protected Position position;
    @ElementList(type=Affliction.class, entry = "affliction")
    protected List<Affliction> afflictions = new ArrayList<Affliction>();
    @Attribute
    @Setter
    private Integer money;
    @ElementList(entry = "item", type = MUDEntity.class)
    private List<MUDEntity> inventory;
    @Setter
    protected UUID rpgReference;
    
    /*
     * Stuff that is initialized when loading the entity
     */
    @Setter
    protected transient MUDEntityTemplate template;
    @Setter
    protected transient EntityState state = EntityState.IDLE;
    @Setter
    protected transient Combat currentCombat;

    private ComponentList extraComponents;

    /** 
     * The owner of this entity - either a MobileEntity (if this is in its inventory) 
     * or an ItemEntity (of this a item inside a container - or null, if this
     * is a mob or player itself
     */
    @Setter
    protected transient MUDEntity parent;
    protected transient Object ruleData;
    protected transient List<Consumer<MUDEntity>> pulseHooks = new ArrayList<Consumer<MUDEntity>>();
    protected transient List<Consumer<MUDEntity>> tickHooks = new ArrayList<Consumer<MUDEntity>>();
    protected transient Map<Component, Map<String, Object>> componentData = new HashMap<Component, Map<String, Object>>();

    //-------------------------------------------------------------------
    /**
     * Empty constructor required for deserialization and initializing lists in this case
     */
    public MUDEntity() {
        this.inventory = new ArrayList<>();
        extraComponents = new ComponentList();
        pulseHooks = new ArrayList<Consumer<MUDEntity>>();
        tickHooks = new ArrayList<Consumer<MUDEntity>>();
        componentData = new HashMap<Component, Map<String, Object>>();
    }

    //-------------------------------------------------------------------
    public MUDEntity(MUDEntityTemplate data) {
        this.template = data;
        if (data!=null)
        	mudReference = data.getId();
        this.inventory = new ArrayList<>();
        extraComponents = new ComponentList();
        pulseHooks = new ArrayList<Consumer<MUDEntity>>();
        tickHooks = new ArrayList<Consumer<MUDEntity>>();
        componentData = new HashMap<Component, Map<String, Object>>();
    }

    //-------------------------------------------------------------------
    public boolean reactsOnKeyword(String keyword) {
        keyword = keyword.toLowerCase();
        if (getName().toLowerCase().startsWith(keyword)) return true;
        if (template != null) {
            for (String tmp : template.getKeywords()) {
                if (tmp.trim().toLowerCase().startsWith(keyword)) return true;
            }
        }
        return false;
    }

    //-------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public <P extends Position> P getPosition() {
        return (P) position;
    }

    //-------------------------------------------------------------------
    public void setPosition(Position pos) {
        this.position = pos;
    }

    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.game.MUDEntity#getName()
     */
    public String getName() {
        if (template == null) return "Entity_without_template";
        return template.getName();
    }

    public Locale getLocale() {
        return Locale.getDefault();
    }

    public void sendOnChannel(CommunicationChannel channel, String text) {
        //Tut erstmal nichts
    }

    //------------------------------------------------

    /**
     * Returns the rule specific representation of the player.
     * Every ruleset will use this object
     */
    @SuppressWarnings("unchecked")
    public <E> E getRuleObject() {
        return (E) ruleData;
    }

    public void setRuleObject(Object value) {
        ruleData = value;
    }


    //-------------------------------------------------------------------
    private List<Component> getComponents() {
        List<Component> comp = new ArrayList<Component>();
        if (template != null)
            comp.addAll(template.getComponents());
        if (extraComponents != null)
            comp.addAll(extraComponents);
        return comp;
    }

    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.game.ReactsOnTime#pulse()
     */
    @Override
    public void pulse() {
        getComponents().forEach(c -> {
            try {
                c.pulse(this);
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Error in component " + c.getClass().getSimpleName() + " for entity " + this.getName(), e);
            }
        });
        // Run extra tasks
        if (pulseHooks == null) return;
        for (Consumer<MUDEntity> task : pulseHooks) {
            try {
                task.accept(this);
            } catch (Throwable e) {
                logger.log(Level.ERROR, "Failed PULSE task " + task.getClass(), e);
            }
        }
    }


    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.game.ReactsOnTime#tick()
     */
    @Override
    public void tick() {
        getComponents().forEach(c -> {
            try {
                c.tick(this);
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Error in component " + c.getClass().getSimpleName() + " for entity " + this.getName(), e);
            }
        });
        // Run extra tasks
        for (Consumer<MUDEntity> task : tickHooks) {
            try {
                task.accept(this);
            } catch (Throwable e) {
                logger.log(Level.ERROR, "Failed TICK task " + task.getClass(), e);
            }
        }
    }

    //-------------------------------------------------------------------
    public EntityType getType() {
        if (template == null) {
            return EntityType.PLAYER;
        } else {
            return template.getType();
        }
    }

    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.game.Customizable#addPulseHook(java.util.function.Consumer)
     */
    @Override
    public void addPulseHook(Consumer<MUDEntity> hook) {
        if (!pulseHooks.contains(hook))
            pulseHooks.add(hook);
    }


    //-------------------------------------------------------------------

    /**
     * @see com.graphicmud.game.Customizable#addTickHook(java.util.function.Consumer)
     */
    @Override
    public void addTickHook(Consumer<MUDEntity> hook) {
        if (!tickHooks.contains(hook))
            tickHooks.add(hook);
    }

    //-------------------------------------------------------------------
    public void storeComponentData(Component src, String key, Object value) {
        Map<String, Object> map = componentData.getOrDefault(src, new HashMap<String, Object>());
        map.put(key, value);
        componentData.put(src, map);
    }

    //-------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public <E> E getComponentData(Component src, String key) {
        Map<String, Object> map = componentData.getOrDefault(src, new HashMap<String, Object>());
        return (E) map.get(key);
    }

    //-------------------------------------------------------------------
    public void clearComponentData(Component src, String key) {
        Map<String, Object> map = componentData.getOrDefault(src, null);
        if (map != null) {
            map.remove(key);
            if (map.isEmpty())
                componentData.remove(src);
        }
    }

    //-------------------------------------------------------------------
    public void addToInventory(MUDEntity item) {
        inventory.add(item);
        item.setPosition(null);
        item.setParent(this);
        if (this instanceof MobileEntity) {
        	MUD.getInstance().getRpgConnector().addToInventory((MobileEntity) this, (ItemEntity) item, null);
        }
        this.fireEvent(new MUDEvent(MUDEvent.Type.INVENTORY_CHANGED, this, item));
    }

    //-------------------------------------------------------------------
    public void removeFromInventory(MUDEntity item) {
        //TODO Check if allowed
        inventory.remove(item);
        item.setParent(null);
        if (this instanceof MobileEntity) {
        	MUD.getInstance().getRpgConnector().removeFromInventory((MobileEntity) this, (ItemEntity) item, null);
        }
        this.fireEvent(new MUDEvent(MUDEvent.Type.INVENTORY_CHANGED, this, item));
    }

    //-------------------------------------------------------------------
    public MUDEntity getFromInventory(String itemName) {
         Optional<MUDEntity> first = inventory.stream().filter(i -> i.reactsOnKeyword(itemName)).findFirst();
        return first.orElse(null);
    }

    //-------------------------------------------------------------------
    public MUDEntity getFromInventory(UUID uuid) {
        Optional<MUDEntity> first = inventory.stream().filter(i -> uuid.equals(i.getRpgReference())).findFirst();
        return first.orElse(null);
    }

    //-------------------------------------------------------------------
    public boolean isInInventory(MUDEntityTemplate template) {
    	return inventory.stream().allMatch(i -> template==i.getTemplate());
    }

    //-------------------------------------------------------------------
    public List<MUDEntity> getAllFromInventory(String itemName) {
        if (itemName == null) {
            return new ArrayList<>(inventory);
        }
        List<MUDEntity> list = inventory.stream().filter(i -> i.getName().equalsIgnoreCase(itemName)).toList();
        if (!list.isEmpty()) {
            return list;
        }
        list = inventory.stream().filter(i -> i.getName().toLowerCase().startsWith(itemName.toLowerCase())).toList();
        if (!list.isEmpty()) {
            return list;
        }
        return inventory.stream().filter(i -> i.reactsOnKeyword(itemName)).toList();
    }

    //-------------------------------------------------------------------
    public void prepare(Location room, Path zoneDir) {
    	// Load inventory
    	Game game = MUD.getInstance().getGame();
    	WorldCenter world = MUD.getInstance().getWorldCenter();
    	if (template!=null) {
    		template.getLoadlist().forEach(load -> {
    			if (load.getType()!=EntityType.ITEM) {
    				return;
    			}
    			MUDEntityTemplate template = world.getItemTemplate(load.getRef());
    			MUDEntity loadedItem = game.instantiate(template);
    			logger.log(Level.INFO, "Add {0} into inventory of {1}", template.getId(), this.getName());
    			inventory.add(loadedItem);
    		});
    	}
    	
    	// Prepare components
        for (Component comp : getComponents()) {
            comp.prepare(zoneDir);
        }

    }

    //-------------------------------------------------------------------
    public void receiveEvent(MUDEvent event) {
//    	logger.log(Level.DEBUG, "RCV {0}", event);
        for (Component comp : getComponents()) {
            try {
                comp.handleEvent(this, event);
            } catch (Exception e) {
                logger.log(Level.ERROR, "Error processing event " + event, e);
            }
        }
        // Look for special event handlers
        if (template==null) {
        	return;
        }
        Context context = new Context();
        context.put(ParameterType.PERFORMED_BY, this);
        context.put(ParameterType.SOURCE, event.getSource());
        for (OnEvent onEv : template.getEventHandlers()) {
        	if (onEv.getType()==event.getType()) {
            	logger.log(Level.ERROR, "Call event handler "+onEv);
//            	for (CookedAction action : onEv.getEventActions()) {
//            		CookedActionProcessor.perform(action, this, context);
//            	}
        	}
        }
    }

    //-------------------------------------------------------------------
    public void sendShortText(Priority prio, String text) {
        //Nothing to do
    }

    //-------------------------------------------------------------------
    public void sendTextWithMarkup(String text) {
        //Nothing to do
    }

    //-------------------------------------------------------------------
    public Component getComponent(Class componentClass) {
        Optional<Component> first = getComponents().stream().filter(c -> c.getClass().equals(componentClass)).findFirst();
        return first.orElse(null);
    }

    //-------------------------------------------------------------------
    public void fireEvent(MUDEvent event) {
        switch (event.getType()) {
            case TALK_REQUEST:
                break;
            case INVENTORY_CHANGED:
            default:
                MUD.getInstance().getRpgConnector().processEvent(this, event);
        }
    }

    //-------------------------------------------------------------------
    public boolean hasAffliction(AfflictionType type) {
    	return afflictions.stream().anyMatch(af -> af.getType()==type);
    }
    
    //-------------------------------------------------------------------
   public void die() {
    	logger.log(Level.WARNING, "ToDo: Entity {0} died", getName());
    	    	
    	MUDEntityTemplate corpseTemp = new MUDEntityTemplate();
    	corpseTemp.setName("The corpse of "+getName());
    	corpseTemp.setType(EntityType.ITEM);
    	corpseTemp.getFlags().add(EntityFlag.CONTAINER);
    	// Create a keywords list for the corpse
    	List<String> corpseKeywords = new ArrayList<String>();
    	corpseKeywords.add(Localization.getString("keyword.corpse"));
    	if (template==null) {
    		corpseKeywords.add(getName().toLowerCase());
    	} else {
    		for (String word : template.getKeywords())
    			corpseKeywords.add(word);
    	}
    	corpseTemp.setKeywords(String.join(",", corpseKeywords));
    	MUDEntity corpse = MUD.getInstance().getGame().instantiate(corpseTemp);
    	corpse.inventory.addAll(inventory);
    	corpse.setPosition(position);
    	
    	Identifier nr = position.getRoomPosition().getRoomNumber();
    	try {
			MUD.getInstance().getWorldCenter().getLocation(nr).addEntity(corpse);
			if (this instanceof PlayerCharacter) {
				Identifier startRoom = new Identifier("1/3/2");
				MUD.getInstance().getWorldCenter().getLocation(startRoom).addEntity(this);
				position = MUD.getInstance().getWorldCenter().createPosition();
				position.setRoomPosition(new RoomPosition(startRoom));
			} else {
				MUD.getInstance().getWorldCenter().getLocation(nr).removeEntity(this);
			}
		} catch (NoSuchPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	inventory.clear();
    }
    
   //-------------------------------------------------------------------
   public List<EntityFlag> getFlags() {
	   if (template==null) return List.of();
	   return template.getFlags();
   }
   
  //-------------------------------------------------------------------
  public boolean hasFlags(EntityFlag flag) {
	  	return getFlags().contains(flag);
  }
   
   //-------------------------------------------------------------------
   public Map<VitalType,Vital> getVitals() { return new HashMap(); }
    
   public String getDescription() {
        if (template != null) {
            return template.getDescription();
        }
       return null;
   }
   
   public String getDescriptionInRoom() {
        if (template != null) {
            return template.getDescriptionInRoom();
        }
       return null;
   }
   
}
