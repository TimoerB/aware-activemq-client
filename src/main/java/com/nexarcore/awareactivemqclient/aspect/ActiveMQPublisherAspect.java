package com.nexarcore.awareactivemqclient.aspect;

import com.nexarcore.awareactivemqclient.config.BeanConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.lang.reflect.Method;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActiveMQPublisherAspect {

    private final BeanConfig beanConfig;

    @Around("@annotation(com.nexarcore.awareactivemqclient.aspect.ActiveMQPublisher)")
    public Object enableActiveMQProducer(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object proceed = proceedingJoinPoint.proceed();

        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();

        ActiveMQPublisher activeMQPublisher = method.getAnnotation(ActiveMQPublisher.class);

        log.debug("Publishing string {}", proceed);
        Session session = producerSession();
        TextMessage message = session.createTextMessage((String) proceed);
        MessageProducer messageProducer = messageProducer(session, activeMQPublisher.topic());
        messageProducer.send(message);

        messageProducer.close();
        session.close();

        return proceed;
    }

    private MessageProducer messageProducer(Session session, String topic) throws Exception {
        return session.createProducer(session.createTopic(topic));
    }

    private Session producerSession() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(beanConfig.getBrokerUrl());
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection.createSession(false, AUTO_ACKNOWLEDGE);
    }
}
