package com.client.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Юлия on 11.12.2016.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name();
}
