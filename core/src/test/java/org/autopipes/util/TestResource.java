package org.autopipes.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.FloorDrawing;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class TestResource {
	private FloorDrawing drawing;
	private DrawingArea area;
	private static final String DWG_FOLDER = "dwg";
	
	public static TestResource create(String dwgName, String areaName) throws IOException, JAXBException {
		TestResource ret = new TestResource();
		Resource resCfg = new ClassPathResource("/" + DWG_FOLDER + "/" + dwgName + "/cfg.xml");
		Resource resArea = new ClassPathResource("/" + DWG_FOLDER + "/" + dwgName + "/" + areaName + ".xml");
		String sCfg = resourceAsString(resCfg);
		String sArea = resourceAsString(resArea);
		FloorDrawing oCfg = (FloorDrawing) unmarshal(sCfg, FloorDrawing.class);
		DrawingArea oArea =  (DrawingArea) unmarshal(sArea, DrawingArea.class);
		ret.setArea(oArea);
		ret.setDrawing(oCfg);
		return ret;
	}
	
	public static String resourceAsString(Resource resource){
        String ret = "";
        InputStream is = null; 
        try {
            is = resource.getInputStream();
            ret = IOUtils.toString(is, Charset.defaultCharset().displayName());
        } catch (IOException e) {
        } finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return ret;
    }

	public static Object unmarshal(String src, Class<? extends Object> cls) throws IOException, JAXBException {
			JAXBContext context = JAXBContext.newInstance(cls);
			Unmarshaller u = context.createUnmarshaller();
	  	  Reader r = new StringReader(src);
	  	  Source s = new StreamSource(r);
	  	  return u.unmarshal(s);
		}


	public FloorDrawing getDrawing() {
		return drawing;
	}

	public void setDrawing(FloorDrawing drawing) {
		this.drawing = drawing;
	}

	public DrawingArea getArea() {
		return area;
	}

	public void setArea(DrawingArea area) {
		this.area = area;
	}

}
