package com.example.lianx;


import com.example.lianx.util.MailClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=LianxApplication.class)
public class Test {

   @Autowired KafkaProducer kafkaProducer;

    @org.junit.Test
    public void test(){
      kafkaProducer.send("test","asd");
        kafkaProducer.send("test","2222");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
@Component
class KafkaProducer{
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void send(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class KafkaConsumer{

    @KafkaListener(topics = "test")
    public void handle(ConsumerRecord consumerRecord){
        System.out.println(consumerRecord.value());
    }
}