package is.monkeydrivers;

import is.monkeydrivers.sensor.CarAheadSpeedSensor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static java.time.Instant.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CarAheadSpeedSensor_ {
    private Bus bus;
    private Subscriber subscriber;

    @Before
    public void setUp() {
        bus = new SimpleBus();
        CarAheadSpeedSensor carAheadSpeedSensor = new CarAheadSpeedSensor();
        carAheadSpeedSensor.registerBus(bus);

        bus.subscribe(carAheadSpeedSensor).to("speed");
        bus.subscribe(carAheadSpeedSensor).to("distance");
        bus.subscribe(carAheadSpeedSensor).to("plate");

        subscriber = mock(Subscriber.class);
        bus.subscribe(subscriber).to("carAheadSpeed");
    }

    @Test
    public void does_not_send_a_car_ahead_speed_message_if_does_not_have_2_messages_with_different_timestamps() {
        bus.send(createMessage("speed", "50", now()));
        bus.send(createMessage("distance", "10", now()));
        bus.send(createMessage("plate", "1980FVK", now()));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(3)).receive(captor.capture());
        for (Message message : captor.getAllValues()) assertThat(message.message(), is("null"));
    }

    @Test
    public void does_not_send_a_car_ahead_speed_message_when_the_ahead_car_changes() {
        bus.send(createMessage("speed", "50", now()));
        bus.send(createMessage("distance", "10", now()));
        bus.send(createMessage("plate", "1980FVK", now()));

        bus.send(createMessage("speed", "50", now().plusSeconds(2)));
        bus.send(createMessage("distance", "12", now().plusSeconds(2)));
        bus.send(createMessage("plate", "9980FVK", now().plusSeconds(2)));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(6)).receive(captor.capture());
        for (Message message : captor.getAllValues()) assertThat(message.message(), is("null"));
    }

    @Test
    public void sends_53_6_km_per_hour_car_ahead_speed_message_when_driving_at_50_km_per_hour_and_car_ahead_reduces_distance_in_2_meters_in_2_seconds() {
        bus.send(createMessage("speed", "50", now()));
        bus.send(createMessage("distance", "10", now()));
        bus.send(createMessage("plate", "1980FVK", now()));

        bus.send(createMessage("speed", "50", now().plusSeconds(2)));
        bus.send(createMessage("distance", "8", now().plusSeconds(2)));
        bus.send(createMessage("plate", "1980FVK", now().plusSeconds(2)));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(6)).receive(captor.capture());

        List<Message> messages = captor.getAllValues();
        for (int i = 0; i < messages.size() - 1; i++) assertThat(messages.get(i).message(), is("null"));
        assertThat(captor.getValue().type(), is("carAheadSpeed"));
        assertEquals(46.4d, Double.parseDouble(captor.getValue().message()), 0.2);
    }

    @Test
    public void sends_null_car_ahead_speed_message_when_driving_at_50_km_per_hour_and_there_is_no_car_ahead() {
        bus.send(createMessage("speed", "50", now()));
        bus.send(createMessage("distance", "-1", now()));
        bus.send(createMessage("plate", "null", now()));

        bus.send(createMessage("speed", "50", now().plusSeconds(2)));
        bus.send(createMessage("distance", "-1", now().plusSeconds(2)));
        bus.send(createMessage("plate", "null", now().plusSeconds(2)));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(6)).receive(captor.capture());
        for (Message message : captor.getAllValues()) assertThat(message.message(), is("null"));
    }

    @Test
    public void sends_2_correct_speed_car_ahead_speed_messages_when_driving_at_50_km_per_hour_and_car_ahead_increments_distance_in_2_time_intervals() {
        bus.send(createMessage("speed", "50", now()));
        bus.send(createMessage("distance", "10", now()));
        bus.send(createMessage("plate", "1980FVK", now()));

        bus.send(createMessage("speed", "50", now().plusSeconds(2)));
        bus.send(createMessage("distance", "12", now().plusSeconds(2)));
        bus.send(createMessage("plate", "1980FVK", now().plusSeconds(2)));

        bus.send(createMessage("speed", "50", now().plusSeconds(4)));
        bus.send(createMessage("distance", "16", now().plusSeconds(4)));
        bus.send(createMessage("plate", "1980FVK", now().plusSeconds(4)));


        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(9)).receive(captor.capture());
        List<Message> messages = captor.getAllValues();
        for (int i = 0; i < messages.size(); i++) {
            assertThat(messages.get(i).type(), is("carAheadSpeed"));
            if (i == 5 || i == 8) continue;
            assertThat(messages.get(i).message(), is("null"));
        }
        assertEquals(53.6d, Double.parseDouble(messages.get(5).message()), 0.2);
        assertEquals(57.2d, Double.parseDouble(captor.getValue().message()), 0.2);
    }

    private Message createMessage(String type, String content, Instant timestamp) {
        Message message = mock(Message.class);
        when(message.type()).thenReturn(type);
        when(message.message()).thenReturn(content);
        when(message.timestamp()).thenReturn(timestamp);
        return message;
    }

}
