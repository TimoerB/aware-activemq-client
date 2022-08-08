package com.nexarcore.awareactivemqclient.aspect;

import com.nexarcore.awareactivemqclient.annotations.ActiveMQConsumer;
import com.nexarcore.awareactivemqclient.config.BeanConfig;
import com.nexarcore.awareactivemqclient.state.ConsumerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.aspectj.lang.annotation.Aspect;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActiveMQConsumerAspect {

    private final BeanConfig beanConfig;
    private final ConsumerState consumerState;

    private void runAllAnnotatedWith(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(new MethodAnnotationsScanner()));

        reflections.getMethodsAnnotatedWith(annotation).forEach(method -> {
            ActiveMQConsumer consumerAnnotation = method.getAnnotation(ActiveMQConsumer.class);
            subscribeInDaemon(method, consumerAnnotation.topic(), consumerAnnotation.keepSessionAlive(), consumerAnnotation.id());
        });
    }

    private void subscribeInDaemon(Method m, String topic, boolean keepAlive, long id) {
        new Thread(() -> {
            String receivedMessage;
            try {
                receivedMessage = subscribe2topic(topic, keepAlive, id);
                m.invoke(null, receivedMessage);
                subscribeInDaemon(m, topic, keepAlive, id);
            } catch (Exception e) {
                log.error("Could not subscribe to topic: {}", e.getMessage());
            }
        }).start();
    }

    private String subscribe2topic(String topic, boolean keepAlive, long id) throws Exception {
        log.debug("Subscribing to topic {}", topic);

        MessageConsumer messageConsumer = consumerState.getConsumers().get(id);
        if (messageConsumer == null) {
            log.debug("No message consumer found for id {}, creating one.", id);
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(beanConfig.getBrokerUrl());
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session consumerSession = connection.createSession(false, AUTO_ACKNOWLEDGE);
            messageConsumer = consumerSession.createConsumer(consumerSession.createTopic(topic));
            consumerState.addConsumer(id, messageConsumer);
        }

        Message message = messageConsumer.receive();

        if (!keepAlive) {
            messageConsumer.close();
            consumerState.removeConsumer(id);
            log.debug("Removed message consumer for id {}", id);
        }

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            return textMessage.getText();
        }

        return null;
    }

    @EventListener(value = ApplicationReadyEvent.class)
    public void startConsumers() {
        runAllAnnotatedWith(ActiveMQConsumer.class);
    }
}
