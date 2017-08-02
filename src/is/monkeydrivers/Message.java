package is.monkeydrivers;

import java.time.Instant;

public interface Message {
    String type();

    String message();

    Instant timestamp();
}
