package com.nexarcore.aware.activemq.client.state;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.jms.MessageConsumer;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class ConsumerState {

    private Map<String, MessageConsumer> consumers = new HashMap<>();

    public void removeConsumer(String id) {
        consumers.remove(id);
    }

    public void addConsumer(String id, MessageConsumer consumer) {
        consumers.put(id, consumer);
    }
}
