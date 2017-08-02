package is.monkeydrivers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class Bus_ {
    private Bus bus;

    @Before
    public void setUp() {
        bus = new SimpleBus();
    }

    @Test
    public void should_not_send_a_message_if_the_subscriber_is_not_subscribed_to_that_type_of_message() throws Exception {
        Message message = messageOfType("foo");
        Subscriber subscriber = mock(Subscriber.class);

        bus.subscribe(subscriber).to("fii");
        bus.send(message);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(0)).receive(captor.capture());
    }

    @Test
    public void should_send_a_message_to_a_subscriber() throws Exception {
        Message message = messageOfType("foo");
        Subscriber subscriber = mock(Subscriber.class);

        bus.subscribe(subscriber).to("foo");
        bus.send(message);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(1)).receive(captor.capture());
        assertThat(captor.getValue().type(), is("foo"));
    }

    @Test
    public void should_send_only_the_messages_associated_to_a_subscriber() throws Exception {
        Subscriber subscriber = mock(Subscriber.class);
        bus.subscribe(subscriber).to("foo");

        Subscriber subscriber2 = mock(Subscriber.class);
        bus.subscribe(subscriber2).to("fii");

        bus.send(messageOfType("foo"));
        bus.send(messageOfType("fii"));
        bus.send(messageOfType("foo"));
        bus.send(messageOfType("fii"));

        ArgumentCaptor<Message> fooCaptor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber, times(2)).receive(fooCaptor.capture());
        for (Message message : fooCaptor.getAllValues())
            assertThat(message.type(), is("foo"));

        ArgumentCaptor<Message> fiiCaptor = ArgumentCaptor.forClass(Message.class);
        verify(subscriber2, times(2)).receive(fiiCaptor.capture());
        for (Message message : fiiCaptor.getAllValues())
            assertThat(message.type(), is("fii"));
    }

    private Message messageOfType(String type) {
        Message message = mock(Message.class);
        when(message.type()).thenReturn(type);
        return message;
    }

}
