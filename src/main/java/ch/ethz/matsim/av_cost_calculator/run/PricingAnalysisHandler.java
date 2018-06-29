package ch.ethz.matsim.av_cost_calculator.run;

import org.matsim.core.events.handler.EventHandler;

public interface PricingAnalysisHandler extends EventHandler {
	long getFleetSize();

	double getRelativeActiveTime();

	double getAverageSpeed();

	double getAveragePassengerTripLength();

	double getRelativeEmptyDistance();

	double getOccupancy();

	void resetHandler();
	
	double getTotalPassengerDistance();
	
	long getNumberOfTrips();
}
