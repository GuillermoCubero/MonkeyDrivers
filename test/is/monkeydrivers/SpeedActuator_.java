package is.monkeydrivers;

import is.monkeydrivers.vehicle.Car;
import is.monkeydrivers.vehicle.Vehicle;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static java.time.Instant.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpeedActuator_ {
    private Bus bus;
    private Vehicle vehicle;

    @Before
    public void setUp() {
        bus = new SimpleBus();
        vehicle = new Car();

        SpeedActuator speedActuator = new SpeedActuator();
        speedActuator.registerVehicle(vehicle);
        bus.subscribe(speedActuator).to("carAheadSpeed");
        bus.subscribe(speedActuator).to("roadMaxSpeed");
    }

    @Test
    public void does_not_change_car_speed_when_have_not_received_any_data() {
        assertThat(vehicle.getSpeed(), is(0d));
    }

    @Test
    public void does_not_change_car_speed_when_does_not_know_road_max_speed() {
        bus.send(createMessage("carAheadSpeed", "10", now()));
        assertThat(vehicle.getSpeed(), is(0d));
        vehicle.setSpeed(80);
        assertThat(vehicle.getSpeed(), is(80d));
    }

    @Test
    public void sets_car_speed_to_max_road_speed() {
        bus.send(createMessage("roadMaxSpeed", "40", now()));
        assertThat(vehicle.getSpeed(), is(40d));
    }

    @Test
    public void reduces_car_speed_to_10_when_car_ahead_drives_at_10_km_per_hour() {
        bus.send(createMessage("roadMaxSpeed", "40", now()));
        assertThat(vehicle.getSpeed(), is(40d));
        bus.send(createMessage("carAheadSpeed", "10", now()));
        assertThat(vehicle.getSpeed(), is(10d));
    }

    @Test
    public void sets_car_speed_to_road_max_speed_when_can_not_calculate_car_ahead_speed() {
        bus.send(createMessage("roadMaxSpeed", "40", now()));
        assertThat(vehicle.getSpeed(), is(40d));
        bus.send(createMessage("carAheadSpeed", "10", now()));
        assertThat(vehicle.getSpeed(), is(10d));
        bus.send(createMessage("carAheadSpeed", "null", now()));
        assertThat(vehicle.getSpeed(), is(40d));
    }

    @Test
    public void does_not_increment_car_speed_to_road_max_speed_if_car_ahead_speed_is_lower() {
        bus.send(createMessage("roadMaxSpeed", "40", now()));
        bus.send(createMessage("carAheadSpeed", "10", now()));
        assertThat(vehicle.getSpeed(), is(10d));
        bus.send(createMessage("roadMaxSpeed", "40", now()));
        assertThat(vehicle.getSpeed(), is(10d));
    }

    private Message createMessage(String type, String content, Instant timestamp) {
        Message message = mock(Message.class);
        when(message.type()).thenReturn(type);
        when(message.timestamp()).thenReturn(timestamp);
        when(message.message()).thenReturn(content);
        return message;
    }

}
