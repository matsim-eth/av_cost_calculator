package ch.ethz.matsim.av_cost_calculator.run;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class ParameterBuilder {
	final private Logger logger = Logger.getLogger(ParameterBuilder.class);
	final private double scaling;
	
	public ParameterBuilder(double scaling) {
		this.scaling = scaling;
	}
	
	public Map<String, String> build(AnalysisHandler analysisHandler) {
		Map<String, String> parameters = new TreeMap<>();
		
		if (analysisHandler.getFleetSize() < 150) {
			logger.warn("Requested fleet size of " + analysisHandler.getFleetSize() + " vehicles is to low. Setting to 150 (minimum value for cost calculator)");
		}
		
		parameters.put("Area", "Urban");
		parameters.put("VehicleType", "Solo");
		parameters.put("FleetSize", String.valueOf(Math.max(150, analysisHandler.getFleetSize())));
		parameters.put("electric", "1");
		parameters.put("automated", "1");
		parameters.put("fleetOperation", "1");

		parameters.put("ph_operationHours_av", "30");
		parameters.put("ph_operationHours", "0");
		parameters.put("ph_relActiveTime", String.valueOf((1.0 / scaling) * analysisHandler.getRelativeActiveTime(108000.0D)));
		parameters.put("ph_avOccupancy", "100");
		parameters.put("ph_avSpeed", String.valueOf(analysisHandler.getAverageSpeed()));
		parameters.put("ph_avTripLengthPass", String.valueOf(analysisHandler.getAveragePassengerTripLength()));
		parameters.put("ph_relEmptyRides", String.valueOf((1.0 / scaling) * analysisHandler.getRelativeEmptyDistance()));
		parameters.put("ph_relMaintenanceRides", "0");
		parameters.put("ph_relMaintenanceHours", "0");
		
		return parameters;
	}
}
