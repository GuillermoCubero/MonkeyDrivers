package is.monkeydrivers.sensor;

import is.monkeydrivers.Bus;
import is.monkeydrivers.Message;
import is.monkeydrivers.utils.MessageParser;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.*;
import static java.time.Instant.*;

public class CarAheadSpeedSensor implements VirtualSensor {
    private Bus bus;

    private InstantMessages old;
    private InstantMessages latest;

    @Override
    public void registerBus(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void receive(Message currentMessage) {
        registerMessage(currentMessage);
        bus.send(carAheadSpeedMessage(generateStringForMessage()));
    }

    private String generateStringForMessage() {
        return isReadyToCalculate() ? String.valueOf(calculateSpeed()) : "null";
    }

    private Message carAheadSpeedMessage(String message) {
        return new Message() {
            @Override
            public String type() {
                return "carAheadSpeed";
            }

            @Override
            public String message() {
                return message;
            }

            @Override
            public Instant timestamp() {
                return now();
            }
        };
    }

    private double calculateSpeed() {
        return latest.speed + msToKmh(distanceDifference() / instantDifferenceInSeconds(latest.instant, old.instant));
    }

    private double msToKmh(double speed) {
        return speed * 3.6f;
    }

    private double distanceDifference() {
        return latest.distance - old.distance;
    }

    private boolean isReadyToCalculate() {
        return bothInstantAreComplete() && isTheSameCar();
    }

    private boolean bothInstantAreComplete() {
        return old != null && old.isComplete() && latest.isComplete();
    }

    private boolean isTheSameCar() {
        return old.plate.equals(latest.plate) && !old.plate.equals("null") && !latest.plate.equals("null");
    }

    private void registerMessage(Message currentMessage) {
        if (latest == null) latest = new InstantMessages(currentMessage);
        else if (!latest.setMessage(currentMessage)) {
            old = latest;
            latest = new InstantMessages(currentMessage);
        }
    }

    private double instantDifferenceInSeconds(Instant t1, Instant t0) {
        return (double) (t1.getEpochSecond() - t0.getEpochSecond()) + (double) (t1.getNano() - t0.getNano()) / 1000000000d;
    }

    private class InstantMessages {
        Instant instant;
        String plate;
        Double distance;
        Double speed;

        private final Map<String, MessageParser> messageParsers = new HashMap();

        {
            messageParsers.put("distance", message -> distance = parseDouble(message.message()));
            messageParsers.put("speed", message -> speed = parseDouble(message.message()));
            messageParsers.put("plate", message -> plate = message.message());
        }

        public InstantMessages(Message currentMessage) {
            this.instant = currentMessage.timestamp();
            setMessage(currentMessage);
        }

        public boolean setMessage(Message currentMessage) {
            if (instantDifferenceInSeconds(currentMessage.timestamp(), instant) > 0.25d) return false;
            setCurrentMessage(currentMessage);
            return true;
        }

        public boolean isComplete() {
            return plate != null && distance != null && speed != null;
        }

        private void setCurrentMessage(Message message) {
            messageParserOfType(message.type()).parse(message);
        }

        private MessageParser messageParserOfType(String type) {
            return messageParsers.get(type);
        }

    }
}
