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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActiveMQConsumerAspect {

    private final BeanConfig beanConfig;
    private final ConsumerState consumerState;
    private final ApplicationContext applicationContext;

    private void runAllAnnotatedWith(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(new MethodAnnotationsScanner()));

        Map<String, Object> consumerBeans = getBeansWithAnyAnnotations(List.of(Component.class, Service.class, Bean.class));
        reflections.getMethodsAnnotatedWith(annotation).forEach(method -> {
            String beanName = firstLetterLowercase(method.getDeclaringClass().getSimpleName());
            Object consumerBean = consumerBeans.get(beanName);
            if (consumerBean != null) {
                ActiveMQConsumer consumerAnnotation = method.getAnnotation(ActiveMQConsumer.class);
                subscribeInDaemon(consumerBean, method, consumerAnnotation.topic(), consumerAnnotation.keepSessionAlive(), beanName + "." + method.getName());
            }
            else {
                log.warn("No bean found for annotated method {}, not starting consumer.", method.getName());
            }
        });
    }

    private Map<String, Object> getBeansWithAnyAnnotations(List<Class<?>> annotations) {
        Map<String, Object> beanMap = new HashMap<>();
        annotations.forEach(aClass -> beanMap.putAll(applicationContext.getBeansWithAnnotation(Component.class)));
        return beanMap;
    }

    private String firstLetterLowercase(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private void subscribeInDaemon(Object o, Method m, String topic, boolean keepAlive, String id) {
        new Thread(() -> {
            String receivedMessage;
            try {
                receivedMessage = subscribe2topic(topic, keepAlive, id);
                m.invoke(o, receivedMessage);
                subscribeInDaemon(o, m, topic, keepAlive, id);
            } catch (Exception e) {
                log.error("Could not subscribe to topic: {}", e.getMessage());
            }
        }).start();
    }

    private String subscribe2topic(String topic, boolean keepAlive, String id) throws Exception {
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
