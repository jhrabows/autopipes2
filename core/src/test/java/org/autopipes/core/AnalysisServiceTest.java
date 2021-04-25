package org.autopipes.core;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.autopipes.model.DrawingArea.Readiness;
import org.autopipes.service.AnalyzerService;
import org.autopipes.util.TestResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/dispatcher-servlet.xml", "/spring-pipe.xml"})
public class AnalysisServiceTest {
	private static Logger logger = Logger.getLogger(AnalysisServiceTest.class);
	
//	@Autowired
//	private Unmarshaller unmarshaller;
	
	@Autowired
	@Qualifier("analyzerService")
	private AnalyzerService analyzerService;
	
	public AnalyzerService getAnalyzerService() {
		return analyzerService;
	}


	public void setAnalyzerService(AnalyzerService analyzerService) {
		this.analyzerService = analyzerService;
	}


	@Test
	public void testCross() throws  IOException, JAXBException {
//		Resource joeweldcross = new ClassPathResource("/dwg/joeweldcross/cfg.xml");
//		Resource simplecross = new ClassPathResource("/dwg/joeweldcross/simplecross.xml");
//		assertNotNull(joeweldcross);
//		assertNotNull(simplecross);
//		String joeweldcrossStr = getResourceAsString(joeweldcross);
//		String simplecrossStr = getResourceAsString(simplecross);
//		FloorDrawing joeWeldCross = (FloorDrawing) getObjectFromXML(joeweldcrossStr);
//		DrawingArea simpleCross =  (DrawingArea) getObjectFromXML(simplecrossStr);
//		assertNotNull(joeWeldCross);
//		assertNotNull(simpleCross);
//    	analyzerService.validateArea(joeWeldCross, simpleCross);
    	
		TestResource tr = TestResource.create("joeweldcross", "simplecross");
    	analyzerService.validateArea(tr.getDrawing(), tr.getArea());

	}
	
	@Test
	public void testWeldTee() throws  IOException, JAXBException {
//		Resource test114main = new ClassPathResource("/dwg/test114main/cfg.xml");
//		Resource weldtee = new ClassPathResource("/dwg/test114main/weldtee.xml");
//		assertNotNull(test114main);
//		assertNotNull(weldtee);
//		String test114mainStr = getResourceAsString(test114main);
//		String weldteeStr = getResourceAsString(weldtee);
//		FloorDrawing test114Main = (FloorDrawing) getObjectFromXML(test114mainStr);
//		DrawingArea weldTee =  (DrawingArea) getObjectFromXML(weldteeStr);
//		assertNotNull(weldTee);
//		assertNotNull(test114Main);
		
		TestResource tr = TestResource.create("test114main", "weldtee");
    	analyzerService.validateArea(tr.getDrawing(), tr.getArea());

	}

	@Test
	public void testSideHead() throws  IOException, JAXBException {
//		Resource errmain = new ClassPathResource("/dwg/error/cfg.xml");
//		Resource sidehead = new ClassPathResource("/dwg/error/side-head.xml");
//		assertNotNull(errmain);
//		assertNotNull(sidehead);
//		String errmainStr = getResourceAsString(errmain);
//		String sideheadStr = getResourceAsString(sidehead);
//		FloorDrawing errMain = (FloorDrawing) getObjectFromXML(errmainStr);
//		DrawingArea sideHead =  (DrawingArea) getObjectFromXML(sideheadStr);
//		assertNotNull(errMain);
//		assertNotNull(sideHead);
		
		TestResource tr = TestResource.create("error", "side-head");
    	analyzerService.validateArea(tr.getDrawing(), tr.getArea());

	}
	
	@Test
	public void testSideMain() throws IOException, JAXBException {
//		Resource oneside = new ClassPathResource("/dwg/testflex/oneside.xml");
		testOneOnMain("oneside");
	}
	@Test
	public void testFlexSideMain() throws  IOException, JAXBException {
//		Resource oneside = new ClassPathResource("/dwg/testflex/onesideflex.xml");
		testOneOnMain("onesideflex");
	}
	@Test
	public void testFlexUpMain() throws  IOException, JAXBException {
//		Resource oneside = new ClassPathResource("/dwg/testflex/oneflexup.xml");
		testOneOnMain("oneflexup");
	}
	@Test
	public void testFlexPendentMain() throws IOException, JAXBException {
//		Resource oneside = new ClassPathResource("/dwg/testflex/oneflexpendent.xml");
		testOneOnMain("oneflexpendent");
	}

	private void testOneOnMain(String areaName) throws IOException, JAXBException {
//		Resource testflex = new ClassPathResource("/dwg/testflex/cfg.xml");
//		assertNotNull(testflex);
//		assertNotNull(oneside);
//		String testflexStr = getResourceAsString(testflex);
//		String onesideStr = getResourceAsString(oneside);
//		FloorDrawing testFlex = (FloorDrawing) getObjectFromXML(testflexStr);
//		DrawingArea oneSide =  (DrawingArea) getObjectFromXML(onesideStr);
//		assertNotNull(testFlex);
//		assertNotNull(oneSide);
		TestResource tr = TestResource.create("testflex", areaName);
		
    	analyzerService.validateArea(tr.getDrawing(), tr.getArea());
    	assertTrue(tr.getArea().getAreaReadiness() == Readiness.Ready);
    	analyzerService.buildCutSheetReport(tr.getDrawing(), tr.getArea());
		
	}
	
	@Test
	public void testFlexHead() throws  IOException, JAXBException {
//		Resource errmain = new ClassPathResource("/dwg/error/cfg.xml");
//		Resource flexhead = new ClassPathResource("/dwg/error/flex-head.xml");
//		assertNotNull(errmain);
//		assertNotNull(flexhead);
//		String errmainStr = getResourceAsString(errmain);
//		String flexheadStr = getResourceAsString(flexhead);
//		FloorDrawing errMain = (FloorDrawing) getObjectFromXML(errmainStr);
//		DrawingArea flexHead =  (DrawingArea) getObjectFromXML(flexheadStr);
//		assertNotNull(errMain);
//		assertNotNull(flexHead);
		
//    	analyzerService.validateArea(errMain, flexHead);
		TestResource tr = TestResource.create("error", "flex-head");
    	analyzerService.validateArea(tr.getDrawing(), tr.getArea());
    	assertTrue(tr.getArea().getAreaReadiness() == Readiness.Ready);

	}



}
