package com.movierama.util.datastructure;


public class SimpleTaskExecutor extends TaskExecutor<SimpleTaskExecutor.Event,SimpleTaskExecutor.Listener> {
    static class Event implements TaskExecutor.Listener.Event{
        final Runnable run;
        public Event(Runnable r) {
            this.run = r;
        }
        public void exec() {
            this.run.run();
        }
    }

    static class Listener implements TaskExecutor.Listener<Event>{
        @Override
        public void onEvent(SimpleTaskExecutor.Event event) {
            event.exec();
        }
    }

    public SimpleTaskExecutor(int poolSize) {
        super(poolSize);
        this.addListener(new Listener());
    }

    public void add(Runnable r) {
        this.addEvent(new Event(r));
    }
};
