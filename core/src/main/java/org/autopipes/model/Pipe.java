package org.autopipes.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.autopipes.model.DrawingLayer.Designation;
import org.autopipes.takeout.Diameter;
import org.autopipes.takeout.Diametrisable;

@XmlRootElement(name="pipe")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pipe implements Cloneable, Diametrisable{
	private static Logger logger = Logger.getLogger(Pipe.class);

	@XmlElement(name = "id")
	private Integer id; // for branches: branch-id, for mains: id of each contiguous chain
	@XmlTransient
	private boolean reversed; // flag defining orientation in the companion graph
	@XmlElement(name = "att")
    private PipeAttachment[] attachments; // attachment to fitting: 0-start, 1-end
	@XmlElement(name = "des")
    private Designation designation; // edge type
	@XmlTransient
    private boolean ignored; // excluded from output by rule
	@XmlElement(name = "vert")
    private boolean vertical; // orientation
	@XmlElement(name = "con")
    private boolean concealed; // additional head attribute
	@XmlTransient
    private String layerName; // layer
	@XmlElement(name = "diam")
    private Diameter diameter; // diameter
	@XmlElement(name = "span")
    private BigDecimal span; // original length rounded to an inch
	@XmlElement(name = "take")
    private BigDecimal takeout; // actual takeout (sum of end-fitting takeouts
	                            // except for chaining
	@XmlElement(name = "after")
    private BigDecimal afterTakeout; // span adjusted for takeout

	public Pipe(){
    	this(null);
    }
    public Pipe(final Designation designation){
    	this.designation = designation;
    	initAttachments();
    }
    private void  initAttachments(){
    	attachments = new PipeAttachment[(designation == Designation.Head) ? 1 : 2];
    	for(int i = 0; i < attachments.length; i++){
    		attachments[i] = new PipeAttachment();
    	}
    }

    @Override
	public String toString(){
    	StringBuilder ret = new StringBuilder();
    	ret.append(layerName);
    	ret.append(':');
    	ret.append(designation);
    	ret.append(':');
    	ret.append(vertical ? "vertical" : "");
    	if(id != null){
    		ret.append('[').append(id).append(']');
    	}
    	return ret.toString();
    }
    
	public static boolean areEqualExact(final Pipe thisPipe, final Pipe thatPipe){
		return areEqual(thisPipe, thatPipe, false);
	}
	public static boolean areEqualRounded(final Pipe thisPipe, final Pipe thatPipe){
		return areEqual(thisPipe, thatPipe, true);
	}
	public static boolean hasMain(Set<Pipe> pp){
	   for(Pipe p : pp){
		   if(p.getDesignation() == Designation.Main){
			   return true;
		   }
	   }
	   return false;
	}
	public static boolean hasNonMain(Set<Pipe> pp){
		   for(Pipe p : pp){
			   if(p.getDesignation() != Designation.Main){
				   return true;
			   }
		   }
		   return false;
	}
	public static Pipe findHeadPipe(List<Set<Pipe>> ps) {
		for(Set<Pipe> pp : ps) {
		   for(Pipe p : pp){
			   if(p.getDesignation() == Designation.Head){
				   return p;
			   }
		   }			
		}
		return null;
	}
	public static Diameter maxDiameter(Set<Pipe> pp){
		Diameter ret = null;
		for(Pipe p : pp){
			Diameter d = p.getDiameter();
			if(ret == null || d != null && ret.compareTo(d) < 0){
				ret = d;
			}
		}
		return ret;
	}
	public static String commonMainName(Set<Pipe> pp){
		String ret = null;
		for(Pipe p : pp){
			if(p.getDesignation() == Designation.Main){
				String d = p.getLayerName();
				if(ret == null){
					ret = d;
				}else if(!ret.equals(d)){
					return null;
				}
			}
		}
		return ret;
	}
	public static String maxMainName(Set<Pipe> pp){
		String ret = null;
		Diameter max = null;
		for(Pipe p : pp){
			if(p.getDesignation() == Designation.Main){
				Diameter d = p.getDiameter();
				String name = p.getLayerName();
				if(max == null || max.compareTo(d) < 0){
					max = d;
					ret = name;
				}else if(max.compareTo(d) == 0){
					if(ret != null && !ret.equals(name)){
						ret = null; // 2 max diameters with different names
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * Used to compare 2 pipes. Cannot overwrite the equals() since these objects are edges in a graph.
	 * The pipes are considered equal if
	 * <ul>
	 * <li>They have the same designation, vertical-orientation and diameter</li>
	 * <li>They have the same spans and takeouts</li>
	 * <li>They either both are not end-attached or are end-attached the same way</li>
	 * Note that start-attachment are not part of the comparison.
	 * Note also that fitting types are not part of this object
	 * @param thisPipe one pipe
	 * @param thatPipe another pipe
	 * @param round if <code>true</code>, pipe-takeouts will be compared rounded
	 * @return <code>true</code> if the two are equal.
	 */
    private static boolean areEqual(final Pipe thisPipe, final Pipe thatPipe, boolean round){
		if(thisPipe == null && thatPipe != null || thisPipe != null && thatPipe == null){
			return false;
		}
		if(thisPipe == null && thatPipe == null){
			return true;
		}
		if(thisPipe.designation != thatPipe.designation
				|| thisPipe.isVertical() != thatPipe.isVertical()
				|| thisPipe.diameter != thatPipe.diameter){
			return false;
		}
		if(thatPipe.designation == Designation.Head){
			return true; // nothing more to compare
		}
		// comparing spans and takeouts
		BigDecimal thisEndTakeout = thisPipe.getEndAttachment() == null ? null : thisPipe.getEndAttachment().getTakeout();
		BigDecimal thatEndTakeout = thatPipe.getEndAttachment() == null ? null : thatPipe.getEndAttachment().getTakeout();
		if(decimalsEqual(thisPipe.span, thatPipe.span, false)
				&& decimalsEqual(thisPipe.takeout, thatPipe.takeout, round)
				&& decimalsEqual(thisEndTakeout, thatEndTakeout, false)){
			return true;
		}
		return false;
    }
    
    private static boolean decimalsEqual(BigDecimal d1, BigDecimal d2, boolean round){
    	if(d1 == null && d2 == null){
    		return true;
    	}
    	if(d1 == null || d2 == null){
    		return false;
    	}
    	if(!round){
    		return d1.compareTo(d2) == 0;
    	}
    	long i1 = Math.round(d1.doubleValue());
    	long i2 = Math.round(d2.doubleValue());
    	return i1 == i2;
    }
	protected int getHashCode(){
		int pipeHash = 7;
		pipeHash = 31*pipeHash + (getSpan()==null ? 0 : getSpan().hashCode());
		pipeHash = 31*pipeHash + (getDiameter()==null ? 0 : getDiameter().hashCode());
		return pipeHash;
	}

/**
 * Switches pipe orientation
 */
    protected void swapEnds(){
    	assert(attachments.length == 2);
    	reversed = !reversed;
    	if(attachments[0] == null && attachments[1] == null){
    		return; // swapping before fittings are attached
    	}
    	PipeAttachment temp = attachments[1];
    	attachments[1] = attachments[0];
    	attachments[0] = temp;
    }
    /**
     * Checks orientation status
     * @return
     */
	protected boolean isReversed() {
		return reversed;
	}
	/**
	 * Changes orientation to reversed - if it is not already such
	 */
	protected void makeReversed() {
		if(!reversed){
			swapEnds();
		}
	}

	/** Clones all members but attachments and reverse are initialized
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Pipe clone(){
		try{
		Pipe ret = (Pipe)super.clone();
		ret.reversed = false;
		ret.initAttachments();
		return ret;
		}catch(CloneNotSupportedException e){
			return null;
		}
	}
	//
	// Getters and setters
	//
	public PipeAttachment getStartAttachment() {
		return attachments[0];
	}
	public void setStartAttachment(final PipeAttachment startAttachment) {
		attachments[0] = startAttachment;
	}
	public PipeAttachment getEndAttachment() {
		return (attachments.length == 1) ? null : attachments[1];
	}
	public void setEndAttachment(final PipeAttachment endAttachment) {
		assert(attachments.length == 2);
		attachments[1] = endAttachment;
	}
	public String getLayerName() {
		return layerName;
	}
	public void setLayerName(final String layerName) {
		this.layerName = layerName;
	}
	public Diameter getDiameter() {
		return diameter;
	}
	public void setDiameter(final Diameter diameter) {
		this.diameter = diameter;
	}
	public boolean isIgnored() {
		return ignored;
	}
	public void setIgnored(final boolean ignored) {
		this.ignored = ignored;
	}
	public boolean isVertical() {
		return vertical;
	}
	public void setVertical(final boolean vertical) {
		this.vertical = vertical;
	}
	public boolean isConcealed() {
		return concealed;
	}
	public void setConcealed(final boolean concealed) {
		this.concealed = concealed;
	}

	public Designation getDesignation() {
		return designation;
	}

	public void setDesignation(final Designation designation) {
		this.designation = designation;
	}
	public Integer getId() {
		return id;
	}
	public void setId(final Integer id) {
		this.id = id;
	}
	public BigDecimal getSpan() {
		return span;
	}
	public void setSpan(final BigDecimal span) {
		this.span = span;
	}
	/*
	public BigDecimal getTakeout() {
		return takeout;
	}
	public void setTakeout(final BigDecimal takeout) {
		this.takeout = takeout;
	}
	*/
    public BigDecimal getAfterTakeout() {
		return afterTakeout;
	}
	public void setAfterTakeout(BigDecimal afterTakeout) {
		this.afterTakeout = afterTakeout;
	}
}
