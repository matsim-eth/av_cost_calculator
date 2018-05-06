package ch.ethz.matsim.av_cost_calculator.run;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class ParameterBuilder {
	final private Logger logger = Logger.getLogger(ParameterBuilder.class);
	final private double scaling;
	final private double totalTime;

	public ParameterBuilder(double scaling, double totalTime) {
		this.scaling = scaling;
		this.totalTime = totalTime;
	}

	public Map<String, String> build(PricingAnalysisHandler analysisHandler) {
		Map<String, String> parameters = new TreeMap<>();

		int fleetSize = (int) (analysisHandler.getFleetSize() / scaling);

		if (fleetSize < 150) {
			logger.warn("Requested fleet size of " + analysisHandler.getFleetSize()
					+ " vehicles is to low. Setting to 150 (minimum value for cost calculator)");
		}

		parameters.put("Area", "Urban");
		parameters.put("VehicleType", "Solo");
		parameters.put("FleetSize", String.valueOf(Math.max(150, fleetSize)));
		parameters.put("electric", "1");
		parameters.put("automated", "1");
		parameters.put("fleetOperation", "1");

		parameters.put("ph_operationHours_av", String.valueOf((int) Math.floor(totalTime / 3600.0)));
		parameters.put("ph_operationHours", "0");
		parameters.put("ph_relActiveTime", String.valueOf(100.0 * analysisHandler.getRelativeActiveTime()));
		parameters.put("ph_avOccupancy", String.valueOf(100.0 * analysisHandler.getOccupancy()));
		parameters.put("ph_avSpeed", String.valueOf(analysisHandler.getAverageSpeed()));
		parameters.put("ph_avTripLengthPass", String.valueOf(analysisHandler.getAveragePassengerTripLength()));
		parameters.put("ph_relEmptyRides", String.valueOf(100.0 * analysisHandler.getRelativeEmptyDistance()));
		parameters.put("ph_relMaintenanceRides", "0");
		parameters.put("ph_relMaintenanceHours", "0");

		return parameters;
	}
}
