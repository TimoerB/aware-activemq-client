package com.nexarcore.awareactivemqclient.state;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class ProducerState {

    private Map<Long, ActiveMQSessionProducer> producers = new HashMap<>();

    public void removeSessionProducer(long id) {
        producers.remove(id);
    }

    public void addSessionProducer(long id, ActiveMQSessionProducer sessionProducer) {
        producers.put(id, sessionProducer);
    }
}
