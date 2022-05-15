package pandorum.mongo;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static pandorum.PluginVars.specialKeys;

public abstract class MongoDataBridge<T extends MongoDataBridge<T>> {

    private static Map<String, Object> latest = new HashMap<>();

    public ObjectId _id;
    public int __v;

    protected static <T extends MongoDataBridge<T>> void findAndApplySchema(MongoCollection<Document> collection, Class<T> sourceClass, Bson filter, Cons<T> cons) {
        try {
            T dataClass = sourceClass.getConstructor().newInstance();

            Seq<Field> fields = Seq.with(sourceClass.getFields());
            Document defaultObject = new Document();

            fields.each(field -> !specialKeys.contains(field.getName()), field -> {
                try {
                    defaultObject.append(field.getName(), field.get(dataClass));
                } catch (Exception ignored) {}
            });

            filter.toBsonDocument().forEach(defaultObject::append);

            collection.findOneAndUpdate(filter, new BasicDBObject("$setOnInsert", defaultObject), new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)).subscribe(new Subscriber<>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Document document) {
                    fields.each(field -> {
                        try {
                            field.set(dataClass, document.getOrDefault(field.getName(), field.get(dataClass)));
                        } catch (Exception ignored) {}
                    });

                    dataClass.resetLatest();

                    try {
                        cons.get(dataClass);
                    } catch (Exception e) {
                        Log.err(e);
                    }
                }

                @Override
                public void onComplete() {}

                @Override
                public void onError(Throwable t) {
                    Log.err(t);
                }
            });
        } catch (Exception e) {
            Log.err(e);
        }
    }

    protected void save(MongoCollection<Document> collection) {
        Map<String, Object> values = getDeclaredPublicFields();
        BasicDBObject operations = DataChanges.toBsonOperations(latest, values);

        if (!operations.isEmpty()) {
            latest = values;
            collection.findOneAndUpdate(new BasicDBObject("_id", values.get("_id")), operations, (new FindOneAndUpdateOptions()).upsert(true).returnDocument(ReturnDocument.AFTER)).subscribe(new Subscriber<>() {
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                public void onNext(Document t) {}

                public void onComplete() {}

                public void onError(Throwable t) {
                    Log.err(t);
                }
            });
        }
    }

    protected Map<String, Object> getDeclaredPublicFields() {
        Field[] fields = getClass().getFields();
        Map<String, Object> values = new HashMap<>();

        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())) {
                try {
                    values.put(field.getName(), field.get(this));
                } catch (Exception ignored) {}
            }
        }

        return values;
    }

    protected void resetLatest() {
        latest = getDeclaredPublicFields();
    }

    @Override
    public String toString() {
        return getDeclaredPublicFields().toString();
    }
}