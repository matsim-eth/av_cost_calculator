package ch.ethz.matsim.av_cost_calculator;

public class CostCalculatorScenario {
    public enum VehicleType {
        SOLO, MIDSIZE, VAN, MINIBUS;

        public String getForR() {
            switch (this) {
                case SOLO: return "Solo";
                case MIDSIZE: return "Midsize";
                case VAN: return "Van";
                case MINIBUS: return "Minibus";
            }

            throw new IllegalStateException();
        }
    }

    public enum AreaType {
        URBAN, REGIONAL;

        public String getForR() {
            return (this == URBAN) ? "Urban" : "Regional";
        }
    }

    private VehicleType vehicleType = null;
    private AreaType areaType = null;

    private long fleetSize = -1;
    private boolean isFleetElectric = false;

    private double totalTime = -1.0;
    private double operationTime = -1.0;

    private double relativeActiveTime = -1.0;
    private double occupancy = -1.0;
    private double speed = -1.0;
    private double averagePassengerTripDistance = -1.0;
    private double relativeEmptyRideDistance = -1.0;
    private double relativeMaintenanceDistance = -1.0;
    private double relativeMaintenanceTime = -1.0;

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public AreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(AreaType areaType) {
        this.areaType = areaType;
    }

    public long getFleetSize() {
        return fleetSize;
    }

    public void setFleetSize(long fleetSize) {
        this.fleetSize = fleetSize;
    }

    public boolean isFleetElectric() {
        return isFleetElectric;
    }

    public void setFleetElectric(boolean fleetElectric) {
        isFleetElectric = fleetElectric;
    }

    public double getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(double operationTime) {
        this.operationTime = operationTime;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public double getRelativeActiveTime() {
        return relativeActiveTime;
    }

    public void setRelativeActiveTime(double relativeActiveTime) {
        this.relativeActiveTime = relativeActiveTime;
    }

    public double getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(double occupancy) {
        this.occupancy = occupancy;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getAveragePassengerTripDistance() {
        return averagePassengerTripDistance;
    }

    public void setAveragePassengerTripDistance(double averagePassengerTripDistance) {
        this.averagePassengerTripDistance = averagePassengerTripDistance;
    }

    public double getRelativeEmptyRideDistance() {
        return relativeEmptyRideDistance;
    }

    public void setRelativeEmptyRideDistance(double relativeEmptyRideDistance) {
        this.relativeEmptyRideDistance = relativeEmptyRideDistance;
    }

    public double getRelativeMaintenanceDistance() {
        return relativeMaintenanceDistance;
    }

    public void setRelativeMaintenanceDistance(double relativeMaintenanceDistance) {
        this.relativeMaintenanceDistance = relativeMaintenanceDistance;
    }

    public double getRelativeMaintenanceTime() {
        return relativeMaintenanceTime;
    }

    public void setRelativeMaintenanceTime(double relativeMaintenanceTime) {
        this.relativeMaintenanceTime = relativeMaintenanceTime;
    }
}
