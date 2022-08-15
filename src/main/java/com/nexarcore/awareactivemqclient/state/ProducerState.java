package com.nexarcore.awareactivemqclient.state;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class ProducerState {

    private Map<String, ActiveMQSessionProducer> producers = new HashMap<>();

    public void removeSessionProducer(String id) {
        producers.remove(id);
    }

    public void addSessionProducer(String id, ActiveMQSessionProducer sessionProducer) {
        producers.put(id, sessionProducer);
    }
}
