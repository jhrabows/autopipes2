package org.autopipes.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ConversionUtils {
	private static Logger logger = Logger.getLogger(ConversionUtils.class);
	
	private static Pattern footInchPat = Pattern.compile("([-\\+]?)(?:(\\d+)')?(?:(\\d+)\")?");
	
	/**
	 * Converts architectural length-string to a numeric inch value
	 * @param s string of the format fff'iii"
	 * @return the inch value or <code>null</code> for badly formatted
	 */
	public static Long string2inches(String s){
		Long ret = null;
		
		Matcher m = footInchPat.matcher(s);
		if(m.matches()){
			String sign =  m.group(1);
			String feet = m.group(2);
			String inches = m.group(3);
			long iRet = 0;
			if(feet != null){
				iRet = Long.parseLong(feet)*12;
			}
			if(inches != null){
				iRet += Long.parseLong(inches);
			}
			if(sign != null && sign.equals("-")){
				iRet *= -1;
			}
			ret = iRet;
		}
		return ret;
	}
	/**
	 * Converts architectural length-string sequence separated by spaces into a list of numeric inch values.
	 * @param value the sequence
	 * @return the list or <code>null</code> if the sequence could not be parsed.
	 */
    public static List<Long> tokenizeFootInchSequence(String value){
    	List<Long> ret = null;
    	if(value != null){
    		String[] values = value.split("\\s");
    		ret = new ArrayList<Long>();
    		for(String v : values){
    			Long item = string2inches(v);
    			if(item == null){
    				logger.error("invalid length format:" + value);
    				return null;
    			}
    			ret.add(item);
    		}
    	}
    	return ret;
    }


}
