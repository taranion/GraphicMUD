package com.graphicmud.behavior;

import java.io.IOException;
import java.io.StringReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.graphicmud.action.ActOnTarget;
import com.graphicmud.action.MoveInRoom;
import com.graphicmud.action.MoveToRoom;
import com.graphicmud.action.RestAction;
import com.graphicmud.action.SelectAsTarget;
import com.graphicmud.action.cooked.Communicate;
import com.graphicmud.action.cooked.DoorsAction;
import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public class TreeLoader {
	
	private final static Logger logger = System.getLogger(TreeLoader.class.getPackageName());
	
	private static DocumentBuilder builder;

	//-------------------------------------------------------------------
	static {
		DocumentBuilderFactory fact =  DocumentBuilderFactory.newInstance();
		fact.setValidating(false);
		try {
			builder = fact.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.log(Level.ERROR, "Failed initializing DOM",e);
		}
	}

	//-------------------------------------------------------------------
	public static void read(Path file) throws SAXException, IOException {
		Document doc = builder.parse(file.toFile());
		convertToBehaviorTree(doc);
	}

	//-------------------------------------------------------------------
	public static CompositeNode read(String data) throws SAXException, IOException {
		InputSource src = new InputSource(new StringReader(data));
		Document doc = builder.parse(src);
		return convertToBehaviorTree(doc);
	}

	//-------------------------------------------------------------------
	private static CompositeNode convertToBehaviorTree(Document doc) {
		Element root = doc.getDocumentElement();
		CompositeNode node = parseAsNode(root, null);
		return node;
	}

	//-------------------------------------------------------------------
	private static CompositeNode parseAsNode(Element elem, CompositeNode parent) {
		CompositeNode node = null;
		switch (elem.getNodeName().toLowerCase()) {
		case "random":
			node = new RandomNode();
			break;
		case "sequence":
			node = new SequenceNode();
			break;
		case "selector":
			node = new SelectorNode();
			break;
		default:
			BiFunction<MUDEntity, Context, TreeResult> leaf = parseLeafNode(elem, parent);
			parent.add(leaf);
			return node;
		}
		
		// If there is an "id" attribute, set it
		if (elem.hasAttribute("id")) {
			node.setId(elem.getAttribute("id"));
		}
		
		NodeList list = elem.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			Node child = list.item(i);
			if (child.getNodeType()==Node.ELEMENT_NODE) {
				CompositeNode childNode = parseAsNode((Element)child, node);
				node.add(childNode);
			}
			
		}
		
		return node;
	}

	private static BiFunction<MUDEntity,Context,TreeResult> parseLeafNode(Element elem, CompositeNode parent) {
		String command = elem.getNodeName().toLowerCase();
		String method  = elem.getAttribute("method");
		Class  clazz   = null;
		Class[] consParam = new Class[0];
		if (method==null) 
			throw new RuntimeException("Missing method-Attribute in "+elem);
		
		switch (command) {
		case "actontarget": clazz = ActOnTarget.class; break;
		case "communicate": clazz = Communicate.class; consParam=new Class[] {String.class}; break;
		case "doors"      : clazz = DoorsAction.class; break;
		case "moveinroom" : clazz = MoveInRoom.class; consParam=new Class[] {int.class}; break;
		case "movetoroom" : clazz = MoveToRoom.class; consParam=new Class[] {int.class}; break;
		case "rest"       : clazz = RestAction.class; break;
		case "selectastarget": clazz = SelectAsTarget.class; break;
//		case "social"     : clazz = Social.class; break;
		default:
			throw new RuntimeException("Unknown leaf node "+elem.getNodeName());
		}
		try {
			Method realMethod = clazz.getMethod(method, MUDEntity.class, Context.class);
			logger.log(Level.DEBUG, "realMethod="+realMethod);
			// Check if there are constructor parameter
			NamedNodeMap map = elem.getAttributes();
			List<Object> consData = new ArrayList<Object>();
			// .. as attributes
			for (int i=0; i<map.getLength(); i++) {
				Attr attrib = (Attr) map.item(i);
				if (attrib.getName().equals("method")) continue;
				logger.log(Level.DEBUG, "Attrib "+attrib);
				String valueS = attrib.getNodeValue();
				if (consParam.length>consData.size()) {
					Class expect = consParam[consData.size()];
//					logger.log(Level.INFO, "Expect {0} to be of type {1}", valueS, expect);
					if (expect==String.class) {
						consData.add(valueS);
					} else if (expect==int.class) {
						consData.add(Integer.parseInt(valueS));
					} else if (expect==Integer.class) {
						consData.add(Integer.valueOf(valueS));
					} else {
						logger.log(Level.ERROR, "Don''t know how to convert {0} into {1}", valueS, expect);
					}
				}
			}
			// .. as CData
			NodeList children = elem.getChildNodes();
			for (int i=0; i<children.getLength(); i++) {
//				logger.log(Level.INFO, "CHild "+children.item(i).getNodeType()+" "+children.item(i));
				switch (children.item(i)) {
				case Element childElem -> {
					if (childElem.hasChildNodes()) {
						Node hopefullyText = childElem.getFirstChild();
						if (hopefullyText instanceof Text) {
							if (consData.size()<consParam.length) {
								consData.add(((Text)hopefullyText).getData());
								logger.log(Level.INFO, "Data now "+consData);
							} else {
								logger.log(Level.WARNING, "Node "+elem+" has more parameter children than expected");
							}
						}
					}
				}
				default -> {}
				}
			}
			

			Object obj = null;
			if (consParam!=null && consParam.length>0) {
				try {
					Constructor cons = clazz.getConstructor(consParam);
					obj = cons.newInstance(consData.toArray());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			final Object objF = obj;
//			BiFunction<MUDEntity,Context,TreeResult> func = toBiFunction(MethodHandles.lookup(), clazz, realMethod);
//			System.err.println("funcA = "+funcA);
			BiFunction<MUDEntity,Context,TreeResult> func = (e,c) -> {
				try {
					return (TreeResult)realMethod.invoke(objF, e,c);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return null;
			};

			
			
			parent.add(func);
			logger.log(Level.INFO, "Added {2}::{0} to {1}", realMethod.getName(), parent, clazz.getSimpleName());
//			func.apply(null, null);
			return func;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
//	static BiFunction<MUDEntity,Context,TreeResult> toBiFunction(MethodHandles.Lookup lookup, Class annotated, Method method) throws Throwable {
//	    MethodType consumeString = MethodType.methodType(TreeResult.class, MUDEntity.class, Context.class);
//	    MethodHandle handle = lookup.unreflect(method);
//	    final CallSite site = LambdaMetafactory.metafactory(lookup, "apply",
//	            MethodType.methodType(BiFunction.class, annotated),
//	            consumeString.changeParameterType(0, Object.class),
//	            handle,
//	            consumeString);
//	    return (BiFunction<MUDEntity,Context,TreeResult>) site.getTarget().invoke(null);
//	}
}
