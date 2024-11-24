/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public class RandomEntityCollector implements Collector<MUDEntity, List<MUDEntity>, MUDEntity> {
	
	private final static Random RANDOM = new Random();

	//-------------------------------------------------------------------
	/**
	 */
	public RandomEntityCollector() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.stream.Collector#supplier()
	 */
	@Override
	public Supplier<List<MUDEntity>> supplier() {
//		System.err.println("RandomEntityCollector.supplier");
		// TODO Auto-generated method stub
		return ArrayList::new;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.stream.Collector#accumulator()
	 */
	@Override
	public BiConsumer<List<MUDEntity>, MUDEntity> accumulator() {
//		System.err.println("RandomEntityCollector.accumulator for ");
		return (internList,toAdd) -> internList.add(toAdd);
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.stream.Collector#combiner()
	 */
	@Override
	public BinaryOperator<List<MUDEntity>> combiner() {
//		System.err.println("RandomEntityCollector.combiner ");
		return (result1, result2) -> {
		    result1.addAll(result2);
		    return result1;
		}
;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.stream.Collector#finisher()
	 */
	@Override
	public Function<List<MUDEntity>, MUDEntity> finisher() {
//		System.err.println("RandomEntityCollector.finisher");
		// TODO Auto-generated method stub
		return (list) -> {
			if (list.isEmpty()) return null;
			return list.get(RANDOM.nextInt(list.size()));
		};
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.stream.Collector#characteristics()
	 */
	@Override
	public Set<Characteristics> characteristics() {
//		System.err.println("RandomEntityCollector.characteristics");
		return Collections.emptySet();
	}

}
