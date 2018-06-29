package ch.ethz.matsim.av_cost_calculator.run;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.GenericEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;

import com.google.common.util.concurrent.AtomicDouble;

import ch.ethz.matsim.av.schedule.AVTransitEvent;

public class MultiOccupancyAnalysisHandler implements PersonDepartureEventHandler, PersonArrivalEventHandler,
		LinkEnterEventHandler, ActivityEndEventHandler, PricingAnalysisHandler, PersonEntersVehicleEventHandler,
		PersonLeavesVehicleEventHandler, GenericEventHandler {
	final private Network network;
	final private AVValidator validator;
	final private double totalScenarioTime;
	final private long numberOfSeats;

	public MultiOccupancyAnalysisHandler(Network network, AVValidator validator, double totalScenarioTime,
			long numberOfSeats) {
		this.network = network;
		this.validator = validator;
		this.totalScenarioTime = totalScenarioTime;
		this.numberOfSeats = numberOfSeats;
	}

	final private Map<Id<Person>, AtomicLong> occupancies = new HashMap<>();
	final private Map<Id<Person>, Double> departureTimes = new HashMap<>();
	final private Map<Id<Person>, AtomicDouble> distances = new HashMap<>();
	final private Set<Id<Person>> allAVIds = new HashSet<>();

	private double totalTravelDistance;
	private double totalEmptyDistance;
	private double totalTravelTime;
	private double totalActiveTime;
	private double totalPassengerTime;
	private double totalPassengerDistance;
	private long numberOfPassegerTrips;

	@Override
	public double getTotalPassengerDistance() {
		return 1e-3 * totalPassengerDistance;
	}

	@Override
	public long getNumberOfTrips() {
		return numberOfPassegerTrips;
	}

	@Override
	public void resetHandler() {
		occupancies.clear();
		departureTimes.clear();
		distances.clear();
		allAVIds.clear();

		totalTravelDistance = 0.0;
		totalEmptyDistance = 0.0;
		totalTravelTime = 0.0;
		totalActiveTime = 0.0;
		totalPassengerTime = 0.0;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (validator.isRelevant(event.getPersonId().toString())) {
			departureTimes.put(event.getPersonId(), event.getTime());
			distances.put(event.getPersonId(), new AtomicDouble(0.0));
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if (validator.isRelevant(event.getPersonId().toString())) {
			long occupancy = occupancies.get(event.getPersonId()).get();
			double distance = distances.remove(event.getPersonId()).get();
			double departureTime = departureTimes.remove(event.getPersonId());
			double travelTime = event.getTime() - departureTime;

			totalTravelDistance += distance;
			totalTravelTime += travelTime;
			totalPassengerTime += travelTime * (double) occupancy;

			if (occupancy == 0) {
				totalEmptyDistance += distance;
			} else {
				totalActiveTime += travelTime;
			}
		}
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if (validator.isRelevant(event.getVehicleId().toString())) {
			Link link = network.getLinks().get(event.getLinkId());
			distances.get(event.getVehicleId()).addAndGet(link.getLength());
		}
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if (validator.isRelevant(event.getPersonId().toString())) {
			if (event.getActType().equals("BeforeVrpSchedule")) {
				allAVIds.add(event.getPersonId());
				occupancies.put(event.getPersonId(), new AtomicLong(0));
			}
		}
	}

	@Override
	public long getFleetSize() {
		return allAVIds.size();
	}

	@Override
	public double getRelativeActiveTime() {
		return totalActiveTime / (double) getFleetSize() / totalScenarioTime;
	}

	@Override
	public double getAverageSpeed() {
		return 3.6 * totalTravelDistance / totalTravelTime;
	}

	@Override
	public double getAveragePassengerTripLength() {
		return 1e-3 * totalPassengerDistance / numberOfPassegerTrips;
	}

	@Override
	public double getRelativeEmptyDistance() {
		return totalEmptyDistance / totalTravelDistance;
	}

	@Override
	public double getOccupancy() {
		return totalPassengerTime / totalActiveTime / (double) numberOfSeats;
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if (validator.isRelevant(event.getVehicleId().toString())) {
			if (!validator.isRelevant(event.getPersonId().toString())) {
				occupancies.get(Id.createPersonId(event.getVehicleId())).incrementAndGet();
			}
		}
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		if (validator.isRelevant(event.getVehicleId().toString())) {
			if (!validator.isRelevant(event.getPersonId().toString())) {
				occupancies.get(Id.createPersonId(event.getVehicleId())).decrementAndGet();
			}
		}
	}

	@Override
	public void handleEvent(GenericEvent event) {
		if (event instanceof AVTransitEvent) {
			AVTransitEvent transitEvent = (AVTransitEvent) event;
			totalPassengerDistance += transitEvent.getDistance();
			numberOfPassegerTrips += 1;
		}
	}
}
