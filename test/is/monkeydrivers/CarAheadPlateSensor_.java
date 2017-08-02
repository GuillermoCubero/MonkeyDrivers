package is.monkeydrivers;

import is.monkeydrivers.utils.json.JSONSerializer;
import is.monkeydrivers.sensor.CarAheadPlateSensor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class CarAheadPlateSensor_ {
    private Bus bus;
    private Subscriber subscriber;

    @Before
    public void setUp() {
        bus = new SimpleBus();
        CarAheadPlateSensor carAheadPlateSensor = new CarAheadPlateSensor();
        carAheadPlateSensor.registerBus(bus);
        bus.subscribe(carAheadPlateSensor).to("camera");
        subscriber = mock(Subscriber.class);
        bus.subscribe(subscriber).to("plate");
    }

    @Test
    public void send_a_null_plate_message_when_receives_a_camera_message_not_containing_any_plate() {
        bus.send(cameraMessage(new JSONSerializer(new String[]{"tunnel"}, new String[]{"true"}).json()));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(1)).receive(captor.capture());
        assertThat(captor.getValue().type(), is("plate"));
        assertThat(captor.getValue().message(), is("null"));
    }

    @Test
    public void sends_a_plate_message_with_plate_1980FVK_when_receives_a_camera_message_containing_that_plate() {
        bus.send(cameraMessage(new JSONSerializer(new String[]{"tunnel", "plate"}, new String[]{"true", "1980FVK"}).json()));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(1)).receive(captor.capture());
        assertThat(captor.getValue().type(), is("plate"));
        assertThat(captor.getValue().message(), is("1980FVK"));
    }

    private Message cameraMessage(String content) {
        Message message = mock(Message.class);
        when(message.type()).thenReturn("camera");
        when(message.message()).thenReturn(content);
        when(message.timestamp()).thenReturn(Instant.now());
        return message;
    }

}
