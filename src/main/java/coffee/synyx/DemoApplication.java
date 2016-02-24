package coffee.synyx;

import coffee.synyx.messaging.Receiver;
import coffee.synyx.messaging.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RestController
@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = "coffee.synyx.messaging")
public class DemoApplication {

    @Autowired
    private Receiver receiver;

    @Autowired
    private Sender sender;

    @RequestMapping(value="/receive",method= RequestMethod.GET)
    String receive() {
        try {
            return String.join(System.lineSeparator(), receiver.poll());
        } catch (Exception e) {
            throw new MessagingException(e);
        }
    }

	@RequestMapping(value="/send",method= RequestMethod.POST)
	String send(@RequestBody String body) {
        try {
            sender.send(body);
            System.out.println("sent message: " + body);
        }
        catch (Exception e) {
            throw new MessagingException(e);
        }
		return "sent message: " + body;
	}

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    class MessagingException extends RuntimeException {
        public MessagingException(Throwable cause) {
            super(cause);
        }
    }

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}