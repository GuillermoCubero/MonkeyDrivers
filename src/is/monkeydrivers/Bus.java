package is.monkeydrivers;

public interface Bus {
    Subscription subscribe(Subscriber subscriber);

    void send(Message message);

    interface Subscription {
        void to(String... types);
    }

}
