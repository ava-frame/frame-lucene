package com.ava.frame.lucene.core;

import org.apache.lucene.search.IndexSearcher;


public interface SearcherHandler {
      QueryResult handler(IndexSearcher searcher);
}
