package is.monkeydrivers.sensor;

import is.monkeydrivers.Bus;
import is.monkeydrivers.Message;
import is.monkeydrivers.utils.json.JSONDeserializer;

import java.time.Instant;

public class CarAheadPlateSensor implements VirtualSensor {

    private Bus bus;

    @Override
    public void registerBus(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void receive(Message message) {
        bus.send(message(message));
    }

    private String getPlateFrom(Message message) {
        return new JSONDeserializer(message.message()).getValueOfField("plate");
    }

    private Message message(Message message) {
        return new Message() {
            @Override
            public String type() {
                return "plate";
            }

            @Override
            public String message() {
                return getPlateFrom(message);
            }

            @Override
            public Instant timestamp() {
                return message.timestamp();
            }
        };
    }

}
