package org.autopipes.util;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.Resource;

/**
 * Utility class which allows declaring Spring beans in terms of their serialized representation.
 * @author Tata
 *
 */
public class OxmResourceReader<T extends Object> {
	public T read(final Resource resource, final Class<? extends Object> cls) throws Exception{
		JAXBContext context = JAXBContext.newInstance(cls);
		Unmarshaller u = context.createUnmarshaller();
		InputStream is = resource.getInputStream();
		Source source = new StreamSource(is);
        return (T) u.unmarshal(source);
	}

}
