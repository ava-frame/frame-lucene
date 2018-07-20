package com.ava.frame.lucene.core;


import com.ava.frame.lucene.LuceneException;

public class Index {
    public enum Field {
        Array, List, String, Long, Int, Double, Float, Boolean,Text,ArrayText;
    }

    public enum Store {
        Yes, NO;

        public org.apache.lucene.document.Field.Store store() {
            switch (this) {
                case Yes:
                    return org.apache.lucene.document.Field.Store.YES;
                case NO:
                    return org.apache.lucene.document.Field.Store.NO;
                default:
                    throw new LuceneException("no store type");
            }
        }
    }
}
