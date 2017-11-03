package ch.ethz.matsim.av_cost_calculator.run;

public class AnyAVValidator implements AVValidator {
	@Override
	public boolean isRelevant(String vehicleOrPersonId) {
		return vehicleOrPersonId.startsWith("av");
	}
}
