/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.symbol;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.prelle.simplepersist.Persister;

/**
 *
 */
public class DefaultSymbolManager implements SymbolManager {

	private final static Logger logger = System.getLogger("mud.symbol");

	private Path dataDir;
	private TileGraphicService graphicLoader;
	private Persister persister;

	private Map<Integer, SymbolSet> symbolSets;

	//-------------------------------------------------------------------
	public DefaultSymbolManager(Path dataDir, TileGraphicService graphLoader) {
		this.dataDir = dataDir;
		this.graphicLoader = graphLoader;

		symbolSets = new HashMap<>();
		loadData();
	}

	//-------------------------------------------------------------------
	public void loadData() {
		logger.log(Level.INFO, "ENTER: loadData from {0}",dataDir.toAbsolutePath());
		try {
			persister = new Persister();
			// Load world files
			int symbolSetsLoaded = 0;
			DirectoryStream<Path> files = Files.newDirectoryStream(dataDir, p ->
					Files.isRegularFile(p)
					&&
					p.getFileName().toString().toLowerCase().startsWith("symbolset_")
					&&
					p.getFileName().toString().toLowerCase().endsWith(".xml") );
			for (Path dataFile : files) {
				loadSymbolSet(dataFile);
				symbolSetsLoaded++;
			}
			if (symbolSetsLoaded==0) {
				logger.log(Level.WARNING, "No symbolsets - that won't work");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			logger.log(Level.INFO, "LEAVE: loadData");
		}
	}

	//-------------------------------------------------------------------
	private void loadSymbolSet(Path worldFile) {
		logger.log(Level.DEBUG, "Trying to load symbol file {0}", worldFile);
		try {
			SymbolSet data = persister.read(SymbolSet.class, new FileInputStream(worldFile.toFile()));
			data.setFile(worldFile);
			logger.log(Level.TRACE, "Read {0} - {1}", data.getId(), data.getTitle());
			symbolSets.put(data.getId(), data);

			// If it exists, load graphic data
			if (graphicLoader!=null) {
				graphicLoader.loadSymbolImages(data);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			System.exit(1);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.symbol.SymbolManager#getTileGraphicService()
	 */
	@Override
	public TileGraphicService getTileGraphicService() {
		return graphicLoader;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.symbol.SymbolManager#createSymbolSet()
	 */
	@Override
	public SymbolSet createSymbolSet() {
		logger.log(Level.INFO, "Create a new symbolset in {0}", dataDir);
		int i=1;
		while (symbolSets.containsKey(i))
			i++;
		logger.log(Level.INFO, "Next free number = {0}",i);

		SymbolSet set = new SymbolSet(i);
		set.setTitle("Fresh created");
		symbolSets.put(i, set);
		logger.log(Level.INFO, "Symbolset {0} created", i);
		return set;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.symbol.SymbolManager#createSymbol(com.graphicmud.symbol.SymbolSet)
	 */
	@Override
	public Symbol createSymbol(SymbolSet set) {
		// TODO Auto-generated method stub
		logger.log(Level.WARNING, "TODO: createSymbol", dataDir);
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.mud.symbol.SymbolManager#getSymbolSets()
	 */
	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.symbol.SymbolManager#getSymbolSets()
	 */
	@Override
	public Collection<SymbolSet> getSymbolSets() {
		return symbolSets.values();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.symbol.SymbolManager#getSymbolSet(int)
	 */
	@Override
	public SymbolSet getSymbolSet(int id) {
		return symbolSets.get(id);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.symbol.SymbolManager#updateSymbolSet(com.graphicmud.symbol.SymbolSet)
	 */
	@Override
	public void updateSymbolSet(SymbolSet value) {
		try {
			StringWriter dummy = new StringWriter();
			persister.write(value, dummy);
			dummy.close();
			persister.write(value, new FileWriter(value.getFile().toFile(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
