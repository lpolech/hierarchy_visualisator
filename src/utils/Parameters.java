package utils;

import java.awt.Color;
import java.nio.file.Path;

public class Parameters {
	private Path inputDataFilePath;
	private Path outputFolder;
	private Color currentLevelColor;
	private Color childGroupsColor;
	private Color parentGroupsColor;
	private Color parentAncestorsColor;
	private Color otherGroupsColor;
	private Color backgroundColor;
	private int imageWidth;
	private int imageHeight;
	private double pointScallingFactor;
	private int numberOfHistogramBins;
	private boolean displayAllPoints;
	private boolean classAttribute;
	private boolean instanceName;
	
	public boolean getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(boolean instanceName) {
		this.instanceName = instanceName;
	}

	private boolean skipVisualisations;
	
	public Color getCurrentLevelColor() {
		return currentLevelColor;
	}

	public void setCurrentLevelColor(Color currentLevelColor) {
		this.currentLevelColor = currentLevelColor;
	}

	public Color getChildGroupsColor() {
		return childGroupsColor;
	}

	public void setChildGroupsColor(Color childGroupsColor) {
		this.childGroupsColor = childGroupsColor;
	}

	public Color getParentGroupsColor() {
		return parentGroupsColor;
	}

	public void setParentGroupsColor(Color parentGroupsColor) {
		this.parentGroupsColor = parentGroupsColor;
	}

	public Color getOtherGroupsColor() {
		return otherGroupsColor;
	}

	public void setOtherGroupsColor(Color otherGroupsColor) {
		this.otherGroupsColor = otherGroupsColor;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public double getPointScallingFactor() {
		return pointScallingFactor;
	}

	public void setPointScallingFactor(double pointScallingFactor) {
		this.pointScallingFactor = pointScallingFactor;
	}

	public boolean isDisplayAllPoints() {
		return displayAllPoints;
	}

	public void setDisplayAllPoints(boolean displayAllPoints) {
		this.displayAllPoints = displayAllPoints;
	}

	public Path getInputDataFilePath() {
		return inputDataFilePath;
	}

	public void setInputDataFilePath(Path path) {
		this.inputDataFilePath = path;
	}

	public Path getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(Path path) {
		this.outputFolder = path;
	}

	public boolean isClassAttribute() {
		return classAttribute;
	}

	public void setClassAttribute(boolean classAttribute) {
		this.classAttribute = classAttribute;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroudColor) {
		this.backgroundColor = backgroudColor;
	}

	public Color getParentAncestorsColor() {
		return parentAncestorsColor;
	}

	public void setParentAncestorsColor(Color parentAncestorsColor) {
		this.parentAncestorsColor = parentAncestorsColor;
	}

	public void setNumberOfHistogramBins(int numberOfHistogramBins) {
		this.numberOfHistogramBins = numberOfHistogramBins;		
	}

	public int getNumberOfHistogramBins() {
		return numberOfHistogramBins;
	}

	public void setSkipVisualisations(boolean skipVisualisations) {
		this.skipVisualisations = skipVisualisations;
	}
	
	public boolean isSkipVisualisations()
	{
		return this.skipVisualisations;
	}
}
