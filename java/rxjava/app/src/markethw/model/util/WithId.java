package markethw.model.util;


import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.util.Objects;

public abstract class WithId {
    private ObjectId id;

    @BsonCreator
    public WithId() {}

    public WithId(ObjectId id) {this.id = id;}

    @BsonId
    public ObjectId getId() {
        return id;
    }

    @BsonId
    public void setId(ObjectId id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WithId withId = (WithId) o;
        return Objects.equals(id, withId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
