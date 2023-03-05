package markethw.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import markethw.model.util.WithId;
import org.bson.Document;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.Optional;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public abstract class DriverBase {
    protected final MongoClient client;
    protected final String dbName;

    protected DriverBase(String connectionUrl, String DbName) {
        this.client = createMongoClient(connectionUrl);
        this.dbName = DbName;
    }

    protected static MongoClient createMongoClient(String connectionUrl) {
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionUrl))
                .codecRegistry(
                        fromRegistries(
                                MongoClientSettings.getDefaultCodecRegistry(),
                                fromProviders(PojoCodecProvider.builder().automatic(true).build())
                        )
                )
                .build();
        return MongoClients.create(clientSettings);
    }

    protected static class CollectionDesc<T extends WithId> {
        public final String name;
        public final Class<T> clazz;

        public CollectionDesc(String name, Class<T> clazz) {
            this.name = name;
            this.clazz = clazz;
        }
    }

    protected <T extends WithId> MongoCollection<T> getCollection(CollectionDesc<T> col) {
        return client.getDatabase(dbName).getCollection(col.name, col.clazz);
    }

    protected <T extends WithId> Single<ObjectId> addToCollection(CollectionDesc<T> col, T obj) {
        return Single.fromPublisher(
                        getCollection(col).insertOne(obj)
                ).mapOptional(x -> Optional.ofNullable(x.getInsertedId()))
                .map(x -> x.asObjectId().getValue()).toSingle();
    }

    protected <T extends WithId> Maybe<T> findInCollection(CollectionDesc<T> col, ObjectId oid) {
        return Observable.fromPublisher(getCollection(col).find(new Document("_id", oid))).firstElement();
    }
}
