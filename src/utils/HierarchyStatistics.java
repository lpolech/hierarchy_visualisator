package utils;

import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.interfaces.Node;

public class HierarchyStatistics {
	private int[] eachLevelNumberOfInstances;
	
	private LinkedList<Double> numberOfChildPerNode;
	private double avgNumberOfChildPerNode;
	private double stdevNumberOfChildPerNode;
	
	private LinkedList<Double> numberOfChildPerInternalNode;
	private double avgNumberOfChildPerInternalNode;
	private double stdevNumberOfChildPerInternalNode;
	
	private LinkedList<Double> numberOfInstancesPerNode;
	private double stdevNumberOfInstancesPerNode;
	private double avgNumberOfInstancesPerNode;
	
	private LinkedList<Double> numberOfChildPerNodeWithSpecifiedBranchingFactor;
	private double avgNumberOfChildPerNodeWithSpecifiedBranchingFactor;
	private double stdevNumberOfChildPerNodeWithSpecifiedBranchingFactor;
	
	private LinkedList<GroupWithEmpiricalParameters> nodesEstimatedParameters;
	private int overallNumberOfInstances;
	private String statsToVisualiseOnImages, statsToWriteOutInFile;
	private HashMap<Integer, Integer> nodeBranchFactorAndCountOfNodesWithThatFactor;
	
	private double[] avgNumberOfChilderPerNodeOnEachHeight;
	private double[] stdevNumberOfChilderPerNodeOnEachHeight;
	
	private double[] hierarchyWidthOnEachHeight;
	private double avgHierarchyWidth;
	private double stdevHierarchyWidth;
	
	private double[] numberOfLeavesOnEachHeight;
	private int numberOfLeaves;	
	
	private LinkedList<Double> pathLength;
	private double avgPathLength;
	private double stdevPathLength;
	
	public HierarchyStatistics(Hierarchy h, String statisticsFilePath)//h variable is here for the future used
	{
		calculate(h, statisticsFilePath);
	}
	
	public void calculate(Hierarchy h, String statisticsFilePath)
	{
		int minimumBranchingFactor = 2;
		traverseHierarchyAndCalculateMeasures(h, minimumBranchingFactor);
		calculateEmpiricalMeanAndVariancesOfEachGroup(h);
		createStatsToVisualiseOnOutputImgsAndSummaryFile();
		saveStatistics(statisticsFilePath, minimumBranchingFactor);
	}
	
	private void traverseHierarchyAndCalculateMeasures(Hierarchy h, int minBranchingFactor) {
		int hierarchyHeight = getHierarchyHeight(h);
		eachLevelNumberOfInstances = new int[hierarchyHeight + 1];
		overallNumberOfInstances = 0;
		nodeBranchFactorAndCountOfNodesWithThatFactor = new HashMap<Integer, Integer>();
		avgNumberOfChilderPerNodeOnEachHeight = new double[hierarchyHeight + 1];
		stdevNumberOfChilderPerNodeOnEachHeight = new double[hierarchyHeight + 1];
		LinkedList<AbstractMap.SimpleEntry<Integer, Integer>> nodeHeightWithItsChildrenCount = new LinkedList<>();
		hierarchyWidthOnEachHeight = new double[hierarchyHeight + 1];
		numberOfLeavesOnEachHeight = new double[hierarchyHeight + 1];
		numberOfChildPerNode = new LinkedList<>();
		numberOfChildPerInternalNode = new LinkedList<>();
		numberOfChildPerNodeWithSpecifiedBranchingFactor = new LinkedList<>();
		numberOfInstancesPerNode = new LinkedList<>();
		pathLength = new LinkedList<>();
		
		
		Stack<AbstractMap.SimpleEntry<Node, Integer>> s = new Stack<>();//node and lvl number
		s.push(new AbstractMap.SimpleEntry<Node, Integer>(h.getRoot(), 0));
		
		while(!s.isEmpty())
		{
			AbstractMap.SimpleEntry<Node, Integer> curr = s.pop();
			Node currentNode = curr.getKey();
			int height = curr.getValue();
			
			for(Node ch: currentNode.getChildren())
			{
				s.push(new AbstractMap.SimpleEntry<Node, Integer>(ch, curr.getValue() + 1));
			}
			
			numberOfInstancesPerNode.add((double)currentNode.getNodeInstances().size());
			
			numberOfChildPerNode.add((double) currentNode.getChildren().size());
			
			if(!currentNode.getChildren().isEmpty())
			{
				numberOfChildPerInternalNode.add((double) currentNode.getChildren().size());
			}
			else
			{
				numberOfLeavesOnEachHeight[height] += 1;
				numberOfLeaves += 1;
				pathLength.add((double)height);
			}
			
			if(currentNode.getChildren().size() >= minBranchingFactor)
			{
				numberOfChildPerNodeWithSpecifiedBranchingFactor.add((double)currentNode.getChildren().size());
			}
			
			hierarchyWidthOnEachHeight[height] += 1;
			eachLevelNumberOfInstances[height] += currentNode.getNodeInstances().size();
			overallNumberOfInstances += currentNode.getNodeInstances().size();
			
			Integer numOfChildren = currentNode.getChildren().size();
			nodeHeightWithItsChildrenCount.add(new AbstractMap.SimpleEntry<Integer, Integer>(height, numOfChildren));
			avgNumberOfChilderPerNodeOnEachHeight[height] += numOfChildren;
			
			if(nodeBranchFactorAndCountOfNodesWithThatFactor.containsKey(numOfChildren))
			{
				Integer incrementedCount = nodeBranchFactorAndCountOfNodesWithThatFactor.get(numOfChildren) + 1;
				nodeBranchFactorAndCountOfNodesWithThatFactor.put(numOfChildren, incrementedCount);
			}
			else
			{
				nodeBranchFactorAndCountOfNodesWithThatFactor.put(numOfChildren, 1);
			}
		}
		
		postprocessObtainedData(nodeHeightWithItsChildrenCount);
	}

	private void postprocessObtainedData(LinkedList<SimpleEntry<Integer,Integer>> nodeHeightWithItsChildrenCount) {
		StandardDeviation stdev = new StandardDeviation(true);
		Mean mean = new Mean();
		double[] primitives;
		
		primitives = ArrayUtils.toPrimitive(numberOfInstancesPerNode.toArray(new Double[numberOfInstancesPerNode.size()]));
		avgNumberOfInstancesPerNode = mean.evaluate(primitives);
		stdevNumberOfInstancesPerNode = stdev.evaluate(primitives);
		
		primitives = ArrayUtils.toPrimitive(numberOfChildPerNode.toArray(new Double[numberOfChildPerNode.size()]));
		avgNumberOfChildPerNode = mean.evaluate(primitives);
		stdevNumberOfChildPerNode = stdev.evaluate(primitives);
		 
		primitives = ArrayUtils.toPrimitive(numberOfChildPerInternalNode.toArray(new Double[numberOfChildPerInternalNode.size()]));
		avgNumberOfChildPerInternalNode = mean.evaluate(primitives);
		stdevNumberOfChildPerInternalNode = stdev.evaluate(primitives);
		
		primitives = ArrayUtils.toPrimitive(numberOfChildPerNodeWithSpecifiedBranchingFactor
				.toArray(new Double[numberOfChildPerNodeWithSpecifiedBranchingFactor.size()]));
		avgNumberOfChildPerNodeWithSpecifiedBranchingFactor = mean.evaluate(primitives);
		stdevNumberOfChildPerNodeWithSpecifiedBranchingFactor = stdev.evaluate(primitives);
		
		avgHierarchyWidth = mean.evaluate(hierarchyWidthOnEachHeight);
		stdevHierarchyWidth = stdev.evaluate(hierarchyWidthOnEachHeight);
		
		primitives = ArrayUtils.toPrimitive(pathLength.toArray(new Double[pathLength.size()]));
		avgPathLength = mean.evaluate(primitives);
		stdevPathLength = stdev.evaluate(primitives);
		
		//filling gaps in histogram
		Set<Integer> keyes = nodeBranchFactorAndCountOfNodesWithThatFactor.keySet();
		int maxKey = Collections.max(keyes);
		int minKey = Collections.min(keyes);
		
		for(int i = minKey+1; i < maxKey; i++)
		{
			if(!nodeBranchFactorAndCountOfNodesWithThatFactor.containsKey(i))
			{
				nodeBranchFactorAndCountOfNodesWithThatFactor.put(i, 0);
			}
		}
		
		for(int i = 0; i < avgNumberOfChilderPerNodeOnEachHeight.length; i++)
		{
			avgNumberOfChilderPerNodeOnEachHeight[i] /= hierarchyWidthOnEachHeight[i];
		}
		
		for(AbstractMap.SimpleEntry<Integer, Integer> elem: nodeHeightWithItsChildrenCount)
		{
			int height = elem.getKey();
			int count = elem.getValue();
			stdevNumberOfChilderPerNodeOnEachHeight[height] += (count - avgNumberOfChilderPerNodeOnEachHeight[height])
					*(count - avgNumberOfChilderPerNodeOnEachHeight[height]); 
		}
		
		for(int i = 0; i < avgNumberOfChilderPerNodeOnEachHeight.length; i++)
		{
			stdevNumberOfChilderPerNodeOnEachHeight[i] /= hierarchyWidthOnEachHeight[i];
		}
		
	}

	private void calculateEmpiricalMeanAndVariancesOfEachGroup(Hierarchy h) {
		nodesEstimatedParameters = new LinkedList<GroupWithEmpiricalParameters>();
		
		LinkedList<Node> s = new LinkedList<>();
		s.add(h.getRoot());
		
		while(!s.isEmpty())
		{
			Node curr = s.removeLast();
			for(int i = curr.getChildren().size() - 1; i >= 0; --i)
			{
				Node ch = curr.getChildren().get(i);
				s.add(ch);
			}
			
			nodesEstimatedParameters.add(new GroupWithEmpiricalParameters(curr));
		}
	}

	private void saveStatistics(String statisticsFilePath, int minimumBranchingFactor) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(statisticsFilePath, "UTF-8");
			pw.print(getStatisticFileContent(minimumBranchingFactor));
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private String getStatisticFileContent(int minimumBranchingFactor) {
		String content = "";
		
		content += "Total num of instances" + Constants.CSV_FILE_SEPARATOR + overallNumberOfInstances + "\n";
		content += "Avg num of children per node" + Constants.CSV_FILE_SEPARATOR + avgNumberOfChildPerNode + "\n";
		content += "Sample stdev num of children per node" + Constants.CSV_FILE_SEPARATOR + stdevNumberOfChildPerNode + "\n";
		content += "Avg num of children per INTERNAL node" + Constants.CSV_FILE_SEPARATOR + avgNumberOfChildPerInternalNode + "\n";
		content += "Sample stdev num of children per INTERNAL node" + Constants.CSV_FILE_SEPARATOR + stdevNumberOfChildPerInternalNode + "\n";
		content += "Avg num of children per INTERNAL node with MIN BRANCHING FACTOR " + minimumBranchingFactor + Constants.CSV_FILE_SEPARATOR
				+ avgNumberOfChildPerNodeWithSpecifiedBranchingFactor + "\n";
		content += "Sample stdev num of children per INTERNAL node with MIN BRANCHING FACTOR " + minimumBranchingFactor + Constants.CSV_FILE_SEPARATOR
				+ stdevNumberOfChildPerNodeWithSpecifiedBranchingFactor + "\n";
		content += "Avg num of instances per node" + Constants.CSV_FILE_SEPARATOR + avgNumberOfInstancesPerNode + "\n";
		content += "Sample stdev num of instances per node" + Constants.CSV_FILE_SEPARATOR + stdevNumberOfInstancesPerNode + "\n";
		content += "Hierarchy height" + Constants.CSV_FILE_SEPARATOR + (eachLevelNumberOfInstances.length - 1) + "\n";
		content += "Avg hierarchy width" + Constants.CSV_FILE_SEPARATOR + avgHierarchyWidth + "\n";
		content += "Sample stdev hierarchy width" + Constants.CSV_FILE_SEPARATOR + stdevHierarchyWidth + "\n";
		content += "Number of nodes" + Constants.CSV_FILE_SEPARATOR + numberOfChildPerNode.size() + "\n";
		content += "Number of INTERNAL nodes" + Constants.CSV_FILE_SEPARATOR + numberOfChildPerInternalNode.size() + "\n";
		content += "Number of INTERNAL nodes with MIN BRANCHING FACTOR " + minimumBranchingFactor + Constants.CSV_FILE_SEPARATOR 
				+ numberOfChildPerNodeWithSpecifiedBranchingFactor.size() + "\n";
		content += "Number of leaves" + Constants.CSV_FILE_SEPARATOR + numberOfLeaves + "\n";
		content += "Avg path length" + Constants.CSV_FILE_SEPARATOR + avgPathLength + "\n";
		content += "Sample stdev path length" + Constants.CSV_FILE_SEPARATOR + stdevPathLength + "\n";
		
		content += "\n" + statsToWriteOutInFile + "\n\n"; 

		content += "Node" + Constants.CSV_FILE_SEPARATOR + "Mean vector"; 
		for(int i = 0; i < nodesEstimatedParameters.getFirst().getEmpiricalMean().length; i++)
		{
			content += Constants.CSV_FILE_SEPARATOR;
		}
		content += "Covariance matrix" + "\n"; 
		
		for(GroupWithEmpiricalParameters g: nodesEstimatedParameters)
		{
			content += g.getId() + Constants.CSV_FILE_SEPARATOR;
			
			for(int i = 0; i < g.getEmpiricalMean().length; i++)
			{
				content += g.getEmpiricalMean()[i] + Constants.CSV_FILE_SEPARATOR;
			}
			content = content.substring(0, content.length()-1);//trim last separator
			content += Constants.CSV_FILE_SEPARATOR;
			
			for(int i = 0; i < g.getEmpiricalCovariance().length; i++)
			{
				for(int j = 0; j < g.getEmpiricalCovariance()[0].length; j++)
				{
					content += g.getEmpiricalCovariance()[i][j] + Constants.CSV_FILE_SEPARATOR;
				}
				
				content = content.substring(0, content.length()-1);//trim last separator
				content += "\n";
				for(int k = 0; k < g.getEmpiricalCovariance().length + 1; k++)// +1 because of node id column
				{
					content += Constants.CSV_FILE_SEPARATOR;
				}
			}
			content = content.substring(0, content.length() - g.getEmpiricalCovariance().length - 1);//trim last spacings, -1 because of node id column
		}
		
		return content;
	}

	private void createStatsToVisualiseOnOutputImgsAndSummaryFile() {
		statsToVisualiseOnImages = "";
		
		statsToVisualiseOnImages += "Total number of points" + Constants.CSV_FILE_SEPARATOR + overallNumberOfInstances + "\n";
		statsToVisualiseOnImages += "Level" + Constants.CSV_FILE_SEPARATOR + "No Inst" + Constants.CSV_FILE_SEPARATOR + "% Inst";
		statsToWriteOutInFile = statsToVisualiseOnImages;//TODO: dodac reszte informacji
		statsToWriteOutInFile += Constants.CSV_FILE_SEPARATOR + "Avg. No of Children per node" + Constants.CSV_FILE_SEPARATOR + "Stdev"
				+ Constants.CSV_FILE_SEPARATOR + "Hierarchy width" + Constants.CSV_FILE_SEPARATOR + "No of leaves";
		
		for(int i = 0; i < eachLevelNumberOfInstances.length; i++)
		{
			String results = "\n" + i + Constants.CSV_FILE_SEPARATOR + eachLevelNumberOfInstances[i] + Constants.CSV_FILE_SEPARATOR + 
					Math.round((eachLevelNumberOfInstances[i]/(double)overallNumberOfInstances)*10000)/100.0;
			statsToVisualiseOnImages += results;
			statsToWriteOutInFile += results;//TODO: dodac reszte informacji
			statsToWriteOutInFile += Constants.CSV_FILE_SEPARATOR + avgNumberOfChilderPerNodeOnEachHeight[i] + Constants.CSV_FILE_SEPARATOR 
					+ stdevNumberOfChilderPerNodeOnEachHeight[i] + Constants.CSV_FILE_SEPARATOR + hierarchyWidthOnEachHeight[i]
					+ Constants.CSV_FILE_SEPARATOR + numberOfLeavesOnEachHeight[i];
		}
		
		int minBranchFactor = Collections.min(nodeBranchFactorAndCountOfNodesWithThatFactor.keySet());
		int maxBranchFactor = Collections.max(nodeBranchFactorAndCountOfNodesWithThatFactor.keySet());
		statsToWriteOutInFile += "\n\nBranching factor histogram\nFactor:";
		for(int i = minBranchFactor; i <= maxBranchFactor; i++)
		{
			statsToWriteOutInFile += Constants.CSV_FILE_SEPARATOR + i; 
		}
		statsToWriteOutInFile += "\nCount:";
		for(int i = minBranchFactor; i <= maxBranchFactor; i++)
		{
			statsToWriteOutInFile += Constants.CSV_FILE_SEPARATOR + nodeBranchFactorAndCountOfNodesWithThatFactor.get(i); 
		}
	}
	
	private int getHierarchyHeight(Hierarchy h) {
		int height = 0;
		Node root = h.getRoot();
		Stack<AbstractMap.SimpleEntry<Node, Integer>> s = new Stack<>(); //Node and its height
		s.push(new AbstractMap.SimpleEntry<Node, Integer>(root, 0));
		
		while(!s.isEmpty())
		{
			AbstractMap.SimpleEntry<Node, Integer> curr = s.pop();
			for(Node ch: curr.getKey().getChildren())
			{
				s.push(new AbstractMap.SimpleEntry<Node, Integer>(ch, curr.getValue() + 1));
			}
			
			height = Math.max(height, curr.getValue());
		}
		
		return height;
	}
	
	public int getHierarchyHeight()
	{
		return eachLevelNumberOfInstances.length-1;
	}
	
	public int getNumberOfInstances(int levelNumber)
	{
		return eachLevelNumberOfInstances[levelNumber];
	}
	
	public int getPercentageNumberOfInstances(int levelNumber)
	{
		return eachLevelNumberOfInstances[levelNumber]*100;
	}

	public String getSummaryString() {
		return statsToVisualiseOnImages;
	}

	public double getAvgNumberOfChildPerNode() {
		return avgNumberOfChildPerNode;
	}

	public double getAvgNumberOfInstancesPerNode() {
		return avgNumberOfInstancesPerNode;
	}

	public GroupWithEmpiricalParameters getEstimatedParameters(String nodeId) {
		for(GroupWithEmpiricalParameters g: nodesEstimatedParameters)
		{
			if(g.getId().equals(nodeId))
			{
				return g;
			}
		}
		return null;
	}

	public int getOverallNumberOfInstances() {
		return overallNumberOfInstances;
	}
}
