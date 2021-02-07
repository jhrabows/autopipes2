package org.autopipes.model;

import static org.autopipes.util.ConversionUtils.tokenizeFootInchSequence;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Drawing Area meta-data.
 */
@XmlRootElement(name = "area-options-root")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctAreaOptions", propOrder = {})
public class AreaOptions {
    @XmlElement(name = "main-start-no")
    protected Integer mainStartNo = 1;
    @XmlElement(name = "takeout-rounding")
    protected Integer takeoutRounding = 1;
    @XmlElement(name = "branch-start-no")
    protected Integer branchStartNo = 1;
    @XmlElement(name = "main-label")
    protected String mainLabel = "M-";
    @XmlElement(name = "branch-label")
    protected String branchLabel = "#";
    @XmlElement(name = "ignore-shorter-than")
    protected String ignoreShorterThan = "1'";
	@XmlElement(name = "cut-longer-than")
    protected String cutLongerThan = "10'6\"";
	@XmlElement(name = "main-cut-clearance")
    protected String mainCutClearance = "6\""; 
	@XmlElement(name = "cut-branch-into")
    protected String branchCut = "7'";
    @XmlElement(name = "cut-main-into")
    protected String mainCut = "10'6\" 10' 9'";
    
    @XmlTransient
    protected List<Long> branchCutList;
    @XmlTransient
    protected List<Long> mainCutList;
    @XmlTransient
    protected Long shortPipe;
    @XmlTransient
    protected Long longPipe;
    @XmlTransient
    protected Long mainCutSpace;

    public Long getMainCutSpace() {
    	if(mainCutSpace == null){
    		mainCutSpace = org.autopipes.util.ConversionUtils.string2inches(mainCutClearance);
    	}
        return mainCutSpace;
    }
    
    public Long getLongPipe() {
    	if(longPipe == null){
    		longPipe = org.autopipes.util.ConversionUtils.string2inches(cutLongerThan);
    	}
       return longPipe;
    }

    
    public Long getShortPipe() {
    	if(shortPipe == null){
    		shortPipe = org.autopipes.util.ConversionUtils.string2inches(ignoreShorterThan);
    	}
        return shortPipe;
    }

    public List<Long> getMainCutList() {
    	if(mainCutList == null){
    		mainCutList = tokenizeFootInchSequence(mainCut);
    	}
        return mainCutList;
    }
    
    public List<Long> getBranchCutList() {
    	if(branchCutList == null){
    		branchCutList = tokenizeFootInchSequence(branchCut);
    	}
        return branchCutList;
    }
    
    public String getBranchCut(){
    	return branchCut;
    }
    public void setBranchCut(final String branchCut){
    	this.branchCut = branchCut;
    	branchCutList = null;
    }
    
    public String getMainCut(){
    	return mainCut;
    }
    public void setMainCut(final String mainCut){
    	this.mainCut = mainCut;
    	mainCutList = null;
    }
    public String getIgnoreShorterThan() {
		return ignoreShorterThan;
	}
	public void setIgnoreShorterThan(String ignoreShorterThan) {
		this.ignoreShorterThan = ignoreShorterThan;
		shortPipe = null;
	}
    public String getCutLongerThan() {
		return cutLongerThan;
	}
	public void setCutLongerThan(String cutLongerThan) {
		this.cutLongerThan = cutLongerThan;
		longPipe = null;
	}
    public String getMainCutClearance() {
		return mainCutClearance;
	}

	public void setMainCutClearance(String mainCutClearance) {
		this.mainCutClearance = mainCutClearance;
		mainCutSpace = null;
	}

	public Integer getTakeoutRounding() {
		return takeoutRounding;
	}
	public void setTakeoutRounding(final Integer takeoutRounding) {
		this.takeoutRounding = takeoutRounding;
	}
	public Integer getMainStartNo() {
		return mainStartNo;
	}
	public void setMainStartNo(final Integer mainStartNo) {
		this.mainStartNo = mainStartNo;
	}
	public Integer getBranchStartNo() {
		return branchStartNo;
	}
	public void setBranchStartNo(final Integer branchStartNo) {
		this.branchStartNo = branchStartNo;
	}
	public String getMainLabel() {
		return mainLabel;
	}
	public void setMainLabel(final String mainLabel) {
		this.mainLabel = mainLabel;
	}
	public String getBranchLabel() {
		return branchLabel;
	}
	public void setBranchLabel(final String branchLabel) {
		this.branchLabel = branchLabel;
	}
    


}
