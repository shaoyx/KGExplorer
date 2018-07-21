package com.org.shark.graphtoolkits.applications;

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.simulation.*;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@GraphAnalyticTool(
        name = "QBE Solver",
        description = "The solver of interactively finding entities by given examples.The Entry of graph detection."
)
public class QbeSolver implements GenericGraphTool {

    private static final Logger logger = LogManager.getLogger(QbeSolver.class.getName());

    private IUserAgent userAgent = new UserAgent();
    private IKGExplorationSystem kgExplorationSystem = new KGExplorationSystem();
    private IKGService kgService = new KGService();

    @Override
    public void registerOptions(Options options) {
        options.addOption("m", "model", true, "The aspect model type.");
        options.addOption("q", "query", true, "The file path of query.");
        options.addOption("r", "ratio", true, "The ratio of input example.");
    }

    @Override
    public void run(CommandLine cmd) {
        this.runPosetModel(cmd);
    }

    private void runPosetModel(CommandLine cmd) {
        String graphFile = cmd.getOptionValue("i");
        logger.info("Initialization");

        if(cmd.getOptionValue("m") != null && cmd.getOptionValue("m").equalsIgnoreCase("prior")) {
            logger.info("Using PriorKGExplorationSystem");
            kgExplorationSystem = new PriorKGExplorationSystem();
        }

        //1. initialization
        kgService.initialization(graphFile);
        kgExplorationSystem.initialization(kgService);
        userAgent.initialization(kgExplorationSystem, cmd.getOptionValue("q"), cmd.getOptionValue("r"));

        //2. begin simulation
        logger.info("Begin to simulate interactive query");
        long begTime = System.currentTimeMillis();
        int cnt = userAgent.simulation();
        long endTime = System.currentTimeMillis();
        logger.info(String.format("Process %d query costs total %.3f secs, avg %.3f secs", cnt,
                (endTime - begTime)/ 1000.0, (endTime - begTime)/ 1000.0/cnt));
    }

    @Override
    public boolean verifyParameters(CommandLine cmd) {
        return true;
    }
}
