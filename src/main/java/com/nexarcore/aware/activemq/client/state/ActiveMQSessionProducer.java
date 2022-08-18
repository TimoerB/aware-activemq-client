package com.nexarcore.aware.activemq.client.state;

import lombok.Builder;
import lombok.Getter;

import javax.jms.MessageProducer;
import javax.jms.Session;

@Getter
@Builder(setterPrefix = "with")
public class ActiveMQSessionProducer {

    private Session session;
    private MessageProducer messageProducer;
}
