package com.mongodb.util;

import com.mongodb.BasicDBObject;
import org.bson.BSONCallback;
import org.bson.Document;

/**
 * Compatibility stub: spring-data-mongodb 2.1.x calls JSON.parse() and JSON.serialize()
 * which were removed in mongodb-driver-legacy 4.x. Delegates to bson 4.x APIs.
 */
public class JSON {

    public static Object parse(final String json) {
        if (json == null || json.trim().isEmpty()) {
            return new BasicDBObject();
        }
        try {
            return new BasicDBObject(Document.parse(json));
        } catch (Exception e) {
            throw new JSONParseException(e.getMessage());
        }
    }

    public static Object parse(final String json, final BSONCallback callback) {
        return parse(json);
    }

    public static String serialize(final Object object) {
        if (object == null) {
            return "null";
        }
        return object.toString();
    }
}
