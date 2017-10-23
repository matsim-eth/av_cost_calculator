package ch.ethz.matsim.av_cost_calculator.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import ch.ethz.matsim.av_cost_calculator.CostCalculator;
import ch.ethz.matsim.av_cost_calculator.CostCalculatorExecutor;

public class ComputeCostsFromEvents {
	public static void main(String[] args) throws java.io.IOException {
		String inputNetworkFile = args[0];
		String inputEventsFile = args[1];
		String outputPath = args[2];

		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(inputNetworkFile);

		EventsManager eventsManager = EventsUtils.createEventsManager();
		AnalysisHandler analysisHandler = new AnalysisHandler(network);
		eventsManager.addHandler(analysisHandler);
		new MatsimEventsReader(eventsManager).readFile(inputEventsFile);

		URL sourceURL = CostCalculator.class.getClassLoader().getResource("ch/ethz/matsim/cost_calculator/");

		File workingDirectory = new File("output/test/cost_calculator_test");
		workingDirectory.mkdirs();

		CostCalculatorExecutor executor = new CostCalculatorExecutor(workingDirectory, sourceURL);

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

		List<String> output = new LinkedList<>();

		double result = executor.computePricePerPassengerKm(parameters);
		output.add("Result = " + result);

		for (Map.Entry<String, String> param : parameters.entrySet()) {
			output.add((String) param.getKey() + " = " + (String) param.getValue());
		}

		String outputString = String.join("\n", output);
		System.out.println(outputString);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
		writer.write(outputString);
		writer.flush();
		writer.close();
	}
}