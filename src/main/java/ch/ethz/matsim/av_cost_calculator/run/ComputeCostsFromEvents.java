package ch.ethz.matsim.av_cost_calculator.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import ch.ethz.matsim.av.schedule.AVTransitEventMapper;
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
		// SingleOccupancyAnalysisHandler analysisHandler = new
		// SingleOccupancyAnalysisHandler(network,
		// new AnyAVValidator(), 24.0 * 3600.0);
		MultiOccupancyAnalysisHandler analysisHandler = new MultiOccupancyAnalysisHandler(network, new AnyAVValidator(),
				24.0 * 3600.0, 1);
		eventsManager.addHandler(analysisHandler);

		EventsReaderXMLv1 reader = new EventsReaderXMLv1(eventsManager);
		reader.addCustomEventMapper("AVTransit", new AVTransitEventMapper());
		reader.readFile(inputEventsFile);

		URL sourceURL = CostCalculator.class.getClassLoader().getResource("ch/ethz/matsim/av_cost_calculator/");

		File workingDirectory = new File("output/test/cost_calculator_test");
		workingDirectory.mkdirs();

		CostCalculatorExecutor executor = new CostCalculatorExecutor(workingDirectory, sourceURL);

		Map<String, String> parameters = new ParameterBuilder(0.1, 24.0 * 3600.0, "Solo").build(analysisHandler);

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