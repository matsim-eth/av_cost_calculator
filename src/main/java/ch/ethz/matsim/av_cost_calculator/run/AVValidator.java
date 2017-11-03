package ch.ethz.matsim.av_cost_calculator.run;

public interface AVValidator {
	boolean isRelevant(String vehicleOrPersonId);
}
