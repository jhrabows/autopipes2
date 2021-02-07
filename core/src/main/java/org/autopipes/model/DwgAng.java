package org.autopipes.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctAng")
/**
 * Support entity class used by Arcs.
 */
public class DwgAng {
//	@XmlTransient
//	protected Root.Unit unit;

    @XmlAttribute(required = true)
    protected double alpha;
    @XmlAttribute
    protected Double beta;

    /**
     * Gets the value of the alpha property.
     *
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Sets the value of the alpha property.
     *
     */
    public void setAlpha(final double value) {
        alpha = value;
    }

    /**
     * Gets the value of the beta property.
     *
     */
    public Double getBeta() {
        return beta;
    }

    /**
     * Sets the value of the beta property.
     *
     */
    public void setBeta(final Double value) {
        beta = value;
    }

/*	public Root.Unit getUnit() {
		return unit;
	}

	public void setUnit(final Root.Unit unit) {
		this.unit = unit;
	}
*/

}
