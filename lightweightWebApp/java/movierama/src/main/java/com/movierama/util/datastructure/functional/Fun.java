package com.movierama.util.datastructure.functional;

public interface Fun<O,E extends Exception> {
    O apply() throws E;
}
