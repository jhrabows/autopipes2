package org.autopipes.util;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.oxm.Marshaller;

/**
 * A variant of bean parameter source which supports object-valued properties.
 * All readable properties are scanned.
 * A property of JDBC-simple type is added to the parameter map.
 * Enums are handled using their String representations.
 * A complex property is Jaxb-marshaled and added as a String.
 * Collection-properties are ignored.
 *
 * @author janh
 *
 */
public class JaxbSqlParameterSource implements SqlParameterSource {
	private final MapSqlParameterSource ms;
	public JaxbSqlParameterSource(final Object bean, final Marshaller marshaller)throws Exception {
		ms = new MapSqlParameterSource();
		BeanPropertySqlParameterSource bs = new BeanPropertySqlParameterSource(bean);
		for(String paramName : bs.getReadablePropertyNames()){
			if("class".equals(paramName)){
				continue; // ignore class property
			}
			Object paramValue = bs.getValue(paramName);
			//int paramType = bs.getSqlType(paramName);
			String pkg = paramValue == null ? null : paramValue.getClass().getPackage().getName();
			if(pkg == null || pkg.startsWith("java.lang")
					|| paramValue instanceof BigDecimal
					|| paramValue instanceof Calendar){
				// pass-through primitive types
				ms.addValue(paramName, paramValue);
			}else if(paramValue instanceof Enum){
				ms.addValue(paramName, paramValue.toString());
			}else if(paramValue instanceof Map
					|| paramValue instanceof List){
				continue;
			}else{
			    //pickle everything else (presumably complex type)
				StringWriter writer = new StringWriter();
				Result res = new StreamResult(writer);
				marshaller.marshal(paramValue, res);
				ms.addValue(paramName, writer.toString()/*, Types.CLOB*/);
			}
		}
	}

//	@Override
	public int getSqlType(final String paramName) {
		return ms.getSqlType(paramName);
	}

//	@Override
	public Object getValue(final String paramName) throws IllegalArgumentException {
		return ms.getValue(paramName);
	}

//	@Override
	public boolean hasValue(final String paramName) {
		return ms.hasValue(paramName);
	}

}
