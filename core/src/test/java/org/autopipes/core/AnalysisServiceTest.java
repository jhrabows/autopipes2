package org.autopipes.core;

import static org.junit.Assert.assertNotNull;

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
