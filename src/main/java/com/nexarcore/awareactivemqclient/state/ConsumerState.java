package com.nexarcore.awareactivemqclient.state;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.jms.MessageConsumer;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class ConsumerState {

    private Map<Long, MessageConsumer> consumers = new HashMap<>();

    public void removeConsumer(long id) {
        consumers.remove(id);
    }

    public void addConsumer(long id, MessageConsumer consumer) {
        consumers.put(id, consumer);
    }
}
