package org.autopipes.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

public class Utils {
	
	public static Object unmarshal(String src, Class<? extends Object> cls) throws IOException, JAXBException {
		JAXBContext context = JAXBContext.newInstance(cls);
		Unmarshaller u = context.createUnmarshaller();
  	  Reader r = new StringReader(src);
  	  Source s = new StreamSource(r);
  	  return u.unmarshal(s);
	}
	
	public static String marshal(Object obj) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(obj.getClass());
		Marshaller m = context.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

	    StringWriter sw = new StringWriter();
	    m.marshal(obj, sw);
	    return sw.toString();
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


}
