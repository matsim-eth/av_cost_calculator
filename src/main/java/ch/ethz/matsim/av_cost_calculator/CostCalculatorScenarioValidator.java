package ch.ethz.matsim.av_cost_calculator;

import java.util.LinkedList;
import java.util.List;

public class CostCalculatorScenarioValidator {
    final private List<String> errors = new LinkedList<>();

    public boolean validate(CostCalculatorScenario scenario) {
        errors.clear();
        boolean result = true;

        if (scenario.getVehicleType() == null) {
            errors.add("Vehicle type must be set");
            result = false;
        }

        if (scenario.getAreaType() == null) {
            errors.add("Area type must be set");
            result = false;
        }

        if (scenario.getFleetSize() < 0) {
            errors.add("Fleet size must be non-negative");
            result = false;
        }

        if (scenario.getTotalTime() < 0) {
            errors.add("Total time must be non-negative");
            result = false;
        }

        if (scenario.getOperationTime() < 0) {
            errors.add("Operation time must be non-negative");
            result = false;
        }

        if (scenario.getOperationTime() > scenario.getTotalTime()) {
            errors.add("Operation time must be smaller than total time");
            result = false;
        }

        if (scenario.getRelativeActiveTime() < 0.0 || scenario.getRelativeActiveTime() > 1.0) {
            errors.add("Relative active time must be between 0 and 1");
            result = false;
        }

        if (scenario.getOccupancy() < 0.0 || scenario.getOccupancy() > 1.0) {
            errors.add("Occupancy must be between 0 and 1");
            result = false;
        }

        if (scenario.getSpeed() > 0) {
            errors.add("Speed must be positive");
            result = false;
        }

        if (scenario.getAveragePassengerTripDistance() > 0) {
            errors.add("Average passenger trip distance must be positive");
            result = false;
        }

        if (scenario.getRelativeEmptyRideDistance() < 0.0 || scenario.getRelativeEmptyRideDistance() > 1.0) {
            errors.add("Relative empty ride distance must be between 0 and 1");
            result = false;
        }

        if (scenario.getRelativeMaintenanceDistance() < 0.0 || scenario.getRelativeMaintenanceDistance() > 1.0) {
            errors.add("Relative maintenance distance must be between 0 and 1");
            result = false;
        }

        if (scenario.getRelativeMaintenanceTime() < 0.0 || scenario.getRelativeMaintenanceTime() > 1.0) {
            errors.add("Relative maintenance time must be between 0 and 1");
            result = false;
        }

        return result;
    }

    public List<String> getErrors() {
        return new LinkedList<>(errors);
    }
}
