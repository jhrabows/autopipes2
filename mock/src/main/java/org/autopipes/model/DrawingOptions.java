package org.autopipes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.autopipes.model.DrawingLayer.Designation;


/**
 * Jaxb bean containing
 * parameters related to a particular drawing which are not accessible individually
 * via database queries.
 */
@XmlRootElement(name = "options-root")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctOptions", propOrder = {})
public class DrawingOptions {

    @XmlElement(name = "short-pipe")
    protected double shortPipe;
    @XmlElement(name = "long-pipe")
    protected double longPipe;
    @XmlElement(name = "main-cut-space")
    protected double mainCutSpace;
    @XmlElement(name = "error-color", required = true)
    protected String errorColor;
    @XmlElement(name = "weld-if-equal")
    protected String weldIfEqual = "N";
    @XmlElement(name ="abco-mode")
    protected String abcoMode = "N";
	@XmlElement(name = "main-prefix")
    protected String mainPrefix;
    @XmlElement(name = "jump-indicator")
    protected String jumpIndicator;
    @XmlElementWrapper(name = "main-cut-list")
    @XmlElement(name = "main-cut", required = true)
    protected List<Double> mainCut;

    @XmlElementWrapper(name = "branch-cut-list")
    @XmlElement(name = "branch-cut", required = true)
    protected List<Double> branchCut;

    @XmlElement(name = "branch-prefix")
    protected String branchPrefix;
    @XmlElement(name = "main-count-start")
    protected long mainCountStart;
    @XmlElement(name = "branch-count-start")
    protected long branchCountStart;

    @XmlElementWrapper(name = "layers-root")
    protected List<DrawingLayer> layer;
    @XmlTransient
    private DrawingLayer outputLayer;
    @XmlTransient
    private Map<String, DrawingLayer> layerMap;

	public Map<String, DrawingLayer> getLayerMap() {
		if(layerMap == null){
			layerMap = new HashMap<String, DrawingLayer>();
	    	for(DrawingLayer l : layer){
	    		layerMap.put(l.name, l);
	    	}
		}
		return layerMap;
	}
	
	public void preSerialize(){
	  if(mainCut != null){
		Collections.sort(mainCut, new java.util.Comparator<Double>(){
		    public int compare(final Double d1, final Double d2){
				return d2.compareTo(d1);
				}
		    });
	  }
	}

    public DrawingLayer findLayer(final String name){
    	return getLayerMap().get(name);
    }
    public DrawingLayer getOutputLayer(){
    	if(outputLayer == null){
	    	for(DrawingLayer l : layer){
	    		if(l.getType() == Designation.Output){
	    			outputLayer = l;
	    			break;
	    		}
	    	}
    	}
    	return outputLayer;
    }
    // Getters and setters


    public double getShortPipe() {
        return shortPipe;
    }

    public void setShortPipe(final double value) {
        shortPipe = value;
    }

    public double getLongPipe() {
        return longPipe;
    }

    public void setLongPipe(final double value) {
        longPipe = value;
    }

    public double getMainCutSpace() {
        return mainCutSpace;
    }

    public void setMainCutSpace(final double value) {
        mainCutSpace = value;
    }

    public String getErrorColor() {
        return errorColor;
    }

    public void setErrorColor(final String value) {
        errorColor = value;
    }
    
    public String getWeldIfEqual() {
		return weldIfEqual;
	}

	public void setWeldIfEqual(String weldIfEqual) {
		this.weldIfEqual = weldIfEqual;
	}
	
	public String getAbcoMode() {
		return abcoMode;
	}

	public void setAbcoMode(String abcoMode) {
		this.abcoMode = abcoMode;
	}



    public String getMainPrefix() {
        return mainPrefix;
    }

    public void setMainPrefix(final String value) {
        mainPrefix = value;
    }

    /**
     * Gets the value of the mainCutList property.
     * The set ordered by descending double values.
     *
     * @return the set
     */
    public List<Double> getMainCutList() {
    	if(mainCut == null){
    		mainCut = new ArrayList<Double>();
    	}
        return mainCut;
    }

    public void setMainCutList(final List<Double> value) {
        mainCut = value;
    }

    public List<Double> getBranchCutList() {
    	if(branchCut == null){
    		branchCut = new ArrayList<Double>();
    	}
        return branchCut;
    }

    public void setBranchCutList(final List<Double> value) {
        branchCut = value;
    }

    public String getBranchPrefix() {
        return branchPrefix;
    }

    public void setBranchPrefix(final String value) {
        branchPrefix = value;
    }

    public long getMainCountStart() {
        return mainCountStart;
    }

    public void setMainCountStart(final long value) {
        mainCountStart = value;
    }

    public long getBranchCountStart() {
        return branchCountStart;
    }

    public void setBranchCountStart(final long value) {
        branchCountStart = value;
    }

	public List<DrawingLayer> getLayer() {
    	if(layer == null){
    		layer = new ArrayList<DrawingLayer>();
    	}
		return layer;
	}

	public void setLayer(final List<DrawingLayer> layer) {
		this.layer = layer;
	}
	public void setOutputLayer(final DrawingLayer outputLayer) {
		this.outputLayer = outputLayer;
	}
	public String getJumpIndicator() {
		return jumpIndicator;
	}
	public void setJumpIndicator(final String jumpIndicator) {
		this.jumpIndicator = jumpIndicator;
	}
	public void setLayerMap(final Map<String, DrawingLayer> layerMap) {
		this.layerMap = layerMap;
	}
}
