package org.autopipes.model;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.autopipes.takeout.Attachment;
import org.autopipes.takeout.Diameter;
import org.autopipes.takeout.Vendor;


/**
 * Class which contains information that a draftsman associates with
 * a single layer.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@XmlType(name = "ctLayer", propOrder = {
    "headcount"
})
public class DrawingLayer {
	public enum Designation {
		  Output,
		  Main,
		  Branch,
		  Head
	}
	/*
	 *
    public enum Attachment {
		  grooved,
		  threaded
	}
	 */

    @XmlAttribute(name = "main-diameter")
    protected Diameter mainDiameter;

    @XmlAttribute(name = "hole-diameter")
    protected Diameter holeDiameter;

//    @XmlElement(name = "branch-diameter")
    protected TreeMap<Integer, Diameter> headcount;

    @XmlAttribute(required = true)
    protected String name;

    @XmlAttribute(name = "sub-type")
    protected Attachment subType;

    @XmlAttribute(required = true)
    protected DrawingLayer.Designation type;

    @XmlAttribute
    protected Vendor vendor;

    /**
     * Diameter for a branch pipe with specified down-stream head count.
     * The result is a lookup of a sparse map with down-rounding.
     *
     * @param count the head count (>= 1)
     *
     * @return the diameter
     * @throws NoSuchElementException if the lookup fails
     */
    public Diameter getBranchDiameter(final int count){
    	if(count < 1){
    		throw new IllegalArgumentException("Non positive count " + count);
    	}
    	Diameter ret = getBranchDiameter().get(count);
    	if(ret == null){
     		ret = headcount.get(headcount.headMap(count).lastKey());
    	}
    	return ret;
    }

    // Getters and setters

    public Diameter getMainDiameter() {
        return mainDiameter;
    }


    public void setMainDiameter(final Diameter value) {
        mainDiameter = value;
    }
/*
    public Diameter getHoleDiameter() {
        return holeDiameter;
    }

    public void setHoleDiameter(final Diameter value) {
        holeDiameter = value;
    }
*/
    public SortedMap<Integer, Diameter> getBranchDiameter() {
        if (headcount == null) {
        	headcount = new TreeMap<Integer, Diameter>();
        }
        return headcount;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        name = value;
    }

    public Attachment getSubType() {
        return subType;
    }

    public void setSubType(final Attachment value) {
        subType = value;
    }

    public DrawingLayer.Designation getType() {
        return type;
    }

    public void setType(final DrawingLayer.Designation value) {
        type = value;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(final Vendor value) {
        vendor = value;
    }

}
