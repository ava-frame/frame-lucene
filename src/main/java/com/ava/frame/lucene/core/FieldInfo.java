package com.ava.frame.lucene.core;


public class FieldInfo {
    private String field;
    private float boost;
    private String keyword;

    public FieldInfo(String field, float boost) {
        this.field = field;
        this.boost = boost;
    }

    public FieldInfo(String field) {
        this.field = field;
        this.boost = 1.0f;
    }

    public FieldInfo(String field, float boost, String keyword) {
        this.field = field;
        this.boost = boost;
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public float getBoost() {
        return boost;
    }

    public void setBoost(float boost) {
        this.boost = boost;
    }
}
