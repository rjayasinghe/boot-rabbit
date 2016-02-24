package coffee.synyx.messaging;

import coffee.synyx.Constants;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

/**
 * Created by jayasinghe on 24/02/16.
 */
@Service
public class Receiver {
    enum Status {
        INITIAL,
        CONNECTING,
        CONNECTED
    }

    private Optional<Connection> conn = Optional.empty();
    private Optional<Channel> channel = Optional.empty();

    private volatile Status status = Status.INITIAL;

    //messages polled from rabbitmq
    private final BlockingQueue<String> receivedMessages = new LinkedBlockingDeque<String>();

    public void init() throws IOException, TimeoutException {
        this.status = Status.CONNECTING;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.99.100");

            this.conn = Optional.of(factory.newConnection());
            this.channel = Optional.of(conn.get().createChannel());

            this.channel.get().queueDeclare(Constants.DEMO_QUEUE_NAME, false, false, false, null);
            System.out.println("waiting for messages.");


            Consumer firstConsumer = new DefaultConsumer(this.channel.get()) {
                public void handleDelivery(String s, Envelope envelope,
                                           AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
                    String receivedMsg = new String(bytes, "UTF-8");
                    try {
                        receivedMessages.put(receivedMsg);
                    }
                    catch(InterruptedException iex) {
                        throw new RuntimeException(iex);
                    }
                    System.out.println("received message " + receivedMsg);
                }
            };
            channel.get().basicConsume(Constants.DEMO_QUEUE_NAME, true, firstConsumer);
            this.status = Status.CONNECTED;
        }
        catch(Exception e) {
            this.status = Status.INITIAL;
            throw e;
        }
    }

    public List<String> poll() throws IOException, TimeoutException, InterruptedException {
        synchronized (this.status) {
            if (this.status == Status.INITIAL) {
                init();
            }
        }
        List<String> results = new ArrayList<String>();
        while(!this.receivedMessages.isEmpty()) {
            results.add(this.receivedMessages.take());
        }
        /*
        for(String result : this.receivedMessages) {
            results.add(result);
        }
        */
        return results;
    }

    public void close() throws IOException, TimeoutException {
        synchronized (this.status) {
            if(channel.isPresent()) {
                channel.get().close();
            }
            if(conn.isPresent()) {
                conn.get().close();
            }
            this.status = Status.INITIAL;
        }
    }
}
