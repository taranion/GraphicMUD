/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface PairStream<K,V> {
    static <K,V> PairStream<K,V> from(Map<K,V> map) {
        return from(map.entrySet().stream());
    }
    static <K,V> PairStream<K,V> from(Stream<Map.Entry<K,V>> s) {
        return ()->s;
    }
    static <K,V> PairStream<K,V> from(Stream<K> s, Function<? super K, ? extends V> f) {
        return ()->s.map(k->new AbstractMap.SimpleImmutableEntry<>(k, f.apply(k)));
    }

    default PairStream<K,V> distinct() {
        return from(entries().distinct());
    }
    default PairStream<K,V> peek(BiConsumer<? super K, ? super V> action) {
        return from(entries().peek(e->action.accept(e.getKey(), e.getValue())));
    }
    default PairStream<K,V> skip(long n) {
        return from(entries().skip(n));
    }
    default PairStream<K,V> limit(long maxSize) {
        return from(entries().limit(maxSize));
    }
    default PairStream<K,V> filterKey(Predicate<? super K> mapper) {
        return from(entries().filter(e->mapper.test(e.getKey())));
    }
    default PairStream<K,V> filterValue(Predicate<? super V> mapper) {
        return from(entries().filter(e->mapper.test(e.getValue())));
    }
    default PairStream<K,V> filter(BiPredicate<? super K, ? super V> mapper) {
        return from(entries().filter(e->mapper.test(e.getKey(), e.getValue())));
    }
    default <R> PairStream<R,V> mapKey(Function<? super K,? extends R> mapper) {
        return from(entries().map(e->new AbstractMap.SimpleImmutableEntry<>(
            mapper.apply(e.getKey()), e.getValue()
        )));
    }
    default <R> PairStream<K,R> mapValue(Function<? super V,? extends R> mapper) {
        return from(entries().map(e->new AbstractMap.SimpleImmutableEntry<>(
            e.getKey(), mapper.apply(e.getValue())
        )));
    }
    default <R> Stream<R> map(BiFunction<? super K, ? super V,? extends R> mapper) {
        return entries().map(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default DoubleStream mapToDouble(ToDoubleBiFunction<? super K, ? super V> mapper) {
        return entries().mapToDouble(e->mapper.applyAsDouble(e.getKey(), e.getValue()));
    }
    default IntStream mapToInt(ToIntBiFunction<? super K, ? super V> mapper) {
        return entries().mapToInt(e->mapper.applyAsInt(e.getKey(), e.getValue()));
    }
    default LongStream mapToLong(ToLongBiFunction<? super K, ? super V> mapper) {
        return entries().mapToLong(e->mapper.applyAsLong(e.getKey(), e.getValue()));
    }
    default <RK,RV> PairStream<RK,RV> flatMap(
            BiFunction<? super K, ? super V,? extends PairStream<RK,RV>> mapper) {
        return from(entries().flatMap(
            e->mapper.apply(e.getKey(), e.getValue()).entries()));
    }
    default <R> Stream<R> flatMapToObj(
            BiFunction<? super K, ? super V,? extends Stream<R>> mapper) {
        return entries().flatMap(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default DoubleStream flatMapToDouble(
            BiFunction<? super K, ? super V,? extends DoubleStream> mapper) {
        return entries().flatMapToDouble(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default IntStream flatMapToInt(
            BiFunction<? super K, ? super V,? extends IntStream> mapper) {
        return entries().flatMapToInt(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default LongStream flatMapToLong(
            BiFunction<? super K, ? super V,? extends LongStream> mapper) {
        return entries().flatMapToLong(e->mapper.apply(e.getKey(), e.getValue()));
    }
    default PairStream<K,V> sortedByKey(Comparator<? super K> comparator) {
        return from(entries().sorted(Map.Entry.comparingByKey(comparator)));
    }
    default PairStream<K,V> sortedByValue(Comparator<? super V> comparator) {
        return from(entries().sorted(Map.Entry.comparingByValue(comparator)));
    }

    default boolean allMatch(BiPredicate<? super K,? super V> predicate) {
        return entries().allMatch(e->predicate.test(e.getKey(), e.getValue()));
    }
    default boolean anyMatch(BiPredicate<? super K,? super V> predicate) {
        return entries().anyMatch(e->predicate.test(e.getKey(), e.getValue()));
    }
    default boolean noneMatch(BiPredicate<? super K,? super V> predicate) {
        return entries().noneMatch(e->predicate.test(e.getKey(), e.getValue()));
    }
    default long count() {
        return entries().count();
    }

    Stream<Map.Entry<K,V>> entries();
    default Stream<K> keys() {
        return entries().map(Map.Entry::getKey);
    }
    default Stream<V> values() {
        return entries().map(Map.Entry::getValue);
    }
    default Optional<Map.Entry<K,V>> maxByKey(Comparator<? super K> comparator) {
        return entries().max(Map.Entry.comparingByKey(comparator));
    }
    default Optional<Map.Entry<K,V>> maxByValue(Comparator<? super V> comparator) {
        return entries().max(Map.Entry.comparingByValue(comparator));
    }
    default Optional<Map.Entry<K,V>> minByKey(Comparator<? super K> comparator) {
        return entries().min(Map.Entry.comparingByKey(comparator));
    }
    default Optional<Map.Entry<K,V>> minByValue(Comparator<? super V> comparator) {
        return entries().min(Map.Entry.comparingByValue(comparator));
    }
    default void forEach(BiConsumer<? super K, ? super V> action) {
        entries().forEach(e->action.accept(e.getKey(), e.getValue()));
    }
    default void forEachOrdered(BiConsumer<? super K, ? super V> action) {
        entries().forEachOrdered(e->action.accept(e.getKey(), e.getValue()));
    }

    default Map<K,V> toMap() {
        return entries().collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    default Map<K,V> toMap(BinaryOperator<V> valAccum) {
        return entries().collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, valAccum));
    }
}