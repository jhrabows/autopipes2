package org.autopipes.controller;

import java.util.ArrayList;
import java.util.List;

import org.autopipes.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/json")
public class JsonResponseController {

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
}
