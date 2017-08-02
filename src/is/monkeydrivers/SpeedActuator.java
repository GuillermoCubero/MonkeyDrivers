package is.monkeydrivers;

import is.monkeydrivers.utils.MessageParser;
import is.monkeydrivers.vehicle.Vehicle;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.*;

public class SpeedActuator implements Actuator {
    private Vehicle vehicle;
    private Double carAheadSpeed;
    private Double roadMaxSpeed;
    private final Map<String, MessageParser> messageParsers = new HashMap();

    {
        messageParsers.put("carAheadSpeed", message -> parseCarAheadSpeed(message));
        messageParsers.put("roadMaxSpeed", message -> parseRoadMaxSpeed(message));
    }

    @Override
    public void registerVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public void receive(Message message) {
        setCurrentMessage(message);
        vehicle.setSpeed(calculateNewSpeed());
    }

    private double calculateNewSpeed() {
        return unknownRoadMaxSpeed() ? vehicle.getSpeed() :
                knownCarAheadSpeed() && carAheadSpeed < roadMaxSpeed ? carAheadSpeed : roadMaxSpeed;
    }

    private void setCurrentMessage(Message currentMessage) {
        messageParserOfType(currentMessage.type()).parse(currentMessage);
    }

    private boolean knownCarAheadSpeed() {
        return carAheadSpeed != null;
    }

    private boolean unknownRoadMaxSpeed() {
        return roadMaxSpeed == null;
    }

    private MessageParser messageParserOfType(String type) {
        return messageParsers.get(type);
    }

    private void parseRoadMaxSpeed(Message message) {
        roadMaxSpeed = parseDouble(message.message());
    }

    private void parseCarAheadSpeed(Message message) {
        carAheadSpeed = message.message().equals("null") ? null : parseDouble(message.message());
    }

}
