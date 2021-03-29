package org.autopipes.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.autopipes.model.AreaCutSheet;
import org.autopipes.model.AreaCutSheet.BranchInfo;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.Employee;
import org.autopipes.model.FloorDrawing;
import org.autopipes.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/json")
public class JsonResponseController {
    private static Logger logger = Logger.getLogger(JsonResponseController.class);
	
	@Autowired
	@Qualifier("storageService")
	private StorageService storageService;

	@RequestMapping(value = "/drawing", method = RequestMethod.GET) //, produces = "application/json")
	public @ResponseBody List<FloorDrawing> getAllDrawings() {
		logger.info("getDrawings");
		return storageService.findAllDrawings();
	}
	
	@RequestMapping(value = "/drawing/{dwgId}/area", method = RequestMethod.GET) //, produces = "application/json")
	public @ResponseBody List<DrawingArea> getDrawingAreas(@PathVariable long dwgId) {
		logger.info("getDrawingAreas(" + dwgId + ")");
		return storageService.findDrawingAreas(dwgId);
	}
	
	@RequestMapping(value = "/area", method = RequestMethod.GET) //, produces = "application/json")
	public @ResponseBody List<DrawingArea> getAllDrawingAreas() {
		logger.info("getAllDrawingAreas()");
		return storageService.findAllDrawingAreas();
	}

	@RequestMapping(value = "/drawing/{dwgId}/area/{areaId}/branch", method = RequestMethod.GET) //, produces = "application/json")
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
	
	@RequestMapping(value = "/drawing/{dwgId}/area/{areaId}", method = RequestMethod.GET) //, produces = "application/json")
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


	
	@RequestMapping(value = "/employees", method = RequestMethod.GET) //, produces = "application/json")
	public @ResponseBody List<Employee> getAllEmployee() {

		// We will set some dummy data and send it as response
		List<Employee> employees = new ArrayList<Employee>();

		Employee empl1 = new Employee("001", "John", "Doe", 25, 5000d);
		Employee empl2 = new Employee("001", "Steve", "Smith", 22, 4500d);
		Employee empl3 = new Employee("001", "Rob", "D", 27, 6000.50);

		employees.add(empl1);
		employees.add(empl2);
		employees.add(empl3);

		return employees;
	}


	public StorageService getStorageService() {
		return storageService;
	}


	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
	
}
