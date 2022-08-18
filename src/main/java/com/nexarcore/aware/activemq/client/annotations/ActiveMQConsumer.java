package com.nexarcore.aware.activemq.client.annotations;

import com.nexarcore.aware.activemq.client.config.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActiveMQConsumer {

    String topic() default Constants.DEFAULT_TOPIC;

    boolean keepSessionAlive() default true;
}
