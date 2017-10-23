package ch.ethz.matsim.av_cost_calculator;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CostCalculator {
    final private static Logger logger = Logger.getLogger(CostCalculator.class);

    final private CostCalculatorScenarioValidator validator;
    final private CostCalculatorExecutor executor;

    public CostCalculator(CostCalculatorScenarioValidator validator, CostCalculatorExecutor executor) {
        this.validator = validator;
        this.executor = executor;
    }

    double computePricePerPassengerKm(CostCalculatorScenario scenario) {
        if (validator.validate(scenario)) {
            Map<String, String> parameters = new HashMap<>();

            parameters.put("Area", scenario.getAreaType().getForR());
            parameters.put("VehicleType", scenario.getVehicleType().getForR());
            parameters.put("FleetSize", String.valueOf(scenario.getFleetSize()));
            parameters.put("electric", scenario.isFleetElectric() ? "1" : "0");
            parameters.put("automated", "");
            parameters.put("fleetOperation", "");

            parameters.put("ph_operationHours_av", String.valueOf(scenario.getOperationTime() / 3600.0));
            parameters.put("ph_operationHours", "0");
            parameters.put("ph_relActiveTime", String.valueOf(scenario.getRelativeActiveTime() * 100.0));
            parameters.put("ph_avOccupancy", String.valueOf(scenario.getOccupancy() * 100.0));
            parameters.put("ph_avSpeed", String.valueOf(scenario.getSpeed() * 3.6));
            parameters.put("ph_avTripLengthPass", String.valueOf(scenario.getAveragePassengerTripDistance() / 1000.0));
            parameters.put("ph_relEmptyRides", String.valueOf(scenario.getRelativeEmptyRideDistance() * 100.0));
            parameters.put("ph_relMaintenanceRides", String.valueOf(scenario.getRelativeMaintenanceDistance() * 100.0));
            parameters.put("ph_relMaintenanceHours", String.valueOf(scenario.getRelativeMaintenanceTime() * 100.0));

            return executor.computePricePerPassengerKm(parameters);
        } else {
            logger.info(scenario);

            for (String error : validator.getErrors()) {
                logger.error(error);
            }

            throw new IllegalStateException();
        }
    }
}
