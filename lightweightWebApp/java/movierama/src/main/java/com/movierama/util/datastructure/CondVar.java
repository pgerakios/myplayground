package com.movierama.util.datastructure;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CondVar {
    private final Lock aLock = new ReentrantLock();
    private final Condition condVar = aLock.newCondition();
    private transient boolean condition = false;
    private final Runnable run;
    private final int awaitMillis;
    public CondVar(Runnable run) {
        this(run, -1);
    }

    public CondVar(Runnable run, int awaitMillis) {
        this.run = run;
        this.awaitMillis = awaitMillis;
    }

    public void signalAll() {
        try {
            aLock.lock();
            condition = true;
            condVar.signalAll();
        } finally {
            aLock.unlock();
        }
    }

    public boolean waitForSignal() {
        boolean retVal = false;
        while (!retVal) {
            aLock.lock();
            try {
                try {
                    if(awaitMillis < 0) {
                        condVar.await();
                    } else {
                        condVar.await(awaitMillis, TimeUnit.MILLISECONDS);
                    }
                    if (condition) {
                        condition = false;
                        retVal = true;
                        run.run(); /* run the external code */
                    }
                } catch (InterruptedException e) {
                    retVal = false;
                    break;
                }
            } finally {
                aLock.unlock();
            }
        }
        return retVal;
    }
}
