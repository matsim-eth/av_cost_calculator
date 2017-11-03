package ch.ethz.matsim.av_cost_calculator.run;

import java.util.Map;
import java.util.TreeMap;

public class ParameterBuilder {
	public Map<String, String> build(AnalysisHandler analysisHandler) {
		Map<String, String> parameters = new TreeMap<>();
		
		parameters.put("Area", "Urban");
		parameters.put("VehicleType", "Solo");
		parameters.put("FleetSize", String.valueOf(analysisHandler.getFleetSize()));
		parameters.put("electric", "1");
		parameters.put("automated", "1");
		parameters.put("fleetOperation", "1");

		parameters.put("ph_operationHours_av", "30");
		parameters.put("ph_operationHours", "0");
		parameters.put("ph_relActiveTime", String.valueOf(100.0D * analysisHandler.getRelativeActiveTime(108000.0D)));
		parameters.put("ph_avOccupancy", "100");
		parameters.put("ph_avSpeed", String.valueOf(analysisHandler.getAverageSpeed()));
		parameters.put("ph_avTripLengthPass", String.valueOf(analysisHandler.getAveragePassengerTripLength()));
		parameters.put("ph_relEmptyRides", String.valueOf(100.0D * analysisHandler.getRelativeEmptyDistance()));
		parameters.put("ph_relMaintenanceRides", "0");
		parameters.put("ph_relMaintenanceHours", "0");
		
		return parameters;
	}
}
