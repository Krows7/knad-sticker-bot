package net.krows_team.sticker_bot.util;

import java.util.function.Function;

public class OptionalPair<T, E> {
	
	private T t;
	private E e;
	
	public static <T, E> OptionalPair<T, E> nonNull(T t) {
		return of(t, null);
	}
	
	public static <T, E> OptionalPair<T, E> of(E e) {
		return of(null, e);
	}
	
	public static <T, E> OptionalPair<T, E> of(T t, E e) {
		return new OptionalPair<T, E>(t, e);
	}
	
	public OptionalPair(T t, E e) {
		this.t = t;
		this.e = e;
	}
	
	public T orElse(Function<E, T> function) {
		if(t == null) return function.apply(e);
		return t;
	}
}
