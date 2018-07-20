package com.ava.frame.lucene.core;

import java.util.List;

public class SearchResult {
    private Integer total;
    private Integer page;
    private Integer size;
    private List results;

    public SearchResult(Integer total, Integer page, Integer size, List results) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.results = results;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public List getResults() {
        return results;
    }

    public void setResults(List results) {
        this.results = results;
    }
}
