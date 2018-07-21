package com.org.shark.graphtoolkits.applications;


import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.old.AspectGraph;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.old.AspectModel;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.old.BasicAspectModel;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.old.MatrixAspectModel;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspect;
import com.org.shark.graphtoolkits.graph.old.RDFGraphWithAspectMatrix;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

@GraphAnalyticTool(
        name = "GroupDetectionDriver",
        description = "The Entry of graph detection."
)
public class AspectFinder implements GenericGraphTool {

    private static final Logger logger = LogManager.getLogger(AspectFinder.class.getName());

    @Override
    public void registerOptions(Options options) {
        options.addOption("m", "model", true, "The aspect model type.");
        options.addOption("q", "query", true, "The file path of query.");
    }

    @Override
    public void run(CommandLine cmd) {
        if(cmd.getOptionValue("m") != null && cmd.getOptionValue("m").equals("matrix")) {
            this.runMatrix(cmd);
        }
        else {
            this.runBasic(cmd);
        }
    }

    private void runBasic(CommandLine cmd) {
        String graphFile = cmd.getOptionValue("i");
        logger.info("Initializing RDF graph");
        //1. initialize graph
        RDFGraphWithAspect rdfGraph = new RDFGraphWithAspect();
        rdfGraph.loadGraphFromTriples(graphFile);
        rdfGraph.buildIndex();
        rdfGraph.saveGraph(graphFile+"/../index/graph.save");
        rdfGraph.saveIndex(graphFile+"/../index/graph.index");

        logger.info("Begin to find Aspects");
        //2. find aspect graph with graph
        AspectModel aspectModel = new BasicAspectModel(rdfGraph);
        long begTime = System.currentTimeMillis();
        int cnt = executeQuery(cmd.getOptionValue("q"), aspectModel);
        long endTime = System.currentTimeMillis();
        logger.info(String.format("Process %d query costs total %.3f secs, avg %.3f secs", cnt,
                (endTime - begTime)/ 1000.0, (endTime - begTime)/ 1000.0/cnt));
    }

    private void runMatrix(CommandLine cmd) {
        String graphFile = cmd.getOptionValue("i");
        logger.info("Initializing RDF graph");
        //1. initialize graph
        RDFGraphWithAspectMatrix rdfGraph = new RDFGraphWithAspectMatrix();
        rdfGraph.loadGraphFromTriples(graphFile);
        rdfGraph.buildIndex();
        rdfGraph.saveGraph(graphFile+"/../index/graph.save");
        rdfGraph.saveIndex(graphFile+"/../index/graph.index");

        logger.info("Begin to find Aspects");
        //2. find aspect graph with graph
        AspectModel aspectModel = new MatrixAspectModel(rdfGraph);
        long begTime = System.currentTimeMillis();
        int cnt = executeQuery(cmd.getOptionValue("q"), aspectModel);
        long endTime = System.currentTimeMillis();
        logger.info(String.format("Process %d query costs total %.3f secs, avg %.3f secs", cnt,
                (endTime - begTime)/ 1000.0, (endTime - begTime)/ 1000.0/cnt));
    }

    private int executeQuery(String queryfile, AspectModel aspectModel) {
        int queryId = 0;
        int invalid = 0;
        try {
            FileInputStream fin = new FileInputStream(queryfile);
            BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));
            String line;
            int rawId = -1;
            while ((line = fbr.readLine()) != null) {
                if(line.startsWith("#")) {
                    rawId = Integer.parseInt(line.substring(1, line.length()));
                    continue;
                }
                queryId ++;
                ArrayList<String> entitySet = parseLine(line);
                AspectGraph res = aspectModel.query(entitySet);
                if(res == null) {
                    logger.info(String.format("Invalid query %d", rawId));
                    invalid ++;
                }
                else {
                    res.save(queryfile + "_" + rawId + ".res");
                }
            }
            fbr.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return queryId - invalid;
    }

    private ArrayList<String> parseLine(String line) {
        String [] raw = line.split("\t");
        ArrayList<String> res = new ArrayList<String> ();
        for(String entity : raw) {
            res.add("<"+entity+">");
        }
        return res;
    }

    @Override
    public boolean verifyParameters(CommandLine cmd) {
        return true;
    }
}
