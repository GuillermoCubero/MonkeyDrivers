package is.monkeydrivers.vehicle;

public class Car implements Vehicle {
    private double speed;

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

}
