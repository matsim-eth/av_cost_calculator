package ch.ethz.matsim.av_cost_calculator;

import org.junit.Test;

import org.junit.Assert;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TestCostCalculatorExecutor {
    @Test
    public void testCostCalculatorExecutor() throws MalformedURLException, URISyntaxException {
        URL sourceURL = getClass().getResource("Main.R").toURI().resolve(".").toURL();

        File workingDirectory = new File("output/test/cost_calculator_test");
        workingDirectory.mkdirs();

        CostCalculatorExecutor executor = new CostCalculatorExecutor(workingDirectory, sourceURL);

        Map<String, String> parameters = new HashMap<>();

        parameters.put("Area", "Urban");
        parameters.put("VehicleType", "Midsize");
        parameters.put("FleetSize", "1500");
        parameters.put("electric", "");
        parameters.put("automated", "");
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
        Assert.assertEquals(0.461058774767991, result, 1e-6);
    }
}
