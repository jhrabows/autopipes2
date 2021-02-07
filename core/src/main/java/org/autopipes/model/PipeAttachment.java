package org.autopipes.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.autopipes.takeout.Fitting.Direction;

@XmlAccessorType(XmlAccessType.FIELD)
public class PipeAttachment {
	@XmlElement(name = "dir")
	private Direction directionInFitting;
	@XmlTransient
	private BigDecimal takeout;

	@Override
	public boolean equals(final Object that){
		if(that == null || !(that instanceof PipeAttachment)){
			return false;
		}
		PipeAttachment thatAttachment = (PipeAttachment)that;
		return directionInFitting == thatAttachment.directionInFitting
			&& equalsOnTakeout(thatAttachment);
		/*
		if(takeout == null && thatAttachment.takeout != null
				|| takeout != null && thatAttachment.takeout == null){
			return false;
		}
		if(directionInFitting == thatAttachment.directionInFitting
				&& ((takeout == null && thatAttachment.takeout == null) || (takeout != null && takeout.compareTo(thatAttachment.takeout) == 0))){
			return true;
		}
		return false;
		*/
	}
	public boolean equalsOnTakeout(PipeAttachment thatAttachment){
		if(thatAttachment == null){
			return false;
		}
		if(takeout == null && thatAttachment.takeout != null
				|| takeout != null && thatAttachment.takeout == null){
			return false;
		}
		if(takeout == null && thatAttachment.takeout == null){
			return true;
		}
		return takeout.compareTo(thatAttachment.takeout) == 0;
	}

	@Override
	public String toString(){
		return directionInFitting.toString() + ":" + takeout;
	}

	public BigDecimal getTakeout() {
		return takeout;
	}
	public void setTakeout(final BigDecimal takeout) {
		this.takeout = takeout;
	}
	public Direction getDirectionInFitting() {
		return directionInFitting;
	}
	public void setDirectionInFitting(final Direction directionInFitting) {
		this.directionInFitting = directionInFitting;
	}

}
