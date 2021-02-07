package org.autopipes.util;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

/**
 * Utility class which allows declaring Spring beans in terms of their serialized representation.
 * @author Tata
 *
 */
public class OxmResourceReader {
	protected Unmarshaller unmarshaller;
	protected Marshaller marshaller;
	public OxmResourceReader(final Unmarshaller unmarshaller, final Marshaller marshaller){
		this.unmarshaller = unmarshaller;
		this.marshaller = marshaller;
	}
	public Object read(final Resource resource) throws Exception{
		InputStream is = resource.getInputStream();
		Source source = new StreamSource(is);
        return unmarshaller.unmarshal(source);
	}
	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}
	public void setUnmarshaller(final Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}
}
