package eu.arima.knativeeventing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class KnativeEventingApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(KnativeEventingApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(KnativeEventingApplication.class, args);
	}

	@Bean
	Consumer<String> eventConsumer(@Value("${app.delay}") int delay) {

		LOGGER.info("Configured delay: {}", delay);

		return (data) -> this.process(data, delay);
	}

	private void process(String data, int delay) {

		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Unexpected error sleeping", e);
		}

		LOGGER.info("Measure processed: {}", data);
	}

}
