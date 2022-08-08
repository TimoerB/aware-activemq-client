package com.nexarcore.awareactivemqclient.aspect;

import com.nexarcore.awareactivemqclient.annotations.ActiveMQPublisher;
import com.nexarcore.awareactivemqclient.config.BeanConfig;
import com.nexarcore.awareactivemqclient.state.ActiveMQSessionProducer;
import com.nexarcore.awareactivemqclient.state.ProducerState;
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
    private final ProducerState producerState;

    @Around("@annotation(com.nexarcore.awareactivemqclient.annotations.ActiveMQPublisher)")
    public Object enableActiveMQProducer(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object proceed = proceedingJoinPoint.proceed();

        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();

        ActiveMQPublisher activeMQPublisher = method.getAnnotation(ActiveMQPublisher.class);
        long id = activeMQPublisher.id();

        log.debug("Publishing string {}", proceed);
        ActiveMQSessionProducer sessionProducer = producerState.getProducers().get(id);
        if (sessionProducer == null) {
            log.debug("No sessionProvider found for id {}, creating one.", id);
            Session session = producerSession();
            sessionProducer = ActiveMQSessionProducer.builder()
                    .withSession(session)
                    .withMessageProducer(messageProducer(session, activeMQPublisher.topic()))
                    .build();
            producerState.addSessionProducer(id, sessionProducer);
        }

        TextMessage message = sessionProducer.getSession().createTextMessage((String) proceed);
        sessionProducer.getMessageProducer().send(message);

        if (!activeMQPublisher.keepSessionAlive()) {
            sessionProducer.getMessageProducer().close();
            sessionProducer.getSession().close();
            producerState.removeSessionProducer(id);
            log.debug("Closed session and message producer for id {}", id);
        }

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
