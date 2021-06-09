package eu.arima.knativedemo.ingester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
public class MeasureController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeasureController.class);

    private final KafkaTemplate<String, Measure> kafkaTemplate;
    
    private final String topic;
    
    public MeasureController(@Value("${app.topic}") String topic,
                             KafkaTemplate<String, Measure> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @PostMapping("/measure")
    void newMeasure(@RequestBody Measure measure) throws InterruptedException {

        LOGGER.info("Measure received: {}", measure);

        this.kafkaTemplate.send(this.topic, measure);

    }

    public static class Measure implements Serializable {

        private String sensorId;

        private String value;

        public String getSensorId() {
            return sensorId;
        }

        public void setSensorId(String sensorId) {
            this.sensorId = sensorId;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Measure{" +
                    "sensorId='" + sensorId + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Measure measure = (Measure) o;

            if (getSensorId() != null ? !getSensorId().equals(measure.getSensorId()) : measure.getSensorId() != null)
                return false;
            return getValue() != null ? getValue().equals(measure.getValue()) : measure.getValue() == null;
        }

        @Override
        public int hashCode() {
            int result = getSensorId() != null ? getSensorId().hashCode() : 0;
            result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
            return result;
        }

    }

}
