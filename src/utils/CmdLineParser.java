package utils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class CmdLineParser {
	private CommandLineParser parser;
	private CommandLine cmd;
	private Options options;
	private HelpFormatter helpText;

	public CmdLineParser() {
		parser = new BasicParser();
		options = new Options();
		helpText = new HelpFormatter();
		createOptions();
	}
	
	@SuppressWarnings("static-access")
	private void createOptions()
	{
		Option input = OptionBuilder.withArgName("file path")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Path to file with input data. It could be a properly formatted *.csv file.")// or weka *.arff file.")
				.withLongOpt("input")
				.create('i');
		
		Option output = OptionBuilder.withArgName("directory path")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Path where output *.PNG files showing every hierarchy level and a *.csv file with hierarchy"
						+ "statistics will be stored.")
				.withLongOpt("output")
				.create('o');
		
		Option backgroundColor = OptionBuilder.withArgName("color")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Background color of every output image. Possible values: {green, black, blue, lightBlue, yellow," + 
						" cyan, lightGray, gray, darkGray, magenta, orange, pink, red, white}. Default: white.")
				.withLongOpt("background-color")
				.create("bg");
		
		Option currentGroup = OptionBuilder.withArgName("color")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Color in which indicate current Level Group on the output" +
						" images. Possible values: {green, black, blue, lightBlue, yellow, cyan, lightGray," +
						" gray, darkGray, magenta, orange, pink, red, white}. Default: red.")
				.withLongOpt("current-level-group-color")
				.create("lg");
		
		Option childGroup = OptionBuilder.withArgName("color")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Color in which all Child Groups (successors) will be painted on the output"
						+ " images. Possible values: {green, black, blue, lightBlue, yellow, cyan, lightGray,"
						+ " gray, darkGray, magenta, orange, pink, red, white}. Default: green.")
				.withLongOpt("child-group-color")
				.create("cg");
		
		Option parentGroup = OptionBuilder.withArgName("color")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Color in which direct Parent Group (immediate ancestor) will be painted on"
						+ " the output images. Possible values: {green, black, blue, lightBlue, yellow, cyan,"
						+ " lightGray, gray, darkGray, magenta, orange, pink, red, white}. Default: blue."
						+ " To display this points, the -da flag must be set.")
				.withLongOpt("parent-group-color")
				.create("pg");
		
		Option parentAncestorsGroup = OptionBuilder.withArgName("color")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Color in which current Parent group all ancestors will be painted"
						+ " on the output images. Possible values: {green, black, blue, lightBlue, yellow, cyan,"
						+ " lightGray, gray, darkGray, magenta, orange, pink, red, white}. Default: lightBlue."
						+ " To display this points, the -da flag must be set.")
				.withLongOpt("parent-ancestors-group-color")
				.create("pa");

		Option otherGroup = OptionBuilder.withArgName("color")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Color in which all Other Groups (e.g. siblings) will be painted on the "
						+ "output images. Possible values: {green, black, blue, lightBlue, yellow, cyan, "
						+ "lightGray, gray, darkGray, magenta, orange, pink, red, white}. Default: lightGray. "
						+ "To display this points, the -da flag must be set.")
				.withLongOpt("other-group-color")
				.create("og");
		
		Option imageWidth = OptionBuilder.withArgName("pixel number")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Width of the instances visualisation part (center image) on the outputimages."
						+ " Provided in pixels. Default: 800 px.")
				.withLongOpt("images-width")
				.create("w");
		
		Option imageHeight = OptionBuilder.withArgName("pixel number")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Height of the instances visualisation part (center image)"+
						" on the output images. Provided in pixels. Default: 600 px.")
				.withLongOpt("images-height")
				.create("ht");
		
		Option pointScale = OptionBuilder.withArgName("real number")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Scaling factor (floating point number) of points drawn on images. Default: 1.0 (no scaling).")
				.withLongOpt("point-scale")
				.create("ps");
		
		Option binsNumber = OptionBuilder.withArgName("number of bins")
				.hasArgs(1)
				.isRequired(false)
				.withDescription("Number of each histogram bins. Default: 100.")
				.withLongOpt("bins-number")
				.create("b");
		
		Option help = OptionBuilder.withDescription("Prints this message.")
				.hasArg(false)
				.isRequired(false)
				.withLongOpt("help")
				.create('h');
		
		options.addOption("da", "display-all", false, "Display all point on the output images, " +
				"so the other non-child groups (e.g. siblings and all parent groups) are also displayed.");
		
		options.addOption("c", "class-attribute", false, "Provided input file contains also a ground truth class assignment."
				+ " Class attribute will be omitted by this program. Assumed that class is in the second column (attribute)"
				+ " in the input file.");
		
		options.addOption("in", "instance-name", false, "Provided input file contains also an unique name of every instance, which "
				+ "will be omitted by this program. Assumed that instance names are in the third column (attribute) in "
				+ "input file when the class attribute is also provided or in the second column otherwise.");
		
		options.addOption("sv", "skip-visualisation", false, "Program will skip printing the output visualisations (images)."
				+ " Only hierarchy statistic file will be produced.");
		
		options.addOption(input);
		options.addOption(output);
		options.addOption(backgroundColor);
		options.addOption(currentGroup);
		options.addOption(childGroup);
		options.addOption(parentGroup);
		options.addOption(parentAncestorsGroup);
		options.addOption(otherGroup);
		options.addOption(imageWidth);
		options.addOption(imageHeight);
		options.addOption(pointScale);
		options.addOption(binsNumber);
		options.addOption(help);
	}
	
	public Parameters parse(String[] args)
	{
		Parameters paramsToSet = new Parameters();
		try {
			cmd = parser.parse(options, args);
		}
		catch( ParseException exp ) {
		    System.err.println(exp.getMessage());
		    System.exit(1);
		}
		if(cmd.hasOption('h') || cmd.hasOption("help") || args.length == 0)
		{
			viewHelp();
			System.exit(0);
		}
		else
		{
			parseParameters(paramsToSet);
		}
		return paramsToSet;
	}

	private void parseParameters(Parameters paramsToSet) {
		paramsToSet.setInputDataFilePath(parseInputFile());
		paramsToSet.setOutputFolder(parseOutputFile());
		
		paramsToSet.setBackgroundColor(parseColor(cmd.getOptionValue("bg", "white")));
		paramsToSet.setCurrentLevelColor(parseColor(cmd.getOptionValue("lg", "red")));
		paramsToSet.setChildGroupsColor(parseColor(cmd.getOptionValue("cg", "green")));
		paramsToSet.setParentGroupsColor(parseColor(cmd.getOptionValue("pg", "blue")));
		paramsToSet.setParentAncestorsColor(parseColor(cmd.getOptionValue("pa", "lightBlue")));
		paramsToSet.setOtherGroupsColor(parseColor(cmd.getOptionValue("og", "lightGray")));
		
		paramsToSet.setImageWidth(parsePositiveIntegerParameter(cmd.getOptionValue("w", "800"),
				"Image width should be a positive integer number pointing number of pixels."));
		paramsToSet.setImageHeight(parsePositiveIntegerParameter(cmd.getOptionValue("ht", "600"),
				"Image height should be a positive integer number pointing number of pixels."));

		paramsToSet.setPointScalingFactor(parsePositiveDoubleParameter(cmd.getOptionValue("ps", "1.0"),
				"Points scaling factor should be a positive real number."));
		
		paramsToSet.setNumberOfHistogramBins(parsePositiveIntegerParameter(cmd.getOptionValue("b", "100"), 
				"Number of bins should be a positive integer number."));
		
		paramsToSet.setDisplayAllPoints(cmd.hasOption("da"));
		paramsToSet.setClassAttribute(cmd.hasOption("c"));
		paramsToSet.setInstanceName(cmd.hasOption("in"));
		paramsToSet.setSkipVisualisations(cmd.hasOption("sv"));
	}
	
	private Color parseColor(String optionValue) {
		Color returnValue = null;
		switch(optionValue)
		{
		case "green":
			returnValue = Color.green;
			break;
		case "black":
			returnValue = Color.black;
			break;
		case "blue":
			returnValue = Color.blue;
			break;
		case "lightBlue":
			returnValue = new Color(0, 191, 255);
			break;
		case "yellow":
			returnValue = Color.yellow;
			break;
		case "cyan":
			returnValue = Color.cyan;
			break;
		case "lightGray" :
			returnValue = Color.lightGray;
			break;
		case "gray":
			returnValue = Color.gray;
			break;
		case "darkGray":
			returnValue = Color.darkGray;
			break;
		case "magenta":
			returnValue = Color.magenta;
			break;
		case "orange":
			returnValue = Color.orange;
			break;
		case "pink":
			returnValue = Color.pink;
			break;
		case "red":
			returnValue = Color.red;
			break;
		case "white":
			returnValue = Color.white;
			break;
		default:
			System.err.println("Cannor parse color: " + optionValue + "."
					+ " It should be one of {green, black, blue, lightBlue, yellow," 
					+ " cyan, lightGray, gray, darkGray, magenta, orange,"
					+ " pink, red, white}.");
			System.exit(1);
		}
		return returnValue;
	}

	private int parsePositiveIntegerParameter(String parsedOptionValue, String invalidArgMsg) {
		int parsedValue = -1;
		try
		{
			parsedValue = Integer.valueOf(parsedOptionValue);
			if(parsedValue <= 0)
			{
				throw new NumberFormatException();
			}
		}
		catch(NumberFormatException e)
		{
			System.err.println("'" + parsedOptionValue + "' " + invalidArgMsg
						+ " " + e.getMessage());
			System.exit(-1);
		}
		return parsedValue;
	}
	
	@SuppressWarnings("unused")
	private int parseIntegerParameter(String parsedOptionValue, String invalidArgMsg) {
		int parsedValue = -1;
		try
		{
			parsedValue = Integer.valueOf(parsedOptionValue);
		}
		catch(NumberFormatException e)
		{
			System.err.println("'" + parsedOptionValue + "' " + invalidArgMsg
						+ " " + e.getMessage());
			System.exit(-1);
		}
		return parsedValue;
	}
	
	private double parsePositiveDoubleParameter(String parsedOptionValue, String invalidArgMsg) {
		double parsedValue = -1;
		try
		{
			parsedValue = Double.valueOf(parsedOptionValue);
			if(parsedValue <= 0.0)
			{
				throw new NumberFormatException();
			}
		}
		catch(NumberFormatException e)
		{
			System.err.println("'" + parsedOptionValue + "' " + invalidArgMsg
						+ " " + e.getMessage());
			System.exit(-1);
		}
		return parsedValue;
	}
	
	@SuppressWarnings("unused")
	private double parseDoubleParameter(String parsedOptionValue, String invalidArgMsg) {
		double parsedValue = -1;
		try
		{
			parsedValue = Double.valueOf(parsedOptionValue);
		}
		catch(NumberFormatException e)
		{
			System.err.println("'" + parsedOptionValue + "' " + invalidArgMsg
						+ " " + e.getMessage());
			System.exit(-1);
		}
		return parsedValue;
	}

	private void viewHelp()
	{		 
		helpText.printHelp( "java -jar hierarchy_visualisator.jar", options );
	}	
	
	private Path parseInputFile()
	{
		File inputFile = null;
		if(cmd.hasOption('i'))
		{
			inputFile = new File(cmd.getOptionValue('i'));
			if(!inputFile.exists() || inputFile.isDirectory())
			{
				System.err.println("Input file shoud be existing file!");
				System.exit(1);
			}
		}
		else
		{
			System.err.println("No input file specified! Use -i option.");
			System.exit(1);
		}
		return inputFile.toPath();
	}
	
	private Path parseOutputFile() 
	{
		File outputFolder = null;
		if(cmd.hasOption('o'))
		{
			String outputFolderName = cmd.getOptionValue('o');
			outputFolder = new File(outputFolderName);
			if(outputFolder.isFile())
			{
				System.err.println(outputFolderName + " should be an path to directory!");
				System.exit(1);
			}
			if(!outputFolder.exists())
			{
				System.out.println(outputFolderName + " doesn't exist, creating folder.");
				try {
					Files.createDirectories(outputFolder.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else
		{
			System.err.println("No output file specified! Use -o option.");
			System.exit(1);
		}
		return outputFolder.toPath();
	}
}

