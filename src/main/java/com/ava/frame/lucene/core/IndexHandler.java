package com.ava.frame.lucene.core;

import org.apache.lucene.index.IndexWriter;


public interface IndexHandler {
    void handler(IndexWriter trackingIndexWriter);
}
