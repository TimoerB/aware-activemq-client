# NexarCore's Aware ActiveMQ Client
Most of this module consists of ActiveMQ logic packed into aspects so that code can be easier run through annotations.

* The original package name 'com.nexarcore.aware-activemq-client' is invalid and this project uses 'com.nexarcore.awareactivemqclient' instead.

# Build

```
mvn clean install
```

Add as dependency in `pom.xml`:

```
<dependency>
    <groupId>com.nexarcore</groupId>
    <artifactId>aware-activemq-client</artifactId>
    <version>0.0.3</version>
</dependency>
```

# Usage

Specify the broker url: 
```
spring:
  activemq:
    broker-url: tcp://127.0.0.1:61616
```

## Subscription
To subscribe to a specific topic: 

```
@ActiveMQConsumer(topic = "topic-a")
public void receive(String received) {
    log.info("Received {}", received);
}
```

The consumer will be started in a separate session and will wait until a message drops on the specified topic. 
If so, the string will be handed over to the method which will log the message in this instance.

Make sure to provide a unique id for each consumer: 
```
@ActiveMQConsumer(topic = "topic", id = 2)
``` 

Alternatively, you may decide to close the session after each message consumption. The session will be instantly recreated after a message has been received to keep listening: 
```
@ActiveMQConsumer(topic = "topic", keepSessionAlive = false)
```

Per default, the session will be kept open.

## Publishing

Sending a message with a producer template can be done as follows:

```
@ActiveMQPublisher(topic = "topic-a")
public String publishRandomUuid(String text) {
    return UUID.randomUUID() + "-" + text;
}
```

After the method is called and has been run, a publisher will be started in a separate session and will send the returned string of the method to the ActiveMQ broker.

In the event that you need to have multiple publishers, ensure to provide a unique id for each publisher: 
```
@ActiveMQPublisher(topic = "topic", id = 37)
``` 

As with the consumer, there is an option to keep the session open. By default a session is closed right after sending a message, but you may decide to keep it open: 
```
@ActiveMQPublisher(topic = "topic", keepSessionAlive = true, id = 2)
```

# Troubleshooting
In case some beans aren't picked up in the bean factory, do a component scan on your spring boot app:

```
@ComponentScan(basePackages = {"com.nexarcore.awareactivemqclient"})
```

