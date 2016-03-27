package visualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import javax.imageio.ImageIO;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import utils.Constants;
import utils.ElementRole;
import utils.HierarchyStatistics;
import utils.Parameters;
import utils.Utils;
import utils.prefuse.histogram.HistogramGraph;
import utils.prefuse.histogram.HistogramTable;
import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.interfaces.Instance;
import basic_hierarchy.interfaces.Node;
//when the histogram is totally flat (every bar have the same height) then the histogram is not drawn
public class Visualisation {
	private int finalSizeOfNodes;
	private int treeOrientation;
	private double betweenLevelSpace;//the spacing to maintain between depth levels of the tree
	private double betweenSiblingsSpace;//the spacing to maintain between sibling nodes
    private double betweenSubtreesSpace;//the spacing to maintain between neighboring subtrees
    private int hierarchyImageHeight;
    private int hierarchyImageWidth;
    double nodeSizeToBetweenLevelSpaceRatio = 2.0;//minimum value
    double nodeSizeToBetweenSiblingsSpaceRatio = 4.0;//minimum value
    
	public void visualise(Hierarchy input, HierarchyStatistics stats, Parameters params)
	{
		int nodeImgLeftBorderWidth = 5;
		int nodeImgRightBorderWidth = 30;//this value determine the width of labels on the OY axis
		int nodeImgTopBorderHeight = 5;
		int nodeImgBottomBorderHeight = 30;//this value determine the width of labels on the OX axis
		int nodeImgFinalWidth = (int)(params.getImageWidth() + Math.max(1.0, params.getPointScallingFactor()/2));
		int nodeImgFinalHeight = (int)(params.getImageHeight() + Math.max(1.0, params.getPointScallingFactor()/2));
		
		double minAndMaxOfDimension[][] = calculateMinAndMaxValuesOfHierarchy(input.getRoot());
		
		Tree hierarchyBase = createHierarchyBase(input.getRoot(), params);
		
		
		LinkedList<Instance> allPoints = null;
		if(params.isDisplayAllPoints())
		{
			allPoints = input.getRoot().getSubtreeInstances();
		}
		
		int imgCounter = 0;
		for(Node n: input.getGroups())
		{
			if(n.getNodeInstances().size() > 0)
			{
				System.out.println("==== " + n.getId() + " ====");
				System.out.println("Point visualisation...");
				BufferedImage nodeImg = new BufferedImage(nodeImgFinalWidth, nodeImgFinalHeight, BufferedImage.TYPE_INT_ARGB);
				
				if(params.getBackgroundColor() != null)
					nodeImg = setBackgroud(nodeImg, params.getBackgroundColor());
				
				nodeImg = fillImage(nodeImg, n, params, minAndMaxOfDimension, allPoints);
				nodeImg = addBorder(nodeImg, nodeImgLeftBorderWidth, nodeImgRightBorderWidth, nodeImgTopBorderHeight, nodeImgBottomBorderHeight, Color.black);
				
				System.out.println("Hierary img...");
				BufferedImage hierarchyImg = createHierarchyImage(hierarchyBase, n.getId(), params);
				
				System.out.println("Prepare histogram data...");
				HistogramTable allDataHistogramTable = createAllDataHistogramTable(input.getRoot().getSubtreeInstances(), params.getNumberOfHistogramBins());
				HistogramGraph.setAllDataHistoTable(allDataHistogramTable);
				HistogramTable histogramTable = prepareHistogramTableUsingAllDataBins(n, allDataHistogramTable, ElementRole.CURRENT.getNumber());
				HistogramTable directParentHistogramTable = n.getParent() == null? 
						null:prepareHistogramTableUsingAllDataBins(n.getParent(), allDataHistogramTable, ElementRole.DIRECT_PARENT.getNumber()); 
				
				System.out.println("Horizontal histogram...");
				BufferedImage horizontalHistogram = createHorizontalHistogram(histogramTable, directParentHistogramTable, params, nodeImgFinalWidth, 
						nodeImgFinalHeight, nodeImgLeftBorderWidth, nodeImgRightBorderWidth);
				
				System.out.println("Vertical histogram...");
				BufferedImage verticalHistogram =  createVerticalHistogram(histogramTable, directParentHistogramTable, params, nodeImgFinalWidth, 
						nodeImgFinalHeight, nodeImgTopBorderHeight, nodeImgBottomBorderHeight);
				
				System.out.println("Statistics...");
				BufferedImage statsImg = createImageWithStatistics(params, n.getId(), stats, horizontalHistogram.getHeight(), verticalHistogram.getWidth());
				
				String finalImgName = imgCounter + "_" + n.getId();
				System.out.println("Concatenating imgs and saving as: " + finalImgName);
				BufferedImage trimmedImg = trimImg(hierarchyImg, params);
				concatenateImagesAndSave(params.getOutputFolder().toString(), finalImgName, nodeImg, trimmedImg,
						horizontalHistogram, verticalHistogram, statsImg);
				imgCounter++;
//				if(finalImgName.startsWith("2_"))
//				{
//					System.exit(0);
//				}
			}
		}
	}

	private BufferedImage createImageWithStatistics(Parameters params, String nodeId, HierarchyStatistics stats, int height, int width) {
		BufferedImage statsImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		String out = stats.getSummaryString();
		String[] splittedOut = out.split("\n");
		String[] outputStrings = new String[splittedOut.length - 1];
		int[] eachLineWidth = new int[outputStrings.length];
		outputStrings[0] = splittedOut[0].replace(Constants.CSV_FILE_SEPARATOR, ": ");
		eachLineWidth[0] = outputStrings[0].length();
		Graphics2D g2d = statsImg.createGraphics();
		float fontSize = 10.0f;
		int horizontalBorder = 10;
		int verticalBorder = 10;
		Font normalFont = new Font(null, Font.PLAIN, (int)fontSize);
		
		//create final lines
		String[] partsOfHeader = splittedOut[1].split(Constants.CSV_FILE_SEPARATOR);
		for(int i = 2; i < splittedOut.length; i++)
		{
			String[] lineVals = splittedOut[i].split(Constants.CSV_FILE_SEPARATOR);
			outputStrings[i-1] = partsOfHeader[0] + " " + (i - 2) + ": " + partsOfHeader[1] + " " + lineVals[1] + " (" + lineVals[2] + "%)"; 
			eachLineWidth[i-1] = outputStrings[i-1].length();
		}
		int linesHeight = splittedOut.length - 1;
		int maxLetterWidth = -1;
		int maxLetterWidthIndex = -1;
		
		for(int i = 0; i < eachLineWidth.length; i++)
		{
			if(eachLineWidth[i] > maxLetterWidth)
			{
				maxLetterWidth = eachLineWidth[i];
				maxLetterWidthIndex = i;
			}
		}
		
		if(maxLetterWidth > linesHeight)//we fit the bigger dimension to available space 
		{
			do
			{
				fontSize++;
				normalFont = normalFont.deriveFont(fontSize);
			}
			while(g2d.getFontMetrics(normalFont).stringWidth(outputStrings[maxLetterWidthIndex]) <= width - 2*horizontalBorder);
			
			fontSize--;
			normalFont = normalFont.deriveFont(fontSize);
		}
		else
		{
			do
			{
				fontSize++;
				normalFont = normalFont.deriveFont(fontSize);
			}
			while(g2d.getFontMetrics(normalFont).getAscent() + g2d.getFontMetrics(normalFont).getHeight()*(outputStrings.length-1) <= height - 2*verticalBorder);
			
			fontSize--;
			normalFont = normalFont.deriveFont(fontSize);
		}
		
		int nodeHeight = nodeId.split(Constants.HIERARCHY_LEVEL_SEPARATOR).length - 2;
		
		g2d.setFont(normalFont);
		for(int i = 0; i < outputStrings.length; i++)
		{
			int yCord = g2d.getFontMetrics().getAscent() + verticalBorder + g2d.getFontMetrics().getHeight()*i;
			if(nodeHeight+1 == i)
			{
				Font normalF = g2d.getFont();
				Color normalC = g2d.getColor();
				g2d.setFont(new Font(null, Font.BOLD, (int)fontSize));
				g2d.setColor(params.getCurrentLevelColor());
				g2d.drawString(outputStrings[i], horizontalBorder, yCord);
				g2d.setFont(normalF);
				g2d.setColor(normalC);
			}
			else
			{
				g2d.drawString(outputStrings[i], horizontalBorder, yCord);
			}
		}
		
		return statsImg;
	}

	private HistogramTable createAllDataHistogramTable(
			LinkedList<Instance> subtreeInstances, int numberOfHistogramBins) {
		Table histogramData = new Table();
		histogramData.addColumn("x", double.class);
		histogramData.addColumn("y", double.class);
		for(Instance i: subtreeInstances)
		{
			int newRowNum = histogramData.addRow();
			histogramData.set(newRowNum, "x", i.getData()[0]);
			histogramData.set(newRowNum, "y", i.getData()[1]);
		}
		
		HistogramTable histogramTable = new HistogramTable(histogramData, numberOfHistogramBins, ElementRole.OTHER.getNumber());
		return histogramTable;
	}

	private BufferedImage addBorder(BufferedImage img, int leftWidth, int rightWidth, int topHeight, int bottomHeight, Color color) {
		BufferedImage borderedImg = new BufferedImage(img.getWidth() + leftWidth + rightWidth, img.getHeight() + topHeight + bottomHeight, img.getType());
		Graphics2D g = borderedImg.createGraphics();
		
		g.setColor(color);
		g.fillRect(0, 0, borderedImg.getWidth(), borderedImg.getHeight());
		g.drawImage(img, leftWidth, topHeight, img.getWidth(), img.getHeight(), null);
		
		return borderedImg;
	}

	private void concatenateImagesAndSave(String outputFolderPath, String imgName, BufferedImage nodeImg,
			BufferedImage hierarchyImg, BufferedImage horizontalHistogram, BufferedImage verticalHistogram, BufferedImage statsImg) 
	{
		int finalImgWidth = nodeImg.getWidth() + hierarchyImg.getWidth()+ verticalHistogram.getWidth();
		int finalImgHeight = nodeImg.getHeight() + horizontalHistogram.getHeight();
		
		BufferedImage finalImg = new BufferedImage(finalImgWidth, finalImgHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = finalImg.createGraphics();
		
		g.drawImage(hierarchyImg, 0, 0, hierarchyImg.getWidth(), hierarchyImg.getHeight(), null);
		g.drawImage(nodeImg, hierarchyImg.getWidth(), 0, nodeImg.getWidth(), nodeImg.getHeight(), null);
		g.drawImage(verticalHistogram, hierarchyImg.getWidth() + nodeImg.getWidth(), 0, verticalHistogram.getWidth(),
				verticalHistogram.getHeight(), null);
		g.drawImage(horizontalHistogram, hierarchyImg.getWidth(), nodeImg.getHeight(), horizontalHistogram.getWidth(), 
				horizontalHistogram.getHeight(), null);
		g.drawImage(statsImg, hierarchyImg.getWidth() + nodeImg.getWidth(), nodeImg.getHeight(), statsImg.getWidth(), statsImg.getHeight(),
				null);
		
		try {
			ImageIO.write(finalImg, "PNG", new File(outputFolderPath + File.separator + imgName + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedImage createHistogram(HistogramTable histogramTable, HistogramTable directParentHistogramTable, String field, 
			Parameters params, int imgSize, int nodeImgLeftBorderWidth, int nodeImgRightBorderWidth) 
	{
		HistogramGraph histogramGraph = new HistogramGraph(histogramTable, directParentHistogramTable, field, imgSize, (int) (0.5*imgSize),
				params, nodeImgLeftBorderWidth, nodeImgRightBorderWidth);

		return getDisplaySnapshot(histogramGraph);
	}

	private BufferedImage createHorizontalHistogram(HistogramTable histogramTable,
			HistogramTable directParentHistogramTable, Parameters params, int nodeImgFinalWidth,
			int nodeImgFinalHeight, int nodeImgLeftBorderWidth, int nodeImgRightBorderWidth) {
		
		return createHistogram(histogramTable, directParentHistogramTable, "x", params, nodeImgFinalWidth, 
				nodeImgLeftBorderWidth, nodeImgRightBorderWidth);
	}
	
	private BufferedImage createVerticalHistogram(HistogramTable histogramTable,
			HistogramTable directParentHistogramTable, Parameters params, int nodeImgFinalWidth,
			int nodeImgFinalHeight, int nodeImgLeftBorderWidth, int nodeImgRightBorderWidth) {
		
		BufferedImage img = createHistogram(histogramTable, directParentHistogramTable, "y", params, nodeImgFinalHeight, 
				nodeImgLeftBorderWidth, nodeImgRightBorderWidth);
		img = Utils.rotate(img, 90);
		
		return img;
	}
	
	private HistogramTable prepareHistogramTableUsingAllDataBins(Node node, 
			HistogramTable allDataHistogramTable, int roleNum) {
		Table histogramData = new Table();
		histogramData.addColumn("x", double.class);
		histogramData.addColumn("y", double.class);
		for(Instance i: node.getNodeInstances())
		{
			int newRowNum = histogramData.addRow();
			histogramData.set(newRowNum, "x", i.getData()[0]);
			histogramData.set(newRowNum, "y", i.getData()[1]);
		}
		
		HistogramTable histogramTable = new HistogramTable(histogramData, /*100,*/ allDataHistogramTable, roleNum);
		return histogramTable;
	}

	private Tree createHierarchyBase(Node root, Parameters params)
	{
		Tree hierarchyVisualisation = new Tree();
		hierarchyVisualisation.addColumn(utils.Constants.PREFUSE_NODE_ID_COLUMN_NAME, String.class);
//		hierarchyVisualisation.addColumn(utils.Constants.PREFUSE_NUMBER_OF_INSTANCES_COLUMN_NAME, Integer.class);
		hierarchyVisualisation.addColumn(utils.Constants.PREFUSE_NODE_ROLE_COLUMN_NAME, int.class);
		
		prefuse.data.Node n = hierarchyVisualisation.addRoot();
		n.set(utils.Constants.PREFUSE_NODE_ID_COLUMN_NAME, root.getId());
//		n.set(utils.Constants.PREFUSE_NUMBER_OF_INSTANCES_COLUMN_NAME, root.getNodeInstances().size());
		n.setInt(utils.Constants.PREFUSE_NODE_ROLE_COLUMN_NAME, ElementRole.OTHER.getNumber());
		
		int maxTreeDepth = 0; //path from node to root
		int maxTreeWidth = 0;
		HashMap<Integer, Integer> treeLevelWithWidth = new HashMap<>();//FIXME: in case of better performance it would be better to change it to LinkedList
		//because HashMap is quick only when it is built, but the building process could be slow
		treeLevelWithWidth.put(0, 1);
		
		int nodesCounter = 1;
		
		Queue<Map.Entry<prefuse.data.Node, Node>> stackParentAndChild = new LinkedList<>(); //FIFO
		for(Node child: root.getChildren())
		{
			stackParentAndChild.add(new AbstractMap.SimpleEntry<prefuse.data.Node, Node>(n, child));
		}		
		
		while(!stackParentAndChild.isEmpty())
		{
			Entry<prefuse.data.Node, Node> sourceNodeWithItsParent = stackParentAndChild.remove();
			Node sourceNode = sourceNodeWithItsParent.getValue();
			
			n = hierarchyVisualisation.addChild(sourceNodeWithItsParent.getKey());
			nodesCounter++;
			n.set(utils.Constants.PREFUSE_NODE_ID_COLUMN_NAME, sourceNode.getId());
//			n.set(utils.Constants.PREFUSE_NUMBER_OF_INSTANCES_COLUMN_NAME, sourceNode.getNodeInstances().size());
			n.setInt(utils.Constants.PREFUSE_NODE_ROLE_COLUMN_NAME, ElementRole.OTHER.getNumber());			
			
			int nodeDepth = n.getDepth(); 
			if(nodeDepth > maxTreeDepth)
			{
				maxTreeDepth = nodeDepth;
			}
			
			Integer depthCount = treeLevelWithWidth.get(nodeDepth);
			if(depthCount == null)
			{
				treeLevelWithWidth.put(nodeDepth, 1);
			}
			else
			{
				treeLevelWithWidth.put(nodeDepth, depthCount + 1);
			}			
			
			for(Node child: sourceNode.getChildren())
			{
				stackParentAndChild.add(new AbstractMap.SimpleEntry<prefuse.data.Node, Node>(n, child));
			}
		}
		System.out.println("Number of nodes: " + nodesCounter);
		
		maxTreeWidth = java.util.Collections.max(treeLevelWithWidth.values());
		
		//predict height and width of hierarchy image
		finalSizeOfNodes = 0;
		int widthBasedSizeOfNodes = 0;
		int heightBasedSizeOfNodes = 0;
		treeOrientation = prefuse.Constants.ORIENT_TOP_BOTTOM;//TODO: the orientation of charts could be set automatically depending on the 
		//size of hierarchy 
	    betweenLevelSpace = 0.0;//the spacing to maintain between depth levels of the tree
	    betweenSiblingsSpace = 0.0;//the spacing to maintain between sibling nodes
	    betweenSubtreesSpace = 0.0;//the spacing to maintain between neighboring subtrees
	    hierarchyImageHeight = params.getImageHeight();
	    hierarchyImageWidth = params.getImageWidth();	    
	    betweenLevelSpace = Math.max(1.0, (hierarchyImageHeight)/
	    		(double)(nodeSizeToBetweenLevelSpaceRatio*(double)maxTreeDepth + nodeSizeToBetweenLevelSpaceRatio + (double)maxTreeDepth));
	    //based on above calculation - compute "optimal" size of each node on image
	    heightBasedSizeOfNodes = (int) (nodeSizeToBetweenLevelSpaceRatio*betweenLevelSpace);
	    
	    System.out.println("Between level space: " + betweenLevelSpace + " node size: " + heightBasedSizeOfNodes);
	    
	   betweenSiblingsSpace = Math.max(1.0, ((double)hierarchyImageWidth)/((double)maxTreeWidth*nodeSizeToBetweenSiblingsSpaceRatio + (double)maxTreeWidth - 1.0));
	   betweenSubtreesSpace = betweenSiblingsSpace;
	   widthBasedSizeOfNodes = (int)(nodeSizeToBetweenSiblingsSpaceRatio*betweenSiblingsSpace);
	   System.out.println("Between siblings space: " + betweenSiblingsSpace + " node size: " + widthBasedSizeOfNodes);
	    
	    //below use MAXIMUM height/width
	    if(widthBasedSizeOfNodes < heightBasedSizeOfNodes)
	    {
	    	finalSizeOfNodes = widthBasedSizeOfNodes;
	    	//assume maximum possible size
	    	betweenLevelSpace = Math.max(1.0, ((double)hierarchyImageHeight-(double)maxTreeDepth*(double)finalSizeOfNodes - (double)finalSizeOfNodes)/(double)maxTreeDepth);
	    }
	    else
	    {
	    	finalSizeOfNodes = heightBasedSizeOfNodes;
	    	//assume maximum possible size
	    	betweenSiblingsSpace = Math.max(1.0, ((double)hierarchyImageWidth - (double)maxTreeWidth*(double)finalSizeOfNodes)/((double)maxTreeWidth-1.0));
	    	betweenSubtreesSpace = betweenSiblingsSpace;
	    }
	    
	    return hierarchyVisualisation;
	}
	
	private BufferedImage createHierarchyImage(Tree hierarchyVisualisation, String currentNodeId, Parameters params) {
		boolean isFound = false;
		
for(int i = 0; i < hierarchyVisualisation.getNodeCount(); i++)
		{
			prefuse.data.Node n = hierarchyVisualisation.getNode(i);
			n.setInt(utils.Constants.PREFUSE_NODE_ROLE_COLUMN_NAME, ElementRole.OTHER.getNumber());
		}
		
		for(int i = 0; i < hierarchyVisualisation.getNodeCount() && !isFound; i++)
		{
			prefuse.data.Node n = hierarchyVisualisation.getNode(i);
			if(n.getString(utils.Constants.PREFUSE_NODE_ID_COLUMN_NAME).equals(currentNodeId))
			{
				isFound = true;
				//colour child groups
				LinkedList<prefuse.data.Node> stack = new LinkedList<>();
				stack.add(n);
				while(!stack.isEmpty())
				{
					prefuse.data.Node current = stack.removeFirst();
					current.setInt(utils.Constants.PREFUSE_NODE_ROLE_COLUMN_NAME, ElementRole.CHILD.getNumber());
					for(@SuppressWarnings("unchecked")
					Iterator<prefuse.data.Node> children = current.children(); children.hasNext();)
					{
						prefuse.data.Node child = children.next();
						stack.add(child);
					}
				}
				
				if(params.isDisplayAllPoints() && n.getParent() != null)
				{
					stack = new LinkedList<>();
					prefuse.data.Node directParent = n.getParent();//when the parent is empty, then we need to search up in the hierarchy because empty
					//parents are skipped,but displayed on output images
					stack.add(directParent);
					while(!stack.isEmpty())
					{
						prefuse.data.Node current = stack.removeFirst();
						current.setInt(utils.Constants.PREFUSE_NODE_ROLE_COLUMN_NAME, ElementRole.INDIRECT_PARENT.getNumber());
						if(current.getParent() != null)
						{
							stack.add(current.getParent());
						}
					}
					directParent.setInt(utils.Constants.PREFUSE_NODE_ROLE_COLUMN_NAME, ElementRole.DIRECT_PARENT.getNumber());
				}
				n.setInt(utils.Constants.PREFUSE_NODE_ROLE_COLUMN_NAME, ElementRole.CURRENT.getNumber());
			}
		}
		
		Visualization vis = new Visualization();
		Display display = new Display(vis);
		display.setBackground(new Color(-1));
		display.setHighQuality(true);
		
		vis.add(utils.Constants.NAME_OF_HIERARCHY, hierarchyVisualisation);
		
		NodeRenderer r = new NodeRenderer(finalSizeOfNodes, params);
		DefaultRendererFactory drf = new DefaultRendererFactory(r);
		EdgeRenderer edgeRenderer = new EdgeRenderer(prefuse.Constants.EDGE_TYPE_LINE);
		drf.setDefaultEdgeRenderer(edgeRenderer);
		vis.setRendererFactory(drf);
				
		ColorAction edgesColor = new ColorAction(utils.Constants.NAME_OF_HIERARCHY + ".edges", VisualItem.STROKECOLOR, ColorLib.color(Color.lightGray));
		
		NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(utils.Constants.NAME_OF_HIERARCHY, treeOrientation, betweenLevelSpace, 
				betweenSiblingsSpace, betweenSubtreesSpace);
		treeLayout.setLayoutBounds(new Rectangle2D.Float(0, 0, hierarchyImageWidth, hierarchyImageHeight));
		treeLayout.setRootNodeOffset(0);//0.5*finalSizeOfNodes);//offset is set in order to show all nodes on images
		ActionList layout = new ActionList();
		layout.add(treeLayout);
		layout.add(new RepaintAction());
		
		vis.putAction(utils.Constants.NAME_OF_HIERARCHY + ".edges", edgesColor);
		vis.putAction(utils.Constants.NAME_OF_HIERARCHY + ".layout", layout);
		
		display.setSize((int)(5.0*hierarchyImageWidth), hierarchyImageHeight);//TODO we can here implement a heuristic that will check if after enlarging is the border lines (rows 
		//and columns) of pixels do not contain other values than background colour. If so, then we are expanding one again, otherwise we have appropriate size of image

		vis.run(utils.Constants.NAME_OF_HIERARCHY + ".edges");//TODO: in run function a threads are used, so threads could be somehow used to fill the images more efficiently
		vis.run(utils.Constants.NAME_OF_HIERARCHY + ".layout");
		
		Utils.waitUntilActivitiesAreFinished();
		
		return getDisplaySnapshot(display);
	}
	
	private BufferedImage trimImg(BufferedImage img, Parameters params) {//TODO instead of iterating through the columns of image, we can use BINARY SEARCH through columns
		//and check if column doesn't contain at least 1 non-background colour pixel
	    int imgHeight = img.getHeight();
	    int imgWidth  = img.getWidth();


	    //TRIM WIDTH - LEFT
	    int startWidth = 0;
	    for(int x = 0; x < imgWidth; x++) {
	        if (startWidth == 0) {
	            for (int y = 0; y < imgHeight; y++) {
	                if (img.getRGB(x, y) != params.getBackgroundColor().getRGB()) {
	                    startWidth = x;
	                    break;
	                }
	            }
	        } else break;
	    }


	    //TRIM WIDTH - RIGHT
	    int endWidth  = 0;
	    for(int x = imgWidth - 1; x >= 0; x--) {
	        if (endWidth == 0) {
	            for (int y = 0; y < imgHeight; y++) {
	                if (img.getRGB(x, y) != params.getBackgroundColor().getRGB()) {
	                    endWidth = x;
	                    break;
	                }
	            }
	        } else break;
	    }
	    
	    int newWidth  = endWidth - startWidth;

        BufferedImage newImg = new BufferedImage(newWidth, imgHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = newImg.createGraphics();
        g.drawImage(img, 0, 0, newImg.getWidth(), newImg.getHeight(), startWidth, 0, endWidth, imgHeight, null );
        img = newImg;
        
        return img;
	}
	
	private BufferedImage setBackgroud(BufferedImage image, Color backgroundColor) {
		Graphics2D g2d = image.createGraphics();
		g2d.setPaint(backgroundColor);
		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
		g2d.dispose();
		return image;
	}

	private BufferedImage fillImage(BufferedImage nodeImg, Node n, Parameters params, double[][] minAndMaxOfDimension, 
			LinkedList<Instance> allPoints) {
		Graphics2D imgContent = nodeImg.createGraphics();
		if(params.isDisplayAllPoints())
		{
			drawPoints(imgContent, allPoints, params.getOtherGroupsColor(), params.getPointScallingFactor(),
					params.getImageWidth(), params.getImageHeight(), minAndMaxOfDimension);
			
			if(n.getParent() != null)
			{
				drawParentAncestorsPoints(imgContent, n, params.getParentAncestorsColor(), params.getPointScallingFactor(),
						params.getImageWidth(), params.getImageHeight(), minAndMaxOfDimension);
				
				drawPoints(imgContent, n.getParent().getNodeInstances(), params.getParentGroupsColor(), params.getPointScallingFactor(),
						params.getImageWidth(), params.getImageHeight(), minAndMaxOfDimension);
			}
		}
		drawPoints(imgContent, n.getSubtreeInstances(), params.getChildGroupsColor(), params.getPointScallingFactor(),
				params.getImageWidth(), params.getImageHeight(), minAndMaxOfDimension);
		drawPoints(imgContent, n.getNodeInstances(), params.getCurrentLevelColor(), params.getPointScallingFactor(),
				params.getImageWidth(), params.getImageHeight(), minAndMaxOfDimension);
		
		imgContent.dispose();
		return nodeImg;
	}

	private void drawParentAncestorsPoints(Graphics2D imgContent, Node parent, Color parentAncestorsColor,
			double pointScallingFactor, int imageWidth, int imageHeight, double[][] minAndMaxOfDimension) {
		Node n = parent;
		while(n.getParent() != null)
		{
			drawPoints(imgContent, n.getParent().getNodeInstances(), parentAncestorsColor, pointScallingFactor, imageWidth, imageHeight,
					minAndMaxOfDimension);
			n = n.getParent();
		}		
	}

	private void drawPoints(Graphics2D imgContent, LinkedList<Instance> points,
			Color color, double pointScallingFactor, int imgWidth, int imgHeight, double[][] minAndMaxOfDimension) {
		Color oldColor = imgContent.getColor();
		imgContent.setColor(color);
		
		int pointSize = (int)pointScallingFactor - 1;
		
		for(Instance i: points)
		{
			double x = i.getData()[0];
			double y = i.getData()[1];
			int pointLeftEdge = rectCoordinateOnImage(x, minAndMaxOfDimension[0][0], minAndMaxOfDimension[0][1], imgWidth, pointSize);
			int pointTopEdge = rectCoordinateOnImage(y, minAndMaxOfDimension[1][0], minAndMaxOfDimension[1][1], imgHeight, pointSize);
			
			imgContent.fillRect(pointLeftEdge, pointTopEdge, pointSize + 1, pointSize + 1); //1 px * scallingFactor
		}
		
		imgContent.setColor(oldColor);
	}

	private int rectCoordinateOnImage(double sourceValue, double min, double max, int dimSize, int pointSize) {
		double doubleReturnValue = dimSize*(sourceValue - min)/(max-min);
		doubleReturnValue -= pointSize/2.0;
		return (int)doubleReturnValue;
	}
	
	private double[][] calculateMinAndMaxValuesOfHierarchy(Node input) {
		double[][] firstTwoDimMinAndMax = new double[2][2];
		double firstDimMin = Double.MAX_VALUE, secondDimMin = Double.MAX_VALUE;
		double firstDimMax = (-1)*Double.MAX_VALUE, secondDimMax = (-1)*Double.MAX_VALUE;
		
		for(Instance i: input.getSubtreeInstances())
		{
			double firstVal = i.getData()[0];
			double secondVal = i.getData()[1];
			
			if(firstDimMax < firstVal)
				firstDimMax = firstVal;
			
			if(firstDimMin > firstVal)
				firstDimMin = firstVal;
			
			if(secondDimMax < secondVal)
				secondDimMax = secondVal;
			
			if(secondDimMin > secondVal)
				secondDimMin = secondVal;
		}
		
		firstTwoDimMinAndMax[0][0] = firstDimMin;
		firstTwoDimMinAndMax[0][1] = firstDimMax;
		
		firstTwoDimMinAndMax[1][0] = secondDimMin;
		firstTwoDimMinAndMax[1][1] = secondDimMax;
		
		return firstTwoDimMinAndMax;
	}
	
	public static BufferedImage getDisplaySnapshot(Display dis)
	{
		BufferedImage img = null;
		try {
            // get an image to draw into
            Dimension d = new Dimension(dis.getWidth(),
            		dis.getHeight());
            if ( !GraphicsEnvironment.isHeadless() ) {
                try {
                    img = (BufferedImage)dis.createImage(dis.getWidth(), dis.getHeight());
                } catch ( Exception e ) {
                    img = null;
                }
            }
            if ( img == null ) {
                img = new BufferedImage(dis.getWidth(), dis.getHeight(),
                                         BufferedImage.TYPE_INT_RGB);
            }
            Graphics2D g = (Graphics2D)img.getGraphics();
            
            // set up the display, render, then revert to normal settings
            Point2D p = new Point2D.Double(0,0);
            dis.zoom(p, 1.0); // also takes care of damage report
            boolean q = dis.isHighQuality();
            dis.setHighQuality(true);
            dis.paintDisplay(g, d);
            dis.setHighQuality(q);
            dis.zoom(p, 1.0); // also takes care of damage report
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
        return img;
	}
}
