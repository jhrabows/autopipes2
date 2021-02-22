package org.autopipes.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.autopipes.model.DrawingLayer.Designation;
import org.autopipes.model.PipeFitting.Jump;
import org.autopipes.takeout.Fitting;
import org.autopipes.takeout.Fitting.Direction;
import org.autopipes.takeout.Fitting.Type;
import org.autopipes.util.CommonDecimal;
import org.autopipes.util.EdgeIterator;
import org.autopipes.util.PlaneGeo;
import org.autopipes.util.PlaneGeo.Point;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.UndirectedSubgraph;


/**
 * The class which represents a physical drawing.
 * It is used to receive information extracted from a drawing for analysis
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dwgEntity"
})
@XmlRootElement(name = "dwg-body")
public class AreaBody
{
    private static Logger logger = Logger.getLogger(AreaBody.class);
    private static int sanityCounter;

    @XmlElement(name = "dwg-entity")
    protected ArrayList<DwgEntity> dwgEntity;
    @XmlTransient
    protected DwgPoint center;
    @XmlTransient
    protected List<DwgPoint> centers;
    @XmlTransient
    protected PipeFitting raiser;
    @XmlTransient
    protected UndirectedGraph<PipeFitting, Pipe> pipeGraph;
    @XmlTransient
    protected Map<DwgPoint, PointInfo> pointMap;
    @XmlTransient
    protected List<Pipe> edgesInOrder;
    @XmlTransient
    protected Map<PipeFitting, Set<Direction>> groovedChainStarts;
    @XmlTransient
    protected Map<PipeFitting, Set<Direction>> threadedChainStarts;
    @XmlTransient
    protected Map<PipeFitting, Set<Direction>> allChainStarts;
    @XmlTransient
    protected List<PipeFitting> pipeFittingList;

	public List<PipeFitting> getPipeFittingList() {
		if(pipeFittingList == null){
			pipeFittingList = new ArrayList<PipeFitting>();
		}
		return pipeFittingList;
	}

    
	public List<DwgEntity> getDwgEntity() {
        if (dwgEntity == null) {
            dwgEntity = new ArrayList<DwgEntity>();
        }
        return dwgEntity;
    }

	public DwgPoint getCenter() {
		if(center == null && !getCenters().isEmpty()){
		    center = new DwgPoint( PlaneGeo.barycenter(getCenters()));
		}
		return center;
	}
	public List<DwgPoint> getCenters() {
		if(centers == null){
			centers = new ArrayList<DwgPoint>();
			calcCenters();
		}
		return centers;
	}
    private void calcCenters(){

    	// verify connectivity
    	ConnectivityInspector<PipeFitting, Pipe> ci =
    		new ConnectivityInspector<PipeFitting, Pipe>(getPipeGraph());
    	List<Set<PipeFitting>> sets = ci.connectedSets();

    	// calculate centers
    	for(Set<PipeFitting> set : sets){
    		double[] x = PlaneGeo.barycenter(set);
    		centers.add(new DwgPoint(x));
    	}
    }

	public PipeFitting getRaiser(){
		return raiser;
	}

	public PipeFitting getStartFitting(final Pipe pi){
		return pi.isReversed() ? pipeGraph.getEdgeTarget(pi) : pipeGraph.getEdgeSource(pi);
	}
	public PipeFitting getEndFitting(final Pipe pi){
		return pi.isReversed() ? pipeGraph.getEdgeSource(pi) : pipeGraph.getEdgeTarget(pi);
	}

	/**
	 * @deprecated
     * Cuts this segment entity into subsegments at provided cut-points.
     * @param cuts the cut-points
     * @return the subsegments. First segment shares start point with the this entity
     * while the last segment shares the end point with it.
     */
    @Deprecated
	private List<Pipe> subdivide(final Pipe pipe, final Collection<PipeFitting> cuts){
    	List<Pipe> ret = new ArrayList<Pipe>();
    	List<PipeFitting> ends = new ArrayList<PipeFitting>();
    	ends.addAll(cuts);
    	ends.add(getEndFitting(pipe));
    	PipeFitting last = getStartFitting(pipe);
        // replace edges
        getPipeGraph().removeEdge(pipe);
    	for(PipeFitting cutend : ends){
			Pipe part = pipe.clone(); // The clone will not be reversed:
			// even if original has been reversed the parts are not.
            getPipeGraph().addEdge(last, cutend, part);
            ret.add(part);
            last = cutend;
    	}
    	return ret;
    }
	/**
	 * Replaces segment which is expected to exist between passed start/end fittings
     * with a chain of new subsegments which join provided cut-fittings.
     * @param cuts the cut-points
     * @return the subsegments. First segment's  start point is the start arg
     * while the last segment's  end point is the end arg
     */
    private List<Pipe> subdivide2(final PipeFitting pipeStart, final PipeFitting pipeEnd, final Collection<PipeFitting> cuts){
    	Pipe pipe = this.getPipeGraph().getEdge(pipeStart, pipeEnd);
    	List<Pipe> ret = new ArrayList<Pipe>();
    	List<PipeFitting> ends = new ArrayList<PipeFitting>();
    	ends.addAll(cuts);
    	ends.add(pipeEnd);
    	PipeFitting last = pipeStart;
        // replace edges
        getPipeGraph().removeEdge(pipe);
    	for(PipeFitting cutend : ends){
			Pipe part = pipe.clone(); // The clone will not be reversed:
			// even if original has been reversed the parts are not.
            getPipeGraph().addEdge(last, cutend, part);
            ret.add(part);
            last = cutend;
    	}
    	return ret;
    }
    
    
    public boolean isOnMain(final DwgPoint p){
		for(PipeFitting pf : getPointInfo(p).fittings){
			if(isOnMain(pf)){
				return true;
			}
		}
		return false;
    }

    public boolean isOnMain(final PipeFitting pf){
    	return Pipe.hasMain(pipeGraph.edgesOf(pf));
    }

    public List<Pipe> getEdgesInOrder(){
    	if(edgesInOrder == null){
    		iterateEdges();
    	}
    	return edgesInOrder;
    }
    public void orderEdges(){
    	getEdgesInOrder();
    }
    private void iterateEdges(){
    // re-create the original graph (without the sink)
    	Set<PipeFitting> trueVertexSet = new HashSet<PipeFitting>();
    	Set<Pipe> trueEdgeSet = new HashSet<Pipe>();

    	trueVertexSet.addAll(pipeGraph.vertexSet());
    	trueEdgeSet.addAll(pipeGraph.edgeSet());
    	if(pipeGraph.vertexSet().contains(PipeFitting.SINK)){
            trueVertexSet.remove(PipeFitting.SINK);
    	    trueEdgeSet.removeAll(pipeGraph.edgesOf(PipeFitting.SINK));
    	}
    	UndirectedGraph<PipeFitting, Pipe> truePipeGraph = new UndirectedSubgraph<PipeFitting, Pipe>(pipeGraph, trueVertexSet, trueEdgeSet);

    // run the iteration algorithm and save the ordered list
    	EdgeIterator<PipeFitting, Pipe> ei
    	    = new EdgeIterator<PipeFitting, Pipe>(truePipeGraph, getRaiser(), PipeFitting.class);
    	edgesInOrder = ei.getEdges();
    // swap the ends of edges with incompatible ordering
    	for(Pipe p : ei.getReversed()){
    		p.makeReversed();
    	}
    }

    /**
     * Degree of a pipe fitting excluding heads (if present)
     * @param pf the fitting
     * @return the degree
     */
    public int degreeOf(final PipeFitting pf){
    	int degree = pipeGraph.degreeOf(pf);
    	if(pipeGraph.getEdge(pf, PipeFitting.SINK) != null){
    		degree--;
    	}
    	return degree;
    }

    /**
     * Aggregates head count on all branch points.
     * Assumes that there are no branch loops
     * but does not require proper edge ordering.
     */
    public void calcHeadCount(){
    	// iterate over main vertices
    	for(PipeFitting pf: pipeGraph.vertexSet()){
    		if(isOnMain(pf)){
				countHeads(pf, null);
    		}
    	}
    }

/**
 * Recursive call for down-stream head count aggregation on branches.
 * @param pf terminal aggregation point
 * @param in edge connecting pf to its predecessor in the recursive traversal.
 * Null for first call.
 */
    private void countHeads(final PipeFitting pf, final Pipe in){
    	int cnt = 0;
    	if(in != null && pipeGraph.degreeOf(pf) == 1){
    		cnt = 1; // can happen only if there is missing head at an end
    	}else{
    	  for(Pipe b : pipeGraph.edgesOf(pf)){
    		if(in != null && in == b){
    			continue; // always go forwards
    		}
    		if(b.getDesignation() == Designation.Main){
    			continue; // skip main
    		}
			if(b.getDesignation() == Designation.Head){
				cnt++;
				continue;
			}
			PipeFitting oppFitting = Graphs.getOppositeVertex(pipeGraph, b, pf);
			countHeads(oppFitting, b);
			cnt += oppFitting.getHeadCount();
    	  }
    	}
        pf.setHeadCount(cnt);
    }

	public void setCenters(final List<DwgPoint> centers) {
		this.centers = centers;
	}


	public Map<DwgPoint, PointInfo> getPointMap() {
		if(pointMap == null){
			pointMap = new HashMap<DwgPoint, PointInfo>();
		}
		return pointMap;
	}
	public int getProblemPointCount(){
		int ret = 0;
		for(PointInfo info : pointMap.values()){
			if(info.getStatus() != Defect.noDefects){
				ret++;
			}
		}
		return ret;
	}

	public PointInfo getPointInfo(final DwgPoint p){
		PointInfo ret = pointMap.get(p);
		if(ret == null){
			ret = new PointInfo();
			pointMap.put(p, ret);
		}
		return ret;
	}


	public void setPointMap(final Map<DwgPoint, PointInfo> pointMap) {
		this.pointMap = pointMap;
	}


    /**
     * 2-end fitting has no side pipes. Otherwise, these are pipes orthogonal
     * to this pipe at this fitting.
     * @param p the pipe
     * @param pf the fitting
     * @return
     */
    public List<Pipe> getSidePipes(final Pipe p, final PipeFitting pf){
    	List<Pipe> ret = new ArrayList<Pipe>();
    	Type type = pf.getFitting().getType();
    	if(type.getEndCount() == 2){
    		return ret;
    	}
   	    PipeAttachment a = getAttachmentToFitting(p, pf);
   	    for(Direction dir : type.adjacent(a.getDirectionInFitting())){
   	    	ret.add(getPipe(pf, dir));
   	    }
   	    return ret;
    }
    /**
     * For a 2-end fittings the continuation is the pipe on other end of this pipe.
     * Otherwise it is the linear continuation (antipode) of this pipe.
     * @param p the pipe
     * @param pf the fitting
     * @return the continuation pipe
     */
    public Pipe getContinuationPipe(final Pipe p, final PipeFitting pf){
    	Type type = pf.getFitting().getType();
    	PipeAttachment a = getAttachmentToFitting(p, pf);
    	Direction oppositeDir = null;
    	if(type.getEndCount() == 2){
    		oppositeDir = type.adjacent(a.getDirectionInFitting()).get(0);
    	}else{
    		oppositeDir = type.antipode(a.getDirectionInFitting());
    	}
        return oppositeDir == null ? null : getPipe(pf, oppositeDir);
    }
    public boolean isInitialMainPipe(final Pipe p){
    	return isExtremeMainPipe(p, getStartFitting(p));
    }
    public boolean isTerminalMainPipe(final Pipe p){
    	return isExtremeMainPipe(p, getEndFitting(p));
    }
    private boolean isExtremeMainPipe(final Pipe p, final PipeFitting pf){
    	if(p.getDesignation() != Designation.Main){
    		throw new IllegalArgumentException("Expected Main pipe");
    	}

    	Type type = pf.getFitting().getType();
    	if(type.getEndCount() <= 2 && type != Type.Coupling){
    	    return true;
    	}
    	Pipe cont = getContinuationPipe(p, pf);
    	return cont == null || cont.getDesignation() != Designation.Main
    	  || cont.getId() != p.getId();
    }
    public Pipe getPipe(final PipeFitting pf, final Direction dir){
    	for(Pipe p : pipeGraph.edgesOf(pf)){
    		PipeAttachment a = getAttachmentToFitting(p, pf);
    		if(a.getDirectionInFitting() == dir){
    			return p;
    		}
    	}
    	return null;
    }
/**
 * Recursive call to determine if 2 branch trees are equal.
 * The trees are equal if they are metrically-equivalent.
 * In other words, if one can be laid on top of the other
 * - possibly rotating the fittings.
 * @param e1 the first tree root
 * @param e2 the second tree root
 * @return
 */
	   public boolean branchStartsEqual(final Pipe e1, final Pipe e2){
	    	// check starting pipe
	    	if(!Pipe.areEqualExact(e1, e2)){
	    		return false; // pipes not equal
	    	}
	    	if(e1.getDesignation() == Designation.Head){
	    		return true; // nothing more to do
	    	}
	    	// starts are equal, check continuations on other end
	    	PipeFitting p1 = getEndFitting(e1);
	    	PipeFitting p2 = getEndFitting(e2);
            if(p1.getFitting().getType() != p2.getFitting().getType()){
            	return false; // fittings not equal
            }
            Type endType = p1.getFitting().getType();
            if(endType != null){
            	PipeAttachment ea1 = e1.getEndAttachment();
            	PipeAttachment ea2 = e2.getEndAttachment();
            	if(ea1 == null && ea2 != null || ea1 != null && ea2 == null){
            		return false;
            	}
            	if(ea1 != null){
            		Direction ed1 = ea1.getDirectionInFitting();
            		Direction ed2 = ea2.getDirectionInFitting();
            		if(!endType.equivalentDirections(ed1, ed2)){
            			return false;
            		}
            	}
            }
	    	Pipe op1 = getContinuationPipe(e1, p1);
	    	Pipe op2 = getContinuationPipe(e2, p2);
	    	if(op1 != null && op2 == null || op1 == null && op2 != null){
	    		return false;
	    	}
	    	if(op1 != null && !branchStartsEqual(op1, op2)){
	    		return false;
	    	}
	    	// continuations are equal - check orthos
	    	List<Pipe> ort1 = getSidePipes(e1, p1);//pf1.getOrtho(e1);
	    	List<Pipe> ort2 = getSidePipes(e2, p2);//pf2.getOrtho(e2);
	    	if(ort1.size() != ort2.size()){
	    		return false;
	    	}
	    	if(ort1.size() == 0){
	    		return true;
	    	}
	    	if(ort1.size() == 1){
	    		return branchStartsEqual(ort1.get(0), ort2.get(0));
	    	}
	        // size == 2
	    	return branchPairsEqual(ort1.get(0), ort1.get(1), ort2.get(0), ort2.get(1));
	    }

	    /**
	     * Check if a pair of branches from the same point going in opposite directions
	     * is equivalent to another such pair
	     * @param areaBody branch location
	     * @param a1 pipe 1 from first pair
	     * @param a2 pipe 2 from first pair
	     * @param b1 pipe 1 from second pair
	     * @param b2 pipe 2 from second pair
	     * @return <code>true</code> if it is
	     */

	    private boolean branchPairsEqual(final Pipe a1, final Pipe a2,
	        final Pipe b1, final Pipe b2){
	    	if(branchStartsEqual(a1, b1) && branchStartsEqual(a2, b2)){
	    		return true;
	    	}
	    	if(branchStartsEqual(a1, b2) && branchStartsEqual(a2, b1)){
	    		return true;
	    	}
	    	return false;
	    }

    public List<Pipe> getPipesFor(final Designation designation){
        List<Pipe> ret = new ArrayList<Pipe>();
    	for(Pipe bp: getPipeGraph().edgeSet()){
    		if(bp.getDesignation() == designation){
    			ret.add(bp);
    		}
    	}
    	return ret;
    }
    /*
    public List<PipeFitting> subdivideGraphOld(final Pipe pi, final List<double[]> cuts){
    	// Create breakup points updating pointMap and graph vertices
        List<PipeFitting> cutPoints = new ArrayList<PipeFitting>();
        for(double[] cut : cuts){
            DwgPoint p = getStartFitting(pi).getCenter().move(cut);
            PointInfo info = getPointInfo(p);
            PipeFitting pf = new PipeFitting(p);
            info.getFittings().add(pf);
            cutPoints.add(pf);
            DwgEntity cup = DwgEntity.createPoint(p);
            info.getCouplings().add(cup);

            getPipeGraph().addVertex(pf);
        }
        // break the segment
        List<Pipe> breakups = subdivide(pi, cutPoints);

        // expand the edge in ordered set
        expandOrdered(pi, breakups);
        return cutPoints;
    }
    */
    

 /**
 * Replaces segment which is expected to exist between passed start/end fittings
 * with a chain of new subsegments at provided cut-points.
 * @param piStart start fitting
 * @param piEnd end fitting
 * @param cuts list of point locations to be used for cutting
 * @return the list of fittings created during breakup
 */
public List<PipeFitting> subdivideGraph2(final PipeFitting piStart, final PipeFitting piEnd, final List<Point> cuts){
     	Pipe pi = this.getPipeGraph().getEdge(piStart, piEnd);
    	// Create breakup points updating pointMap and graph vertices
        List<PipeFitting> cutFittings = new ArrayList<PipeFitting>();
        for(Point cut : cuts){
            DwgPoint p = (DwgPoint)cut;//getStartFitting(pi).getCenter().move(cut);
            PointInfo info = getPointInfo(p);
            PipeFitting pf = new PipeFitting(p);
            info.getFittings().add(pf);
            cutFittings.add(pf);
            DwgEntity cup = DwgEntity.createPoint(p);
            info.getCouplings().add(cup);

            getPipeGraph().addVertex(pf);
        }
        // break the segment
        List<Pipe> breakups = subdivide2(piStart, piEnd, cutFittings);

        // expand the edge in ordered set
        expandOrdered(pi, breakups);
        return cutFittings;
    }
    /**
     * @deprecated
     * @param pi
     * @param cuts
     * @return
     */
    @Deprecated
	public List<PipeFitting> subdivideGraph(final Pipe pi, final List<Point> cuts){
    	// Create breakup points updating pointMap and graph vertices
        List<PipeFitting> cutPoints = new ArrayList<PipeFitting>();
        for(Point cut : cuts){
            DwgPoint p = (DwgPoint)cut;//getStartFitting(pi).getCenter().move(cut);
            PointInfo info = getPointInfo(p);
            PipeFitting pf = new PipeFitting(p);
            info.getFittings().add(pf);
            cutPoints.add(pf);
            DwgEntity cup = DwgEntity.createPoint(p);
            info.getCouplings().add(cup);

            getPipeGraph().addVertex(pf);
        }
        // break the segment
        List<Pipe> breakups = subdivide(pi, cutPoints);

        // expand the edge in ordered set
        expandOrdered(pi, breakups);
        return cutPoints;
    }
    public void expandOrdered(final Pipe b, final List<Pipe> breakups){
        List<Pipe> inOrder = getEdgesInOrder();
        int idx = inOrder.indexOf(b);
        inOrder.remove(idx);
        inOrder.addAll(idx, breakups);
    }


    public PipeAttachment getAttachmentToFitting(final Pipe pipe, final PipeFitting pf){
    	return getStartFitting(pipe) == pf ? pipe.getStartAttachment() : pipe.getEndAttachment();
    }
    /**
     * Converts a portion of a line segment to an edge of a pipe graph.
     * Each vertex will be the first fitting at the point - it will be created if necessary.
     * @param e the segment
     * @param start start of the portion
     * @param end end of the portion
     */
	public void addPipeToGraph(final Pipe pipe, final DwgPoint start, final DwgPoint end){
		PipeFitting[] fittings = new PipeFitting[2];
		for(int i = 0; i < 2; i++){
			DwgPoint p = (i == 0) ? start : end;
			List<PipeFitting> pFittings = getPointInfo(p).getFittings();
			if(pFittings.isEmpty()){
				pFittings.add(new PipeFitting(p));
			}
			fittings[i] = pFittings.get(0);
			getPipeGraph().addVertex(fittings[i]);
		}
		getPipeGraph().addEdge(fittings[0], fittings[1], pipe);
	}
	/**
	 * Produces tree-based orientation on branch pipes.
	 * Also does general validation - in particular checks if branches are trees
	 * @return success/failure of the operation
	 */
	public boolean orderBranchPipes(){
		Set<DwgPoint> visited = new HashSet<DwgPoint>();
		for(PipeFitting pf : getPipeGraph().vertexSet()){
            if(!isOnMain(pf)){
            	continue; // skip purely branch points
            }
            // assume only one fitting at this stage
            for(Pipe pi : getPipeGraph().edgesOf(pf)){
            	if(pi.getDesignation() == Designation.Branch){
            		// recursive call
            		this.sanityCounter = 0;
            		if(!orderBranchPipe(pi, pf, visited)){
            			return false;
            		}
            	}
            }
		}
        return true;
	}
	private boolean orderBranchPipe(final Pipe pi, final PipeFitting start, final Set<DwgPoint> visited){
        // make sure the pipe is ordered
		if(getStartFitting(pi) != start){
		    pi.makeReversed();
		}
		PipeFitting end = getEndFitting(pi);
		if(visited.contains(end)){
			getPointInfo(end.getCenter()).setStatus(Defect.loopInBranch);
			return false;
		}

		for(Pipe out : getPipeGraph().edgesOf(end)){
			if(out == pi){
				continue; // skip self
			}
			if(!out.getLayerName().equals(pi.getLayerName())){
				getPointInfo(end.getCenter()).setStatus(Defect.wrongBranchLayer);
				return false;
			}
			if(++sanityCounter > 50){
				getPointInfo(end.getCenter()).setStatus(Defect.cycleInBranch);
				return false;
			}
			if(!orderBranchPipe(out, end, visited)){
				return false;
			}
		}
		return true;
	}
	/*
    private double calcTakeout(final Pipe e, double totalTakeout){
		// span
		PipeFitting start =  getStartFitting(e);// e.getEntStart();//areaBody.getPointGraph().getEdgeSource(e);
		PipeFitting end = getEndFitting(e);//areaBody.getPointGraph().getEdgeTarget(e);
 //       logger.info("+calcTakeout(" + e + "," + start.getFitting() + end.getFitting() + ")");
		double span = PlaneGeo.distance(start, end);
		e.setSpan(new BigDecimal((int)Math.round(span)));
		if(start.getFitting().getType() == Type.Raiser){
			return 0.0; // ignore raiser
		}
		double startTakeout = e.getStartAttachment().getTakeout() == null ? 0.0
			: e.getStartAttachment().getTakeout().doubleValue();
		double endTakeout = e.getEndAttachment().getTakeout() == null ? 0.0
		    : e.getEndAttachment().getTakeout().doubleValue();
		if((start.getFitting().getType() != Type.Coupling || start.getFitting().getAttachment() != Attachment.threaded)
			&& (end.getFitting().getType() != Type.Coupling || end.getFitting().getAttachment() != Attachment.threaded)){
        // no threaded couplings on either side: just add both takeouts
			int sum = (int)Math.round(startTakeout + endTakeout);
			e.setTakeout(new BigDecimal(sum));
			return 0.0;
		}
		if(end.getFitting().getType() == Type.Coupling && end.getFitting().getAttachment() == Attachment.threaded){
        // end is threaded coupling chain: takeout is deferred (added to total)
			if(start.getFitting().getType() != Type.Coupling || start.getFitting().getAttachment() != Attachment.threaded){
				totalTakeout = 0.0; // start of threaded chain: reset total
			}
			totalTakeout += (startTakeout + endTakeout);
			e.setTakeout(CommonDecimal.Zero.getMeasure());
			return totalTakeout;
		}
		// end of threaded coupling chain: use accumulated total of takeouts (including this one)
		totalTakeout += (startTakeout + endTakeout);
		int sum = (int)Math.round(totalTakeout);
		e.setTakeout(new BigDecimal(sum));
		return 0.0;
    }
*/
	/*
    public void calcSpan(){
        logger.info("+calcSpan");

    	for(Pipe e : getEdgesInOrder()){
    		PipeFitting start =  getStartFitting(e);
    		PipeFitting end = getEndFitting(e);
    		double d = PlaneGeo.distance(start, end);
    		e.setSpan(new BigDecimal(Math.round(d)));
            // set main ignores
            if(e.getDesignation() == Designation.Main){
            	PipeFitting source = getStartFitting(e);
            	if(source.getFitting().getType() == Type.Raiser){
            		e.setIgnored(true);
            	}else if (!e.isVertical()){
            		BigDecimal span = e.getSpan();
           			BigDecimal takeout = e.getTakeout();
           			if(takeout.compareTo(span) >= 0){
           				e.setIgnored(true);
           			}
            	}
            }
    	}
        logger.info("-calcSpan");
    }
    */
    public void calcSpanAndTakout(int roundScale){
        logger.info("+calcSpanAndTakeout");
    //	AreaBody areaBody = area.getAreaBody();
   // 	AreaOptions areaOptions = area.getAreaOptions();
   // 	int roundScale = areaOptions.getTakeoutRounding();
    	long roundFactor = Math.round(Math.pow(2, roundScale));
    	BigDecimal bdRoundFactor = new BigDecimal(roundFactor);
    	
    	// iterate over starts
    	for(Map.Entry<PipeFitting, Set<Direction>> chainStart
    			: this.getAllChainStarts().entrySet()){
    		
    		// iterate over chains
    		for(Direction dir : chainStart.getValue()){
    			// get pipe which starts the chain
    	//		Pipe pipe = areaBody.getPipe(chainStart.getKey(), dir);
		//		boolean startOfChain = true;
		//		boolean endOfChain = false;
				double deferredTakeout = 0;
				double totalSpan = 0;
				long sumOfRoundedSpans = 0;
				double breakStartSpan = 0;
				double breakStartTakeout = 0;
				Pipe breakStartPipe = null;
				long breakStartRoundedSpan = 0;
							
				List<PipeFitting> chainList = this.getPipeFittingList();
				chainList.clear();
				this.fillChainList(chainList, chainStart.getKey(), dir);
				
				// iterate the chain
				for(int pipeIndex = 0; pipeIndex < chainList.size() - 1; pipeIndex++){
		   			Pipe pipe = this.getPipeFromChain(chainList, pipeIndex);

		   			BigDecimal bdStartTakeout = this.getAttachmentToFitting(pipe, chainList.get(pipeIndex)).getTakeout();
		   			double startTakeout = (bdStartTakeout == null) ? 0 : bdStartTakeout.doubleValue();
		   			BigDecimal bdEndTakeout = this.getAttachmentToFitting(pipe, chainList.get(pipeIndex + 1)).getTakeout();
		   			double endTakeout = (bdEndTakeout == null) ? 0 : bdEndTakeout.doubleValue();
		   			double takeout = startTakeout + endTakeout;
		   			PipeFitting startPf = chainList.get(pipeIndex);
		   			PipeFitting endPf = chainList.get(pipeIndex + 1);
		   			boolean isBreakStart = (pipeIndex == 0) || startPf.getCouplingContinuation();
		   			boolean isBreakEnd = (pipeIndex == chainList.size() - 2) || endPf.getCouplingContinuation();
		   			double span = PlaneGeo.distance(startPf, endPf);

		   			if(startPf.getFitting().getType() == Type.Raiser){
			   			pipe.setIgnored(true);
			   		}
		   			
		   			// If start is a break (chain-start or coupling), end is a coupling and following end is a break (coupling or chain-end)
		   			// then takeout will be deferred. This means that the display-span of pipe will be shortened by the sum of both takeouts
		   			// and considered equal to after-takeout span (so no takeout will be reported).
		   			// The sum of deferred takeouts will be added to the span and to the takeout of the 1st following non-defer segment.
		   			boolean deferTakeout = false;
		   			if(pipeIndex + 1 < chainList.size() - 1){
		   				// not end segment
		   				PipeFitting pfEnd = chainList.get(pipeIndex + 1);
		   				PipeFitting pfNextEnd = chainList.get(pipeIndex + 2);
		   				if((pipeIndex == 0 || startPf.getCouplingContinuation())
		   						&& pfEnd.getCouplingContinuation()
		   						&&((pipeIndex + 2 == chainList.size() - 1) || pfNextEnd.getCouplingContinuation())){
		   					deferTakeout = true;
		   				}
		   			}
		   			
		   			if(deferTakeout){
		   				deferredTakeout += takeout;
		   				span -= takeout;
		   				takeout = 0;
		   			}else{
		   				takeout += deferredTakeout;
		   				span += deferredTakeout;
		   				deferredTakeout = 0;
		   			}
		   			
		   			// calc and record span
		   			long roundedSpan = Math.round(totalSpan + span - sumOfRoundedSpans);
		   			BigDecimal bdSpan = new BigDecimal(roundedSpan);
		   			pipe.setSpan(bdSpan);
		   			
		   			// calc and record after-takeout span
		   			BigDecimal bdAfter = null;
		   			if(!isBreakStart && !isBreakEnd){
		   				bdAfter = bdSpan;	// no takeout to report	   			
				   		pipe.setAfterTakeout(bdAfter);
		   			}else if(isBreakStart && isBreakEnd){
		   				// combine both takeouts. round after only if not zero
			   			double after = span - takeout;
			   			if(takeout == 0){
			   				bdAfter = bdSpan;	// no takeout to report	  
			   			}else if(pipe.getDesignation() == Designation.Main && roundScale > 0){
			   				bdAfter = (new BigDecimal(Math.round(roundFactor*after))).divide(bdRoundFactor);
			   			}else{
			   				bdAfter = new BigDecimal(Math.round(after));
			   			}
				   		pipe.setAfterTakeout(bdAfter);
		   			}else if(isBreakStart && !isBreakEnd){
		   				// save state for later - after will be calculated at the end
						breakStartSpan = span;
						breakStartTakeout = takeout;
						breakStartPipe = pipe;
						breakStartRoundedSpan = roundedSpan;
		   			} else{ // end of unbroken sub-chain
		   				// calculate chain-takeout
		   				double chainTakeout = breakStartTakeout + takeout;
		   				if(chainTakeout == 0){
		   					// no takeout to distribute
		   					breakStartPipe.setAfterTakeout(breakStartPipe.getSpan());
		   					pipe.setAfterTakeout(pipe.getSpan());
		   				}else{
		   					long sumOfRoundedMiddleSpans = sumOfRoundedSpans - breakStartRoundedSpan;
		   					// calculate chain-after
		   					BigDecimal bdChainAfter = null;
		   					double chainAfter = totalSpan + span - chainTakeout;
		   					if(pipe.getDesignation() == Designation.Main && roundScale > 0){
		   						bdChainAfter = (new BigDecimal(Math.round(roundFactor*chainAfter))).divide(bdRoundFactor);
				   			}else{
				   				bdChainAfter = new BigDecimal(Math.round(chainAfter));
				   			}
		   					BigDecimal bdStartEndAfter = bdChainAfter.subtract(new BigDecimal(sumOfRoundedMiddleSpans));
		   					// extremity with smaller takeout (usually 0) needs to reported first
		   					// the other one will be derived from chain-takeout
		   					if(breakStartTakeout < takeout){
		   						BigDecimal bdBreakStartAfter = null;
		   						if(breakStartTakeout == 0){
		   							bdBreakStartAfter = breakStartPipe.getSpan();
		   						}else{
		   							double breakStartAfter = breakStartSpan - breakStartTakeout;
				   					if(pipe.getDesignation() == Designation.Main && roundScale > 0){
				   						bdBreakStartAfter = (new BigDecimal(Math.round(roundFactor*breakStartAfter))).divide(bdRoundFactor);
						   			}else{
						   				bdBreakStartAfter = new BigDecimal(Math.round(breakStartAfter));
						   			}
		   						}
			   					breakStartPipe.setAfterTakeout(bdBreakStartAfter);
			   					
			   					BigDecimal bdBreakEndAfter = bdStartEndAfter.subtract(bdBreakStartAfter);
			   					pipe.setAfterTakeout(bdBreakEndAfter);
		   					}else{ // end takeout is smaller
		   						BigDecimal bdBreakEndAfter = null;
		   						if(takeout == 0){
		   							bdBreakEndAfter = pipe.getSpan();
		   						}else{
		   							double breakEndAfter = span - takeout;
				   					if(pipe.getDesignation() == Designation.Main && roundScale > 0){
				   						bdBreakEndAfter = (new BigDecimal(Math.round(roundFactor*breakEndAfter))).divide(bdRoundFactor);
						   			}else{
						   				bdBreakEndAfter = new BigDecimal(Math.round(breakEndAfter));
						   			}
		   						}
			   					pipe.setAfterTakeout(bdBreakEndAfter);
			   					
			   					BigDecimal bdBreakStartAfter = bdStartEndAfter.subtract(bdBreakEndAfter);
			   					breakStartPipe.setAfterTakeout(bdBreakStartAfter);
		   					}
		   				}
				   		if(breakStartPipe.getAfterTakeout().compareTo(CommonDecimal.Zero.getMeasure()) <= 0){
				   			breakStartPipe.setIgnored(true);
				   		}
		   			}
			   		
			   		// set ignore flag
			   		if(pipe.getAfterTakeout() != null && pipe.getAfterTakeout().compareTo(CommonDecimal.Zero.getMeasure()) <= 0){
			   			pipe.setIgnored(true);
			   		}

			   		if(isBreakEnd){
				   		// end of unbroken chain: rest totals
			   			sumOfRoundedSpans = 0;
			   			totalSpan = 0;
			   		}else{
			   			// middle of unbroken chain: accumulate
			   			sumOfRoundedSpans += roundedSpan;
			   			totalSpan += span;			   			
			   		}
				}
    		}
    	}
        logger.info("-calcSpanAndTakeout");
    }
/*    
    private BigDecimal round(double val, int roundScale){
    	if(roundScale == 0){
    		return new BigDecimal(Math.round(val));
    	}
    	return (new BigDecimal(Math.round(val*Math.pow(10, roundScale)))).movePointLeft(roundScale);
    }
*/
    private boolean isChainStart(Pipe pipe){
    	return isInitialGroovedPipe(pipe)
    		&& !getStartFitting(pipe).getCouplingContinuation();
    }
    private boolean isChainEnd(Pipe pipe){
    	return isTerminalGroovedPipe(pipe)
    		&& !getEndFitting(pipe).getCouplingContinuation();
    }
    // gets the pipe between i-th and (i+1)-st fitting in chain
    public Pipe getPipeFromChain(List<PipeFitting> chainList, int i){
    	if(i < 0 || i >= chainList.size() - 1){
    		return null;
    	}
    	PipeFitting pf1 = chainList.get(i);
    	PipeFitting pf2 = chainList.get(i + 1);
    	return this.getPipeGraph().getEdge(pf1, pf2);
    }
    // calculates maximal list of chain extremes which does not include both ends of a chain
    private void calcChainStarts() {
    	allChainStarts = new HashMap<PipeFitting, Set<Direction>>();
    	Set<Pipe> excludes = new HashSet<Pipe>();
		for(Pipe p : getPipeGraph().edgeSet()){
			if(p.isVertical() || p.getDesignation() == Designation.Head){
				continue; // not horizontal pipe
			}
			if(excludes.contains(p)){
				continue;
			}
			PipeFitting pf = null;
			if(isChainStart(p)){
				pf = this.getStartFitting(p);
			}
			if(pf == null && isChainEnd(p)){
				pf = this.getEndFitting(p);
			}
			if(pf != null){
				PipeAttachment pa =  this.getAttachmentToFitting(p, pf);
				Direction direction = pa.getDirectionInFitting();
				//TODO: add pipe on the other end to exclude list
				List<PipeFitting> chainList = getPipeFittingList();
				chainList.clear();
				fillChainList(chainList, pf, direction);
				if(chainList.size() == 2){
					// single-item chain
					pf = this.getStartFitting(p);
				}else{
					Pipe pLast = getPipeFromChain(chainList, chainList.size() - 2);
					boolean swap = false;
					if(isChainStart(p) != isChainStart(pLast)){
						// determined based on direction: from start to end
						swap = isChainStart(pLast);
					}else if(p.getDesignation() != pLast.getDesignation()){
						// determined based on designation: from main to branch
						swap = (pLast.getDesignation() == Designation.Main);
					}else if(p.getDiameter() != pLast.getDiameter()){
						// determined based on diameter from bigger to smaller
						swap = (pLast.getDiameter().compareTo(p.getDiameter()) > 0);
					}
					if(swap){
						excludes.add(p);
						p = pLast;
						pf = chainList.get(chainList.size() - 1);
					}else{
						excludes.add(pLast);
					}
				}
				Set<Direction> directions = allChainStarts.get(pf);
				if(directions == null){
					directions = new HashSet<Direction>(4);
					allChainStarts.put(pf, directions);
				}
				direction = this.getAttachmentToFitting(p, pf).getDirectionInFitting();
				directions.add(direction);
			}
		}
    }
    
    // Traverse the longest coupling chain starting from this fitting
    // and continuing in the specified direction.
	public void fillChainList(final List<PipeFitting> chainList, PipeFitting pf, Direction direction){
		 fillChainList(chainList, pf, direction, true);
	}
    public void fillChainList(final List<PipeFitting> chainList, PipeFitting pf, Direction direction, boolean includeCouplings){
		chainList.add(pf); // add initial fitting
		boolean isLast = false;
		while(!isLast){
			Pipe pipe = getPipe(pf, direction);
			if(pipe.getDesignation() == Designation.Head){
				break;
			}
			if(pf == this.getStartFitting(pipe)){
				isLast = includeCouplings ? isChainEnd(pipe) : isTerminalGroovedPipe(pipe);
				pf = this.getEndFitting(pipe);
			}else{ // pf is end fitting
				isLast = includeCouplings ? isChainStart(pipe) : this.isInitialGroovedPipe(pipe);
				pf = this.getStartFitting(pipe);
			}
			chainList.add(pf);
			PipeAttachment pa = this.getAttachmentToFitting(pipe, pf);
			direction = pa.getDirectionInFitting().getOpposite();
		}
	}


	/**
	 * Checks if a pipe continues (un-broken) beyond one of its extreme vertices.
	 * @param areaBody the area
	 * @param pipe the pipe
	 * @param pf the vertex
	 * @return <code>true</code> if it does not continue
	 */
    public boolean isExtremeGroovedPipe(final Pipe pipe, final PipeFitting pf){
		 PipeFitting startPf = getStartFitting(pipe);
		 Fitting f = pf.getFitting();
         if(!f.isHoleBased()){
        	 return true;
         }
         // mechanical or welded.
         // find how is 'pipe' attached to fitting 'f'.
         // main run will be E-W
         PipeAttachment pa = (pf == startPf) ? pipe.getStartAttachment()
        		 : pipe.getEndAttachment();
		 Direction pd = pa.getDirectionInFitting();
		 return !pd.equalOrOpposite(Direction.E);
		 /*
         if(f.getType().getEndCount() == 3
        		 || f.getDiameterList().size() > 1){
        	 return !pd.equalOrOpposite(Direction.E);
         }
         // must be welded cross with equal diameters.
         // we just need a way to choose main-run.
         // find first incoming(?)
         int firstIndex = getEdgesInOrder().size();
         Direction firstDirection = null;
         for(Direction d : f.getType().getDirections()){
        	 Pipe p = getPipe(pf, d);
        	 PipeFitting end = getEndFitting(p);
        	 if(end != pf){
        		 continue;
        	 }
        	 // incoming
        	 int index = getEdgesInOrder().indexOf(p);
        	 if(index < firstIndex){
        		 firstIndex = index;
        		 firstDirection = d;
        	 }
         }
    	 return !pd.equalOrOpposite(firstDirection);
    	 */
    }

    /**
     * Checks if a pipe continues (un-broken) beyond its start vertex.
     * @param areaBody the area
     * @param pipe the pipe
     * @return <code>true</code> if it does not continue
     */
    public boolean isInitialGroovedPipe(final Pipe pipe){
        return isExtremeGroovedPipe(pipe, getStartFitting(pipe));
     }
 /**
  * Checks if a pipe continues (un-broken) beyond its end vertex.
  * @param areaBody the area
  * @param pipe the pipe
  * @return <code>true</code> if it does not continue
  */
     public boolean isTerminalGroovedPipe(final Pipe pipe){
 	       return isExtremeGroovedPipe(pipe, getEndFitting(pipe));
 	}
     
     public static class HeadInfo {
 		private Pipe pipe;
    	private Jump jumpLocation;
    	
    	 public Pipe getPipe() {
			return pipe;
		}
		public void setPipe(Pipe pipe) {
			this.pipe = pipe;
		}
		public Jump getJumpLocation() {
			return jumpLocation;
		}
		public void setJumpLocation(Jump jumpLocation) {
			this.jumpLocation = jumpLocation;
		}
     }

	/**
	 * Collection of properties related to one point.
	 * @author janh
	 *
	 */
	public static class PointInfo{
		private List<DwgEntity> jumps;
		private List<DwgEntity> heads;
		private List<DwgEntity> couplings;
		private List<DwgEntity> blocks;
		private List<PipeFitting> fittings;
		private int headCount;
		private Defect status = Defect.noDefects;

		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("status=").append(status);
			sb.append("\nheadCount=").append(headCount);
			return sb.toString();
		}



		public List<DwgEntity> getJumps() {
			if(jumps == null){
				jumps = new ArrayList<DwgEntity>();
			}
			return jumps;
		}
		public DwgEntity getBlock(){
			return (blocks == null || blocks.isEmpty()) ? null : blocks.get(0);
		}
		public DwgEntity getJump(){
			return (jumps == null || jumps.isEmpty()) ? null : jumps.get(0);
		}
		public DwgEntity getHead(){
			return (heads == null || heads.isEmpty()) ? null : heads.get(0);
		}
		public DwgEntity getCoupling(){
			return (couplings == null || couplings.isEmpty()) ? null : couplings.get(0);
		}
		public void setJumps(final List<DwgEntity> jumps) {
			this.jumps = jumps;
		}
		public List<DwgEntity> getHeads() {
			if(heads == null){
				heads = new ArrayList<DwgEntity>();
			}
			return heads;
		}
		public void setHeads(final List<DwgEntity> heads) {
			this.heads = heads;
		}
		public List<DwgEntity> getCouplings() {
			if(couplings == null){
				couplings = new ArrayList<DwgEntity>();
			}
			return couplings;
		}
		public void setCouplings(final List<DwgEntity> couplings) {
			this.couplings = couplings;
		}
		public int getHeadCount() {
			return headCount;
		}
		public void setHeadCount(final int headCount) {
			this.headCount = headCount;
		}
		public List<PipeFitting> getFittings() {
			if(fittings == null){
				fittings = new ArrayList<PipeFitting>();
			}
			return fittings;
		}
		public void setFittings(final List<PipeFitting> fittings) {
			this.fittings = fittings;
		}

		public Defect getStatus() {
			return status;
		}

		public void setStatus(final Defect status) {
			this.status = status;
		}



		public List<DwgEntity> getBlocks() {
			if(blocks == null){
				blocks = new ArrayList<DwgEntity>();
			}
			return blocks;
		}



		public void setBlocks(final List<DwgEntity> blocks) {
			this.blocks = blocks;
		}
	}
	/**
	 * Entity-related issue description
	 * @author janh
	 *
	 */
	public enum Defect {
		noDefects,
		zeroLengthPipe,
		loopInBranch,
		cycleInBranch,
		wrongBranchLayer,
		isolatedPoint,
        missingHead,
        wrongSidewallLocation,
        missingJump,
        missingCoupling,
        headCoupling,
        wrongBranchEnd,
        endCoupling,
        jumpCoupling,
        overlapingSymbols,
        unknownBlock,
        endJump,
        pipeOverlap,
        tooManyPipes,
        tooManyVendors,
        cannotDetermineAttachment,
        cannotCut,
        possibleRaiser;
	}

	public UndirectedGraph<PipeFitting, Pipe> getPipeGraph() {
		if(pipeGraph == null){
			pipeGraph = new SimpleGraph<PipeFitting, Pipe>(Pipe.class);
		}
		return pipeGraph;
	}

	public void setPipeGraph(final UndirectedGraph<PipeFitting, Pipe> pipeGraph) {
		this.pipeGraph = pipeGraph;
	}

	public void setRaiser(final PipeFitting raiser) {
		this.raiser = raiser;
	}

	public Map<PipeFitting, Set<Direction>> getGroovedChainStarts() {
		return groovedChainStarts;
	}

	public Map<PipeFitting, Set<Direction>> getThreadedChainStarts() {
		return threadedChainStarts;
	}
	public Map<PipeFitting, Set<Direction>> getAllChainStarts() {
		if(allChainStarts == null){
			this.calcChainStarts();
		}
		return allChainStarts;
	}
	/**
	 * Finds the extremity of a pipe which starts a coupling chain.
	 * @param pipe the pipe
	 * @return the extremity of <code>null</code> if the pipe is not a chain start
	 */
	public PipeFitting getChainStart(Pipe pipe){
		PipeFitting source = getStartFitting(pipe);
		PipeFitting term = getEndFitting(pipe);
		Direction sourceDir = getAttachmentToFitting(pipe, source).getDirectionInFitting();
		Direction termDir = getAttachmentToFitting(pipe, term).getDirectionInFitting();
		PipeFitting chainStart = null;
		if(getAllChainStarts().containsKey(source)
				&& getAllChainStarts().get(source).contains(sourceDir)){
			chainStart = source;
		}
		else if(getAllChainStarts().containsKey(term)
				&& getAllChainStarts().get(term).contains(termDir)){
			chainStart = term;
		}
		return chainStart;
	}

	public void setGroovedChainStarts(
			Map<PipeFitting, Set<Direction>> groovedChainStarts) {
		this.groovedChainStarts = groovedChainStarts;
	}

	public void setThreadedChainStarts(
			Map<PipeFitting, Set<Direction>> threadedChainStarts) {
		this.threadedChainStarts = threadedChainStarts;
	}

}
