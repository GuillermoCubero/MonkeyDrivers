package is.monkeydrivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SimpleBus implements Bus {
    private Map<String, List<Subscriber>> subscribers = new HashMap<>();

    @Override
    public Subscription subscribe(Subscriber subscriber) {
        return types -> {
            for (String type : types) subscribersOf(type).add(subscriber);
        };
    }

    @Override
    public void send(Message message) {
        subscribersOf(message.type()).forEach(s -> s.receive(message));
    }

    private List<Subscriber> subscribersOf(String type) {
        createIfNotExistSubscribersOf(type);
        return subscribers.get(type);
    }

    private void createIfNotExistSubscribersOf(String type) {
        if (!subscribers.containsKey(type))
            subscribers.put(type, new ArrayList<>());
    }

}
