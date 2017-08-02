package is.monkeydrivers;

import is.monkeydrivers.vehicle.Vehicle;

public interface Actuator extends Subscriber {
    void registerVehicle(Vehicle vehicle);
}
