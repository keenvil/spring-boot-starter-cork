package com.mongodb.util;

/**
 * Compatibility stub: spring-data-mongodb 2.1.x catches this exception type
 * which was removed in mongodb-driver-legacy 4.x. Driver 4.x never throws it,
 * so this stub satisfies the JVM class verifier without affecting behavior.
 */
public class JSONParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    JSONParseException(final String json, final int index) {
        super("JSON parse error at position " + index);
    }

    JSONParseException(final String json, final int index, final Throwable t) {
        super("JSON parse error at position " + index, t);
    }

    public JSONParseException(final String message) {
        super(message);
    }
}
