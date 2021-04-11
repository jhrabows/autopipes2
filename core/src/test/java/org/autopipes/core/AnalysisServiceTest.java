package org.autopipes.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.DrawingArea.Readiness;
import org.autopipes.model.FloorDrawing;
import org.autopipes.service.AnalyzerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/dispatcher-servlet.xml", "/spring-pipe.xml"})
public class AnalysisServiceTest {
	private static Logger logger = Logger.getLogger(AnalysisServiceTest.class);
	
	@Autowired
	private Unmarshaller unmarshaller;
	
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
	public void testCross() throws XmlMappingException, IOException {
		Resource joeweldcross = new ClassPathResource("/dwg/joeweldcross/cfg.xml");
		Resource simplecross = new ClassPathResource("/dwg/joeweldcross/simplecross.xml");
		assertNotNull(joeweldcross);
		assertNotNull(simplecross);
		String joeweldcrossStr = getResourceAsString(joeweldcross);
		String simplecrossStr = getResourceAsString(simplecross);
		FloorDrawing joeWeldCross = (FloorDrawing) getObjectFromXML(joeweldcrossStr);
		DrawingArea simpleCross =  (DrawingArea) getObjectFromXML(simplecrossStr);
		assertNotNull(joeWeldCross);
		assertNotNull(simpleCross);
		
    	analyzerService.validateArea(joeWeldCross, simpleCross);

	}
	
	@Test
	public void testWeldTee() throws XmlMappingException, IOException {
		Resource test114main = new ClassPathResource("/dwg/test114main/cfg.xml");
		Resource weldtee = new ClassPathResource("/dwg/test114main/weldtee.xml");
		assertNotNull(test114main);
		assertNotNull(weldtee);
		String test114mainStr = getResourceAsString(test114main);
		String weldteeStr = getResourceAsString(weldtee);
		FloorDrawing test114Main = (FloorDrawing) getObjectFromXML(test114mainStr);
		DrawingArea weldTee =  (DrawingArea) getObjectFromXML(weldteeStr);
		assertNotNull(weldTee);
		assertNotNull(test114Main);
		
    	analyzerService.validateArea(test114Main, weldTee);

	}

	@Test
	public void testSideHead() throws XmlMappingException, IOException {
		Resource errmain = new ClassPathResource("/dwg/error/cfg.xml");
		Resource sidehead = new ClassPathResource("/dwg/error/side-head.xml");
		assertNotNull(errmain);
		assertNotNull(sidehead);
		String errmainStr = getResourceAsString(errmain);
		String sideheadStr = getResourceAsString(sidehead);
		FloorDrawing errMain = (FloorDrawing) getObjectFromXML(errmainStr);
		DrawingArea sideHead =  (DrawingArea) getObjectFromXML(sideheadStr);
		assertNotNull(errMain);
		assertNotNull(sideHead);
		
    	analyzerService.validateArea(errMain, sideHead);

	}
	
	@Test
	public void testSideMain() throws XmlMappingException, IOException {
		Resource testflex = new ClassPathResource("/dwg/testflex/cfg.xml");
		Resource oneside = new ClassPathResource("/dwg/testflex/oneside.xml");
		assertNotNull(testflex);
		assertNotNull(oneside);
		String testflexStr = getResourceAsString(testflex);
		String onesideStr = getResourceAsString(oneside);
		FloorDrawing testFlex = (FloorDrawing) getObjectFromXML(testflexStr);
		DrawingArea oneSide =  (DrawingArea) getObjectFromXML(onesideStr);
		assertNotNull(testFlex);
		assertNotNull(oneSide);
		
    	analyzerService.validateArea(testFlex, oneSide);
    	assertTrue(oneSide.getAreaReadiness() == Readiness.Ready);
    	analyzerService.buildCutSheetReport(testFlex, oneSide);
		
	}
	
	@Test
	public void testFlexHead() throws XmlMappingException, IOException {
		Resource errmain = new ClassPathResource("/dwg/error/cfg.xml");
		Resource flexhead = new ClassPathResource("/dwg/error/flex-head.xml");
		assertNotNull(errmain);
		assertNotNull(flexhead);
		String errmainStr = getResourceAsString(errmain);
		String flexheadStr = getResourceAsString(flexhead);
		FloorDrawing errMain = (FloorDrawing) getObjectFromXML(errmainStr);
		DrawingArea flexHead =  (DrawingArea) getObjectFromXML(flexheadStr);
		assertNotNull(errMain);
		assertNotNull(flexHead);
		
    	analyzerService.validateArea(errMain, flexHead);
    	assertTrue(flexHead.getAreaReadiness() == Readiness.Ready);

	}


	
    public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}


	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}


	private String getResourceAsString(Resource resource){
        String ret = "";
        InputStream is = null; 
        try {
            is = resource.getInputStream();
            ret = IOUtils.toString(is, Charset.defaultCharset().displayName());
        } catch (IOException e) {
            logger.error("Cannot read resource", e);
        } finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("Cannot close input stream", e);
                }
            }
        }
        return ret;
    }

	private Object getObjectFromXML(String src) throws XmlMappingException, IOException {
	  	  Reader r = new StringReader(src);
	  	  Source s = new StreamSource(r);
		  assertNotNull(unmarshaller);
	  	  return unmarshaller.unmarshal(s);
		}

}
