package org.autopipes.controller;

import java.util.ArrayList;
import java.util.List;

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
	
	@Autowired
	@Qualifier("storageService")
	private StorageService storageService;

	@RequestMapping(value = "/drawing", method = RequestMethod.GET) //, produces = "application/json")
	public @ResponseBody List<FloorDrawing> getAllDrawings() {
		return storageService.findAllDrawings();
	}
	
	@RequestMapping(value = "/drawing/{dwgId}/area", method = RequestMethod.GET) //, produces = "application/json")
	public @ResponseBody List<DrawingArea> getDrawingAreas(@PathVariable long dwgId) {
		return storageService.findDrawingAreas(dwgId);
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
