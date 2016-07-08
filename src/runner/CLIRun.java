package runner;

import java.io.File;

import basic_hierarchy.interfaces.Hierarchy;
//import basic_hierarchy.reader.GeneratedARFFReader;
import basic_hierarchy.reader.GeneratedCSVReader;
import utils.CmdLineParser;
import utils.HierarchyStatistics;
import utils.Parameters;
import visualisation.Visualisation;

public class CLIRun {
	public static void main(String[] args) {

		CmdLineParser parser = new CmdLineParser();
		Parameters params = parser.parse(args);
		
		Hierarchy inputData = null;
		if(params.getInputDataFilePath().getFileName().toString().endsWith(".csv"))
		{
			inputData = new GeneratedCSVReader().load(params.getInputDataFilePath().toString(), params.getInstanceName(), params.isClassAttribute(), false);//TODO: z ostatniego booleana mozna zrobic flage
		}
//		else if(params.getInputDataFilePath().getFileName().toString().endsWith(".arff"))
//		{
//			inputData = new GeneratedARFFReader().load(params.getInputDataFilePath().toString(), params.getInstanceName(), params.isClassAttribute(), false);//TODO: z ostatniego booleana mozna zrobic flage
//		}
		else
		{
			System.err.println("Unrecognised extension of input file: " + params.getInputDataFilePath().getFileName()
					+ " only *.csv files are supported.");//and *.arff files are supported.");
			System.exit(1);
		}
		
		String statisticsFilePath = params.getOutputFolder() + File.separator + params.getInputDataFilePath().getFileName();
		statisticsFilePath = statisticsFilePath.substring(0, statisticsFilePath.lastIndexOf(".")) + "_hieraryStatistics.csv";
		HierarchyStatistics stats = new HierarchyStatistics(inputData, statisticsFilePath);
		
		if(!params.isSkipVisualisations())
		{
			Visualisation vis = new Visualisation();
			vis.visualise(inputData, stats, params);
		}
	}
}
