package ch.ethz.matsim.av_cost_calculator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CostCalculatorExecutor {
	final private Logger logger = Logger.getLogger(CostCalculatorExecutor.class);
	
    final private URL sourceURL;

    final private File workingDirectoryPath;
    final private File originalRunScriptPath;
    final private File runScriptPath;
    final private File templatePath;
    final private File inputPath;
    final private File outputPath;

    final private Map<String, Integer> rowMapping = new HashMap<>();

    public CostCalculatorExecutor(File workingDirectoryPath, URL sourceURL) {
        this.sourceURL = sourceURL;

        this.workingDirectoryPath = workingDirectoryPath;

        this.runScriptPath = new File(workingDirectoryPath, "Main.R");
        this.originalRunScriptPath = new File(workingDirectoryPath, "Main_Original.R");
        this.templatePath = new File(workingDirectoryPath, "Template.xlsx");
        this.inputPath = new File(workingDirectoryPath, "Input.xlsx");
        this.outputPath = new File(workingDirectoryPath, "results_main.csv");

        rowMapping.put("Area", 2);
        rowMapping.put("VehicleType", 5);
        rowMapping.put("FleetSize", 6);
        rowMapping.put("electric", 7);
        rowMapping.put("automated", 8);
        rowMapping.put("fleetOperation", 9);

        rowMapping.put("ph_operationHours_av", 12);
        rowMapping.put("ph_operationHours", 13);
        rowMapping.put("ph_relActiveTime", 14);
        rowMapping.put("ph_avOccupancy", 15);
        rowMapping.put("ph_avSpeed", 16);
        rowMapping.put("ph_avTripLengthPass", 17);
        rowMapping.put("ph_relEmptyRides", 18);
        rowMapping.put("ph_relMaintenanceRides", 19);
        rowMapping.put("ph_relMaintenanceHours", 20);
        
        rowMapping.put("oph_operationHours_av", 23);
        rowMapping.put("oph_operationHours", 24);
        rowMapping.put("oph_relActiveTime", 25);
        rowMapping.put("oph_avOccupancy", 26);
        rowMapping.put("oph_avSpeed", 27);
        rowMapping.put("oph_avTripLengthPass", 28);
        rowMapping.put("oph_relEmptyRides", 29);
        rowMapping.put("oph_relMaintenanceRides", 30);
        rowMapping.put("oph_relMaintenanceHours", 31);
        
        rowMapping.put("ngt_operationHours_av", 34);
        rowMapping.put("ngt_operationHours", 35);
        rowMapping.put("ngt_relActiveTime", 36);
        rowMapping.put("ngt_avOccupancy", 37);
        rowMapping.put("ngt_avSpeed", 38);
        rowMapping.put("ngt_avTripLengthPass", 39);
        rowMapping.put("ngt_relEmptyRides", 40);
        rowMapping.put("ngt_relMaintenanceRides", 41);
        rowMapping.put("ngt_relMaintenanceHours", 42);

        setupWorkingDirectory();
        updateWorkingDirectoryForR();
    }

    private void setupWorkingDirectory() {
    	logger.info("Setting up working directory at " + workingDirectoryPath);
    	
        try {
            FileUtils.copyURLToFile(new URL(sourceURL, "CostCalculator.R"), new File(workingDirectoryPath, "CostCalculator.R"));
            FileUtils.copyURLToFile(new URL(sourceURL, "Main.R"), new File(workingDirectoryPath, "Main_Original.R"));
            FileUtils.copyURLToFile(new URL(sourceURL, "ScenarioAnalyzer.R"), new File(workingDirectoryPath, "ScenarioAnalyzer.R"));
            FileUtils.copyURLToFile(new URL(sourceURL, "Input.xlsx"), new File(workingDirectoryPath, "Template.xlsx"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error setting up working directory for Cost Calculator");
        }
    }
    
    private static final String INITIAL_WORKING_DIRECTORY = "P:/_TEMP/Becker_Henrik/_AV-cost_comparison/Original_Suisse/CostCalculatorExtern/";

    private void updateWorkingDirectoryForR() {
    	logger.info("Updating working directory in R script");
    	
        try {
            String script = FileUtils.readFileToString(originalRunScriptPath);
            script = script.replace(INITIAL_WORKING_DIRECTORY, workingDirectoryPath.getAbsolutePath());
            script = script.replace("Realizations_ZH", "Realizations");
            FileUtils.writeStringToFile(runScriptPath, script);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateInputFile(Map<String, String> parameters) {
    	logger.info("Updating input file...");
        if (inputPath.exists()) inputPath.delete();
        
        parameters.put("oph_operationHours_av", "0");
        parameters.put("oph_operationHours", "0");
        parameters.put("oph_relActiveTime", "0");
        parameters.put("oph_avOccupancy", "0");
        parameters.put("oph_avSpeed", "0");
        parameters.put("oph_avTripLengthPass", "0");
        parameters.put("oph_relEmptyRides", "0");
        parameters.put("oph_relMaintenanceRides", "0");
        parameters.put("oph_relMaintenanceHours", "0");
        
        parameters.put("ngt_operationHours_av", "0");
        parameters.put("ngt_operationHours", "0");
        parameters.put("ngt_relActiveTime", "0");
        parameters.put("ngt_avOccupancy", "0");
        parameters.put("ngt_avSpeed", "0");
        parameters.put("ngt_avTripLengthPass", "0");
        parameters.put("ngt_relEmptyRides", "0");
        parameters.put("ngt_relMaintenanceRides", "0");
        parameters.put("ngt_relMaintenanceHours", "0");

        try {
            XSSFWorkbook originalWorkbook = new XSSFWorkbook(templatePath);
            originalWorkbook.write(new FileOutputStream(inputPath));

            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(inputPath));
            XSSFSheet sheet = workbook.getSheet("Realizations");

            for (Map.Entry<String, Integer> mapping : rowMapping.entrySet()) {
                if (!parameters.containsKey(mapping.getKey())) {
                    throw new IllegalStateException("Parameter " + mapping.getKey() + " is missing");
                }

                sheet.getRow(mapping.getValue().intValue() - 1).getCell(1).setCellValue(parameters.get(mapping.getKey()));
            }

            workbook.write(new FileOutputStream(inputPath));
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while updating Cost Calculator input");
        }
    }

    private double readOutputFile(Map<String, String> parameters) {
    	logger.info("Reading output file...");
        Double result = null;

        String item = parameters.get("electric").equals("1") ? "AE-S02" : "A-S02";

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputPath)));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(item)) {
                	try {
                		result = Double.parseDouble(line.split(";")[6]);
                	} catch (NumberFormatException e) {
                		result = Double.NaN;
                		logger.warn("R script returned invalid expression (" + result + "), returning NaN");
                	}
                }
            }

            reader.close();

            if (result == null) {
                throw new RuntimeException("Could not find result in Cost Calculator output");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        return result;
    }

    private void runRScript() {
    	logger.info("Running R script... ");
        Runtime runtime = Runtime.getRuntime();

        double startTime = System.nanoTime() * 1e-9;
        
        try {
            Process process = runtime.exec("Rscript --vanilla Main.R", null, workingDirectoryPath);

            while (process.isAlive()) {
                Thread.sleep(10);

                if (System.nanoTime() * 1e-9 - startTime > 30) {
                	logger.info("Waiting for cost calculator ...");
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while executing Cost Calculator script");
        }
    }

    public double computePricePerPassengerKm(Map<String, String> parameters) {
        updateInputFile(parameters);
        runRScript();
        return readOutputFile(parameters);
    }
}
