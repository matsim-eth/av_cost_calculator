package ch.ethz.matsim.av_cost_calculator.run;

public class IdAVValidator implements AVValidator {
	final private String operatorId;
	
	public IdAVValidator(String operatorId) {
		this.operatorId = operatorId;
	}
	
	@Override
	public boolean isRelevant(String vehicleOrPersonId) {
		return vehicleOrPersonId.startsWith("av") && vehicleOrPersonId.contains(operatorId);
	}
}
