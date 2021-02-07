package org.autopipes.model;

import org.autopipes.takeout.Fitting;
import org.autopipes.util.PlaneGeo.Point;

/**
 * Class that represents a node in a pipe graph.
 * @author janh
 *
 */
public class PipeFitting implements Point, Cloneable{
	
	public enum Jump {
		NONE,
		BOTTOM,
		MIDDLE,
		TOP
	}

	public static final PipeFitting SINK = new PipeFitting();

	private Fitting fitting;
	private int headCount;
	private Jump jump;
	private final DwgPoint center;

	public double x(final int i){
		return center.x(i);
	}
	/**
	 * Default constructor (required by the edge iterator)
	 */
	public PipeFitting(){
		this(null);
	}

	/**
	 * Actual constructor.
	 * @param center geometric location of the node.
	 * A graph may contain multiple nodes with the same center.
	 */
	public PipeFitting(final DwgPoint center){
		this.center = center;
		this.jump = Jump.NONE;
	}

	@Override
	public PipeFitting clone(){
		try{
		return (PipeFitting)super.clone();
		}catch(CloneNotSupportedException e){
			return null;
		}
	}

	@Override
	public String toString(){
		StringBuilder ret = new StringBuilder();

		if( this == SINK ){
			ret.append("Sink");
		}else{
			ret.append('[');
			if(fitting != null){
				ret.append(fitting.getType());
			}
			ret.append(']');
			if(center != null){
				ret.append(center.toString());
			}
		}
		return ret.toString();
	}


	/**
	 * Alters end ordering in a non-coupling
	 * to guarantee that the direction from the first end
	 * to the third (second for ell) is counterclockwise.
	 */
	/*
    public void orderEnds(){
    	if(getShape() == Fitting.Type.Coupling){
    		return; // couplings ignored
    	}
    	DwgEntity e1 = getEnds()[0];
		if(e1.getEntEnd() == null){
		    return;
		}
   	    DwgEntity e2;
    	if(getShape() == Fitting.Type.Ell){
    		e2 = getEnds()[1];
    	}else{ // tee or cross
    		e2 = getEnds()[2];
    	}
		if(e2.getEntEnd() == null){
		    return;
		}
		if(DwgEntity.crossProduct(e1, e2) > 0){
			return;
		}
		// swap
		getEnds()[0] = getEnds()[1];
		getEnds()[1] = e1;
    }
    */


    public DwgPoint getCenter(){
        return center;
    }


	public Fitting getFitting() {
		return fitting;
	}


	public void setFitting(final Fitting fitting) {
		this.fitting = fitting;
	}

	public int getHeadCount() {
		return headCount;
	}

	public void setHeadCount(final int headCount) {
		this.headCount = headCount;
	}
	public Boolean getHoleBased() {
		return fitting.isHoleBased();
	}
	public Boolean getCouplingContinuation(){
		return fitting.isCouplingContinuation();
	}
	public Jump getJump() {
		return jump;
	}
	public void setJump(Jump jump) {
		this.jump = jump;
	}

}
