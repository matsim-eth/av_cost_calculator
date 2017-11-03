package ch.ethz.matsim.av_cost_calculator.run;

public class IdOperatorValidator implements AVValidator {
	final private String operatorId;
	
	public IdOperatorValidator(String operatorId) {
		this.operatorId = operatorId;
	}
	
	@Override
	public boolean isRelevant(String vehicleOrPersonId) {
		return vehicleOrPersonId.startsWith("av") && vehicleOrPersonId.contains(operatorId);
	}
}
