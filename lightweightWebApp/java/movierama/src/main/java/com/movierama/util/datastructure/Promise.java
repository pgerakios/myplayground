package com.movierama.util.datastructure;

import com.movierama.config.Environment;
import com.movierama.util.datastructure.functional.PureFun;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Promise<T> implements Runnable {
    private final CondVar condVar;
    private final PureFun<T> code;
    private final AtomicReference<T> result = new AtomicReference<>();
    private final AtomicBoolean executed = new AtomicBoolean(false);

    public Promise(int millis, PureFun<T> code) {
        this.code = code;
        this.condVar = new CondVar(this, millis);
        Environment.env().pool().add(this);
    }
    public Promise(PureFun<T> code) {
       this(-1, code);
    }

    public void exec() {
        if(!executed.get()) {
            executed.set(true);
            T retVal = code.apply();
            result.set(retVal);
        }
        condVar.signalAll();
    }

    public T result() {
        return result.get();
    }

    public T waitResult() {
        condVar.waitForSignal();
        return result();
    }

    @Override
    public void run() {
        exec(); /* when invoked indirectly by a thread */
    }
}
