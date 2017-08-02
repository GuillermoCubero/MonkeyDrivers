package is.monkeydrivers.utils;

import is.monkeydrivers.Message;

@FunctionalInterface
public interface MessageParser {
    void parse(Message message);
}
