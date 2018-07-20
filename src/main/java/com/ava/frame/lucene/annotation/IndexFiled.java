package com.ava.frame.lucene.annotation;





import com.ava.frame.lucene.IndexCase;
import com.ava.frame.lucene.core.Index;

import java.lang.annotation.ElementType;


@java.lang.annotation.Target({ElementType.FIELD})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface IndexFiled {
    Index.Field field();

    Index.Store store() default Index.Store.Yes;

    IndexCase indexCase() default IndexCase.none;
}
