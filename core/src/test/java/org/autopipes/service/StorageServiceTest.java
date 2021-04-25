package org.autopipes.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.autopipes.model.DrawingArea;
import org.autopipes.model.DrawingOptions;
import org.autopipes.model.FloorDrawing;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/dispatcher-servlet.xml", "/spring-pipe.xml"})
public class StorageServiceTest {
	private static final String UNIT_TEST_DWG_NAME = "_JUNIT_";
	private static final String UNIT_TEST_BR_PREFIX = "B";
	@Autowired
	private JdbcStorageService storageService;
	
	@Test
	public void drawingTest() throws Exception {
		assertNotNull(storageService);
		FloorDrawing fd = new FloorDrawing();
		fd.setDwgName(UNIT_TEST_DWG_NAME);
		fd.setOptionsRoot(testOptions(UNIT_TEST_BR_PREFIX));
		fd.setDwgTextSize(5.0);
		fd.setDwgUpdateDate(new GregorianCalendar());
		storageService.mergeDrawing(fd);
		FloorDrawing fdFound = findByName(UNIT_TEST_DWG_NAME);
		assertNotNull(fdFound);
		FloorDrawing fdFoundById = storageService.findOneDrawing(fdFound.getId());
		DrawingOptions options = fdFoundById.getOptionsRoot();
		assertNotNull(options);
		assertEquals(UNIT_TEST_BR_PREFIX, options.getBranchPrefix());
		storageService.deleteDrawing(fdFound.getId());
		fdFound = findByName(UNIT_TEST_DWG_NAME);
		assertNull(fdFound);
	}

	@Test
	public void findAreasTest() {
		assertNotNull(storageService);
		long dwgId = -1L;
		List<DrawingArea> result = storageService.findDrawingAreas(dwgId);
		assertEquals(0, result.size());
	}
	
	private FloorDrawing findByName(String name) {
		List<FloorDrawing> results = storageService.findAllDrawings();
		Long unitDwgId = null;
		for(FloorDrawing result : results) {
			if(result.getDwgName().equals(name)) {
				return result;
			}
		}
		return null;
	}
	
	private static DrawingOptions testOptions(String brPrefix) {
		DrawingOptions options = new DrawingOptions();
		options.setBranchPrefix(brPrefix);
		return options;
	}

}
