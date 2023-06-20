package com.projecki.fusion.util;

import java.util.Objects;

/**
 * A simple immutable pair object
 *
 * @param <T> The type of the first object
 * @param <U> The type of the second object
 */
public class Pair<T, U> {

    public final T fst;
    public final U snd;

    public Pair(T fst, U snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return fst.equals(pair.fst) && snd.equals(pair.snd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fst, snd);
    }

    @Override
    public String toString() {
        return "<" + fst.toString() + ", " + snd.toString() + ">";
    }

}
