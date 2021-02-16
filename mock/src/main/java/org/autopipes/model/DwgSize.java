package org.autopipes.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctSize")
/**
 * Support entity class used by Text labels.
 */
public class DwgSize {

    @XmlAttribute(required = true)
    protected double h;
    @XmlAttribute
    protected Double w;

    public DwgSize(){
    }

    public DwgSize(final double h, final double w){
    	this.h = h;
    	this.w = w;
    }

    public double getH() {
        return h;
    }

    public void setH(final double value) {
        h = value;
    }

    public Double getW() {
        return w;
    }

    public void setW(final Double value) {
        w = value;
    }

}
