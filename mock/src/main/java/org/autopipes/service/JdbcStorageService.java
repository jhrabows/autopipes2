package org.autopipes.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.FloorDrawing;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;

// Mock version
public class JdbcStorageService  implements StorageService  {
	  private static Logger logger = Logger.getLogger(JdbcStorageService.class);
 
      private Marshaller marshaller;
      private Unmarshaller unmarshaller;
	
      @Value("classpath:/rest/findAllDrawings.json")
      private Resource allDrawings;
      
      @Value("classpath:/rest/findAllAreas.json")
      private Resource allDrawingAreas;
      
      @Value("classpath:/rest/test2joe/findDrawingAreas.json")
      private Resource test2joeAreas;
      @Value("classpath:/rest/test2joe/area1/findOneDrawingArea.json")
      private Resource test2joeArea1;

	@Override
	public List<FloorDrawing> findAllDrawings(){
		List<FloorDrawing> ret = new ArrayList<>();
		String json = getResourceAsString(allDrawings);
		try {	
			ret = (List<FloorDrawing>) getObjectFromJSONList(json, FloorDrawing.class);
			logger.info("Found " + ret.size() + " drawings");
		} catch (IOException e) {
			logger.error("Cannot find drawings", e);
		}
		return ret;
	}
	
	@Override
	public List<DrawingArea> findAllDrawingAreas() {
		List<DrawingArea> ret = new ArrayList<>();
		String json = getResourceAsString(allDrawingAreas);
		try {	
			ret = (List<DrawingArea>) getObjectFromJSONList(json, DrawingArea.class);
			logger.info("Found " + ret.size() + " areas");
		} catch (IOException e) {
			logger.error("Cannot find areas", e);
		}
		return ret;
	}

	@Override
	public List<DrawingArea> findDrawingAreas(Long dwgId) {
		List<DrawingArea> ret = new ArrayList<>();
		String json = getResourceAsString(test2joeAreas);
		try {	
			ret = (List<DrawingArea>) getObjectFromJSONList(json, DrawingArea.class);
			logger.info("Found " + ret.size() + " areas");
		} catch (IOException e) {
			logger.error("Cannot find areas for dwgId:" + dwgId, e);
		}
		return ret;
	}

	@Override
	public DrawingArea findOneDrawingArea(Long dwgId, Long areaId, boolean withBody, boolean withCutSheet) {
		DrawingArea ret = new DrawingArea();
		String json = getResourceAsString(test2joeArea1);
		try {	
			ret = (DrawingArea) getObjectFromJSON(json, DrawingArea.class);
			logger.info("Found " + (ret==null?"Null":"") + "area");
		} catch (IOException e) {
			logger.error("Cannot find area for dwgId:" + dwgId + ", id:" + areaId, e); 
		}
		return ret;
	}

	@Override
	public FloorDrawing findOneDrawing(Long id) {
		FloorDrawing ret = new FloorDrawing();
		ret.setId(id);
		return ret ;
	}

	@Override
	public Long findDrawingId(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mergeDrawing(FloorDrawing dwg) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteDrawing(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DrawingArea mergeArea(DrawingArea area) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Long findAreaId(Long drawingId, String areaName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteArea(Long drawingId, Long areaId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long maxAreaId(Long dwgId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Object getObjectFromXML(String src) throws XmlMappingException, IOException {
  	  Reader r = new StringReader(src);
  	  Source s = new StreamSource(r);
  	  return unmarshaller.unmarshal(s);
	}
	
	public Object getObjectFromJSONList(String src, Class<?> elementClass) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JavaType type = mapper.getTypeFactory().
				  constructCollectionType(List.class, elementClass);
		return mapper.readValue(src, type);
	}
	public Object getObjectFromJSON(String src, Class<?> objectClass) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(src, objectClass);
	}
	
    public String getResourceAsString(Resource resource){
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

	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	public Marshaller getMarshaller() {
		return marshaller;
	}


	public void setMarshaller(final Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	public void setDataSource(final DataSource dataSource) {}
	public DataSource getDataSource() {return null;}

	public void setSchema(final Resource resource) {}
	public void setSchemaSeparator(final String separator) {}
	public void init() {}
}
