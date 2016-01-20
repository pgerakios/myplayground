package com.movierama.util.datastructure;

public class Klass<C> implements Comparable<Klass<?>>{

    Class<C> klass;
    int hashCode;
    public Klass(Class<C> klass) {
        this.klass = klass;
        hashCode = klass.getName().hashCode();
    }

    public Class<C> klass() {
        return klass;
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object o) {
        if(o == null || !(o instanceof Klass<?>)){
            return false;
        }
        return compareTo(((Klass<?>)o)) == 0;
    }

    @Override
    public int compareTo(Klass<?> o) {
        if(o == null) {
            return -1;
        }
        return klass.getName().compareTo(o.klass.getName());
    }
}
