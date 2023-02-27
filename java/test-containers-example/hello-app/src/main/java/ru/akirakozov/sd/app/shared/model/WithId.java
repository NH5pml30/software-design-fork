package ru.akirakozov.sd.app.shared.model;

import java.util.Objects;

public abstract class WithId {
    protected final int id;

    protected WithId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(int id) {
            super(String.format("No such element: %d", id));
        }
    }

    public static NotFoundException createNotFound(int id) {
        return new NotFoundException(id);
    }

    public static <T extends WithId> T validate(int id, T obj) {
        if (obj == null) {
            throw createNotFound(id);
        }
        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WithId withId = (WithId) o;
        return id == withId.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
