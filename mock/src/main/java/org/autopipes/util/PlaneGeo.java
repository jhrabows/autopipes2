package org.autopipes.util;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Computation library ala Math which deals with 2-dimensional vectors.
 * @author jhrabowski
 *
 */
public class PlaneGeo {
	private static Logger logger = Logger.getLogger(PlaneGeo.class);
	
// unit vector for the x-axis
	public static double[] E1 = { 1.0, 0.0};

//
// Inner
//
	public interface Point{
		double x(int axis);
	}
	/**
	 * Algorithm to cut long segments.
	 * @author janh
	 *
	 */
	public static class Divider{
	    private enum BuildCutStatus{
	    	wrongCutSize,
	    	notDone,
	    	done;
	    }		
		
	    // max length that can remain un-cut (main and branch)
		private long maxSize;	
		// proposed length of a cut in decreasing order of preference
		private long[] cutSizes;
		// single cut size for branches
		private long threadedCutSize;
		// the closest that a cut-point can appear to an existing vertex
		private long avoidMargin;
		// the vector library
		private final PlaneGeo geo;
		// the result of the Divider calculation.
		// the keys are sub-segments into which the list of avoid-vertices
		// subdivides the segment to be cut (in segment order)
		// the value is a list of cut points inserted into the sub-segment
		// in sub-segment order.
		private Map<Point[], List<Point>> cutMap;
		private List<Point> cutList;
		
		public Divider(PlaneGeo geo){
			this.geo = geo;
		}
		
	    public void subdivideThreaded(final Point start, final Point end){
	    	subdivideLastThreaded(start, end, 0, 0);
	    }
	    /**
	     * @deprecated
	     * @param start
	     * @param end
	     * @param takeout
	     * @param cutTakeout
	     */
	    @Deprecated
		public void subdivideLastThreaded(final Point start, final Point end,
	    		double takeout, double cutTakeout){
	    	getCutList().clear();
	    	// create vector for the cut-segments
	    	double[] d = difference(end, start);
	    	double dist = norm(d);
	    	streachInPlace(d, threadedCutSize/dist);
	    	long fullLength = Math.round(dist);
	    	for(int i = 0; lengthLeft(fullLength, takeout, cutTakeout, i) > maxSize; i++){
	    		double[] c = makeStreach(d, i + 1);
	    		for(int j = 0; j < 2; j++){
	    			c[j] += start.x(j);
	    		}
	    		Point p = geo.createPoint(c);
	    		cutList.add(p);
	    	}
	    }
	    private long lengthLeft(long fullLength, double takeout, double cutTakeout, int index){
	    	return fullLength - index*threadedCutSize
	    		- Math.round(takeout + index*cutTakeout);
	    }
	    /**
	     * Inserts cut-points between start/end points so that all
	     * resulting segments are shorter than the max property.
	     * The cut-locations should be farther than the margin property from passed avoid-locations
	     * and the end. The length of the cut-segments (except the last)
	     * are taken from the cutSizes property. First item from the list
	     * which does not violate the margin should be used.
	     * The length of the cut-segment is the cutSize with takeouts added on both sides.
	     * @param start start point of the segment to cut
	     * @param end end point of the segment to cut
	     * @param startTakeout start takeout to be used with the first cut-segmen
	     * @param endTakeout end takeout to be used with the last cut-segmen
	     * @param cutTakeout start/end takeout used except in 2 cases above
	     * @param avoidLocations locations to avoid
	     * @return <code>true</code> when the algorithm succeeds
	     */
	    public boolean subdivide(final Point start, final Point end,
	    		double startTakeout,
	    		double endTakeout,
	    		double cutTakeout,
	    		final List<? extends Point> avoidLocations,
	    		final List<Long> cutSizes){
	    // create unit vector for the segment we divide
	    	double[] uv = difference(end, start);
	    	double dist = norm(uv);
	    	streachInPlace(uv, 1.0/dist);
	    // clear cut Map
	    	getCutMap().clear();
	    // incremental build
	    	BuildCutStatus status = BuildCutStatus.notDone;
	    	while(status == BuildCutStatus.notDone){
	    		// try all cut-sizes in turn
	    		for(long cutSize : cutSizes){
	    			status = buildCutMap(uv, start, end, startTakeout, endTakeout, cutTakeout,
	    					cutSize, avoidLocations);
	    			if(status != BuildCutStatus.wrongCutSize){
	    				break;
	    			}
	    		}
	    	}
	    	logger.debug("-subdivide->" + status);
	    	return status == BuildCutStatus.done;
	    }
	    
	    /**
	     * Iterative step in subdivide() call
	     * @param unitVector
	     * @param start
	     * @param end
	     * @param startTakeout
	     * @param endTakeout
	     * @param cutSize
	     * @param avoidLocations
	     * @return
	     */
	    private BuildCutStatus buildCutMap(double[] unitVector,
	    		final Point start, final Point end,
	    		double startTakeout,
	    		double endTakeout,
	    		double cutTakeout,
	    		long cutSize,
	    		final List<? extends Point> avoidLocations){
	    // first we figure out where we are in the iteration
	    	Point basePoint = null; // last pipe break
	    	Point[] baseSeg = null; // last cut segment
	    	int startAvoidIndex = 0; // index to next avoid on the list
	 //   	int startCutCount = 0;
	    	if(cutMap.isEmpty()){
	    		// initial call
	    		basePoint = start;
	    	}else{
	    		// find last avoid on the cut-map
	    		for(Point[] seg : cutMap.keySet()){
	    			baseSeg = seg;
	   // 			startCutCount += cutMap.get(seg).size();
	    		}
	    		// calc avoid indices
	    		if(baseSeg[0] != start){
	    			startAvoidIndex = avoidLocations.indexOf(baseSeg[0]) + 1;
	    		}
	    		// find the last cut
	    		List<Point> cutList = cutMap.get(baseSeg);
	    		basePoint = cutList.get(cutList.size() - 1);
	    	}
	    	double adjustedSpan = distance(basePoint, end) - endTakeout
    			- (basePoint == start ? startTakeout : cutTakeout);
	    	if(adjustedSpan - geo.getLinearTolerance() <= maxSize){
	    		// nothing more to do
	    		return BuildCutStatus.done;
	    	}
	    	
	    	// this loop does:
	    	// 1. calculate effectiveSpan: distance from last cut to the end of segment to-be-divided
	    	// 2. finds if the distance from any avoids (or end) to the
	    	//    proposed cut is less than the allowed margin
	    	// 3. finds the last avoid (or base) strictly before the proposed cut
	    //	long effectiveSpan = 0;// (basePoint == start) ? -startTakeout : 0;
//	    	Point beforeCutAvoid = basePoint;
	    	int beforeCutAvoidIdx = -1;
	//    	Point prevAvoid = basePoint; // used in this loop only
	//    	boolean isClose = false;
    		double proposedCutLocation = cutSize + cutTakeout
				+ (basePoint == start ? startTakeout : cutTakeout);
	    	for(int i = startAvoidIndex; i <= avoidLocations.size(); i++){
	    		Point avoid = (i < avoidLocations.size())
	    			? avoidLocations.get(i) : end;
	    	//	long ld = Math.round(distance(prevAvoid, avoid));
	    	//	effectiveSpan += ld;
	    		double avoidLocation = distance(basePoint, avoid);
	    		// comparing proposed cutSize with the total length from the
	    		// physical start of the pipe to the center of this avoid.
	    		if(Math.abs(avoidLocation - proposedCutLocation) < avoidMargin){
		    		return BuildCutStatus.wrongCutSize;
		    		// cut needed but too close to one of the end points,
		    		// return to continue with an alternative cut size
	    		}
	    		if(avoidLocation < proposedCutLocation){
	    			//beforeCutAvoid = avoid;
	    			beforeCutAvoidIdx = i;
	    		}
	    //		effectiveSpan -= (avoid == end ? endTakeout : 0);
	    //		prevAvoid = avoid;
	    	}
	    	// cut is needed, we will add a single cut
	    	// and return to continue iteration
	    	
	    	// check if a new sub-segment should be created
	    	if(baseSeg == null || beforeCutAvoidIdx != -1){
	    		// need to create new cut segment
		    	Point beforeCutAvoid = basePoint;
		    	if(beforeCutAvoidIdx >= 0){
		    		beforeCutAvoid =
		    			(beforeCutAvoidIdx < avoidLocations.size()) ?
		    					avoidLocations.get(beforeCutAvoidIdx)
		    					: end;
		    	}
	    		Point afterCutAvoid =
	    			((beforeCutAvoidIdx + 1) < avoidLocations.size()) ?
	    					avoidLocations.get(beforeCutAvoidIdx + 1)
	    					: end;

	    		baseSeg = new Point[2];
	    		baseSeg[0] = beforeCutAvoid;
	    		baseSeg[1] = afterCutAvoid;
	    		cutMap.put(baseSeg, new ArrayList<Point>());
	    	}
	    	// locate cut-list
	    	List<Point> cutList = cutMap.get(baseSeg);
	    	// create cut-point and put it on list
	    	double[] center = new double[2];
	//    	cutSize += (basePoint == start ? startTakeout : 0);
	    	for(int i = 0; i < 2; i++){
	    		center[i] = basePoint.x(i) + unitVector[i]*proposedCutLocation;
	    	}
	    	Point cut = geo.createPoint(center);
	    	cutList.add(cut);
	    	return BuildCutStatus.notDone;
	    }
	    
		public Map<Point[], List<Point>> getCutMap() {
			if(cutMap == null){
				cutMap = new LinkedHashMap<Point[], List<Point>>();
			}
			return cutMap;
		}
		
		public List<Point> getCutList() {
			if(cutList == null){
				cutList = new ArrayList<Point>();
			}
			return cutList;
		}

		public long getAvoidMargin() {
			return avoidMargin;
		}
		public void setAvoidMargin(long avoidMargin) {
			this.avoidMargin = avoidMargin;
		}
		public long getMaxSize() {
			return maxSize;
		}
		public void setMaxSize(long maxSize) {
			this.maxSize = maxSize;
		}
		public long[] getCutSizes() {
			return cutSizes;
		}
		
		public long getThreadedCutSize() {
			return threadedCutSize;
		}

		public void setThreadedCutSize(long threadedCutSize) {
			this.threadedCutSize = threadedCutSize;
		}

		public void setCutSizes(long[] cutSizes) {
			this.cutSizes = cutSizes;
		}
		public void setCutSizes(List<Long> cutList) {
			this.cutSizes = new long[ cutList.size()];
			for(int i = 0; i < this.cutSizes.length; i++){
				this.cutSizes[i] = cutList.get(i);
			}
		}
	}

	private double linearTolerance;
	private double angularTolerance;
	private Class<? extends Point> pointClass;
//	private Divider divider;

/*	public Divider getDivider() {
		if(divider == null){
			divider = new Divider(this);
		}
		return divider;
	}
*/
	public static double sqNorm(final double[] v){
		return dotProduct(v, v);
	}
	public static double norm(final double[] v){
		return Math.sqrt(sqNorm(v));
	}
    public static double dotProduct(final double[] v, final double[] w){
    	return v[0]*w[0] + v[1]*w[1];
    }
    public static double crossProduct(final double[] v, final double[] w){
    	return v[0]*w[1] - w[0]*v[1];
    }
    public static double crossProduct(final Point[] triple){
    	double[] v = difference(triple[0], triple[1]);
    	double[] w = difference(triple[2], triple[1]);
    	return crossProduct(v, w);
    }
    public static double angleMeasure(final double[] v, final double[] w){
    	double cos = (dotProduct(v, w)/(norm(v)*norm(w)));
    	cos = (cos > 1.0) ? 1.0 : ((cos < -1.0) ? -1.0 : cos);
    	return Math.acos(cos);
    }
    public double signAngleMeasure(final double[] v, final double[] w){
        // get unsigned angle between 0 and PI
    	double ret = angleMeasure(v, w);
    	// add sign except for angles close to 0 and PI
    	if(ret < Math.PI - angularTolerance && ret > angularTolerance){
	        if(crossProduct(v, w) < 0){
	        	ret = -ret;
	        }
    	}
    	return ret;
    }
    public double rotation(final double[] w){
    	return signAngleMeasure(E1, w);
    }
    public double tilt(final double[] w){
    	double ret = rotation(w);
    	if(ret > 0.5*Math.PI){
    		ret -= Math.PI;
    	}else if(ret < -0.5*Math.PI){
    		ret += Math.PI;
    	}
    	if(Math.abs(ret + 0.5*Math.PI) < angularTolerance){
    		ret = 0.5*Math.PI;
    	}
    	return ret;
    }

    public double tilt(final Point start, final Point end){
    	return tilt(difference(end, start));
    }
    public double signAngleMeasure(final Point[] triple){
    	double[] v = difference(triple[0], triple[1]);
    	double[] w = difference(triple[2], triple[1]);
    	return signAngleMeasure(v, w);
    }
    public static double angleMeasure(final Point[] triple){
    	double[] v = difference(triple[0], triple[1]);
    	double[] w = difference(triple[2], triple[1]);
    	return angleMeasure(v, w);
    }
    public static double[] difference(final Point v, final Point w){
    	double[] ret = new double[2];
    	ret[0] = v.x(0) - w.x(0);
    	ret[1] = v.x(1) - w.x(1);
    	return ret;
    }
    public boolean vectorIntersectsArc(final Point origin, final Point end, double arcStart, double arcEnd){
    	double[] v = difference(end, origin);
    	double ang = signAngleMeasure(E1, v);
    	if(ang < 0){
    		ang += 2*Math.PI;
    	}
    	if(arcStart < arcEnd){
    		return ang >= arcStart && ang <= arcEnd;
    	}
    	return ang >= arcStart || ang <= arcEnd;
    }
    public static double distance(final Point v, final Point w){
    	return norm(difference(v, w));
    }

    public boolean pointOnPoint( final Point v, final Point w){
    	return sqNorm(difference(v, w)) <= linearTolerance * linearTolerance;
    }

    public boolean pointOnSegment(
    	    final Point ptP, final Point ptA, final Point ptB)
    {
    	boolean ret = false;
    	double dAB;
    	double dAP;
    	double dotAPB;
    	double proj;

    	    // project P on AB
    	dAB = distance(ptA, ptB);
    	dAP = distance(ptA, ptP);
    	dotAPB = dotProduct(difference(ptP, ptA), difference(ptB, ptA));
    	proj = dotAPB / dAB;

    	if((proj > 0) && (proj < dAB)) // projects in the middle
    	{
    	      // find distance between the projection and P (Pythagoras Thm)
    	    if(((dAP * dAP) - (proj * proj)) < (linearTolerance * linearTolerance))
    	    {
    	        ret = true;
    	    }
    	}

        return ret;
    }
    public static double[] barycenter(final Collection<? extends Point> points){
    	if(points.isEmpty()){
    		logger.info("Empty set has no barycenter");
    		return null;
    	}
    	double[] ret = new double[2];
    	for(Point p : points){
    		ret[0] += p.x(0);
    		ret[1] += p.x(1);
    	}
    	ret[0] /= points.size();
    	ret[1] /= points.size();
    	return ret;
    }
    private static void streach(final double[] in, final double[] out, final double factor){
    	for(int i = 0; i < in.length; i++){
    	    out[i] = in[i] * factor;
    	}
    }
    public static double[] makeStreach(final double[] v, final double factor){
    	double[] ret = new double[2];
    	streach(v, ret, factor);
    	return ret;
    }
    public static void streachInPlace(final double[] v, final double factor){
    	streach(v, v, factor);
    }
    /**
     * Subdivides segment from start to end into subsegments.
     * All subsegments have the same cut size except the last one which needs to be
     * shorter than a maximal size.
     * @param pointClass used as factory
     * @param start star of segment
     * @param end end of segment
     * @param maxSize the maximal size
     * @param cutSize the cut size
     * @param cuts the cut points.
     * Empty list, if segment size is less than max size.
     *
    public void subdivide(Class<? extends Point> pointClass, final Point start, final Point end, final double maxSize, final double cutSize,
    		List<Point> cuts){
    	cuts.clear();
 //   	List<double[]> ret = new ArrayList<double[]>();
    	double[] d = difference(end, start);
    	double dist = norm(d);
    	streachInPlace(d, cutSize/dist);
    	for(int i = 0; (dist - cutSize*i) > maxSize + linearTolerance   ; i++){
    		double[] c = makeStreach(d, (i + 1));
    		for(int j = 0; j < 2; j++){
    			c[j] += start.x(j);
    		}
    		Point p = createPoint(c);
    		cuts.add(p);
    	}
    }*/
    
    
    /**
     * Subdivides segment from start to end into subsegments.
     * All subsegments have cut sizes picked from a list except the last one
     * which needs to be shorter than a maximal size. In addition, no cut
     * can be too close to any of the passed avoid locations.
     * @param start start of segment to be cut
     * @param end end of segment to be cut
     * @param maxSize the maximal segment length allowed
     * @param cutSizes possible collection of cut sizes (in the order of preference)
     * @param avoidLocations collection of points to avoid (in order of appearance)
     * @param avoidMargin avoid location's radius
     * @return map indexed by starts of subsegments into which the avoid points divide the segment.
     * The value is a list of vector offsets from the subsegment's start to the cuts which fall
     * on the subsegment. Empty list, if segment size is less than max size.
     *
    public void subdivide(final Point start, final Point end, final double maxSize, final Collection<Double> cutSizes, final Collection<? extends Point> avoidLocations, final double avoidMargin,
    		Map<Point, List<double[]>> cutMap){
    // create unit vector for the segment we divide
    	double[] u = difference(end, start);
    	double dist = norm(u);
    	streachInPlace(u, 1.0/dist);
    	double lastLocation = 0;
    	cutMap.clear();
    	while((dist - lastLocation) > maxSize + linearTolerance){
    		double goodCutSize = -1;
    		Point predecessor = start;
    		double predecessorLocation = 0;
    		for(double cutSize : cutSizes){ // iterate over allowed cut sizes
    			boolean avoids = true;
    			Point prevAvoid = start;
    			// distance of avoid from start calculated by adding rounded spans
    			double avoidLocation = 0;
    			for(Point avoidPoint : avoidLocations){
    				avoidLocation += Math.round(distance(prevAvoid, avoidPoint));
    			    if(Math.abs(lastLocation + cutSize - avoidLocation) < avoidMargin){
    			    	avoids = false; // too close to avoid: try next cut size
    			    	break;
    			    }
    			    if(avoidLocation < lastLocation + cutSize){
    			    	// save the location of last avoid before the cut
    			    	predecessor = avoidPoint;
    			    	predecessorLocation = avoidLocation;
    			    }
    			    prevAvoid = avoidPoint;
    			}
    			if(avoids){
    				goodCutSize = cutSize;
    				break;
    			}
    		}
    		if(goodCutSize < 0){
    			throw new IllegalArgumentException("Cannot find suitable cut location on main");
    		}
    		lastLocation += goodCutSize;
    	//	double predecessorLocation = distance(start, predecessor);
    		double[] c = makeStreach(u, lastLocation - predecessorLocation);
    		List<double[]> cutList = cutMap.get(predecessor);
    		if(cutList == null){
    			cutList = new ArrayList<double[]>();
    			cutMap.put(predecessor, cutList);
    		}
    		cutList.add(c);
    	}
    }*/
    
    //
    // new code
    //
    /**
     * Creates a collection of cut points for a start-end segment
     * using passed specs. The collection is keyed by a sub-collection
     * of points-to avoid: any each such key is an immediate predecessor for
     * a contiguous cut-chain.
     * @param pointClass used as a factory for cut-points
     * @param start the start of segment
     * @param end the end of segment
     * @param maxSize maximal unbroken length allowed
     * @param cutSizes list of possible cut-sizes in order of precedence
     * @param avoidLocations the points on the segment to avoid
     * @param avoidMargin how far a cut has to be from a point-to-avoid
     * @param cutMap the resulting map
     * @return <code>true</code> if successful
     *
    public boolean subdivide(Class<? extends Point> pointClass,
    		final Point start,
    		final Point end,
    		final double maxSize,
    		final Collection<Double> cutSizes,
    		final List<? extends Point> avoidLocations,
    		final double avoidMargin,
    		Map<Point, List<Point>> cutMap) {
    // round margin
    	long margin = Math.round(avoidMargin);
    	long max = Math.round(maxSize);
    // create unit vector for the segment we divide
    	double[] u = difference(end, start);
    	double dist = norm(u);
    	streachInPlace(u, 1.0/dist);
    // clear cut Map
    	cutMap.clear();
    // incremental build
    	BuildCutStatus status = BuildCutStatus.notDone;
    	while(status == BuildCutStatus.notDone){
    		// try all cut-sizes in turn
    		for(double cutSize : cutSizes){
    			long cut = Math.round(cutSize);
    			status = buildCutMap(pointClass, u, start, end, max,
    					cut, avoidLocations, margin, cutMap);
    			if(status != BuildCutStatus.wrongCutSize){
    				break;
    			}
    		}
    	}
    	logger.debug("-subdivide->" + status);
    	return status == BuildCutStatus.done;
    }
 */
 /*   private enum BuildCutStatus{
    	wrongCutSize,
    	notDone,
    	done;
    }*/
    
    /**
     * An incremental step in building the cutMap. Algorithm:
     * Determine base point: If the cutMap is empty this will be the start,
     * otherwise, it is the last cut found on the map.
     * Iterate over avoid locations beyond the base: If the cutMap is empty
     * start with the first avoid on the list. Otherwise, locate the last
     * avoid on the cut list and start with the follower on the avoid list.
     * If all avoids are done - iterate one more time using the endPoint.
     * Calculate effective span from the base to the avoids being iterated
     * (adding the rounded distances) until either the next effective-span exceeds
     * the max or the list reaches the end. Verify if the effective span
     * was ever closer to the cut-length than the avoidMargin.
     * If the iteration terminates at the end-point and the effective span
     * does not exceed the max we return Done. Otherwise, if this is margin-close
     * situation, we return WrongCutSize. Otherwise we create a new Point.
     * It is located relative to the predecessor avoid in the direction of
     * the unitVector at the offset which the cutSize minus the effective-span
     * of the predecessor. As a result the effective span for the cut will be
     * exactly the cutSize. If during the iteration the predecessor avoid
     * has changed, we use it to create a new entry in the map.
     * Otherwise (consecutive cuts) we just append the new cut to the last list.
     * We return with NotDone.
     * 
     * @param pointClass
     * @param unitVector
     * @param start
     * @param end
     * @param maxSize
     * @param cutSize
     * @param avoidLocations
     * @param avoidMargin
     * @param cutMap
     * @return
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws IllegalArgumentException 
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     *
    public BuildCutStatus buildCutMap(Class<? extends Point> pointClass,
    		double[] unitVector,
    		final Point start,
    		final Point end,
    		final long maxSize,
    		long cutSize,
    		final List<? extends Point> avoidLocations,
    		final long avoidMargin,
    		Map<Point, List<Point>> cutMap){
    	Point basePoint = null;
    	Point baseAvoid = null;
    	int startAvoidIndex = 0;
    	if(cutMap.isEmpty()){
    		basePoint = start;
    		baseAvoid = start;
    	}else{
    		// find last avoid on the cut-map
    		for(Point avoid : cutMap.keySet()){
    			baseAvoid = avoid;
    		}
    		// calc avoid indices
    		if(baseAvoid != start){
    			startAvoidIndex = avoidLocations.indexOf(baseAvoid) + 1;
    		}
    		// find the last cut
    		for(Point cut : cutMap.get(baseAvoid)){
    			basePoint = cut;
    		}
    	}
    	// locate predecessor of potential new cut
    	long effectiveSpan = 0;
    	Point lastAvoid = basePoint;
    	boolean isClose = false;
    	for(int i = startAvoidIndex; i <= avoidLocations.size(); i++){
    		Point avoid = (i < avoidLocations.size())
    			? avoidLocations.get(i) : end;
    		long ld = Math.round(distance(lastAvoid, avoid));
    		if(Math.abs(effectiveSpan + ld - cutSize) < avoidMargin){
    			isClose = true;
    		}
    		if((ld + effectiveSpan) > maxSize){
    			break;
    		}
    		baseAvoid = avoid;
    		lastAvoid = avoid;
    		effectiveSpan += ld;
    	}
    	if(lastAvoid == end){
    		// no need to cut
    		return BuildCutStatus.done;
    	}
    	if(isClose){
    		// cut needed but too close to one of the end points
    		return BuildCutStatus.wrongCutSize;
    	}
    	// locate or create cut-list
    	List<Point> cutList = cutMap.get(baseAvoid);
    	if(cutList == null){
    		cutList = new ArrayList<Point>();
    		cutMap.put(baseAvoid, cutList);
    	}
    	// create cut-point and put it on list
    	double[] center = new double[2];
    	for(int i = 0; i < 2; i++){
    		center[i] = lastAvoid.x(i) + unitVector[i]*(cutSize - effectiveSpan);
    	}
    	Point cut = createPoint(center); ///constructor.newInstance(center);
    	cutList.add(cut);
    	return BuildCutStatus.notDone;
    }
    */
    private Point createPoint(double[] center){
    	Point ret = null;
		try {
			Constructor<? extends Point> constructor
				= pointClass.getConstructor(center.getClass());
	    	ret = constructor.newInstance(center);
		} catch (Exception e) {
			logger.error("Failed to create point", e);
		}
		return ret;
    }

    
	public double getLinearTolerance() {
		return linearTolerance;
	}
	public void setLinearTolerance(final double linearTolerance) {
		this.linearTolerance = linearTolerance;
	}
	public double getAngularTolerance() {
		return angularTolerance;
	}
	public void setAngularTolerance(final double angularTolerance) {
		this.angularTolerance = angularTolerance;
	}
	public Class<? extends Point> getPointClass() {
		return pointClass;
	}
	public void setPointClass(Class<? extends Point> pointClass) {
		this.pointClass = pointClass;
	}

}
