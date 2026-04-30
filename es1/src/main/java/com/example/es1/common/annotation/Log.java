package com.example.es1.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String operation() default "";

    String docIdParam() default "";

    String keywordParam() default "";

    boolean recordParams() default true;

    boolean recordResult() default false;

    boolean recordDuration() default true;
}
