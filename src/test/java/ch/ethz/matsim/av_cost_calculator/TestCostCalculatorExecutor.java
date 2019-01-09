package ch.ethz.matsim.av_cost_calculator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestCostCalculatorExecutor {
	@Test
	public void testCostCalculatorExecutor() throws URISyntaxException, IOException {
		URL sourceURL = getClass().getResource("Main.R").toURI().resolve(".").toURL();

		File workingDirectory = new File("output/test/cost_calculator_test");
		workingDirectory.mkdirs();

		CostCalculatorExecutor executor = new CostCalculatorExecutor(workingDirectory, sourceURL);

		Map<String, String> parameters = new HashMap<>();

		parameters.put("Area", "Urban");
		parameters.put("VehicleType", "Midsize");
		parameters.put("FleetSize", "1500");
		parameters.put("electric", "1");
		parameters.put("automated", "1");
		parameters.put("fleetOperation", "1");

		parameters.put("ph_operationHours_av", "3.8");
		parameters.put("ph_operationHours", "0");
		parameters.put("ph_relActiveTime", "57");
		parameters.put("ph_avOccupancy", "65");
		parameters.put("ph_avSpeed", "20.6");
		parameters.put("ph_avTripLengthPass", "3.4");
		parameters.put("ph_relEmptyRides", "8");
		parameters.put("ph_relMaintenanceRides", "5");
		parameters.put("ph_relMaintenanceHours", "5");

		double result = executor.computePricePerPassengerKm(parameters);
		Assert.assertEquals(0.5562841614, result, 1e-6);

		FileUtils.deleteDirectory(workingDirectory);
	}

	@Test
	public void testCostCalculatorExecutorWithCustomTemplate() throws URISyntaxException, IOException {
		File workingDirectory = new File("output/test/cost_calculator_test");
		workingDirectory.mkdirs();

		// Prepare template
		URL templateUrl = new URL(getClass().getResource("InputBerlin.xlsx").toURI().resolve(".").toURL(),
				"InputBerlin.xlsx");
		URL sourceURL = getClass().getResource("Main.R").toURI().resolve(".").toURL();

		// Call executor

		CostCalculatorExecutor executor = new CostCalculatorExecutor(workingDirectory, sourceURL, templateUrl);

		Map<String, String> parameters = new HashMap<>();

		parameters.put("Area", "Urban");
		parameters.put("VehicleType", "Solo_Berlin");
		parameters.put("FleetSize", "1500");
		parameters.put("electric", "1");
		parameters.put("automated", "1");
		parameters.put("fleetOperation", "1");

		parameters.put("ph_operationHours_av", "3.8");
		parameters.put("ph_operationHours", "0");
		parameters.put("ph_relActiveTime", "57");
		parameters.put("ph_avOccupancy", "65");
		parameters.put("ph_avSpeed", "20.6");
		parameters.put("ph_avTripLengthPass", "3.4");
		parameters.put("ph_relEmptyRides", "8");
		parameters.put("ph_relMaintenanceRides", "5");
		parameters.put("ph_relMaintenanceHours", "5");

		double result = executor.computePricePerPassengerKm(parameters);
		Assert.assertEquals(1.26256476770728, result, 1e-6);

		FileUtils.deleteDirectory(workingDirectory);
	}
}
