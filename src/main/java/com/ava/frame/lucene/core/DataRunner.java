package com.ava.frame.lucene.core;


public interface DataRunner {
    long time();
    Class clazz();
    void handler(DocumentDefinition documentDefinition);
}
