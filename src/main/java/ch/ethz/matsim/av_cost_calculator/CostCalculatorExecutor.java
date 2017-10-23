package ch.ethz.matsim.av_cost_calculator;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CostCalculatorExecutor {
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

        setupWorkingDirectory();
        updateWorkingDirectoryForR();
    }

    private void setupWorkingDirectory() {
        try {
            FileUtils.copyURLToFile(new URL(sourceURL, "CostCalculator.R"), new File(workingDirectoryPath, "CostCalculator.R"));
            FileUtils.copyURLToFile(new URL(sourceURL, "Main.R"), new File(workingDirectoryPath, "Main_Original.R"));
            FileUtils.copyURLToFile(new URL(sourceURL, "ScenarioAnalyzer.R"), new File(workingDirectoryPath, "ScenarioAnalyzer.R"));
            FileUtils.copyURLToFile(new URL(sourceURL, "Template.xlsx"), new File(workingDirectoryPath, "Template.xlsx"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error setting up working directory for Cost Calculator");
        }
    }

    private void updateWorkingDirectoryForR() {
        try {
            String script = FileUtils.readFileToString(originalRunScriptPath);
            script = script.replace("{{ working_directory }}", workingDirectoryPath.getAbsolutePath());
            FileUtils.writeStringToFile(runScriptPath, script);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateInputFile(Map<String, String> parameters) {
        if (inputPath.exists()) inputPath.delete();

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
        Double result = null;

        String item = parameters.get("electric").equals("1") ? "AE-S05" : "A-S05";

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputPath)));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(item)) {
                    result = Double.parseDouble(line.split(";")[5]);
                }
            }

            reader.close();

            if (result == null) {
                throw new RuntimeException("Could not find result in Cost Calculator output");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void runRScript() {
        Runtime runtime = Runtime.getRuntime();

        long startTime = System.currentTimeMillis();
        long nextOutput = System.currentTimeMillis();

        try {
            Process process = runtime.exec("Rscript Main.R", new String[] {}, workingDirectoryPath);

            while (process.isAlive()) {
                if (nextOutput <= System.currentTimeMillis()) {
                    nextOutput = System.currentTimeMillis() + 1000;
                }

                Thread.sleep(10);

                if (System.currentTimeMillis() - startTime > 10000) {
                    throw new RuntimeException("Cost Calculator took longer than 10s");
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
