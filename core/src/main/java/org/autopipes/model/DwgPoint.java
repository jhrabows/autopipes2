package org.autopipes.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.autopipes.util.PlaneGeo;


@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ctPoint")
/**
 * Support entity class used by Points.
 * A wrapper around an internal array of doubles.
 * For serialization purposes the array is exposed as a pair X, Y of double properties.
 */
public class DwgPoint implements PlaneGeo.Point{

	protected double[] coordinate = new double[2];

	public DwgPoint(){
	}
/**
 * Creates a new object and assigns specified coordinates to its internal array.
 * @param coordinate
 */
	public DwgPoint(final double[] coordinate){
		for(int i = 0; i < coordinate.length; i++){
		    this.coordinate[i] = coordinate[i];
		}
	}

	public double x(final int i){
		return coordinate[i];
	}

	public DwgPoint move(final double[] direction){
		DwgPoint ret = new DwgPoint(coordinate);
		for(int i = 0; i <2; i++){
			ret.getCoordinate()[i] += direction[i];
		}
		return ret;
	}

	@Override
	public String toString(){
		return "(" + coordinate[0] + "," + coordinate[1] + ")";
	}

    @XmlAttribute(required = true)
    public double getX() {
        return coordinate[0];
    }

    public void setX(final double value) {
    	coordinate[0] = value;
    }

    @XmlAttribute(required = true)
    public double getY() {
        return coordinate[1];
    }

    public void setY(final double value) {
    	coordinate[1] = value;
    }

    @XmlTransient
    public double[] getCoordinate(){
    	return coordinate;
    }
/**
 * Replaces the internal array with passed one.
 * @param coordinate
 */
    public void setCoordinate(final double[] coordinate){
    	this.coordinate = coordinate;
    }
}
