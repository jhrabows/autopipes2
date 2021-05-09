package org.autopipes.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.autopipes.model.AreaCutSheet;
import org.autopipes.model.AreaCutSheet.BranchInfo;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.DrawingArea.Readiness;
import org.autopipes.model.FloorDrawing;
import org.autopipes.model.RenderDwg;
import org.autopipes.service.AnalyzerService;
import org.autopipes.service.ReportingService;
import org.autopipes.service.StorageService;
import org.autopipes.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/")
public class JsonResponseController {
    private static Logger logger = Logger.getLogger(JsonResponseController.class);

    @Autowired
	@Qualifier("storageService")
	private StorageService storageService;
    @Autowired
	@Qualifier("analyzerService")
	private AnalyzerService analyzerService;
    @Autowired
    @Qualifier("reportingService")
	private ReportingService reportingService;

	@RequestMapping(value = "json/drawing", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<FloorDrawing> getAllDrawings() {
		logger.info("getDrawings");
		return storageService.findAllDrawings();
	}
	
	@RequestMapping(value = "json/drawing/{dwgId}/area", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<DrawingArea> getDrawingAreas(@PathVariable long dwgId) {
		logger.info("getDrawingAreas(" + dwgId + ")");
		return storageService.findDrawingAreas(dwgId);
	}
	
	@RequestMapping(value = "json/area", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<DrawingArea> getAllDrawingAreas() {
		logger.info("getAllDrawingAreas()");
		return storageService.findAllDrawingAreas();
	}

	@RequestMapping(value = "json/drawing/{dwgId}/area/{areaId}/branch", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Map<Integer, BranchInfo> getBranchInfoForArea(@PathVariable final long dwgId, @PathVariable final long areaId){
    try{
		logger.info("getBranchInfoForArea(" + dwgId + "," + areaId + ")");
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
    	return area.getAreaCutSheet().getBranchMap();
    }catch(Exception e){
    	logger.error("getBranchInfoForArea", e);
    	return null;
    }
    }
	
	@RequestMapping(value = "json/drawing/{dwgId}/area/{areaId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AreaCutSheet getCutSheetForArea(@PathVariable final long dwgId, @PathVariable final long areaId){
    try{
		logger.info("getCutSheetForArea(" + dwgId + "," + areaId + ")");
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
    	return area.getAreaCutSheet();
    }catch(Exception e){
    	logger.error("getCutSheetForArea", e);
    	return null;
    }
    }


	// Handler method to consume JSON request and produce text response
	@RequestMapping(value = "/post/json", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST,
	         produces = MediaType.TEXT_PLAIN_VALUE)
	   public ResponseEntity<String> postJSON(@RequestBody List<String> body) {
	      System.out.println(body);
	      // Process request
	      //....
	      return ResponseEntity
	            .ok()
	            .body("Done");
	   }
	
	// Handler method to consume XML request and produce text response
	@RequestMapping(value = "/post/xml", consumes = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.POST,
	         produces = MediaType.TEXT_PLAIN_VALUE)
	   public ResponseEntity<String> postJSON(@RequestBody String body) {
	      System.out.println(body);
	      // Process request
	      //....
	      return ResponseEntity
	            .ok()
	            .body("Done");
	   }
	

	// Handler method to consume XML request and produce XML response
	@RequestMapping(value = "xml/dwg-root", consumes = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.POST,
	         produces = MediaType.APPLICATION_XML_VALUE)
   public @ResponseBody ResponseEntity<String> loadDrawingConfiguration(@RequestBody String body) throws Exception {
	logger.info(body);
	FloorDrawing dwg = (FloorDrawing) Utils.unmarshal(body, FloorDrawing.class);
	storageService.mergeDrawing(dwg);
	List<DrawingArea> aList = storageService.findDrawingAreas(dwg.getId());
	for(DrawingArea a : aList){
	    dwg.addArea(a);
	}
  	dwg.setOptionsRoot(null);

      String xml = Utils.marshal(dwg);
    		  //"<ns2:dwg-root xmlns:ns2=\"http://dwg.autopipes.org\"><ns2:dwg-id>1203</ns2:dwg-id><ns2:dwg-name>new5thFloorShop</ns2:dwg-name><ns2:dwg-text-size>7.0</ns2:dwg-text-size><ns2:dwg-update-date>2021-05-04T23:10:41-04:00</ns2:dwg-update-date><ns2:area/></ns2:dwg-root>";

      return ResponseEntity
            .ok()
            .body(xml);
   }
	
	@RequestMapping(value = "xml/area-root", consumes = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.POST,
	         produces = MediaType.APPLICATION_XML_VALUE)
  public @ResponseBody ResponseEntity<String> loadDrawingArea(@RequestBody String body) throws Exception {
	logger.info(body);
	DrawingArea area= (DrawingArea) Utils.unmarshal(body, DrawingArea.class);
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
    String xml = Utils.marshal(area);

	return ResponseEntity
	  .ok()
	  .body(xml);

	}

	// Handler method to consume XML request and produce XML response
	@RequestMapping(value = "xml/render-dwg", consumes = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.POST,
	         produces = MediaType.APPLICATION_XML_VALUE)
   public @ResponseBody ResponseEntity<String> renderDrawingArea(@RequestBody String body) throws Exception {
	logger.info(body);
	RenderDwg render = (RenderDwg) Utils.unmarshal(body, RenderDwg.class);
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
    String xml = Utils.marshal(area);

	return ResponseEntity
	  .ok()
	  .body(xml);

	}
	
	
	public StorageService getStorageService() {
		return storageService;
	}


	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public AnalyzerService getAnalyzerService() {
		return analyzerService;
	}

	public void setAnalyzerService(AnalyzerService analyzerService) {
		this.analyzerService = analyzerService;
	}

	public ReportingService getReportingService() {
		return reportingService;
	}

	public void setReportingService(ReportingService reportingService) {
		this.reportingService = reportingService;
	}
	
}
