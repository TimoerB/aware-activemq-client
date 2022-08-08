package com.nexarcore.awareactivemqclient.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.nexarcore.awareactivemqclient.config.Constants.DEFAULT_TOPIC;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActiveMQPublisher {

    String topic() default DEFAULT_TOPIC;

    boolean keepSessionAlive() default false;

    long id() default 1L;
}
