package com.nexarcore.aware.activemq.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.nexarcore.aware.activemq.client.config.Constants.DEFAULT_TOPIC;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActiveMQPublisher {

    String topic() default DEFAULT_TOPIC;

    boolean keepSessionAlive() default false;
}
