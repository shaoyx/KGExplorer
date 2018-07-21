package com.org.shark.graphtoolkits.applications;

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.simulation.*;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@GraphAnalyticTool(
        name = "Interactive QBE Solver",
        description = "The solver of interactively finding entities by given examples.The Entry of graph detection."
)
public class InteractiveQbeSolver implements GenericGraphTool {
    private static final Logger logger = LogManager.getLogger(QbeSolver.class.getName());

    private IUserAgent userAgent = new InteractiveUserAgent();
//    private IKGExplorationSystem kgExplorationSystem = new KGExplorationSystem();
    private IKGExplorationSystem kgExplorationSystem = new PriorKGExplorationSystem();
    private IKGService kgService = new KGService();

    @Override
    public void registerOptions(Options options) {
        options.addOption("m", "model", true, "The aspect model type.");
        options.addOption("q", "query", true, "The file path of query.");
        options.addOption("r", "ratio", true, "The ratio of input example.");
    }

    @Override
    public void run(CommandLine cmd) {
        String graphFile = cmd.getOptionValue("i");
        logger.info("Initialization");

        //1. initialization
        kgService.initialization(graphFile);
        kgExplorationSystem.initialization(kgService);
        userAgent.initialization(kgExplorationSystem, null, null);

        //2. begin simulation
        logger.info("Begin to simulate interactive query");
        int cnt = userAgent.simulation();
    }

    @Override
    public boolean verifyParameters(CommandLine cmd) {
        return true;
    }
}
