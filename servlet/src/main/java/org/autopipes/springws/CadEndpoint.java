package org.autopipes.springws;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.autopipes.model.AreaCutSheet.BranchInfo;
import org.autopipes.model.AreaCutSheet.CutSheetInfo;
import org.autopipes.model.AreaCutSheet.MainCutSheetInfo;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.DrawingArea.Readiness;
import org.autopipes.model.FloorDrawing;
import org.autopipes.model.RenderDwg;
import org.autopipes.service.AnalyzerService;
import org.autopipes.service.ReportingService;
import org.autopipes.service.StorageService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

@Endpoint
public class CadEndpoint {
    private static Logger logger = Logger.getLogger(CadEndpoint.class);

	StorageService storageService;
	AnalyzerService analyzerService;
	ReportingService reportingService;

//
// Web Service calls
//
    @PayloadRoot(localPart = "dwg-root", namespace = "http://dwg.autopipes.org")
/**
 * Updates drawing configuration passed in the options tag of an xml message.
 * @param dwg the xml msg
 * @return the passed configuration header with an attached list containing
 * state information for all drawing areas.
 */
    public FloorDrawing loadDrawingConfiguration(final FloorDrawing dwg) throws Exception{
    	logger.info("+loadDrawingConfiguration");
    	storageService.mergeDrawing(dwg);
    	List<DrawingArea> aList = storageService.findDrawingAreas(dwg.getId());
        for(DrawingArea a : aList){
            dwg.addArea(a);
        }
    	dwg.setOptionsRoot(null);
    	logger.info("-loadDrawingConfiguration->" + dwg.getId());
    	return dwg;
    }
    /**
     * Gets an Acad rendering for an area Id (some rendering may be suppressed
     * by options in the request). Also calculates and saves the cut-sheet info.
     * @param render the rendering request
     * @return the Acad entities to be rendered
     * @throws Exception
     */
    @PayloadRoot(localPart = "render-dwg", namespace = "http://dwg.autopipes.org")
    public DrawingArea renderDrawingArea(final RenderDwg render) throws Exception{
    	logger.info("+renderDrawingArea");
    // fetch raw drawing info from DB
    	long dwgId = render.getDrawingId();
    	long areaId = render.getAreaId();
    	FloorDrawing dwg = storageService.findOneDrawing(dwgId);
        if(dwg == null){
        	throw new IllegalArgumentException(
        			"Unknown drawing id " + dwgId);
        }
        DrawingArea area = storageService.findOneDrawingArea(dwgId, areaId, true, false);
        if(area == null){
        	throw new IllegalArgumentException(
        			"Unknown area id " + areaId + " for drawing " + dwgId);
        }
    // perform calculations
    	analyzerService.validateArea(dwg, area);
    	if(area.getAreaReadiness() != Readiness.Ready){
    		// this could happen due to configuration change on the drawing
    		// so we should really extract the errors here and return them
    		throw new IllegalStateException("Area id " + areaId + " for drawing " + dwgId
    				+ " is in a not-ready state " + area.getAreaReadiness());
    	}
    // build cut-sheet report
    	analyzerService.buildCutSheetReport(dwg, area);
    // prepare ACAD entities for the reply
    	if(render.isBranchLabels() != null && render.isBranchLabels()){
    	    reportingService.renderBranchLabels(dwg, area);
    	}
    	if(render.isMainLabels() != null && render.isMainLabels()){
    	    reportingService.renderMainLabels(dwg, area);
    	}
    	
    	boolean doMainBreaks =  render.isMainBreakups() != null && render.isMainBreakups();
    	boolean doBranchBreaks = render.isBranchBreakups() != null && render.isBranchBreakups();
    	reportingService.renderBreakups(dwg, area, doMainBreaks, doBranchBreaks);
    	
    	boolean doMainSpans = render.isMainSpans() != null && render.isMainSpans();
    	boolean doBranchSpans = render.isBranchSpans() != null && render.isBranchSpans();
    	boolean doMainE2E = render.isMainE2E() != null && render.isMainE2E();
    	boolean doBranchE2E = render.isBranchE2E() != null && render.isBranchE2E();
    	
    	reportingService.renderSizes(dwg, area, doMainSpans, doBranchSpans, doMainE2E, doBranchE2E);
    	
    	// removed raw Acad drawing from the reply and from the update
        area.setAreaBody(null);
        // persist cut-sheet report
    	storageService.mergeArea(area);
    	// remove cut-sheet from the reply
    	area.setAreaCutSheet(null);
    	logger.info("-renderDrawingArea");
    	return area;
    }

    @PayloadRoot(localPart = "area-root", namespace = "http://dwg.autopipes.org")
    /**
     * Saves new or changed area in a drawing.
     * Creates Area object from XML. Fetches corresponding drawing configuration from
     * FloorDrawing. Validates and persists the area together with
     * the calculated cut-sheet report.
     * Converts validation errors (if any) to Acad text-boxes and adds them to the reply.
     */
    public DrawingArea loadDrawingArea(final DrawingArea area) throws Exception{
    	logger.info("+loadDrawingArea");
    	// flag new area
    	boolean newArea = (area.getAreaId() == null);
    	// pre-fetch parent drawing
    	FloorDrawing dwg = storageService.findOneDrawing(area.getDrawingId());
        if(dwg == null){
        	throw new IllegalArgumentException(
        			"Area references unknown drawing id " + area.getDrawingId());
        }
    	// validate
    	analyzerService.validateArea(dwg, area);
    	// persist
    	storageService.mergeArea(area);
    	// prepare reply
    	reportingService.renderAreaStatus(dwg, area);
    	if(newArea){
    		reportingService.renderAreaIdStamp(dwg, area);
    	}
    	// remove the received collection of Acad entities from the reply
    	area.setAreaBody(null);
    	return area;
    }
//
// Complex DWR calls
//
    public Map<Integer, BranchInfo> getBranchInfoForArea(final long dwgId, final long areaId){
    try{
    	FloorDrawing dwg = storageService.findOneDrawing(dwgId);
        if(dwg == null){
        	throw new IllegalArgumentException(
        			"Unknown drawing id " + dwgId);
        }
        DrawingArea area = storageService.findOneDrawingArea(dwgId, areaId, false, true);
        if(area == null){
        	throw new IllegalArgumentException(
        			"Unknown area id " + areaId + " for drawing " + dwgId);
        }
    	//analyzerService.validateArea(dwg, area);
    	//area.countEdgeMultiplicity();
    	return area.getAreaCutSheet().getBranchMap();
    }catch(Exception e){
    	logger.error("getBranchInfoForArea", e);
    	return null;
    }
    }
    
    public List<CutSheetInfo> getMainThreadedList(final long dwgId, final long areaId){
    	FloorDrawing dwg = storageService.findOneDrawing(dwgId);
        if(dwg == null){
        	throw new IllegalArgumentException(
        			"Unknown drawing id " + dwgId);
        }
        DrawingArea area = storageService.findOneDrawingArea(dwgId, areaId, false, true);
        if(area == null){
        	throw new IllegalArgumentException(
        			"Unknown area id " + areaId + " for drawing " + dwgId);
        }
        List<CutSheetInfo> ret = area.getAreaCutSheet().getMainThreadedList();
        reportingService.sortThreadedMain(ret);
        return ret;
    }
    
    public List<MainCutSheetInfo> getMainGroovedList(final long dwgId, final long areaId){
    	FloorDrawing dwg = storageService.findOneDrawing(dwgId);
        if(dwg == null){
        	throw new IllegalArgumentException(
        			"Unknown drawing id " + dwgId);
        }
        DrawingArea area = storageService.findOneDrawingArea(dwgId, areaId, false, true);
        if(area == null){
        	throw new IllegalArgumentException(
        			"Unknown area id " + areaId + " for drawing " + dwgId);
        }
        List<MainCutSheetInfo> ret = area.getAreaCutSheet().getMainGroovedList();
//        reportingService.sortThreadedMain(ret);
        return ret;
    }

    public List<MainCutSheetInfo> getMainWeldedList(final long dwgId, final long areaId){
    	FloorDrawing dwg = storageService.findOneDrawing(dwgId);
        if(dwg == null){
        	throw new IllegalArgumentException(
        			"Unknown drawing id " + dwgId);
        }
        DrawingArea area = storageService.findOneDrawingArea(dwgId, areaId, false, true);
        if(area == null){
        	throw new IllegalArgumentException(
        			"Unknown area id " + areaId + " for drawing " + dwgId);
        }
        List<MainCutSheetInfo> ret = area.getAreaCutSheet().getMainWeldedList();
 //       reportingService.sortThreadedMain(ret);
        return ret;
    }

    //
    // getters and setters for IoC
    //
	public StorageService getStorageService() {
		return storageService;
	}
	public void setStorageService(final StorageService storageService) {
		this.storageService = storageService;
	}
	public AnalyzerService getAnalyzerService() {
		return analyzerService;
	}
	public void setAnalyzerService(final AnalyzerService analyzerService) {
		this.analyzerService = analyzerService;
	}
	public ReportingService getReportingService() {
		return reportingService;
	}
	public void setReportingService(final ReportingService reportingService) {
		this.reportingService = reportingService;
	}

}
