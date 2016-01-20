package com.movierama.util.datastructure;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutor<E extends TaskExecutor.Listener.Event , L extends TaskExecutor.Listener<E>> {

    public interface Listener<E extends TaskExecutor.Listener.Event> {
        public interface Event {
        };

        public void onEvent(E event);
    };

    final Queue<E> eventQueue = new ConcurrentLinkedQueue<>();
    final Queue<L> eventListeners = new ConcurrentLinkedQueue<>();
    final ExecutorService executorPool;

    public TaskExecutor(int threadPoolSize) {
        this.executorPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void addListener(final L listener) {
        eventListeners.add(listener);
    }

    public void addEvent(final E event) {
        executorPool.execute(new Runnable() {
            @Override
            public void run() {
                for(Listener<E> listener: eventListeners) {
                    listener.onEvent(event);
                }
            }
        });
    }
}
