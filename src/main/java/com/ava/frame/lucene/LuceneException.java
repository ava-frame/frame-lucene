package com.ava.frame.lucene;


public class LuceneException extends RuntimeException {

    public LuceneException() {
    }

    public LuceneException(String message) {
        super(message);
    }

    public LuceneException(String message, Throwable cause) {
        super(message, cause);
    }

    public LuceneException(Throwable cause) {
        super(cause);
    }

    public LuceneException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
