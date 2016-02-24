package coffee.synyx.messaging;

import coffee.synyx.Constants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Created by jayasinghe on 24/02/16.
 */
@Service
public class Sender {
    public void send(String message) throws IOException, TimeoutException
    {
        Optional<Connection > conn = Optional.empty();
        Optional<Channel> channel = Optional.empty();

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.99.100");
            conn = Optional.of(factory.newConnection());
            channel = Optional.of(conn.get().createChannel());
            channel.get().queueDeclare(Constants.DEMO_QUEUE_NAME, false, false, false, null);
            channel.get().basicPublish("", Constants.DEMO_QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
        finally {
            if(channel.isPresent()) {
                channel.get().close();
            }
            if(conn.isPresent()) {
                conn.get().close();
            }
        }
    }
}
