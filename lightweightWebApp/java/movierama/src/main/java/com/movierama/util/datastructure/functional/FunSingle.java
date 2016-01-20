package com.movierama.util.datastructure.functional;

public interface FunSingle<I,O,E extends Exception> {
    O apply(I arg) throws E;
}
