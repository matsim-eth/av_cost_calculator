package ch.ethz.matsim.projects.astra;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.trafficmonitoring.VrpTravelTimeModules;
import org.matsim.contrib.dynagent.run.DynQSimModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import ch.ethz.matsim.av.framework.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimProvider;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.routing.AVRouteFactory;
import ch.ethz.matsim.baseline_scenario.BaselineModule;
import ch.ethz.matsim.projects.astra.av.AVNetworkModule;
import ch.ethz.matsim.projects.astra.av.AVPricingModule;
import ch.ethz.matsim.baseline_scenario.analysis.simulation.ModeShareListenerModule;
import ch.ethz.matsim.projects.astra.config.ASTRAConfigGroup;
import ch.ethz.matsim.projects.astra.mode_choice.ASTRAModeChoiceModule;
import ch.ethz.matsim.projects.astra.scoring.ASTRAScoringModule;
import ch.ethz.matsim.projects.astra.traffic.ASTRATrafficModule;
import ch.ethz.matsim.r5.matsim.R5ConfigGroup;
import ch.ethz.matsim.r5.matsim.R5Module;

public class RunASTRA {
	static public void main(String[] args) {
		R5ConfigGroup r5Config = new R5ConfigGroup();
		ASTRAConfigGroup astraConfig = new ASTRAConfigGroup();		

		AVConfigGroup avConfig = new AVConfigGroup();
		avConfig.setConfigPath("av_services.xml");
		avConfig.setParallelRouters(4);
		
		DvrpConfigGroup dvrpConfigGroup = new DvrpConfigGroup();
		dvrpConfigGroup.setTravelTimeEstimationAlpha(0.05);

		Config config = ConfigUtils.loadConfig(args[0], r5Config, astraConfig, avConfig, dvrpConfigGroup);

		config.global().setNumberOfThreads(Integer.parseInt(args[1]));
		config.qsim().setNumberOfThreads(Integer.parseInt(args[2]));
		
		config.planCalcScore().getOrCreateModeParams("av"); // For mode share plot

		Scenario scenario = ScenarioUtils.createScenario(config);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(AVRoute.class, new AVRouteFactory());
		ScenarioUtils.loadScenario(scenario);
		
		Controler controler = new Controler(scenario);

		controler.addOverridingModule(new R5Module());
		controler.addOverridingModule(VrpTravelTimeModules.createTravelTimeEstimatorModule());
		controler.addOverridingModule(new DynQSimModule<>(AVQSimProvider.class));
		controler.addOverridingModule(new AVModule());
		controler.addOverridingModule(new AVPricingModule(0.01, 0.42));
		
		controler.addOverridingModule(new BaselineModule());
		controler.addOverridingModule(new ASTRATrafficModule());
		controler.addOverridingModule(new ASTRAScoringModule());
		controler.addOverridingModule(new ASTRAModeChoiceModule());
		controler.addOverridingModule(new AVNetworkModule());
		controler.addOverridingModule(new ModeShareListenerModule());

		controler.run();
	}
}
