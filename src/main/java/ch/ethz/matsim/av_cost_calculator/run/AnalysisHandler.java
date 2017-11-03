package ch.ethz.matsim.av_cost_calculator.run;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;

public class AnalysisHandler implements ActivityStartEventHandler, ActivityEndEventHandler, LinkEnterEventHandler {
	private final Network network;
	private final Set<Id<Person>> allAVIds = new HashSet<>();

	private final Map<Id<Person>, AtomicDouble> dropoffDistance = new HashMap<>();
	private final Map<Id<Person>, Double> pickupTimes = new HashMap<>();

	private final DescriptiveStatistics speedStatistics = new DescriptiveStatistics();
	private final DescriptiveStatistics passengerDistanceStatistics = new DescriptiveStatistics();

	private double activeTime = 0.0D;
	private double totalDistance = 0.0D;
	
	final private AVValidator validator;

	public AnalysisHandler(Network network, AVValidator validator) {
		this.network = network;
		this.validator = validator;
		reset(0);
	}

	public long getFleetSize() {
		return allAVIds.size();
	}

	public double getRelativeActiveTime(double totalTime) {
		return activeTime / totalTime / getFleetSize();
	}

	public double getAverageSpeed() {
		return speedStatistics.getMean();
	}

	public double getAveragePassengerTripLength() {
		return passengerDistanceStatistics.getMean();
	}

	public double getRelativeEmptyDistance() {
		return 1.0D - passengerDistanceStatistics.getSum() / totalDistance;
	}

	public void handleEvent(ActivityEndEvent event) {
		if (validator.isRelevant(event.getPersonId().toString())) {
			if (event.getActType().equals("AVPickup")) {
				pickupTimes.put(event.getPersonId(), Double.valueOf(event.getTime()));
				dropoffDistance.put(event.getPersonId(), new AtomicDouble(0.0D));
			}
		}
	}

	public void handleEvent(ActivityStartEvent event) {
		if (validator.isRelevant(event.getPersonId().toString())) {
			if (event.getActType().equals("AVStay")) {
				allAVIds.add(event.getPersonId());
			}
	
			if (event.getActType().equals("AVDropoff")) {
				Double pickupTime = (Double) pickupTimes.remove(event.getPersonId());
				AtomicDouble distance = (AtomicDouble) dropoffDistance.remove(event.getPersonId());
	
				if ((pickupTime != null) && (distance != null)) {
					double travelTime = event.getTime() - pickupTime.doubleValue();
					speedStatistics.addValue(distance.doubleValue() / 1000.0D / (travelTime / 3600.0D));
	
					activeTime += travelTime;
					passengerDistanceStatistics.addValue(distance.doubleValue() / 1000.0D);
				}
			}
		}
	}

	public void handleEvent(LinkEnterEvent event) {
		if (validator.isRelevant(event.getVehicleId().toString())) {
			AtomicDouble distance = (AtomicDouble) dropoffDistance.get(Id.createVehicleId(event.getVehicleId()));
	
			if (distance != null) {
				distance.addAndGet(((Link) network.getLinks().get(event.getLinkId())).getLength());
			}
	
			if (event.getVehicleId().toString().contains("av")) {
				totalDistance += ((Link) network.getLinks().get(event.getLinkId())).getLength() / 1000.0D;
			}
		}
	}

	public void reset(int iteration) {
		dropoffDistance.clear();
		pickupTimes.clear();
		speedStatistics.clear();
		passengerDistanceStatistics.clear();
		activeTime = 0.0;
		totalDistance = 0.0;
	}
}