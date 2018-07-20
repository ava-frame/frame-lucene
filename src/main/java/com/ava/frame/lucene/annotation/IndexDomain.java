package com.ava.frame.lucene.annotation;

import java.lang.annotation.ElementType;

@java.lang.annotation.Target({ElementType.TYPE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface IndexDomain {
    String domain();
}
