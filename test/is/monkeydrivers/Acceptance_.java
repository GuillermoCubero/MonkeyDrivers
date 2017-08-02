package is.monkeydrivers;

import is.monkeydrivers.utils.json.JSONSerializer;
import is.monkeydrivers.sensor.CarAheadPlateSensor;
import is.monkeydrivers.sensor.CarAheadSpeedSensor;
import is.monkeydrivers.vehicle.Car;
import is.monkeydrivers.vehicle.Vehicle;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static java.time.Instant.now;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Acceptance_ {

    private Bus bus;
    private Vehicle vehicle;
    private Instant instantZero = now();

    @Before
    public void setUp() {
        bus = new SimpleBus();
        vehicle = new Car();
        vehicle.setSpeed(50);
        CarAheadSpeedSensor carAheadSpeedSensor = new CarAheadSpeedSensor();
        CarAheadPlateSensor carAheadPlateSensor = new CarAheadPlateSensor();
        SpeedActuator speedActuator = new SpeedActuator();

        carAheadSpeedSensor.registerBus(bus);
        carAheadPlateSensor.registerBus(bus);
        speedActuator.registerVehicle(vehicle);

        bus.subscribe(carAheadSpeedSensor).to("speed");
        bus.subscribe(carAheadSpeedSensor).to("distance");
        bus.subscribe(carAheadSpeedSensor).to("plate");

        bus.subscribe(carAheadPlateSensor).to("camera");

        bus.subscribe(speedActuator).to("carAheadSpeed");
        bus.subscribe(speedActuator).to("roadMaxSpeed");
    }

    @Test
    public void speed_actuator_sets_vehicle_speed_and_vehicle_accelerates_to_equalize_car_ahead_speed() {
        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"true", "1980FVK"}).json(), instantZero.plusSeconds(0)));
        bus.send(createMessage("speed", "60", instantZero.plusSeconds(0)));
        bus.send(createMessage("distance", "10", instantZero.plusSeconds(0)));
        bus.send(createMessage("roadMaxSpeed", "80", instantZero.plusSeconds(0)));

        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"false", "1980FVK"}).json(), instantZero.plusSeconds(2)));
        bus.send(createMessage("speed", "60", instantZero.plusSeconds(2)));
        bus.send(createMessage("distance", "12", instantZero.plusSeconds(2)));
        assertEquals(63.6d, vehicle.getSpeed(), 0.2);
    }

    @Test
    public void speed_actuator_sets_vehicle_speed_and_vehicle_decelerates_to_equalize_car_ahead_speed() {
        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"false", "1980FVK"}).json(), instantZero.plusSeconds(0)));
        bus.send(createMessage("roadMaxSpeed", "80", instantZero.plusSeconds(0)));
        bus.send(createMessage("distance", "10", instantZero.plusSeconds(0)));
        bus.send(createMessage("speed", "60", instantZero.plusSeconds(0)));

        bus.send(createMessage("speed", "60", instantZero.plusSeconds(2)));
        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"true", "1980FVK"}).json(), instantZero.plusSeconds(2)));
        bus.send(createMessage("distance", "8", instantZero.plusSeconds(2)));
        assertEquals(56.4d, vehicle.getSpeed(), 0.2);
    }

    @Test
    public void speed_actuator_sets_vehicle_speed_and_vehicle_decelerates_to_equalize_max_road_speed_when_car_ahead_disappears() {
        bus.send(createMessage("roadMaxSpeed", "80", instantZero.plusSeconds(0)));
        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"false", "1980FVK"}).json(), instantZero.plusSeconds(0)));
        bus.send(createMessage("speed", "60", instantZero.plusSeconds(0)));
        bus.send(createMessage("distance", "10", instantZero.plusSeconds(0)));

        bus.send(createMessage("speed", "60", instantZero.plusSeconds(2)));
        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"true", "1980FVK"}).json(), instantZero.plusSeconds(2)));
        bus.send(createMessage("distance", "8", instantZero.plusSeconds(2)));
        assertEquals(56.4d, vehicle.getSpeed(), 0.2);

        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel"}, new String[]{"false"}).json(), instantZero.plusSeconds(0)));
        assertThat(vehicle.getSpeed(), is(80d));
    }

    @Test
    public void speed_actuator_sets_vehicle_speed_and_vehicle_does_not_accelerate_to_equalize_car_ahead_speed_when_car_ahead_speed_is_higher_than_max_road_speed() {
        bus.send(createMessage("roadMaxSpeed", "80", instantZero.plusSeconds(0)));
        assertThat(vehicle.getSpeed(), is(80d));

        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"false", "1980FVK"}).json(), instantZero.plusSeconds(0)));
        bus.send(createMessage("speed", "70", instantZero.plusSeconds(0)));
        bus.send(createMessage("distance", "10", instantZero.plusSeconds(0)));

        bus.send(createMessage("speed", "70", instantZero.plusSeconds(2)));
        bus.send(createMessage("camera", new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"true", "1980FVK"}).json(), instantZero.plusSeconds(2)));
        bus.send(createMessage("distance", "50", instantZero.plusSeconds(2)));
        assertThat(vehicle.getSpeed(), is(80d));
    }

    @Test
    public void speed_actuator_sets_vehicle_speed_and_vehicle_sets_speed_to_new_max_road_speed() {
        bus.send(createMessage("roadMaxSpeed", "80", instantZero.plusSeconds(0)));
        assertThat(vehicle.getSpeed(), is(80d));
        bus.send(createMessage("roadMaxSpeed", "60", instantZero.plusSeconds(2)));
        assertThat(vehicle.getSpeed(), is(60d));
        bus.send(createMessage("roadMaxSpeed", "100", instantZero.plusSeconds(6)));
        assertThat(vehicle.getSpeed(), is(100d));
    }

    private Message createMessage(String type, String content, Instant timestamp) {
        Message message = mock(Message.class);
        when(message.message()).thenReturn(content);
        when(message.type()).thenReturn(type);
        when(message.timestamp()).thenReturn(timestamp);
        return message;
    }

}
