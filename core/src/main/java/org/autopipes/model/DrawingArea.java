package org.autopipes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.autopipes.model.AreaCutSheet.BranchInfo;
import org.autopipes.model.AreaCutSheet.CutSheetInfo;
import org.autopipes.model.AreaCutSheet.EdgeMultiplicity;
import org.autopipes.model.DrawingLayer.Designation;

/**
 * Jaxb bean which contains information about an area within a drawing.
 * It contains searchable attributes, area-level configuration XML
 * (areaOptions property of type AreaOptions) and the actual raw Acad drawing
 * (areaBody of type AreaBody). The rendering property contains a collection
 * of DwgEntity objects passed back to AutoCAD for decoration. This property is ignored
 * by SQL operations and is injected when returning rendering information.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "area-root")
public class DrawingArea {
	private static Logger logger = Logger.getLogger(DrawingArea.class);
//	
// Inner
//
	/**
	 * Area-level properties stored in the Acad block attached to the area.
	 */
	public enum AreaTag{
		areaId("area-id"),
		areaName("area-name"),
		mainStartNo("main-start-no"),
		branchStartNo("branch-start-no"),
		mainLabel("main-label"),
		branchLabel("branch-label"),
		shortLimit("ignore-shorter-than"),
		cutLimit("cut-longer-than"),
		cutSizeB("cut-branch-into"),
		cutSizeM("cut-main-into"),
		cutClearance("main-cut-clearance"),
		takeoutRounding("takeout-rounding");

		private final String tag;
		AreaTag(final String tag){
			this.tag = tag;
		}
		public String getTag() {
			return tag;
		}
	}

	@XmlRootElement(name = "readiness")
	public enum Readiness{
    	Ready,
    	Empty,
    	Disconnected,
    	LoopInBranch,
    	MainFittingFailure,
    	FittingFailure,
    	NoRaiser,
    	BadLengthOptionFormat,
    	NoClearanceToCutMain,
    	NotReady;
    }
//
// Main
//
	@XmlElement(name = "dwg-id")
    protected Long drawingId;
	@XmlElement(name = "area-id")
    protected Long areaId;
    @XmlElement(name = "area-name", required = true)
    protected String areaName;
    @XmlElement(name = "area-options")
    protected AreaOptions areaOptions;
    @XmlElement(name = "area-readiness", required = true)
    protected Readiness areaReadiness;
    @XmlElement(name = "defect-count", required = true)
    protected int defectCount;
    @XmlElement(name = "area-body")
    protected AreaBody areaBody;
    @XmlElement(name = "area-cut-sheet")
    protected AreaCutSheet areaCutSheet;
	@XmlElementWrapper(name = "rendering")
    @XmlElement(name = "dwg-entity")
    protected List<DwgEntity> rendering;
	
	/**
	 * Adds cut-sheet info records to the branch map. 
	 */
    public void countEdgeMultiplicity(){
      logger.info("+countEdgeMultiplicity");
	  Map<Integer, Boolean> visitedMap = new HashMap<Integer, Boolean>();
	  Map<CutSheetInfo, EdgeMultiplicity> branchEdgeMap
	    = new HashMap<CutSheetInfo, EdgeMultiplicity>();
	  for(Pipe e : getAreaBody().getEdgesInOrder()){
   		if(e.getDesignation() != Designation.Branch){
   			continue; // skip main and heads
   		}
   		if(e.isIgnored()){
   			continue; // skip short branches
   		}
   		int brId = e.getId();

   		// logic to skip duplicate branch ids
		Boolean visited = visitedMap.get(brId);
		if(getAreaBody().isOnMain(getAreaBody().getStartFitting(e))){
			// a branch root
   			if(visited == null){
   				// 1st time a branch-id encountered
   				// visitedMap is initialized to false to allow non-roots on this branch
   				visited = false;
   				visitedMap.put(brId, false);
   				// initialize the set
   				branchEdgeMap.clear();
   			}else if(!visited){
   				// we already seen a root with this branch id - skip all
   				visited = true;
   				visitedMap.put(brId, true);
   			}
   		}
   		if(visited != null && visited){
   			continue;
   		}

   		BranchInfo bInfo = getAreaCutSheet().getBranchMap().get(e.getId());
   		CutSheetInfo cInfo = cutSheetInfoForPipe(e);
  		if(getAreaBody().isOnMain(getAreaBody().getStartFitting(e))){
   			bInfo.setOrigin(e);
   		}
    		
   		EdgeMultiplicity em = branchEdgeMap.get(cInfo);
   		if(em == null){
   			em = new EdgeMultiplicity();
   			em.setCount(1);
   			em.setEdgeInfo(cInfo);
   			branchEdgeMap.put(cInfo, em);
   	        bInfo.getEdgeMultiplicity().add(em);
   		}else{
	   		em.setCount(em.getCount() + 1);
   		}
   	  }
      logger.info("-countEdgeMultiplicity");
    }
    /**
     * Creates CutSheetInfo object from a pipe
     * @param p the pipe
     * @return the info
     */
    public CutSheetInfo cutSheetInfoForPipe(Pipe p){
   		CutSheetInfo cInfo = new CutSheetInfo();
   		cInfo.setPipe(p);
   		PipeFitting end = getAreaBody().getEndFitting(p);
   		cInfo.setEndFitting(end.getFitting());
		PipeFitting start = getAreaBody().getStartFitting(p);
  		cInfo.setStartFitting(start.getFitting());
  		return cInfo;
    }
    /*
    public MainCutSheetInfo mainCutSheetInfoForChain(List<PipeFitting> chainList){
   		MainCutSheetInfo cInfo = new MainCutSheetInfo();
		for(int pipeIndex = 0; pipeIndex < chainList.size() - 1; pipeIndex++){
			Pipe nextMain = areaBody.getPipeFromChain(chainList, pipeIndex); 
			Integer id = nextMain.getId();
            if(id  != null){
            	cInfo.setId(id);
            	Diameter d = nextMain.getDiameter();
            	cInfo.setDiameter(d);
            }
		}
		if(chainList.size() == 2){
			Pipe pipe = areaBody.getPipeFromChain(chainList, 0);
			BigDecimal length = pipe.getAfterTakeout();
			cInfo.setLength(length);
		}else{
			PipeFitting startFitting = chainList.get(0);
			DwgPoint startCenter = startFitting.getCenter();
		}
  		return cInfo;
    }
    public MainCutSheetInfo mainCutSheetInfoForJump(Pipe e){
   		MainCutSheetInfo cInfo = new MainCutSheetInfo();
   		Integer id = e.getId();
   		Diameter d = e.getDiameter();
   		cInfo.setId(id);
   		cInfo.setDiameter(d);
   		return cInfo;
    }
    */

    
	public Long getDrawingId() {
		return drawingId;
	}
	public void setDrawingId(final Long drawingId) {
		this.drawingId = drawingId;
	}
	public Long getAreaId() {
		return areaId;
	}
	public void setAreaId(final Long areaId) {
		this.areaId = areaId;
	}
	public String getAreaName() {
		return areaName;
	}
	public void setAreaName(final String areaName) {
		this.areaName = areaName;
	}
	public AreaBody getAreaBody() {
		return areaBody;
	}
	public void setAreaBody(final AreaBody areaBody) {
		this.areaBody = areaBody;
	}
	public List<DwgEntity> getRendering() {
    	if(rendering == null){
    		rendering = new ArrayList<DwgEntity>();
    	}
		return rendering;
	}
	public void setRendering(final List<DwgEntity> rendering) {
		this.rendering = rendering;
	}
	public Readiness getAreaReadiness() {
		return areaReadiness;
	}
	public void setAreaReadiness(final Readiness areaReadiness) {
		this.areaReadiness = areaReadiness;
	}
	public int getDefectCount() {
		return defectCount;
	}
	public void setDefectCount(final int defectCount) {
		this.defectCount = defectCount;
	}
	public AreaOptions getAreaOptions() {
		return areaOptions;
	}
	public void setAreaOptions(final AreaOptions areaOptions) {
		this.areaOptions = areaOptions;
	}
    public AreaCutSheet getAreaCutSheet() {
    	if(areaCutSheet == null){
    		areaCutSheet = new AreaCutSheet();
    	}
		return areaCutSheet;
	}
	public void setAreaCutSheet(AreaCutSheet areaCutSheet) {
		this.areaCutSheet = areaCutSheet;
	}

}
