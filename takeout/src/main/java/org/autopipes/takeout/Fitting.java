package org.autopipes.takeout;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Class representing an actual fitting.
 * It consists of a type and a sequence of up to 4 diameters: A, B, C, D
 * arranged geometrically as
 *    A                  A
 * C (+) D (not ellbow) (L) B     (ellbow)
 *    B
 * In other words, an ellbow is treated as a bent coupling.
 * The diameters are ordered as follows
 * <ul>
 * <li>A >= B</li>
 * <li>C >= D</li>
 * <li>A >= C (in effect only when D is present)</li>
 * </ul>
 * Note that any sequence A, B, C, D can be reordered in this way.
 *
 * @author janh
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "",
		propOrder = { })
@XmlRootElement(name = "fitting")
public class Fitting {
//
// Inner
//
	/**
	 * 4 labels used for end-directions.
	 * Based on compass directions.
	 */
	public enum Direction{
		E("W"),
		W("E"),
		N("S"),
		S("N");

		private Direction opposite;
		private final String oppString;

		Direction(final String opp){
			oppString = opp;
		}
/**
 * Gets a direction which is at 180deg to this direction - in a compass sense.
 * @return the direction
 */
		public Direction getOpposite() {
			if(opposite == null){
				opposite = Direction.valueOf(oppString);
			}
			return opposite;
		}
/**
 * Tests if the argument is equal or at 180deg to this direction.
 * @param thatDirection the direction tested
 * @return <code>true</code> if it is
 */
		public boolean equalOrOpposite(final Direction thatDirection){
			return thatDirection == this || thatDirection == getOpposite();
		}

	}
/**
 * Type is an enumerator which defines a geometry of a fitting.
 * A type consists of a name and a list of 1-4 end-directions labeled
 * as follows:
 * 
 * Cap, Raiser: --E
 * Coupling, Reducer, Ell, Ell45: W--E
 * Tee:    N
 *       W_|_E
 *
 * Cross:  N
 *       W_|_E
 *         |
 *         S
 */
	public enum Type{
		Cap(Angle.deg0, Direction.E),
		Raiser(Angle.deg0, Direction.E),
		Coupling(Angle.deg180, Direction.E, Direction.W),
		Reducer(Angle.deg180, Direction.E, Direction.W),
		Ell45(Angle.deg135, Direction.E, Direction.W),
		Ell(Angle.deg90, Direction.E, Direction.W),
		Tee(Angle.deg90, Direction.E, Direction.N, Direction.W),
		Cross(Angle.deg90, Direction.E, Direction.N, Direction.W, Direction.S);

		protected UndirectedGraph <Direction, DefaultEdge> directionGraph
            = new SimpleGraph<Direction, DefaultEdge>(DefaultEdge.class);

        protected Angle base;
/**
 * Internal Constructor.
 * @param base angle between adjacent directions
 * @param directions list of labeled directions used. 2 consecutive ones are considered
 * adjacent. In the case of a cross, the first and the last are also adjacent.
 */
        Type(final Angle base, final Direction ... directions){
        	this.base = base;
        	Direction last = null;
            for(Direction direction : directions){
            	directionGraph.addVertex(direction);
            	if(last != null){
            		directionGraph.addEdge(last, direction);
            	}
            	last = direction;
            }
            if(directions.length == 4){
            	directionGraph.addEdge(directions[0], directions[3]);
            }
        }
/**
 * Computes an angle between to end-directions in this fitting.
 * @param d1 1st direction
 * @param d2 2nd direction
 * @return the angle
 */
        protected Angle angle(final Direction d1, final Direction d2){
        	if(d1 == d2){
        		return Angle.deg0;
        	}
        	if(getEndCount() <= 2 || directionGraph.getEdge(d1, d2) != null){
        		return base;
        	}
        	return Angle.deg180;
        }
/**
 * Computes directions which are adjacent in this fitting
 * to a specified end-direction.
 * @param d the end-direction specified
 * @return the list of neighbors
 */
        public List<Direction> adjacent(final Direction d){
        	return	Graphs.neighborListOf(directionGraph, d);
        }
        /**
         * Computes a direction in this fitting which is at 180deg angle
         * to a specified end-direction.
         * @param d the end-direction specified
         * @return the antipode direction or <code>null</code> if none exists
         */
        public Direction antipode(final Direction d){
        	for(Direction v : directionGraph.vertexSet()){
        		if(angle(d, v) == Angle.deg180){
        			return v;
        		}
        	}
        	return null;
        }
/**
 * Computes how many end-labels are used by this type.
 * @return the count
 */
		public int getEndCount(){
			return directionGraph.vertexSet().size();
		}
/**
 * Computes a set of labels used by the type.
 * @return the set
 */
		public Set<Direction> getDirections(){
			return directionGraph.vertexSet();
		}
		/**
		 * Checks if directions are equivalent, i.e., an instance with d1
		 * can be moved over an instance with d2 can so that d1 and d2 overlap.
		 * @param d1
		 * @param d2
		 * @return
		 */
		public boolean equivalentDirections(final Direction d1, final Direction d2){
			if(getEndCount() != 3){
				return true;
			}
			// check tee
			return d1 == Direction.N && d2 == Direction.N
				|| d1 != Direction.N && d2 != Direction.N;
		}
		public Angle getBaseAngle() {
			return base;
		}
		
	}
	
	/**
	 * Fitting factory class.
	 * Maintains a list where every Fitting used appears exactly once.
	 * Singleton configured in Spring xml.
	 *
	 */
//	public static class Factory {
//		private final List<Fitting> allFittings = new ArrayList<Fitting>();
//		
//		// injected by Spring
//        private TakeoutRepository takeout;
//		/**
//		 * Produces a fitting matching given type, attachment, vendor, and optionally
//		 * a list of diametrisables in canonical order (see {@link orderDiametrisables# }).
//		 * As a result of this operation, the diameter property of the diametrisables
//		 * may be bumped up to a required minimum.
//		 * @param type the type
//		 * @param attachment type of fitting attachment
//		 * @param vendor the vendor. <code>null</code> indicates theaded fitting.
//		 * @param d the diametrisables or <code>null</code> if not provided
//		 * @return the fitting.
//		 */
//		public synchronized Fitting instanceOf(final Type type, final Attachment attachment, final Vendor vendor,
//				final List<? extends Diametrisable> d, final List<Attachment> attList){
//			int length = d == null ? 0 : d.size();
//			Diameter[] diameters = new Diameter[length];
//			for(int i = 0; i < length; i++){
//			    diameters[i] = d.get(i).getDiameter();
//			}
//			Attachment[] attachments = attList != null ?  attList.toArray(new Attachment[0])
//					: new Attachment[0];
//			Attachment fittingAttachment = attachment;
//			if(length > 0){
//	            assert(!Diameter.checkDiametrisables(d));
//	                // if grooved after all, other than cap, coupling, raiser or reducer,
//	                if(fittingAttachment == Attachment.grooved
//	                		&& (type.getEndCount() > 2 || type.getEndCount() == 2
//	                				&& type.getBaseAngle() != Angle.deg180)){
//	                	// we need to select main pipe which diameter will be used
//	                	// as the fitting diameter. For now we select the biggest.
//	                	Diameter groovedDiameter = null;
//	                	for(int i = 0; i < diameters.length; i++){
//	                		if(groovedDiameter == null || diameters[i].compareTo(groovedDiameter) > 0){
//	                			groovedDiameter = diameters[i];
//	                		}
//	                	}
//	                	Diameter minGroovedOutlet = takeout.getMinGroovedOutlet();
//	                	if(minGroovedOutlet.compareTo(groovedDiameter) > 0){
//	                		minGroovedOutlet = groovedDiameter; // outlet cannot be wider than the pipe
//	                	}
//	                	
//	                	for(int i = 0; i < diameters.length; i++){
//	                		if(diameters[i].compareTo(minGroovedOutlet) < 0){
//	        	                // bump diameters of branch pipes to configurable minimum
//	                			d.get(i).setDiameter(minGroovedOutlet);
//	                		}	                		
//	                		diameters[i] = groovedDiameter; // in grooved all fitting legs are equal
//	                		// regardless of actual pipe diameters
//		                }
//	                }else if(fittingAttachment == Attachment.mechanical
//	                		&& type.getEndCount() == 4
//	                		&& (diameters[2].compareTo(diameters[3]) != 0)){
//	                	// make opposite legs of mechanical cross equal (pipe changes as well)
//	                	diameters[3] = diameters[2];
//	                	d.get(3).setDiameter(diameters[3]);
//	                }
////	            }
//			}
//			Fitting probe = new Fitting(type, fittingAttachment, vendor, diameters, attachments);
//			int idx = allFittings.indexOf(probe);
//			if(idx < 0){
//				allFittings.add(probe);
//				return probe;
//			}
//			return allFittings.get(idx);
//		}
//
//		public void setTakeout(final TakeoutRepository takeout) {
//			this.takeout = takeout;
//		}
//
//	}

//
// Main Fitting Class
//
	@XmlElement(name = "type")
	private final Type type;
	@XmlElement(name = "attachment")
	private final Attachment attachment;
	@XmlElement(name = "vendor")
	private final Vendor vendor;
	@XmlElement(name = "diameter")
	private Diameter[] diameters;
	@XmlElement(name = "attachments")
	private final Attachment[] attachments;

	/**
	 * Checks if all end dimensions are the same.
	 * @return <code>true</code> if they are
	 */
	protected boolean hasEqualEnds(){
		for(int i = 0; i < diameters.length - 1; i++){
			if(!diameters[i].equals(diameters[i + 1])){
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if this fitting introduces a break in a pipe.
	 * There is no break in the case of a mechanical or welded tee/cross.
	 * @return <code>true</code> if it does not
	 */
	public boolean isHoleBased(){
		return (attachment == Attachment.mechanical
				|| attachment == Attachment.welded || attachment == Attachment.weldedGroove);
	}
	public void setHoleBased(boolean holeBased) {
		// read only
	}
	public boolean isCouplingContinuation(){
		return type == Type.Coupling || type == Type.Reducer;
	}
	public void setCouplingContinuation(boolean couplingContinuation){
	// read only property
	}
	/**
	 * Default constructor for Jaxb
	 */
    public Fitting(){
    	this.vendor = null;
    	this.type = null;
    	this.diameters = null;
    	this.attachment = null;
    	this.attachments = null;
    }

	/**
	 * Hidden constructor.
	 * Creates an object with specified type, attachment and vendor.
	 * If diameters are present they are associated with fitting directions.
	 * The index of a diameter on the list corresponds to the natural index of the direction enum.
	 * @param type
	 * @param attachment
	 * @param vendor
	 * @param diameters
	 */
	protected Fitting(final Type type, final Attachment attachment, final Vendor vendor,
			final Diameter[] diameters, final Attachment[] attachments){
		this.type = type;
		this.attachment = attachment;
		this.attachments = attachments;
		this.vendor = vendor;
		this.diameters = diameters;
		if(diameters.length == 0){
			return;
		}
		if(diameters.length < 1 || diameters.length > 4){
			throw new IllegalArgumentException("Invalid number of ends: " + diameters.length);
		}
		if(diameters.length != type.getEndCount()){
			throw new IllegalArgumentException("Invalid end number " + diameters.length + " for " + type);
		}

	}
	@Override
	public boolean equals(final Object that){
		if(!(that instanceof Fitting)){
			return false;
		}
		Fitting thatFitting = (Fitting)that;
		if(thatFitting.type != type
				|| attachment != thatFitting.attachment
				|| vendor != thatFitting.vendor){
			return false;
		}
		for(int i = 0; i < diameters.length; i++){
			if(!diameters[i].equals(thatFitting.diameters[i])){
				return false;
			}
		}
		if(attachments.length != thatFitting.attachments.length){
			return false;
		}
		for(int i = 0; i < attachments.length; i++){
			Attachment a = attachments[i];
			Attachment ta = thatFitting.attachments[i];
			if(a == null){
				if(ta != null){
					return false;
				}
			}else{
				if(!a.equals(ta)){
					return false;
				}
			}
		}
		return true;
	}

    /**
     * Finds a diameter at a specified direction-label.
     * @param direction the label
     * @return the diameter
     */
    public Diameter diameter(final Direction direction){
		return diameters[positionOfDirection(direction)];
    }
    
    private int positionOfDirection(final Direction direction){
		int position = direction.ordinal();
		if(position < 0 || position >= diameters.length){
			throw new IllegalArgumentException("Invalid direction "
					+ direction + " for " + type);
		}
		return position;
    }
    
 //   public Diameter getGroovedDiameter(){
 //   	return diameter(Direction.E);
 //   }

    /**
     * Finds maximum of dimensions adjacent to a direction
     * @param direction the direction
     * @return the maximum
     */
	public Diameter getMaxAdjacentDiameter(final Direction direction){
		int position = positionOfDirection(direction);
		int orthoIdx;
		if(diameters.length == 2){
			orthoIdx = 1 - position;
		}else{ // Tee or Cross
			orthoIdx = (position < 2) ? 2 : 0;
		}
		return diameters[orthoIdx];
	}

	List<Direction> adjacentDirectionsMaxDiam(final Direction direction){
		List<Direction> adjacentList = type.adjacent(direction);
		if(adjacentList.size() <= 1){
			return adjacentList;
		}
		int comp = diameter(adjacentList.get(1)).compareTo(diameter(adjacentList.get(0)));
        if(comp == 0){
        	return adjacentList;
        }
        int idx = comp > 0 ? 1 : 0;
        return adjacentList.subList(idx, idx + 1);
	}

/**
 * Returns fitting diameters as a list. If all diameters are equal
 * the list will be abbreviated to contain only one element.
 * @return the list
 */
	public List<Diameter> getDiameterList() {
		List<Diameter> ret = Arrays.asList(diameters);
		return hasEqualEnds() ? ret.subList(0, 1) : ret;
	}
	public void setDiameterList(List<Diameter> list) {
		// to set from JSON (this may not correspond to the number of ends in fitting)
		if(list != null ) {
			diameters = new Diameter[list.size()];
	        list.toArray(diameters);
		}
	}

	@Override
	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(type).append('[').append(attachment).append("]:");
		for(Diameter d : diameters){
			ret.append(' ').append(d);
		}
		if(attachments != null && attachments.length > 0){
			ret.append(" {");
			for(Attachment a : attachments){
				if(a != null){
					ret.append(' ').append(a);
				}
			}
			ret.append("}");
		}
		
		return ret.toString();
	}

	public Attachment getAttachment() {
		return attachment;
	}
    public Attachment attachment(final Direction direction){
    	if(attachments.length > 0){
    		return attachments[positionOfDirection(direction)];
    	}
    	return null;
    }


	public Vendor getVendor() {
		return vendor;
	}

	public Type getType() {
		return type;
	}

}
