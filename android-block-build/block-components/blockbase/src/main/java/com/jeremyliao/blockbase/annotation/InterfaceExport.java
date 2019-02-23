package com.jeremyliao.blockbase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liaohailiang on 2019/1/24.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface InterfaceExport {

    String module();

    Class impl();

    int version() default 0;
}
