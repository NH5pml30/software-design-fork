package ru.akirakozov.sd.app.shared.model;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WithIdMap<T extends WithId> {
    private final Map<Integer, T> theMap;
    private final AtomicInteger nextId = new AtomicInteger(0);

    public WithIdMap(Map<Integer, T> theMap) {
        this.theMap = theMap;
    }

    public Map<Integer, T> get() {
        return theMap;
    }

    public int genId() {
        return nextId.getAndIncrement();
    }
}
