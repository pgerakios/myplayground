package com.movierama.util.datastructure;

import com.movierama.util.Var;
import com.movierama.util.datastructure.functional.PureFun;

import java.util.ArrayList;
import java.util.List;


public class Promises<T> implements Runnable {
    List<Promise<T>> args = new ArrayList<>();

    public Promises(int millis, List<PureFun<T>> args) {
        this(millis, args.size() > 0 ? Var.toArray(args):null);
    }

    public Promises(int millis, PureFun<T>... args) {
        if (args != null)
            for (PureFun<T> arg : args) {
                this.args.add(new Promise<T>(millis, arg));
            }
    }

    public void exec() {
        for (Promise<? extends T> arg : args) {
            arg.exec();
        }
    }

    public List<? extends T> waitResult() {
        List<T> retVal = new ArrayList<>();
        for (Promise<? extends T> arg : args) {
            retVal.add(arg.waitResult());
        }
        return retVal;
    }

    @Override
    public void run() {
        exec();
    }
}
