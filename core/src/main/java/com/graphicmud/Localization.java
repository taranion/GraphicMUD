/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;

import de.rpgframework.MultiLanguageResourceBundle;

/**
 * Localizer.java
 *
 * @author Stefan Prelle
 */

public class Localization {

	private final static Logger logger = System.getLogger("Localizer");

	private static MultiLanguageResourceBundle i18n;
	private static Properties props;

	//-------------------------------------------------------------------------
	static {
		props = new Properties();
		i18n = new MultiLanguageResourceBundle(Localization.class, Locale.ENGLISH, Locale.GERMAN);
	}

	//-------------------------------------------------------------------------
	public static MultiLanguageResourceBundle getI18N() {
		return i18n;
	}

	//-------------------------------------------------------------------------
	public static void addPropertiesFrom(File dir, String name) throws IOException {
		try {
			File file = new File(dir,name+".properties");
			if (file.exists()) {
				props.load(new FileInputStream(file));
			} else {
				InputStream in = ClassLoader.getSystemResourceAsStream(name);
				if (in==null) {
					logger.log(Level.ERROR,"Failed loading from resource "+name);
					return;
				}
				props.load(in);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	//-------------------------------------------------------------------------
//	public static void addPropertiesFrom(FileSystem fs, String name) throws IOException {
//		try {
//			File file = fs.getFile(name+".properties");
//			if (file.exists()) {
//				props.load(new FileInputStream(file));
//			} else {
//				InputStream in = ClassLoader.getSystemResourceAsStream(name);
//				if (in==null) {
//					logger.log(Level.ERROR,"Failed loading from resource "+name);
//					throw new IOException("No system resource "+name+" in filesystem "+fs.getFSName());
//				}
//				props.load(in);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	//-------------------------------------------------------------------------
	public static void addPropertiesFrom(ClassLoader loader, String name) throws IOException {
		try {
			InputStream stream = loader.getResourceAsStream(name+".properties");
			if (stream==null)
				throw new FileNotFoundException(name+".properties");
			props.load(stream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------
	public static void addPropertiesFrom(InputStream stream) throws IOException {
		if (stream==null) {
			logger.log(Level.ERROR,"Cannot load from NullPointer");
			return;
		}
		props.load(stream);
	}

	//-------------------------------------------------------------------------
	public static String getString(String key, Locale loc) {
		return i18n.getString(key, loc);
	}

	//-------------------------------------------------------------------------
	public static String getString(String key) {
		return i18n.getString(key);
//		String ret = props.getProperty(key);
//		if (ret!=null)
//			return ret;
//		logger.log(Level.ERROR,"Missing resource '"+key+"'");
//		System.err.println("Missing resource '"+key+"'");
//		return key;
	}

//	//-------------------------------------------------------------------------
//	public static String getString(PropertyResourceBundle i18n, String key) {
//		if (i18n.containsKey(key)) {
//			return i18n.getString(key);
//		}
//		logger.log(Level.WARNING, "Missing property ''{0}'' in {1}", key, i18n.getBaseBundleName());
//		return key;
//	}

	//-------------------------------------------------------------------------
	//	public Localizer(String i18nDir) {
	//	this.i18nDir = new File(i18nDir);
	//	}

	//-------------------------------------------------------------------------
	protected static List<String> getResourceVariants(String name) {
		// Normal classes
		List<String> variants = new ArrayList<String>();
		Locale locale = Locale.getDefault();
		if (locale.getVariant()!=null && locale.getVariant().length()>0)
			variants.add(locale.getLanguage()+"_"+locale.getCountry()+"_"+locale.getVariant()+"/"+name+".properties");
		if (locale.getCountry()!=null && locale.getCountry().length()>0)
			variants.add(locale.getLanguage()+"_"+locale.getCountry()+"/"+name+".properties");
		variants.add(locale.getLanguage()+"/"+name+".properties");
		variants.add(name+".properties");

		return variants;
	}

	//--------------------------------------------------------------------
	public static String fillString(String key, Object... args) throws MissingResourceException {
		try {
			String pattern = Localization.getString(key);
			return MessageFormat.format(pattern, args);
		} catch (MissingResourceException mre) {
			throw new MissingResourceException(mre.getMessage(), null, key);
		}
	}

	//--------------------------------------------------------------------
	public static String fillString(String key, Locale loc, Object... args) {
		try {
			String pattern = getString(key);
			return MessageFormat.format(pattern, args);
		} catch (MissingResourceException mre) {
			logger.log(Level.WARNING, "Missing property ''{0}'' in {1}", key, i18n.getBaseBundleName());
			return key;
		}
	}

} // Localizer
