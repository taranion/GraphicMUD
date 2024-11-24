package com.graphicmud.action.script;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.graphicmud.Identifier;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.character.EquipmentPosition;
import com.graphicmud.character.EquippedGear;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.game.MUDEvent.Type;
import com.graphicmud.world.Location;

/**
 * 
 */
public class EventProcessor {
	
	private final static Logger logger = System.getLogger(EventProcessor.class.getPackageName());

	//-------------------------------------------------------------------
	/**
	 */
	public static boolean process(OnEvent onEv, MUDEntity entity, Context context, String actionID, String parameter) {
    	if (onEv.getType()==Type.COMMAND_ISSUED && actionID!=null && actionID.equals(onEv.getValue())) {
    		// This element applies to the command issued. Now check what kind of script to
    		// perform
    		switch (onEv) {
    		case OnEventXML onEvXML -> {
    			// XML scripting
        		if (onEvXML.getParam()!=null && onEvXML.getParam().equals(parameter)) {
        			logger.log(Level.ERROR, "TODO Call event handler "+onEv);
	            	return processXMLScript(onEvXML, entity, context);
        		}
     		}
    		case OnEventJS onEvJS -> {
    			// Javascript
    			return processAnyScript(onEvJS, entity, context);
     		}
    		default -> {}
    		}
    	} else {
    		logger.log(Level.ERROR, "Dont know what to do for type={0} and actionID={1}", onEv.getType(), actionID);
    	}
    	return true;
	}

	//-------------------------------------------------------------------
	private static boolean processXMLScript(HasSubActionsNode onEvXML, MUDEntity entity, Context context) {
		logger.log(Level.DEBUG, "ENTER: processXMLScript");
		boolean continueExecution = true;
		try {
			outer:
		    	for (XMLScriptAction action : onEvXML.getEventActions()) {
		        	logger.log(Level.ERROR, "Action "+action);
		        	switch (action) {
		        	case IfScriptAction isa -> {
		        		List<ItemEntity> list = new ArrayList<ItemEntity>();	
		        		switch (isa.getWhat()) {
		        		case EQUIPMENT:
		        			if (entity instanceof MobileEntity) {
		            			boolean result = equipmentCheck((MobileEntity) entity, context, ((MobileEntity)entity).getEquippedGear(), isa);
		            			logger.log(Level.ERROR, "Result was {0}", result);
		            			if (result) {
		            				continueExecution = processXMLScript(isa, entity, context);
		            				break outer;
		            			}
		        			}
		        			break;
		        		case INVENTORY:
		        			entity.getInventory().stream().filter(me -> me instanceof ItemEntity).forEach(me -> list.add((ItemEntity)me));
		        			break;
		        		default:
		        			logger.log(Level.ERROR, "Dont know how to deal with WHAT={0}", isa.getWhat());
		        			continue;
		        		}
		        		
		        		logger.log(Level.DEBUG, "If test fails");
		        	}
		        	case Echo echo -> echo.execute(entity, context); 
		        	case NoExecute noExec -> { 
		        		// Stop script processing here
		        		continueExecution=false; 
		        		break outer; 
		        	}        	
					default -> logger.log(Level.ERROR, "Dont know how to deal with {0}", action.getClass());
		        	
		        	}
		    	}
		    	return continueExecution;
		} finally {
			logger.log(Level.DEBUG, "ENTER: processXMLScript = {0}", continueExecution);
		}
	}

	//-------------------------------------------------------------------
	private static boolean equipmentCheck(MobileEntity actor, Context context, EquippedGear gear,
			IfScriptAction isa) {
		
		switch (isa.getOp()) {
		case IS_FREE    : 
			EquipmentPosition pos = EquipmentPosition.valueOf(isa.getValue());
			return gear.isSlotFree(pos);
		case IS_OCCUPIED: 
			pos = EquipmentPosition.valueOf(isa.getValue());
			return gear.isSlotUsed(pos);
		case CONTAINS:
			Identifier ident = new Identifier(isa.getValue());
			for (ItemEntity ent : gear) {
				if (ent.getTemplate()!=null && ent.getTemplate().getId().equals(ident))
					return true;
			}
			return false;
		default:
			logger.log(Level.ERROR, "Unsupported operation {0} on equipped gear", isa.getOp());
		}
		return false;
	}

	//-------------------------------------------------------------------
	private static boolean processAnyScript(OnEventJS onEvJS, MUDEntity entity, Context context) {
		// Javascript
		logger.log(Level.WARNING, "Execute JavaScript\n"+onEvJS.getScriptContent());
		Location loc = context.get(ParameterType.ROOM_CURRENT);
		ScriptEngineManager manager = new ScriptEngineManager();
		try {
			
			ScriptEngine engine = manager.getEngineByName("js");
			Bindings bindings = engine.createBindings();
			bindings.put("room", loc);
			bindings.put("actor", entity);
			bindings.put("inventory", entity.getInventory());
			for (ParameterType type : context.keySet()) {
				bindings.put(type.name(), context.get(type));
			}
			Object retVal = null;
			if (entity instanceof MobileEntity) {
				bindings.put("equipment", ((MobileEntity)entity).getEquippedGear());
			}
			engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
//			if (onEvJS.getCompiled()!=null) {
//				logger.log(Level.WARNING, "Calling precompiled "+onEvJS.getCompiled());
//				retVal = onEvJS.getCompiled().eval(bindings);
//				retVal = ((Invocable)onEvJS.getCompiled().getEngine()).invokeFunction("runScript");
//			} else {
				StringBuilder content = new StringBuilder();
				content.append("function runScript() {\r\n");
				content.append("load(\"nashorn:mozilla_compat.js\");\n");
				content.append("importClass(org.prelle.mud.character.EquipmentPosition);\n");
				content.append("importClass(org.prelle.mud.network.ClientConnection);\n");
				content.append(onEvJS.getScriptContent());
				content.append("\n};\n");
				
//				CompiledScript script = ((Compilable) engine).compile(content.toString());
//				Object retVal = script.eval();
				engine.eval(content.toString());
				retVal = ((Invocable)engine).invokeFunction("runScript");
//			}
			logger.log(Level.WARNING, "Return value {0}", retVal);
			if (retVal!=null)
				return (Boolean)retVal;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		return true;
	}
}
