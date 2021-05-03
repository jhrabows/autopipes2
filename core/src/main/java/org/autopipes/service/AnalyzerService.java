package org.autopipes.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.autopipes.model.AreaBody;
import org.autopipes.model.AreaBody.Defect;
import org.autopipes.model.AreaBody.PointInfo;
import org.autopipes.model.AreaCutSheet;
import org.autopipes.model.AreaCutSheet.BranchInfo;
import org.autopipes.model.AreaCutSheet.CutSheetInfo;
import org.autopipes.model.AreaCutSheet.MainCutSheetInfo;
import org.autopipes.model.AreaCutSheet.OutletInfo;
import org.autopipes.model.AreaOptions;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.DrawingArea.Readiness;
import org.autopipes.model.DrawingLayer;
import org.autopipes.model.DrawingLayer.Designation;
import org.autopipes.model.DrawingOptions;
import org.autopipes.model.DwgAng;
import org.autopipes.model.DwgEntity;
import org.autopipes.model.DwgEntity.AcClass;
import org.autopipes.model.DwgPoint;
import org.autopipes.model.FloorDrawing;
import org.autopipes.model.Pipe;
import org.autopipes.model.PipeAttachment;
import org.autopipes.model.PipeConfig;
import org.autopipes.model.PipeFitting;
import org.autopipes.model.PipeFitting.Jump;
import org.autopipes.takeout.Attachment;
import org.autopipes.takeout.Diameter;
import org.autopipes.takeout.Fitting;
import org.autopipes.takeout.Fitting.Direction;
import org.autopipes.takeout.Fitting.Type;
import org.autopipes.takeout.TakeoutRepository;
import org.autopipes.takeout.Vendor;
import org.autopipes.util.CollectionComparator;
import org.autopipes.util.CommonDecimal;
import org.autopipes.util.GraphUtils;
import org.autopipes.util.PlaneGeo;
import org.autopipes.util.PlaneGeo.Divider;
import org.autopipes.util.PlaneGeo.Point;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class AnalyzerService {
	private static Logger logger = Logger.getLogger(AnalyzerService.class);

    private PlaneGeo planeGeo;
    private TakeoutRepository takeout;
    private Fitting.Factory fittingFactory;
    private PipeConfig pipeConfig;
   	private PipeCollectionComparator pipeCollectionComparator;
   	private UndirectedGraph<Pipe, DefaultEdge> parallelGraph;
	private Set<Pipe> verticalSet;
	private Set<Pipe> sidewallSet;
   	private List<Pipe> parallelEntityList;
   	private CollectionComparator<Pipe> pipeSetCollectionComparator;
	private PipeFitting[] pipeFittingTripple;
   	private Set<DrawingLayer> drawingLayerSet;
   	
/*   	public List<Point> getThreadCutList() {
   		if(threadCutList == null){
   			threadCutList = new ArrayList<Point>();
   		}
		return threadCutList;
	}*/

	private List<Pipe> pipeList;

	public List<Pipe> getPipeList() {
		if(pipeList == null){
		   	pipeList = new ArrayList<Pipe>();
		}
		return pipeList;
	}
    private List<PipeFitting> pipeFittingList;

	public List<PipeFitting> getPipeFittingList() {
		if(pipeFittingList == null){
			pipeFittingList = new ArrayList<PipeFitting>();
		}
		return pipeFittingList;
	}
/*
	public Map<Point, List<Point>> getSubMap() {
		if(subMap == null){
			subMap = new LinkedHashMap<Point, List<Point>>();
		}
		return subMap;
	}
	*/
/*
	public List<Pipe> getPipeSubdivision() {
		if(pipeSubdivision == null){
			pipeSubdivision = new ArrayList<Pipe>();
		}
		return pipeSubdivision;
	}
	*/
/*
	public List<PipeFitting> getAvoidedPoints() {
		if(avoidedPoints == null){
			avoidedPoints = new ArrayList<PipeFitting>();
		}
		return avoidedPoints;
	}
*/
	public Set<DrawingLayer> getDrawingLayerSet() {
		if(drawingLayerSet == null){
			drawingLayerSet = new HashSet<DrawingLayer>();
		}
		return drawingLayerSet;
	}

	public PipeFitting[] getPipeFittingTripple() {
		if(pipeFittingTripple == null){
			pipeFittingTripple = new PipeFitting[3];
		}
		return pipeFittingTripple;
	}

	public CollectionComparator<Pipe> getPipeSetCollectionComparator() {
		if(pipeSetCollectionComparator == null){
			pipeSetCollectionComparator = new CollectionComparator<Pipe>();
		}
		return pipeSetCollectionComparator;
	}

	public List<Pipe> getParallelEntityList() {
		if(parallelEntityList == null){
			parallelEntityList = new ArrayList<Pipe>();
		}
		return parallelEntityList;
	}

	public Set<Pipe> getVerticalSet() {
   		if(verticalSet == null){
   			verticalSet = new HashSet<Pipe>();
   		}
		return verticalSet;
	}

	public Set<Pipe> getSidewallSet() {
		if(sidewallSet == null){
			sidewallSet = new HashSet<Pipe>();
		}
		return sidewallSet;
	}


    public UndirectedGraph<Pipe, DefaultEdge> getParallelGraph() {
    	if(parallelGraph == null){
    		parallelGraph = new SimpleGraph<Pipe, DefaultEdge>(DefaultEdge.class);
    	}
		return parallelGraph;
	}

	public PipeCollectionComparator getPipeCollectionComparator() {
    	if(pipeCollectionComparator == null){
    		pipeCollectionComparator = new PipeCollectionComparator();
    	}
		return pipeCollectionComparator;
	}
	/**
	 * Returns full list of grooved pipes which start is not hole-based
	 * and is not a grooved coupling which joins 2 grooved pipes.
	 * The list is in a form of vertex/direction pairs so is unchanged
	 * by partitions.
	 * @param dwg
	 * @param areaBody
	 * @return the list
	 */
    private Map<PipeFitting, Set<Direction>> getGroovedChainStarts(FloorDrawing dwg, AreaBody areaBody) {
    	if(areaBody.getGroovedChainStarts() == null){
    		calcChainStarts(dwg, areaBody);
    	}
    	return areaBody.getGroovedChainStarts();
    }
	/**
	 * @param dwg
	 * @param areaBody
	 * @return the list
	 */
    private Map<PipeFitting, Set<Direction>> getThreadedChainStarts(FloorDrawing dwg, AreaBody areaBody) {
    	if(areaBody.getThreadedChainStarts() == null){
    		calcChainStarts(dwg, areaBody);
    	}
    	return areaBody.getThreadedChainStarts();
    }
    /**
     * Checks if this vertex is a middle of a coupling chain. This is a case if
     * a coupling joins 2 threaded or 2 non-threaded. In the latter case we also
     * require that the coupling be non-threaded.
     * @param dwg the drawing
     * @param areaBody the area
     * @param pf the vertex
     * @return <code>true</code> if it is
     */
    private boolean isCouplingContinuation(FloorDrawing dwg, AreaBody areaBody, PipeFitting pf){
		if(pf.getFitting().getType() != Type.Coupling
				&& pf.getFitting().getType() != Type.Reducer){
			return false; // not a coupling or reducer
		}
		DrawingLayer firstLayer = null;
		for(Pipe p :areaBody.getPipeGraph().edgesOf(pf)){
			if(p.getDesignation() == Designation.Head){
				return false; // pipe ended with horizontal head
			}
			DrawingLayer layer = dwg.getOptionsRoot().findLayer(p.getLayerName());
			if(firstLayer == null){
				firstLayer = layer;
			}else{
				// 2nd iteration
				if((layer.getType() == Designation.Branch || layer.getSubType() == Attachment.threaded)
						&& (firstLayer.getType() == Designation.Branch || firstLayer.getSubType() == Attachment.threaded)){
					return true; // branch or threaded main on both sides
				}
				if(layer.getType() == Designation.Branch || layer.getSubType() == Attachment.threaded
						|| firstLayer.getType() == Designation.Branch || firstLayer.getSubType() == Attachment.threaded){
					return false; // branch or threaded main meets non-threaded
				}
				if(pf.getFitting().getAttachment() == Attachment.grooved){
					return true; // 2 grooved meeting at a grooved coupling
				}
				return false; // 2 grooved meeting at threaded
			}	
		}
		throw new IllegalArgumentException("Unable to check coupling configuration");
    }

    /**
     * Finds horizontal pipes which starts are true ends (not hole-based)
     * and are not coupling continuations. The pipes are collected into
     * a threaded and non-threaded list.
     */
    
    private void calcChainStarts(FloorDrawing dwg, AreaBody areaBody) {
    	Map<PipeFitting, Set<Direction>> groovedChainStarts
    		= new HashMap<PipeFitting, Set<Direction>>();
    	Map<PipeFitting, Set<Direction>>  threadedChainStarts
    		= new HashMap<PipeFitting, Set<Direction>>();

		for(Pipe p : areaBody.getPipeGraph().edgeSet()){
			if(p.isVertical() || p.getDesignation() == Designation.Head){
				continue; // not horizontal pipe
			}
			PipeFitting start = areaBody.getStartFitting(p);
			if(isCouplingContinuation(dwg, areaBody, start)){
				continue;
			}
			DrawingLayer layer = dwg.getOptionsRoot().findLayer(p.getLayerName());
			Map<PipeFitting, Set<Direction>> targetMap = null;
			if(layer.getType() == Designation.Main
					&& layer.getSubType() != Attachment.threaded){
				 // grooved or welded main
				if(!areaBody.isInitialGroovedPipe(p)){
					continue; // extension of grooved across hole
				}
				targetMap = groovedChainStarts;
			}
			else{ 
				 // branch or threaded main
				targetMap = threadedChainStarts;
			}
			Set<Direction> directions = targetMap.get(start);
			if(directions == null){
				directions = new HashSet<Direction>(4);
				targetMap.put(start, directions);
			}
			directions.add(p.getStartAttachment().getDirectionInFitting());
		}
		areaBody.setGroovedChainStarts(groovedChainStarts);
		areaBody.setThreadedChainStarts(threadedChainStarts);
	}
/*
    private void calcThreadTakout(FloorDrawing dwg, AreaBody areaBody){
    	for(Map.Entry<PipeFitting, Set<Direction>> chainStart
    			: getThreadedChainStarts(dwg, areaBody).entrySet()){
    		for(Direction dir : chainStart.getValue()){
    			// get pipe which starts the chain
    			Pipe pipe = areaBody.getPipe(chainStart.getKey(), dir);
				boolean endOfChain = false;
				double chainTakeout = 0;
				// iterate the chain
    			while(!endOfChain){
    				endOfChain = !isCouplingContinuation(dwg, areaBody,
    							areaBody.getEndFitting(pipe));
					BigDecimal startTakeout = pipe.getStartAttachment().getTakeout();
					BigDecimal endTakeout = pipe.getEndAttachment().getTakeout();
					chainTakeout += (startTakeout == null) ? 0 : startTakeout.doubleValue();
					chainTakeout += (endTakeout == null) ? 0 : endTakeout.doubleValue();
					
    				pipe.setTakeout((endOfChain ? new BigDecimal(chainTakeout)
    					: CommonDecimal.Zero.getMeasure()));
					PipeFitting pf = areaBody.getEndFitting(pipe);
					pipe = areaBody.getContinuationPipe(pipe, pf);
    			}
    		}
    	}
    }
    */
    /**
     * Assigns takeout attribute on each segment.
     * For coupling chains the combined takeout is assigned to the last segment
     * @param dwg
     * @param area
     */
    /*
    private void calcMainTakout(FloorDrawing dwg, DrawingArea area){
    	AreaBody areaBody = area.getAreaBody();
    	AreaOptions areaOptions = area.getAreaOptions();
    	int roundScale = areaOptions.getTakeoutRounding();
    	long roundFactor = Math.round(Math.pow(10, roundScale));
    	for(Map.Entry<PipeFitting, Set<Direction>> chainStart
    			: getGroovedChainStarts(dwg, areaBody).entrySet()){
    		for(Direction dir : chainStart.getValue()){
    			// get pipe which starts the chain
    			Pipe pipe = areaBody.getPipe(chainStart.getKey(), dir);
		//		boolean startOfChain = true;
				boolean endOfChain = false;
				double deferredTakeout = 0;
				// iterate the chain
    			while(!endOfChain){
    				double startTakeout = pipe.getStartAttachment().getTakeout().doubleValue();
					double endTakeout = pipe.getEndAttachment().getTakeout().doubleValue();
    				endOfChain = areaBody.isTerminalGroovedPipe(pipe)
    					&& !isCouplingContinuation(dwg, areaBody,
    							areaBody.getEndFitting(pipe));
    				long takeout = 0;
    				BigDecimal bdTakeout;
    				if(!endOfChain){
	    				takeout = Math.round(startTakeout + endTakeout);
	    				deferredTakeout += (startTakeout + endTakeout
	    						- takeout);
	    				bdTakeout = new BigDecimal(takeout);
    				}else{
						takeout = Math.round(roundFactor*(startTakeout + endTakeout
								+ deferredTakeout));
	    				bdTakeout = (new BigDecimal(takeout)).movePointLeft(roundScale);
    				}
    				pipe.setTakeout(bdTakeout);
					PipeFitting pf = areaBody.getEndFitting(pipe);
					pipe = areaBody.getContinuationPipe(pipe, pf);
    			}
    		}
    	}
    }
    */
    /*
    private void calcMainTakout2(FloorDrawing dwg, DrawingArea area){
    	AreaBody areaBody = area.getAreaBody();
    	AreaOptions areaOptions = area.getAreaOptions();
    	int roundScale = areaOptions.getTakeoutRounding();
    	long roundFactor = Math.round(Math.pow(10, roundScale));
    	
    	// iterate over starts
    	for(Map.Entry<PipeFitting, Set<Direction>> chainStart
    			: areaBody.getAllChainStarts().entrySet()){
    		
    		// iterate over chains
    		for(Direction dir : chainStart.getValue()){
    			// get pipe which starts the chain
    	//		Pipe pipe = areaBody.getPipe(chainStart.getKey(), dir);
		//		boolean startOfChain = true;
		//		boolean endOfChain = false;
				double deferredTakeout = 0;
				
				
				List<PipeFitting> chainList = this.getPipeFittingList();
				chainList.clear();
				areaBody.fillChainList(chainList, chainStart.getKey(), dir);
				
				// iterate the chain
				for(int pipeIndex = 0; pipeIndex < chainList.size() - 1; pipeIndex++){
		   			Pipe pipe = areaBody.getPipeFromChain(chainList, pipeIndex);

		   			BigDecimal bdStartTakeout = areaBody.getAttachmentToFitting(pipe, chainList.get(pipeIndex)).getTakeout();
		   			double startTakeout = (bdStartTakeout == null) ? 0 : bdStartTakeout.doubleValue();
		   			BigDecimal bdEndTakeout = areaBody.getAttachmentToFitting(pipe, chainList.get(pipeIndex + 1)).getTakeout();
		   			double endTakeout = (bdEndTakeout == null) ? 0 : bdEndTakeout.doubleValue();
		   			double takeout = startTakeout + endTakeout;
		   			PipeFitting startPf = chainList.get(pipeIndex);
		   			PipeFitting endPf = chainList.get(pipeIndex + 1);
		   			double span = PlaneGeo.distance(startPf, endPf);
	    		//	while(!endOfChain){
		   			
		   			// if start is a break (start or coupling), end is a coupling and following end is a break (coupling or end)
		   			// then takeout will be deferred
		   			boolean deferTakeout = false;
		   			if(pipeIndex + 1 < chainList.size() - 1){
		   				// not end segment
		   				PipeFitting pfEnd = chainList.get(pipeIndex + 1);
		   				PipeFitting pfNextEnd = chainList.get(pipeIndex + 2);
		   				if( (pipeIndex == 0 || startPf.getCouplingContinuation())
		   						&& pfEnd.getCouplingContinuation()
		   							&&((pipeIndex + 2 == chainList.size() - 1)) || pfNextEnd.getCouplingContinuation()){
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
		   			BigDecimal bdSpan = new BigDecimal(Math.round(span));
		   			pipe.setSpan(bdSpan);
		   			BigDecimal bdTakeout = (pipe.getDesignation() == Designation.Main && roundScale > 0)?
		   				(new BigDecimal(Math.round(roundFactor*takeout))).movePointLeft(roundScale)
			   			: new BigDecimal(Math.round(takeout));
    				pipe.setTakeout(bdTakeout);
           			if(bdTakeout.compareTo(bdSpan) >= 0
           					|| startPf.getFitting().getType() == Type.Raiser){
           				pipe.setIgnored(true);
           			}   			
				}
    		}
    	}
    }
    */

	/**
	 * Make sure that individual pieces add up
	 */
	public void correctMainSpan(FloorDrawing dwg, AreaBody areaBody){
    	for(Map.Entry<PipeFitting, Set<Direction>> chainStart
    			: getGroovedChainStarts(dwg, areaBody).entrySet()){
    		for(Direction dir : chainStart.getValue()){
    			// get pipe which starts the chain
    			Pipe pipe = areaBody.getPipe(chainStart.getKey(), dir);
				boolean endOfChain = false;
				BigDecimal totalSpan = CommonDecimal.Zero.getMeasure();
				Pipe maxLenghPiece = null;
				PipeFitting startOfGroovedPipe = chainStart.getKey();
				// iterate the chain
    			while(!endOfChain){
    				boolean endOfGroovedPipe = areaBody.isTerminalGroovedPipe(pipe);
    				endOfChain = endOfGroovedPipe && !isCouplingContinuation(dwg, areaBody,
    							areaBody.getEndFitting(pipe));
    				totalSpan = totalSpan.add(pipe.getSpan());
    				if(!endOfGroovedPipe){
    					if(maxLenghPiece == null){
    						maxLenghPiece = pipe;
    					}else{
    						if(pipe.getSpan().compareTo(maxLenghPiece.getSpan()) > 0){
    							maxLenghPiece = pipe;
    						}
    					}
    				}else{
   			    		PipeFitting end = areaBody.getEndFitting(pipe);
   			    	    if(maxLenghPiece != null){
    			    		double d = PlaneGeo.distance(startOfGroovedPipe, end);
    			    		BigDecimal trueSpan = new BigDecimal(Math.round(d));
    			    		BigDecimal delta = trueSpan.subtract(totalSpan);
    			    		if(delta.compareTo(CommonDecimal.Zero.getMeasure()) != 0){
    			    			logger.debug("correcting span by " + delta);
    			    			BigDecimal oldSpan = maxLenghPiece.getSpan();
    			    			maxLenghPiece.setSpan(oldSpan.add(delta));
    			    		}
    					}
    				// reset for next threaded pipe
    					totalSpan = CommonDecimal.Zero.getMeasure();
    					maxLenghPiece = null;
    					startOfGroovedPipe = end;
    				}
					PipeFitting pf = areaBody.getEndFitting(pipe);
					pipe = areaBody.getContinuationPipe(pipe, pf);
    			}
    		}
    	}
	}

    
    private void subdivideThreaded(FloorDrawing dwg, DrawingArea area){
    	 AreaBody areaBody = area.getAreaBody();
    	 long longPipe = area.getAreaOptions().getLongPipe();
    	// initialize divider
    	Divider divider = new Divider(this.getPlaneGeo());
    	divider.setMaxSize(longPipe);
    	if(area.getAreaOptions().getBranchCutList().isEmpty()){
    		return; // no size specified
    	}
    	divider.setThreadedCutSize(area.getAreaOptions().getBranchCutList().get(0).longValue());
    	// iterate over chain starts
    	for(Map.Entry<PipeFitting, Set<Direction>> chainStart
    			: getThreadedChainStarts(dwg, areaBody).entrySet()){
    		for(Direction dir : chainStart.getValue()){
    			// get pipe which starts the chain
    			Pipe nextInChain = areaBody.getPipe(chainStart.getKey(), dir);
				boolean endOfChain = false;
				// iterate the chain
				double chainTakeout = 0;
				while(!endOfChain){
					Pipe pipe = nextInChain;
					BigDecimal startTakeout = pipe.getStartAttachment().getTakeout();
					BigDecimal endTakeout = pipe.getEndAttachment().getTakeout();
					chainTakeout += (startTakeout == null) ? 0 : startTakeout.doubleValue();
					chainTakeout += (endTakeout == null) ? 0 : endTakeout.doubleValue();
    				PipeFitting start = areaBody.getStartFitting(pipe);    				
    				PipeFitting end = areaBody.getEndFitting(pipe);
					endOfChain = !isCouplingContinuation(dwg, areaBody, end);
					if(!endOfChain){
						divider.subdivideThreaded(start, end);
						// continue iteration
						nextInChain = areaBody.getContinuationPipe(pipe, end);
					}else{
						// figure out the coupling that might be used for this pipe
						Pipe[] pipes = {pipe, pipe};
				    	Fitting f = fittingFactory.instanceOf(Type.Coupling, Attachment.threaded, null,
				    			Arrays.asList(pipes), null);
				    	double cutTakeout = 2*takeout.locateTakeout(f, Direction.E).doubleValue();
				    	divider.subdivideLastThreaded(start, end, chainTakeout, cutTakeout);
					}
					if(!divider.getCutList().isEmpty()){
					    List<PipeFitting> breakups = areaBody.subdivideGraph(
					    		 pipe, divider.getCutList());
					    updateBreakups(dwg.getOptionsRoot(), areaBody, pipe, start, end, breakups);
					    if(!endOfChain){
					    	// add takeouts for created cuts to the total
						    if(!breakups.isEmpty()){
						    	Fitting f = breakups.get(0).getFitting();
						    	double cutTakeout = takeout.locateTakeout(f, Direction.E).doubleValue();
						    	chainTakeout += cutTakeout*2*breakups.size();
						    }
					    }
					}
				}
     		}
    	}
    }
    /*
    private Readiness subdivideMain(FloorDrawing dwg, DrawingArea area){
    	AreaBody areaBody = area.getAreaBody();
    	long longPipe = area.getAreaOptions().getLongPipe();
    	long cutClearance = area.getAreaOptions().getMainCutSpace();
    	Readiness ret = Readiness.Ready;
    	// initialize divider
    	Divider divider = new Divider(this.getPlaneGeo());
    	List<PipeFitting> avoids = new ArrayList<PipeFitting>(4);
    	divider.setMaxSize(longPipe);
    	divider.setAvoidMargin(cutClearance);
    	divider.setCutSizes(area.getAreaOptions().getMainCutList());
    	// iterate over chain starts
    	for(Map.Entry<PipeFitting, Set<Direction>> chainStart
    			: getGroovedChainStarts(dwg, areaBody).entrySet()){
    		for(Direction dir : chainStart.getValue()){
    			// get pipe which starts the chain
    			Pipe startUnbroken = areaBody.getPipe(chainStart.getKey(), dir);
			//	boolean startOfChain = true;
				boolean endOfChain = false;
				// iterate the chain
				double deferredTakeout = 0;
    			while(!endOfChain){		
    				double startTakeout = startUnbroken.getStartAttachment().getTakeout().doubleValue();
    				avoids.clear();
    				// continue across holes to avoid
    				Pipe endUnbroken = startUnbroken;
    				while(!areaBody.isTerminalGroovedPipe(endUnbroken)){
    					PipeFitting avoid = areaBody.getEndFitting(endUnbroken);
    					avoids.add(avoid);
    					endUnbroken = areaBody.getContinuationPipe(endUnbroken, avoid);
    				}
    				// found next true pipe break
    				PipeFitting start = areaBody.getStartFitting(startUnbroken);    				
    				PipeFitting end = areaBody.getEndFitting(endUnbroken);
    				// test for end of chain
    				endOfChain = !isCouplingContinuation(dwg, areaBody, end);
    				// first takeout rounded the usual way
    				long rndStartTakeout = Math.round(startTakeout);
    				deferredTakeout += (startTakeout - rndStartTakeout);
    				long rndEndTakeout;
					double endTakeout = endUnbroken.getEndAttachment().getTakeout().doubleValue();
    				if(endOfChain){
    					// end of coupling chain
    					// round end takeout considering all previous errors
    					rndEndTakeout = Math.round(endTakeout
    						+ deferredTakeout);
    				}else{
    					// coupling: no takeout, reducer: usual rounding
    					rndEndTakeout = Math.round(endTakeout);
        				deferredTakeout += (endTakeout - rndEndTakeout);
    					// continue chain iteration across coupling
    					startUnbroken = areaBody.getContinuationPipe(endUnbroken, end);
    				}
    				if(divider.subdivide(start, end, rndStartTakeout, rndEndTakeout, 0, avoids)){
    					// subdivide
    					for(Map.Entry<Point[], List<Point>> entry : divider.getCutMap().entrySet()){
    						PipeFitting dividedStart = (PipeFitting)entry.getKey()[0];
    						PipeFitting dividedEnd = (PipeFitting)entry.getKey()[1];
    						Pipe toBeDivided = areaBody.getPipeGraph().getEdge(
    								dividedStart, dividedEnd);
    					    List<PipeFitting> breakups = areaBody.subdivideGraph(
    					    		 toBeDivided, entry.getValue());
    					    updateBreakups(dwg.getOptionsRoot(), areaBody, toBeDivided, dividedStart, dividedEnd, breakups);
    					}
    				}else{
    					ret = Readiness.NoClearanceToCutMain;
    				}
    			}
    		}
    	}
    	return ret;
    }
    */
    private double cutTakeout(FloorDrawing dwg, Pipe pipe){
    	DrawingLayer dl = dwg.getOptionsRoot().findLayer(pipe.getLayerName());
    	if(dl.getType() == Designation.Main &&
    			!(dl.getSubType() == Attachment.threaded || dl.getSubType() == Attachment.welded )){
    		return 0;
    	}
		Pipe[] pipes = {pipe, pipe};
    	Fitting f = fittingFactory.instanceOf(Type.Coupling, Attachment.threaded, null,
    			Arrays.asList(pipes), null);
    	return takeout.locateTakeout(f, Direction.E).doubleValue();
    }
    
    private Readiness subdivideAll(FloorDrawing dwg, DrawingArea area){
    	AreaBody areaBody = area.getAreaBody();
    	long longPipe = area.getAreaOptions().getLongPipe();
    	long cutClearance = area.getAreaOptions().getMainCutSpace();
    	Readiness ret = Readiness.Ready;
    	// initialize divider
    	Divider divider = new Divider(this.getPlaneGeo());
    	List<PipeFitting> avoids = new ArrayList<PipeFitting>(4);
    	divider.setMaxSize(longPipe);
    	divider.setAvoidMargin(cutClearance);
 //   	divider.setCutSizes(area.getAreaOptions().getMainCutList());
 // TODO: avoid logic for threaded
 //   	divider.setThreadedCutSize(area.getAreaOptions().getBranchCutList().get(0).longValue());
    	List<Long> branchCutList = area.getAreaOptions().getBranchCutList();
    	List<Long> mainCutList = area.getAreaOptions().getMainCutList();
    	
    	// iterate over chain starts
    	for(Map.Entry<PipeFitting, Set<Direction>> chainStart
    			: areaBody.getAllChainStarts().entrySet()){
    		//	: getGroovedChainStarts(dwg, areaBody).entrySet()){
    		
    		// iterate over chains
    		for(Direction dir : chainStart.getValue()){
    			double chainTakeout = 0;
    			// get pipe which starts the chain
 			//	boolean startOfChain = true;
				boolean endOfChain = false;
				// iterate the chain
				double deferredTakeout = 0;
				List<PipeFitting> chainList = this.getPipeFittingList();
				chainList.clear();
				areaBody.fillChainList(chainList, chainStart.getKey(), dir);
				int startUnbrokenIndex = 0;
	    		//	while(!endOfChain){
				
				// iterate over couplings/reducers
	   			while(startUnbrokenIndex < chainList.size() - 1){
		   			Pipe startUnbroken = areaBody.getPipeFromChain(chainList, startUnbrokenIndex);   //getPipe(chainStart.getKey(), dir);
		   			List<Long> cutList = startUnbroken.getDesignation() == Designation.Main ? mainCutList : branchCutList;
		   			double cutTk = cutTakeout(dwg, startUnbroken);
		   			//		double startTakeout = startUnbroken.getStartAttachment().getTakeout().doubleValue();
    				PipeFitting startUnbrokenFitting = chainList.get(startUnbrokenIndex);
    				BigDecimal bdStartTakeout = areaBody.getAttachmentToFitting(startUnbroken, startUnbrokenFitting).getTakeout();
    				double startTakeout = bdStartTakeout == null ? 0 : bdStartTakeout.doubleValue();
    	//			chainTakeout += startTakeout;
    				//	areaBody.getAttachmentToFitting(startUnbroken, startUnbrokenFitting).getTakeout().doubleValue();
 
    				// continue across holes to avoid
    				avoids.clear();
    				Pipe endUnbroken = startUnbroken;
    				int endUnbrokenIndex = startUnbrokenIndex;
    				PipeFitting termFitting = chainList.get(startUnbrokenIndex + 1);
    			//	while(!areaBody.isTerminalGroovedPipe(endUnbroken)){
    				while(!areaBody.isExtremeGroovedPipe(endUnbroken, termFitting)){
    			//		PipeFitting avoid = areaBody.getEndFitting(endUnbroken);
    					avoids.add(termFitting);
    					endUnbrokenIndex++;
    					endUnbroken = areaBody.getPipeFromChain(chainList, endUnbrokenIndex);  //areaBody.getContinuationPipe(endUnbroken, avoid);
        				termFitting = chainList.get(endUnbrokenIndex + 1);
    				}
    				startUnbrokenIndex = endUnbrokenIndex + 1;
    				// found next true pipe break
    				PipeFitting start = startUnbrokenFitting;    				
    				PipeFitting end = termFitting;
    				// test for end of chain
    		//		endOfChain = end.getCouplingContinuation(); // !isCouplingContinuation(dwg, areaBody, end);
    				// first takeout rounded the usual way
    		//		long rndStartTakeout = Math.round(startTakeout);
    		//		deferredTakeout += (startTakeout - rndStartTakeout);
    		//		long rndEndTakeout;
    				BigDecimal bdEndTakeout = areaBody.getAttachmentToFitting(endUnbroken, end).getTakeout();
					double endTakeout = bdEndTakeout == null ? 0 : bdEndTakeout.doubleValue();

				//	if(endOfChain){
				/*	if(endUnbrokenIndex == (chainList.size() - 2)){
    					// end of coupling chain
    					// round end takeout considering all previous errors
    					rndEndTakeout = Math.round(endTakeout
    						+ deferredTakeout);
    				}else{
    					// coupling: no takeout, reducer: usual rounding
    					rndEndTakeout = Math.round(endTakeout);
        				deferredTakeout += (endTakeout - rndEndTakeout);
    					// continue chain iteration across coupling
    			//		startUnbroken = areaBody.getContinuationPipe(endUnbroken, end);
    				}
    				*/
    				if(divider.subdivide(start, end, startTakeout, endTakeout, cutTk, avoids, cutList)){
    					// subdivide
    					for(Map.Entry<Point[], List<Point>> entry : divider.getCutMap().entrySet()){
    						PipeFitting dividedStart = (PipeFitting)entry.getKey()[0];
    						PipeFitting dividedEnd = (PipeFitting)entry.getKey()[1];
    						Pipe toBeDivided = areaBody.getPipeGraph().getEdge(
    								dividedStart, dividedEnd);
    						PipeAttachment startPa = areaBody.getAttachmentToFitting(toBeDivided, dividedStart);
    						PipeAttachment endPa = areaBody.getAttachmentToFitting(toBeDivided, dividedEnd);
    						DrawingLayer dl = dwg.getOptionsRoot().findLayer(toBeDivided.getLayerName());
    					    List<PipeFitting> breakups = areaBody.subdivideGraph2(
    					    		dividedStart, dividedEnd, entry.getValue());
    					    updateBreakups2(dl, areaBody, startPa, endPa, dividedStart, dividedEnd, breakups);
    					}
    				}else{
    					// TODO: log this location
    					ret = Readiness.NoClearanceToCutMain;
    				}
    			}
    		}
    	}
    	return ret;
    }


	/**
     * Determines if this is a valid drawing area and sets up auxiliary structures
     * in AreaBody.
     * @param dwg
     * @param area
     */
    public void validateArea(final FloorDrawing dwg, final DrawingArea area){
    	try{
        logger.info("+validateArea(" + dwg.getDwgName() + ")");
        // Post de-serialization (attaching layer objects to entities etc).
    	area.setAreaReadiness(Readiness.Ready);
        prepareArea(dwg, area.getAreaBody()); // ???
        if(area.getAreaBody().getDwgEntity().isEmpty()){
            logger.info("-validateArea->Empty");
        	area.setAreaReadiness(Readiness.Empty);
        	return;
        }
        // combines info at end points snapping close-by locations
        if(!buildPointMap(area.getAreaBody())){
            logger.info("-validateArea->Cannot build map");
        	area.setAreaReadiness(Readiness.NotReady);
        }
        // creates plane graph from edges, detecting mid-point breaks.
        // (checks that branches are trees and orders them ?)
        if(!buildEdgeGraph(dwg.getOptionsRoot(), area.getAreaBody())){
            logger.info("-validateArea->Cannot build edge graph");
        	area.setAreaReadiness(Readiness.NotReady);
         }
        // Check connectivity (before adding Sink)
        int connectCnt = area.getAreaBody().getCenters().size();
    	if(connectCnt != 1){
            logger.info("-validateArea->" + (connectCnt != 0 ? "Not connected" : "No edges"));
        	area.setAreaReadiness(connectCnt > 1 ? Readiness.Disconnected : Readiness.Empty);
    		return;
    	}
    	if(area.getAreaReadiness() != Readiness.Ready){
    		return;
    	}

        // this will not affect branch tree structure
        if(!addJumpsAndHeadsToGraph(dwg.getOptionsRoot(), area.getAreaBody())){
            logger.info("-validateArea->Cannot add heads/jumps");
        	area.setAreaReadiness(Readiness.NotReady);
        	return;
        }

 //       logger.debug("Graph:" + area.getAreaBody().getPipeGraph());

    	// Aggregate head count and store it in branch nodes.
        calcBranchDiameters(dwg.getOptionsRoot(), area.getAreaBody());

        // Verify angles and insert shapes into fittings.
        // Labels attachment point on each pipe
        // Check for raiser and heads at branch ends.
    	if(!setupFittings(dwg.getOptionsRoot(), area.getAreaBody())){
        	area.setAreaReadiness(Readiness.NotReady);
            logger.info("-validateArea->Unable to set up fittings");
        	return; // no fittings
    	}
        if(area.getAreaBody().getRaiser() == null){
        	area.setAreaReadiness(Readiness.NoRaiser);
            logger.info("-validateArea->No raiser");
        	return; // no raiser
        }
        // the entities starts/ends are arranged correctly
        area.getAreaBody().orderEdges();

        // Subdivide main and create remaining pipe fittings (error?)
        Readiness ready = subdividePipes(dwg, area);
        if(ready != Readiness.Ready){
        	area.setAreaReadiness(ready);
        	logger.info("-validateArea->Cannot subdivide:" + ready);
        	return;
        }

        //   add spans and summary takeout to each pipe
 //       calcMainTakout(dwg, area);
 //       calcThreadTakout(dwg,area.getAreaBody());
 //   	area.getAreaBody().calcSpan();
 //   	this.correctMainSpan(dwg, area.getAreaBody());

    	area.getAreaBody().calcSpanAndTakout(area.getAreaOptions().getTakeoutRounding());

    	area.setAreaReadiness(Readiness.Ready);
    	area.setDefectCount(area.getAreaBody().getProblemPointCount());

        logger.info("-validateArea");
    	}catch(Exception t){
    		logger.error("Validation Failed", t);
    		throw new IllegalArgumentException("Validation Failed");

    	}
    }
    
    public void buildCutSheetReport(final FloorDrawing dwg, final DrawingArea area){
		try{
		    	numberEdges2(dwg, area);
		    	area.countEdgeMultiplicity();
		    	area.getAreaCutSheet().orderCutSheet();
		}catch(Exception e){
			logger.error("Failed to build report", e);
			throw new IllegalStateException("Failed to build report");
		}
    }

    /**
     * Performs post-deserialization init of the layers and area entities.
     * <ul>
     * <li>Sets default values for hole diameters in layers which do not have them</li>
     * <li>Removes non-structure entities; removes and reports zero-length pipes</li>
     * </ul>

     * @param dwg
     * @param area
     */
    protected void prepareArea(final FloorDrawing dwg, final AreaBody areaBody){
        logger.info("+prepareArea");
        /*
		for(DrawingLayer layer : dwg.getOptionsRoot().getLayer()){
			if(layer.getHoleDiameter() == null && layer.getType() == Designation.Main){
				Diameter md = layer.getMainDiameter();
				TakeoutInfo info = takeout.takeoutInfo(md);
				Diameter hd = info.getDrillLimit();
				layer.setHoleDiameter(hd);
			}
		}
		*/
		List<DwgEntity> ignorable = new ArrayList<DwgEntity>();
		
    	for(DwgEntity e : areaBody.getDwgEntity()){
    	    e.setLayer(dwg.getOptionsRoot().findLayer(e.getLy()));
			if(!e.isStructure()){
				ignorable.add(e);
			}
        }
    	areaBody.getDwgEntity().removeAll(ignorable);
        logger.info("-prepareArea");
	}

    /**
     * Scans an area body entities and builds the area Point Map from their ends.
     * Use only start points and end points of line-segments
     * Ends close within tolerance are snapped together.
     * Verifies that point entities of different type do not overlap.
     * @param areaBody
     * @return <code>true</code> if there is no overlap
     */
   public boolean buildPointMap(final AreaBody areaBody){
       logger.info("+buildPointMap");
	   boolean ret = true;
	   areaBody.getPointMap().clear();
    	for(DwgEntity e : areaBody.getDwgEntity()){
    		for(int i = 0; i < 2; i++){
    			DwgPoint newPoint = i == 0 ? e.getEntStart() : e.getEntEnd();
    			if(newPoint != null && (i == 0 || e.getCls() == AcClass.AcDbLine)){
    				DwgPoint match = null;
    		        for(DwgPoint p : areaBody.getPointMap().keySet()){
    			        if(planeGeo.pointOnPoint(newPoint, p)){
    			        	match = p;
    			        	break;
    			        }
    		        }
    		        if(match == null){
    		        	match = newPoint;
    		        }else{
    		        	// correct entity end-point
    		        	if(i == 0){
    		        		e.setEntStart(match);
    		        	}else{
    		        		e.setEntEnd(match);
    		        	}
    		        }
    		        PointInfo info = areaBody.getPointInfo(match);
		            if(e.getCls() == AcClass.AcDbArc){
		            	info.getJumps().add(e);
		            }else if(e.getCls() == AcClass.AcDbCircle){
		            	info.getHeads().add(e);
		            }else if(e.getCls() == AcClass.AcDbPoint){
		            	info.getCouplings().add(e);
		            }else if(e.getCls() == AcClass.AcDbBlockReference){
		            	String blockName = e.getName();
		            	logger.info("Found Block");
                    	if(!pipeConfig.isKnownBlockName(blockName)){
    		            	logger.info("Unknown name="+blockName);
        					info.setStatus(Defect.unknownBlock);
                    		ret = false;
                    	}else {
                    		if(pipeConfig.isCouplingBlock(blockName)) {
                    			info.getCouplings().add(e);
                    		}else {
        		            	info.getBlocks().add(e);                    			
                    		}
                    		
                    	}
		            }
    				// check homogenity
    				if((info.getHead() == null ? 0 : 1)
    						+ (info.getBlock() == null ? 0 : 1)
    						+ (info.getJump() == null ? 0 : 1)
    						+ (info.getCoupling() == null ? 0 : 1) > 1){
    					info.setStatus(Defect.overlapingSymbols);
    					ret = false;
    				}
    			}
    		}
    	}
        logger.info("-buildPointMap->" + ret);
    	return ret;
   }
   /**
    * Create preliminary pipe graph by analyzing edge entities.
    * This graph will have have only one fitting at a point.
    * Two fittings form an edge if there is a segment entity containing both
    * and without any point in-between them.
    * @param areaBody
    */
   public boolean buildEdgeGraph(final DrawingOptions opt, final AreaBody areaBody){
       logger.info("+buildEdgeGraph");
   	Set<DwgPoint> pointSet = areaBody.getPointMap().keySet();

		SortedMap<Double, DwgPoint> cutMap = new TreeMap<Double, DwgPoint>();
	   	for(DwgEntity e : areaBody.getDwgEntity()){
	   		if(e.getCls() != AcClass.AcDbLine){
	   			continue; // skip point entities
	   		}
	   		DwgPoint start = e.getEntStart();
	   		DwgPoint end = e.getEntEnd();
	   		if(start == end || end == null){
	   			PointInfo pInfo = areaBody.getPointInfo(start);
				pInfo.setStatus(Defect.zeroLengthPipe);	   			 
	   			continue;
	   		}
            cutMap.clear();
	   		for(DwgPoint p : pointSet){
	   			if(p == start || p == end){
	   				continue; // skip incidences at the extremities
	   			}
	   			if(planeGeo.pointOnSegment(p, start, end)){
		   			cutMap.put(PlaneGeo.distance(start, p), p);	   					
	   			}
	   		}

			DwgPoint last = e.getEntStart();
			for(DwgPoint next : cutMap.values()){
	   			addSegmentToPipeGraph(opt, areaBody, e, last, next);
	   			last = next;
			}
			addSegmentToPipeGraph(opt, areaBody, e, last, e.getEntEnd());
    	}
       logger.info("-buildEdgeGraph");
   	   return areaBody.orderBranchPipes();
   }

   private void addSegmentToPipeGraph(final DrawingOptions opt, final AreaBody areaBody, final DwgEntity e, final DwgPoint start, final DwgPoint end){
		Pipe pipe = new Pipe();
		String name = e.getLy();
		DrawingLayer layer = opt.findLayer(name);
		pipe.setDesignation(layer.getType());
		pipe.setLayerName(layer.getName());
		if(pipe.getDesignation() == Designation.Main){
			pipe.setDiameter(layer.getMainDiameter());
		}
		areaBody.addPipeToGraph(pipe, start, end);
	}

    private boolean addJumpsAndHeadsToGraph(final DrawingOptions opt, final AreaBody areaBody){
        logger.info("+addJumpsAndHeadsToGraph");
    	boolean ret = true;
    	boolean sink = false;
        for(DwgPoint p : areaBody.getPointMap().keySet()){
        	PointInfo pInfo = areaBody.getPointInfo(p);
        	if(pInfo.getFittings().isEmpty()){
        		pInfo.setStatus(Defect.isolatedPoint);
        		continue;
        	}
        	PipeFitting pf = pInfo.getFittings().get(0);
        	boolean isHead = false;
        	if(pInfo.getHead() != null){
        		isHead = true;
        	}else if(pInfo.getBlock() != null){
        		if(pipeConfig.lookupHeadTemplate(pInfo.getBlock().getName()) != null){
        			isHead = true;
        		}
        	}
        	if(isHead){
        		if(!addHeadToPipeGraph(areaBody, pf, opt)) {
        			ret = false;
        		}
        	}else if(pInfo.getJump() != null){
        		if(!addJumpToPipeGraph(opt, areaBody, pf)){
        			ret = false;
        		}
        	}
        }
        logger.info("-addJumpsAndHeadsToGraph");
        return ret;
    }
    private boolean addHeadToPipeGraph(final AreaBody areaBody, final PipeFitting pf, final DrawingOptions opt){
		boolean ret =  true;
    	PointInfo pInfo = areaBody.getPointInfo(pf.getCenter());
		Pipe pipe = null;
		String layerName = null;
		DwgEntity block = pInfo.getBlock();
		if(block != null){
			layerName = block.getLy();
			AreaBody.HeadInfo template = pipeConfig.lookupHeadTemplate(block.getName());
			pipe = template.getPipe().clone();
			pf.setJump(template.getJumpLocation());
		}else if(pInfo.getHead() != null){
			// unknown head type
			DwgEntity head = pInfo.getHead();
			layerName = head.getLy();
			pipe = new Pipe(Designation.Head);
	        pipe.setDiameter(takeout.getHeadDiameter());
	        pipe.setVertical(true);
	        pf.setJump(Jump.NONE);
		}
		if(pipe != null) {
			DrawingLayer layer = opt.findLayer(layerName);
			if(layer != null && layer.getType() == Designation.Head) {
				pipe.setDiameter(layer.getMainDiameter());
				pipe.setLayerName(layer.getName());
			}
			PipeFitting otherFitting = null;
			if(!pipe.isVertical()) {
				DwgPoint endPt = block.getEntEnd(); // horizontal, so block-based
				DwgPoint startPt = pf.getCenter();
				if(endPt == null || planeGeo.pointOnPoint(startPt, endPt)){
					PointInfo startInfo = areaBody.getPointMap().get(startPt);
					startInfo.setStatus(Defect.zeroLengthPipe);
					ret = false;
				}else {
					PointInfo endInfo = areaBody.getPointMap().get(endPt);
					if(endInfo != null) {
						endInfo.setStatus(Defect.overlapingSymbols);
						ret = false;
					}else {
						otherFitting = new PipeFitting(endPt);
						areaBody.getPipeGraph().addVertex(otherFitting);
					}
				}
			}else {
				// all vertical heads point to an imaginary vertex called sink.
				otherFitting = PipeFitting.SINK;
				if(!areaBody.getPipeGraph().containsVertex(PipeFitting.SINK)){
					 areaBody.getPipeGraph().addVertex(PipeFitting.SINK);
				}
			}
			if(otherFitting != null) {
		        areaBody.getPipeGraph().addEdge(pf, otherFitting, pipe);							
			}
		}
		return ret;
    }
    
    private boolean pipesIntersectJump(final AreaBody areaBody, final PipeFitting pf, final Set<Pipe> pipes){
    	DwgPoint center = pf.getCenter();
    	PointInfo pInfo = areaBody.getPointInfo(center);
    	for(DwgEntity jump : pInfo.getJumps()){
    		for(Pipe pipe: pipes){
    			if(pipeIntersectJump(areaBody, pf, pipe, jump)){
    				return true;
    			}
    		}
    	}
    	return false;	
    }
    private boolean pipeIntersectJump(final AreaBody areaBody, final PipeFitting pf, final Pipe pipe, DwgEntity jump){
    	PipeFitting start = areaBody.getStartFitting(pipe);
    	PipeFitting end = areaBody.getEndFitting(pipe);
    	PipeFitting opposite = start == pf ? end : start;
    	DwgAng ang = jump.getEntAng();
    	return this.planeGeo.vectorIntersectsArc(pf, opposite, ang.getAlpha(), ang.getBeta());
    }    
    private boolean addJumpToPipeGraph(final DrawingOptions drawingOptions, final AreaBody areaBody, final PipeFitting pf){
		UndirectedGraph<PipeFitting, Pipe> graph = areaBody.getPipeGraph();
		PointInfo pInfo = areaBody.getPointInfo(pf.getCenter());
		List<Set<Pipe>> ps = paralleSets(drawingOptions, areaBody, pf, pInfo);
		if(ps == null){
			return false;
		}
        if(ps.get(0).size() > 2){
        	pInfo.setStatus(Defect.pipeOverlap);
        	return false;
        }

        if(ps.size() == 1){
        	if(ps.get(0).size() == 1){
        		return true; // ignore jumps at ends
        	}
            // ignore parallelism by splitting this 2-pipe bucket
        	Set<Pipe> set = new HashSet<Pipe>();
        	Pipe pipe = ps.get(0).iterator().next();
        	ps.get(0).remove(pipe);
        	set.add(pipe);
            ps.add(set);
        }
        // reorder paralel sets main first
       	PipeCollectionComparator comp = getPipeCollectionComparator();
       	comp.setDrawingOptions(drawingOptions);
       	comp.setAscending(false);
       	Collections.sort(ps, comp);
       	
       	Jump startJumpType = this.pipesIntersectJump(areaBody, pf, ps.get(0)) ? Jump.BOTTOM : Jump.TOP;
       	Jump endJumpType =startJumpType == Jump.BOTTOM ? Jump.TOP : Jump.BOTTOM;

        // remove this vertex and all edges
        // iterate over parallel set
        Pipe lastJump = null;
        PipeFitting lastFitting = null;
        pInfo.getFittings().clear();
        for(int i = 0; i < ps.size(); i++){
        	Jump jumpType = Jump.MIDDLE;
        	if(i == 0){
        		jumpType = startJumpType;
        	}else if(i == ps.size() - 1){
        		jumpType = endJumpType;
        	}
        	PipeFitting jpf = pf.clone();
        	jpf.setJump(jumpType);
        	pInfo.getFittings().add(jpf);
        	graph.addVertex(jpf);
        	Pipe jPipe = null;
        	if(i < ps.size() - 1){
        		DrawingLayer layer = comp.selectLayer(ps.get(i+ 1));
        		jPipe = new Pipe();
        		jPipe.setVertical(true);
        		jPipe.setLayerName(layer.getName());
        		jPipe.setDesignation(layer.getType());
        		if(layer.getType() == Designation.Main){
        			jPipe.setDiameter(layer.getMainDiameter());
        		}
        	}
        	// Re-wire edges in this set from pf to the newly created fitting.
        	// Make sure not to reverse pipe ordering while doing this.
        	for(Pipe pipe : ps.get(i)){
        	   PipeFitting opposite = Graphs.getOppositeVertex(graph, pipe, pf);
        	   boolean oppositeIsGraphTarget = opposite == graph.getEdgeTarget(pipe);
        	   graph.removeEdge(pipe);
        	   if(oppositeIsGraphTarget){
        	     graph.addEdge(jpf, opposite, pipe);
        	   }else{
          	     graph.addEdge(opposite, jpf, pipe);
        	   }
        	}
        	if(lastJump != null){
        		graph.addEdge(jpf, lastFitting, lastJump);
        	}
        	lastJump = jPipe;
        	lastFitting = jpf;
        }
        graph.removeVertex(pf);
        return true;
    }

   /**
    * Finds groups of parallel entities and orders them by descending cardinality.
    * @param the entities analyzed
    * @return the ordered groups
    */
   private List<Set<Pipe>> paralleSets(final DrawingOptions opt, final AreaBody areaBody, final PipeFitting pFitting, final PointInfo pInfo){
   	Collection<Pipe> entities = areaBody.getPipeGraph().edgesOf(pFitting);
   	UndirectedGraph<Pipe, DefaultEdge> pg = getParallelGraph();
   	GraphUtils.clearGraph(pg);
   	
   	Set<Pipe> vs = getVerticalSet();
   	vs.clear();
//   	Set<Pipe> sS = getSidewallSet();
//   	sS.clear();
   	// add all vertices
   	for(Pipe e : entities){
   		if(e.isVertical()){
   			vs.add(e);
//   		}else if(e.getDesignation() == Designation.Head){
//   			sS.add(e);
   		} else{
   		    pg.addVertex(e);
   		}
   	}
   	// iterate over all distinct pairs and make edges from parallel ones
   	List<Pipe> entList = getParallelEntityList();
   	entList.clear();
   	entList.addAll(pg.vertexSet());
   	for(int i = 0; i < entList.size(); i++){
   		Pipe e1 = entList.get(i);
   		for(int j = i + 1; j < entList.size(); j++){
   			Pipe e2 = entList.get(j);
   			if(atAngle(areaBody, e1, e2, Math.PI)){
   				pg.addEdge(e1, e2);
   			}
   		}
   	}
   	ConnectivityInspector<Pipe, DefaultEdge> ci
	        = new ConnectivityInspector<Pipe, DefaultEdge>(pg);
    List<Set<Pipe>> ret = ci.connectedSets();
    if(!vs.isEmpty()){
        ret.add(vs);
    }
//    if(!sS.isEmpty()){
//    	if(ret.size() == 1 && sS.size() == 1){
//    		if(ret.get(0).size() == 1){
//    			ret.get(0).addAll(sS);
//    		}else{
//    			ret.add(sS);
//    		}
//    	}else{
//    		pInfo.setStatus(Defect.wrongSidewallLocation);
//    		return null;
//    	}
//    }

 //  	CollectionComparator<Pipe> comp =  getPipeSetCollectionComparator();
 //  	comp.setAscending(false);
   	Collections.sort(ret, new Comparator<Set<Pipe>>(){

//		@Override
		// if return is > 0 then o1 will be positioned after o2
		public int compare(Set<Pipe> o1, Set<Pipe> o2) {
			int s1 = o1.size();
			int s2 = o2.size();
			if(s1 != s2){
				return s2 - s1; // bigger cardinality sets go first
			}
			// same cardinality
			boolean hasMain1 = Pipe.hasMain(o1);
			boolean hasMain2 = Pipe.hasMain(o2);
			if(hasMain1 != hasMain2){
				return hasMain2 ? 1 : -1; // main (if present in one only) goes first)
			}
			Diameter d1 = Pipe.maxDiameter(o1);
			Diameter d2 = Pipe.maxDiameter(o2);
			if(d1 != d2){
				// for same cardinality - bigger diameter is first
				return (d2 != null) && (d1 == null || d2.compareTo(d1) > 0) ? 1 : -1;
			}
			if(hasMain1){ // also hasMain2
				boolean hasMainOnly1 = Pipe.hasNonMain(o1);
				boolean hasMainOnly2 = Pipe.hasNonMain(o2);
				if(hasMainOnly1 != hasMainOnly2){
					return hasMainOnly2 ? 1 : -1;
					// pure main has precedence over branch/main mix
				}
				// if 2 mains of equal diameter - one which has unique name goes first
				String name1 = Pipe.commonMainName(o1);
				String name2 = Pipe.commonMainName(o2);
				boolean hasName1 = name1 != null;
				boolean hasName2 = name2 != null;
				if(hasName1 != hasName2){
					return hasName2 ? 1 : -1;
				}
				if(hasName1){ // and hasName2
					DrawingLayer l1 = opt.findLayer(name1);
					DrawingLayer l2 = opt.findLayer(name2);
					boolean isThread1 = l1.getSubType() == Attachment.threaded;
					boolean isThread2 = l2.getSubType() == Attachment.threaded;
					if(isThread1 != isThread2){
						return isThread1 ? 1 : -1;
					}
				}
			}
			return 0;
		}
   		
   	});

   	return ret;
   }
   

   /**
    * Checks if 2 entities form an expected angle.
    * Assumes that the entities have a point in common.
    * The angle is unsigned so the order in which entities are passed is irrelevant.
    * @param e1 first entity
    * @param e2 second entity
    * @param ang expected angle between 0 and Math.PI
    * @return <code>true</code> if the formed angle has expected value (within tolerance)
    */
   protected boolean atAngle(final AreaBody areaBody, final Pipe e1, final Pipe e2, final double ang){
	double ang12;
	if(e1.isVertical() != e2.isVertical()){
    	ang12 = Math.PI*0.5; // one vertical, another not
    }else if(e1.isVertical()){
    	ang12 = Math.PI; // both vertical
    }else{
	   PipeFitting[] tripple = getPipeFittingTripple();
   	   if(!GraphUtils.tripple(areaBody.getPipeGraph(), e1, e2, tripple)){
   		  throw new IllegalArgumentException("Compared segments have no points in common");
   	    }
		ang12 = PlaneGeo.angleMeasure(tripple);
    }
	// directions of head-segments come from bounding box and are approximate
	boolean sidewallComparison = e1.getDesignation() == Designation.Head && !e1.isVertical()
			|| e2.getDesignation() == Designation.Head && !e2.isVertical();
	double tol = sidewallComparison ? Math.PI*0.25 : planeGeo.getAngularTolerance();
	return Math.abs(ang - ang12) < tol;
   }

   /**
    * Finds all main, non-threaded layers meeting at a vertex
    * which are of maximal diameter.
    * If horizontals are present then verticals are excluded from comparison.
    * @param opt drawing options
    * @param areaBody body of the drawing
    * @param pf the vertex
    * @param maxLayerSet the layers
    */
   private void getMaxLayers(final DrawingOptions opt, final AreaBody areaBody, final PipeFitting pf,
		   Set<DrawingLayer> maxLayerSet){
	   maxLayerSet.clear();
       Pipe maxDiameterPipe = null;
   	   for(Pipe pipe : areaBody.getPipeGraph().edgesOf(pf)){
   		if(pipe.getDesignation() != Designation.Main){
   			continue;
   		}
	    DrawingLayer layer = opt.findLayer(pipe.getLayerName());
	    if(layer.getSubType() == Attachment.threaded){
	    	continue;
	    }
	    if(layer.getSubType() == Attachment.grooved
	    		|| layer.getSubType() == Attachment.welded){
	    	int comp;
	    	if(maxDiameterPipe == null){
	    		comp = 1; // first is always max
	    	}else{
	    		// comparing differently positioned - horizontal always wins
	    		if(maxDiameterPipe.isVertical() && !pipe.isVertical()){
	    			comp = 1;
	    		}else if(!maxDiameterPipe.isVertical() && pipe.isVertical()){
	    			comp = -1;
	    		}else{
	    			comp = pipe.getDiameter().compareTo(maxDiameterPipe.getDiameter());
	    		}
	    	}
   		    if(comp > 0){
   		    	maxDiameterPipe = pipe;
   		    	maxLayerSet.clear();
   		    }
   		    if(comp >= 0){
   		    	maxLayerSet.add(layer);
   		    }
		}
   	}
   }
    private void  calcBranchDiameters(final DrawingOptions opt, final AreaBody areaBody){
        areaBody.calcHeadCount();
        for(Pipe b : areaBody.getPipesFor(Designation.Branch)){
        	DrawingLayer layer = opt.findLayer(b.getLayerName());
        	PipeFitting epf = areaBody.getEndFitting(b);
        	PipeFitting spf = areaBody.getStartFitting(b);
        	int hc = Math.min(spf.getHeadCount(), epf.getHeadCount());
        	Diameter d = layer.getBranchDiameter(hc);
        	b.setDiameter(d);
        }
    }
    public boolean setupFittings(final DrawingOptions opt, final AreaBody areaBody){
      logger.info("+setupFittings");
      boolean ret = true;
   	  Set<PipeFitting> ends = new HashSet<PipeFitting>();
      List<Pipe> pipes = new ArrayList<Pipe>();

      // order vertices with those attached to main first (excluding sink-vertex)
   	  List<PipeFitting> mainFirst = new ArrayList<PipeFitting>();
   	  mainFirst.addAll(areaBody.getPipeGraph().vertexSet());
//   	  mainFirst.remove(PipeFitting.SINK);
   	  Collections.sort(mainFirst, new Comparator<PipeFitting>(){
//		@Override
		public int compare(PipeFitting o1, PipeFitting o2) {
			
			boolean isMain1 = Pipe.hasMain(areaBody.getPipeGraph().edgesOf(o1));
			boolean isMain2 = Pipe.hasMain(areaBody.getPipeGraph().edgesOf(o2));
			if(isMain1 && !isMain2){
				return -1;
			}if(isMain2 && !isMain1){
				return 1;
			}
			return 0;
		}
	});
   	  
      for(PipeFitting pf : mainFirst){
          PointInfo pInfo = areaBody.getPointMap().get(pf.getCenter());
          if(pInfo == null) {
        	  continue;
          }
          /*
          Set<DrawingLayer> layers = getDrawingLayerSet();
       	  getMaxLayers(opt, areaBody, pf, layers);
       	  if(layers.size() > 1){
       		pInfo.setStatus(Defect.tooManyVendors);
       		logger.error(Defect.tooManyVendors);
       		ret = false;
       		continue;
       	  }
       	  DrawingLayer layer = layers.size() == 0 ? null : layers.iterator().next();
			*/
          // determine shape
          List<Set<Pipe>> ps = paralleSets(opt, areaBody, pf, pInfo);
          if(ps == null){
         	  logger.error("paralleSets returned null");
        	  ret = false;
        	  continue;
          }
          if(ps.get(0).size() > 2){
        	  // implies that each parallel set has at most 2 items
           	pInfo.setStatus(Defect.pipeOverlap);
       		logger.error(Defect.pipeOverlap);
           	ret = false;
           	continue;
          }
          if(ps.size() > 2){
               	pInfo.setStatus(Defect.tooManyPipes);
           		logger.error(Defect.tooManyPipes);
               	ret = false;
               	continue;
          }
   	      Type fittingType = null;
          if(ps.size() == 2){
			Pipe p1 = ps.get(0).iterator().next();
			Pipe p2 = ps.get(1).iterator().next();
			if(p1.getDesignation() != Designation.Head
					&& p2.getDesignation() != Designation.Head
					&& !atAngle(areaBody, p1, p2, 0.5*Math.PI)){
				// 2 directions not at right angle
				// no need to check heads
				if(ps.get(0).size() == 1){
					if(atAngle(areaBody, p1, p2, 0.75*Math.PI)){
						fittingType = Type.Ell45;
					}
				}
				if(fittingType == null){
					pInfo.setStatus(Defect.missingJump);
	           		logger.error(Defect.missingJump);
					ret = false;
					continue;
				}
			}
			if(fittingType == null){
			  if(ps.get(0).size() == 1){
			    fittingType = Type.Ell;
			  }else if(ps.get(1).size() == 1){
			    fittingType = Type.Tee;
			  }else{
			    fittingType = Type.Cross;
			  }
			}
          }else{ // ps.size()==1
           	  if(ps.get(0).size() == 1){
             		// ignore ends - also report error for headless branch
             		Pipe endPipe = ps.get(0).iterator().next();
             		if(endPipe.getDesignation() == Designation.Branch){
      	    		    pInfo.setStatus(Defect.missingHead);
    	           		logger.error(Defect.missingHead);
      	    		    ret = false;
          	    		continue;
             		}else{
             			if(pInfo.getCoupling() == null && pInfo.getJump() == null){
                          ends.add(pf);
                          fittingType = Type.Raiser;
             			}else{
             			  fittingType = Type.Cap;
             			}
             		}
             	}else{
             		if(pInfo.getBlock() == null && pInfo.getCoupling() == null){
      	    		    pInfo.setStatus(Defect.missingCoupling);
    	           		logger.error(Defect.missingCoupling);
      	    		    ret = false;
          	    		continue;
             		}
             		fittingType = (pInfo.getBlock() != null)
             		  && pipeConfig.isReducerBlock(pInfo.getBlock().getName()) ?
             				Type.Reducer : Type.Coupling;
             	}
	      }
          //List<Pipe> pipes = new ArrayList<Pipe>();
          pipes.clear();
          for(Set<Pipe> set : ps){
              pipes.addAll(set);
          }
          Attachment attachment = determineAttachment(opt, ps);
       	  if(attachment == null){
       		pInfo.setStatus(Defect.cannotDetermineAttachment);
       		logger.error(Defect.cannotDetermineAttachment);
       		ret = false;
       		continue;
     	  }
          Vendor vendor = null;
          if(attachment == Attachment.grooved){
              vendor = determineVendor(opt, ps);
           	  if(vendor == null){
           		pInfo.setStatus(Defect.tooManyVendors);
           		logger.error(Defect.tooManyVendors);
           		ret = false;
           		continue;
         	  }
          }
          setupOneFitting(opt, areaBody, pf, fittingType, attachment, vendor, pipes);
       }
   	   if(ends.size() == 1){
		 areaBody.setRaiser(ends.iterator().next());
	   }else{
		 areaBody.setRaiser(null);
		 for(PipeFitting end : ends){
			areaBody.getPointInfo(end.getCenter()).setStatus(Defect.possibleRaiser);
		 }
	   }
       logger.info("-setupFittings->" + ret);
       return ret;
   }
    private Attachment determineAttachment(final DrawingOptions opt, List<Set<Pipe>> ps){
    	Attachment ret = null;
    	String name = null;
    	if(!Pipe.hasMain(ps.get(0))){
    		ret = Attachment.threaded;
    		Pipe head = Pipe.findHeadPipe(ps);
    		if(head == null) {
        		return ret; // all branches    			
    		}
    		// head on branch
    		name = head.getLayerName();
    		if(name != null) {
    		    DrawingLayer layer = opt.findLayer(name);
    		    if(layer != null) {
    		    	ret = layer.getSubType();
    		    }
    		}
    		return ret;
    		
    	}
    	// hasMain
		name = Pipe.maxMainName(ps.get(0));
		if(name != null){
		    DrawingLayer layer = opt.findLayer(name);
		   	boolean onePiece = true;
	    	if(ps.size() == 1 || ps.get(0).size() == 1){
	    		onePiece = false; // not tee or cross
	    	}else{ // tee or cross
	    		if(Pipe.hasNonMain(ps.get(0)) || Pipe.commonMainName(ps.get(0))==null){
	    			onePiece = false; // non-main or mains with different names
	    		}else{
	    			if(layer.getSubType() == Attachment.threaded){
	    				onePiece =  false; // treaded main
	    			}else if(layer.getSubType() == Attachment.grooved){
	    				if(!this.takeout.isMechanicalAllowed(Pipe.maxDiameter(ps.get(0)),
	    						Pipe.maxDiameter(ps.get(1)))){
	    					onePiece =  false; // condition for mechanical tee fails
	    				}
	    			}else if(layer.getSubType() == Attachment.weldedGroove
	    						|| layer.getSubType() == Attachment.welded){    				
	    				if(!opt.getWeldIfEqual().equalsIgnoreCase("Y")){
	    					Diameter d0 = Pipe.maxDiameter(ps.get(0));
	    					Diameter d1 = Pipe.maxDiameter(ps.get(1));
	    					if(d0 == d1){
	    						onePiece =  false; // welding equal pipes has been explicitly disabled by user
	    					}
	    				}
	    			}
	    		}
	    	}
    		ret = layer.getSubType();
	    	if(onePiece){
	    		if(layer.getSubType() == Attachment.grooved){
	    			// non-welded approach to one-piece
	    			ret = Attachment.mechanical;
	    		}
	    		/*
	    		else if(layer.getSubType() == Attachment.weldedGroove){
	    			if(isThreaded(opt, ps.get(1))){
	    				// when branch/threaded is attached to welded-groove main
	    				// the outlet is always threaded
	    				ret = Attachment.welded;
	    			}
	    		} // else - remains weld (threaded) TODO: what if outlet pipe is grooved?
	    		*/
	    	}else{
	    		// in this case it is either grooved or threaded
	    		if(layer.getSubType() == Attachment.weldedGroove){
	    			ret = Attachment.grooved;
	    		}else if(layer.getSubType() == Attachment.welded){
	    			ret = Attachment.threaded;
	    		}
	    	}
		}
     	return ret;
    }
    // checks if all pipes in set are thread-like
    private static boolean isThreaded(final DrawingOptions opt, Set<Pipe> pp){
    	for(Pipe p : pp){
    		if(p.getDesignation() == Designation.Main){
    			String name = p.getLayerName();
    			Attachment attachment = opt.findLayer(name).getSubType();
    			if(attachment != Attachment.threaded && attachment != Attachment.welded){
    				return false;
    			}
    		}
    	}
    	return true;
    }
    private Vendor determineVendor(final DrawingOptions opt, List<Set<Pipe>> ps){
    	Vendor ret = null;
    	String name = null;
    	Pipe head = Pipe.findHeadPipe(ps);
    	if(head != null) {
    		name = head.getLayerName();
    	}else {
        	name = Pipe.maxMainName(ps.get(0));    		
    	}
    	if(name != null){
    		ret = opt.findLayer(name).getVendor();
    	}
    	return ret;
    }
    private void setupOneFitting(final DrawingOptions opt, final AreaBody areaBody, final PipeFitting pf, final Type fittingType, final Attachment attachment, final Vendor vendor, final List<Pipe> pipes){

    	Diameter.orderDiametrisables(pipes);
    	
    	List<Attachment> attachements = null;
    	if(attachment == Attachment.weldedGroove){
    		attachements = new ArrayList<Attachment>();
    		for(int i = 0; i < pipes.size(); i++){
    			Attachment a = null;
    			if(i >= 2){
        			Pipe p = pipes.get(i);
        			String layerName = p.getLayerName();
    				DrawingLayer layer = layerName != null ? opt.findLayer(layerName): null;
        			if((p.getDesignation() == Designation.Main||p.getDesignation() == Designation.Head) && layer != null){
        				a = Attachment.weldedGroove;
        				Attachment subType = layer.getSubType();
        				if(subType == Attachment.threaded || subType == Attachment.welded){
        					a = Attachment.welded;
        				}
        			}else{
        				a = Attachment.welded;
        			}
        			if(a == Attachment.welded && opt.getAbcoMode().equalsIgnoreCase("Y")){
        				Diameter d = p.getDiameter();
        				if(d.compareTo(Diameter.D2) >= 0){
        					a = Attachment.weldedGroove;
        				}
        			}			
     			}
    			attachements.add(a);
    		}
    	}
    	Fitting fitting = fittingFactory.instanceOf(fittingType, attachment, vendor, pipes, attachements);
    	logger.debug("Created fitting:" + fitting + " at " + pf.getCenter());
        pf.setFitting(fitting);
        for(int i = 0; i < pipes.size(); i++){
            PipeAttachment pa = areaBody.getAttachmentToFitting(pipes.get(i), pf);
            Direction direction = Direction.values()[i];
            pa.setDirectionInFitting(direction);
            BigDecimal tk = takeout.locateTakeout(fitting, direction);
            pa.setTakeout(tk);
            if(logger.isDebugEnabled()){
            	Pipe p = pipes.get(i);
            	String whichEnd = (areaBody.getStartFitting(p)==pf)? "Start" : "End";
                logger.debug(direction + " attached to " + whichEnd + " of "
                		+ p.getDiameter() + "-" + p.getDesignation() + " pipe, requires " + tk + " takeout");
            }
        }
        return;
    }

   public static class PipeCollectionComparator implements Comparator<Collection<Pipe>> {
	    private DrawingOptions drawingOptions;
	    private int direction = 1;
//		@Override
		public int compare(final Collection<Pipe> o1, final Collection<Pipe> o2) {
			DrawingLayer layer1 = selectLayer(o1);
			DrawingLayer layer2 = selectLayer(o2);
			if(layer1.getType() == Designation.Branch
					&& layer2.getType() == Designation.Branch){
				return 0;
			}
			if(layer2.getType() == Designation.Branch){
				return direction;
			}
			if(layer1.getType() == Designation.Branch){
				return -direction;
			}
			int comp = layer1.getMainDiameter().compareTo(layer2.getMainDiameter());
			if(comp != 0){
				return comp*direction;
			}
			if(layer1.getSubType() == layer2.getSubType()){
				return 0;
			}
			return direction * ((layer1.getSubType() == Attachment.grooved) ? 1 : -1);
		}
		/**
		 * Selects a layer from a collection of pipes in the following order of preferences
         * main over branch, higher diameter, grooved over threaded.
		 * @param pipes the collection
		 * @return the layer
		 */
		public DrawingLayer selectLayer(final Collection<Pipe> pipes){
			DrawingLayer ret = null;
			for(Pipe p : pipes){
				if(p.getDesignation() == Designation.Head){
					continue;
				}
				DrawingLayer pLayer = getLayer(p);
				if(ret == null){
					ret = pLayer; // always set something (even branch)
				}else if(p.getDesignation() != Designation.Main){
					continue; // subsequent branches are ignored
				}else{ // main
					if(ret.getType() == Designation.Branch){
						ret = pLayer;
						continue; // main over branch
					}
					int comp = pLayer.getMainDiameter().compareTo(ret.getMainDiameter());
					if(comp > 0){
						ret = pLayer;
					}else if(comp == 0){
						if(pLayer.getSubType() == Attachment.grooved){
							ret = pLayer;
						}
					}
				}
			}
			return ret;
		}
		private DrawingLayer getLayer(final Pipe p){
			return drawingOptions.findLayer(p.getLayerName());
		}
		public DrawingOptions getDrawingOptions() {
			return drawingOptions;
		}
		public void setDrawingOptions(final DrawingOptions drawingOptions) {
			this.drawingOptions = drawingOptions;
		}
        public void setAscending(final boolean ascending){
        	direction = ascending ? 1 : -1;
        }
        public boolean isAscending(){
        	return direction == 1;
        }
	}
   
   /*
    * Iterates over ordered edges list and calculates the numeric ids
    * for the main pipes and for the branches. The ids are set on the list
    * and a branch map is created in the CutSheet sub-object.
    * @param dwg the drawing
    * @param area the ares
    *
   private void numberEdges(final FloorDrawing dwg,
		   final DrawingArea area){
       logger.info("+numberEdges");
    AreaBody areaBody = area.getAreaBody();
   	double ignoreSize = //dwg.getOptionsRoot().getShortPipe();
   		area.getAreaOptions().getShortPipe().doubleValue();
   	int branchNumber = -1; // initialized to compile - otherwise should not be used
    int ignoreNumber = -1;
   	int mainNumber = area.getAreaOptions().getMainStartNo() == null ? 1 : area.getAreaOptions().getMainStartNo();
   	Map<Integer, Pipe> branchLookup = new HashMap<Integer, Pipe>();
	List<Pipe> chain = new ArrayList<Pipe>();
   	for(Pipe e : areaBody.getEdgesInOrder()){
		PipeFitting source = areaBody.getStartFitting(e);
   		if(e.getDesignation() == Designation.Main){
   			if(!areaBody.isInitialGroovedPipe(e)){
   				continue;
   			}
   			Pipe nextMain = e;
   			boolean isIgnored = e.isIgnored();
   			chain.clear();
   			chain.add(e);
   			while(!areaBody.isTerminalGroovedPipe(nextMain)){
   				PipeFitting nextEnd = areaBody.getEndFitting(nextMain);
   		        Direction endDirection = nextMain.getEndAttachment().getDirectionInFitting();
   		       	Direction oppositeDirection = nextEnd.getFitting().getType().antipode(endDirection);
   		       	nextMain = areaBody.getPipe(nextEnd, oppositeDirection);
                if(!nextMain.isIgnored()){
                	isIgnored = false;
                }
                chain.add(nextMain);
   		    }
   			if(!isIgnored){
   				for(Pipe p : chain){
   					p.setId(mainNumber);
   				}
   				mainNumber++;
   			}else{
   				for(Pipe p : chain){
   					p.setId(ignoreNumber);
   				}
   				ignoreNumber--;
   			}
   			// treaded cut-sheet report
   			DrawingLayer dl = dwg.getOptionsRoot().findLayer(e.getLayerName());
   			if(dl != null && dl.getSubType() == Attachment.threaded){
   				CutSheetInfo cs = area.cutSheetInfoForPipe(e);
   				area.getAreaCutSheet().getMainThreadedList().add(cs);
   			}
   		}else{
   			// branch cut-sheet report
   			if(isIgnoredBranch(areaBody, e, ignoreSize)){
   				e.setIgnored(true);
   				continue;
   			}
   			if(areaBody.isOnMain(source)){
   				branchNumber = calcBranchNumber(area, e, branchLookup);
   				branchLookup.put(branchNumber, e);
   			}
   			e.setId(branchNumber);
   		}
   	}
       logger.info("-numberEdges");
   }
   */
   /**
    * Generates list of outlet records for side pipes at a given fitting
    * @param area
    * @param bdOffest
    * @param toPipe
    * @param pipeFitting
    * @param isWeld
    * @return
    */
   public List<OutletInfo> outletInfosForFitting(DrawingArea area, BigDecimal bdOffest, Pipe toPipe, PipeFitting pipeFitting,
		   boolean isWeld){
	   AreaBody areaBody = area.getAreaBody();
	   List<OutletInfo> ret = new ArrayList<OutletInfo>();
	   
	   // iterate over side pipes. If none, produce one record anyway
	   List<Pipe> sidePipes = areaBody.getSidePipes(toPipe, pipeFitting);
	   for(int sideIndex = 0; sideIndex < Math.max(sidePipes.size(), 1); sideIndex++){
		   Pipe sidePipe = sideIndex < sidePipes.size() ? sidePipes.get(sideIndex) : null;
		   
		   OutletInfo out = outletInfoForFitting(area, bdOffest, toPipe, pipeFitting, isWeld, sidePipe);
		   /*
		   if(sideIndex > 0){
			   // if the 2nd outlet is the same as the first, just set the count to 2
			   Diameter d0 = ret.get(0).getDiameter();
			   Diameter d = out.getDiameter();

			   if(d != null && d0 != null && d.compareTo(d0) == 0 ){
				   ret.get(0).setSideCount(2);
				   break;
			   }
		   }	
		   */
		   ret.add(out);
	   }   
	   return ret;
   }
   /**
    * Generates an outlet record for a side pipe at a given fitting
    * @param area
    * @param bdOffest
    * @param toPipe
    * @param pipeFitting
    * @param isWeld
    * @param sidePipe the side pipe, or null to produce a record without diameters
    * @return
    */
   public OutletInfo outletInfoForFitting(DrawingArea area, BigDecimal bdOffest, Pipe toPipe, PipeFitting pipeFitting,
		   boolean isWeld, Pipe sidePipe){
	  logger.info("+outletInfoForFitting");
	   AreaBody areaBody = area.getAreaBody();
	   OutletInfo outlet = new OutletInfo();
	   
	   outlet.setOffset(bdOffest);
	   outlet.setJumpLocation(pipeFitting.getJump());
	   outlet.setSideCount(0);
	   Direction sideDirection = null;
	   Fitting fitting = pipeFitting.getFitting();
	   Attachment sideAttachment = null;
	   if(sidePipe != null){
		   sideDirection = areaBody.getAttachmentToFitting(sidePipe, pipeFitting).getDirectionInFitting();
		   sideAttachment = fitting.attachment(sideDirection);
	   }
	   if(sideAttachment == null){
		   sideAttachment = fitting.getAttachment();
	   }
	   outlet.setAttachment(sideAttachment);
	   
	   if(sidePipe != null){
		   Diameter d = sidePipe.getDiameter();
		   outlet.setDiameter(d);
		   if(pipeFitting.getJump() == Jump.NONE && !sidePipe.isVertical()){ // plane orientation
//			   ObjectMapper om = new ObjectMapper();

				   // find orientation of tee
//			   if(sidePipe.getDesignation() == Designation.Head){
				   // for horizontal/unknown heads outlet location cannot be determined
//				   outlet.setSideCount(0);
//			   }else{
				   PipeFitting startOfToPipe = areaBody.getEndFitting(toPipe);
				   if(startOfToPipe == pipeFitting){
					   startOfToPipe = areaBody.getStartFitting(toPipe);
				   }
				   PipeFitting endOfSidePipe = areaBody.getEndFitting(sidePipe);
				   if(endOfSidePipe == pipeFitting){
					   endOfSidePipe = areaBody.getStartFitting(sidePipe);
				   }
				   boolean hasAngle = true;
				   PipeFitting[] triple = {startOfToPipe, pipeFitting, endOfSidePipe};
				   for(PipeFitting pf : triple) {
					   if(pf == null || pf.getCenter() == null) {
						   hasAngle = false;
					   }
				   }
				   if(hasAngle) {
					   double sign = planeGeo.signAngleMeasure(triple);
					   outlet.setSideCount(sign < 0 ? -1 : 1);					   
				   }else {
						logger.error("Side pipe without end point (cannot position): " + sidePipe);
				   }
			   
		   }		   
		   if(!isWeld){
				Diameter mechd = mechanicalDiameter(toPipe.getDiameter(), outlet.getDiameter());
				outlet.setDiameter(mechd);
		   }  
	   } 
	logger.info("-outletInfoForFitting");
	   return outlet;
   }
   /*
   public OutletInfo outletInfoForFitting(DrawingArea area, DwgPoint startCenter, double dStartTakeout,
		   		PipeFitting pipeFitting, Pipe toPipe){
	   AreaBody areaBody = area.getAreaBody();
	   int roundScale = area.getAreaOptions().getTakeoutRounding();
	   long roundFactor = Math.round(Math.pow(2, roundScale));
	   BigDecimal bdRoundFactor = new BigDecimal(roundFactor);

	   OutletInfo outlet = new OutletInfo();
	   
	   DwgPoint center = pipeFitting.getCenter();
	   double dOffest = this.planeGeo.distance(startCenter, center) - dStartTakeout;
	   BigDecimal bdOffest = (roundScale > 0) ?
	   			(new BigDecimal(Math.round(roundFactor*dOffest))).divide(bdRoundFactor) :
	   			(new BigDecimal(Math.round(dOffest)));
	   outlet.setOffset(bdOffest);
	   
	   List<Pipe> sidePipes = areaBody.getSidePipes(toPipe, pipeFitting);
	   if(sidePipes.size() > 0){
		   Pipe sidePipe = sidePipes.get(0);
		   Diameter d = sidePipe.getDiameter();
		   outlet.setDiameter(d); // TODO: for grooved this needs to be a hole defined in table
	   }
	   
	   return outlet;
   }
   */
   
   // TODO: move to takeout xml
   public Diameter mechanicalDiameter(Diameter md, Diameter od){
	   if(md == Diameter.D8){
		   if(od == Diameter.D4){
			   return Diameter.D45;
		   }
		   if(od == Diameter.D3){
			   return Diameter.D35;
		   }
		   if(od == Diameter.D25 || od == Diameter.D2){
			   return Diameter.D275;
		   }
	   }else if(md == Diameter.D6){
		   if(od == Diameter.D4){
			   return Diameter.D45;
		   }
		   if(od == Diameter.D3){
			   return Diameter.D35;
		   }
		   if(od == Diameter.D25){
			   return Diameter.D275;
		   }
		   if(od == Diameter.D2){
			   return Diameter.D25;
		   }
		   if(od == Diameter.D15){
			   return Diameter.D2;
		   }
		   if(od == Diameter.D125){
			   return Diameter.D175;
		   }
	   }else if(md == Diameter.D4){
		   if(od == Diameter.D3){
			   return Diameter.D35;
		   }
		   if(od == Diameter.D25){
			   return Diameter.D275;
		   }
		   if(od == Diameter.D2){
			   return Diameter.D25;
		   }
		   if(od == Diameter.D15){
			   return Diameter.D2;
		   }
		   if(od == Diameter.D125){
			   return Diameter.D175;
		   }
		   if(od == Diameter.D1 || od == Diameter.D05){
			   return Diameter.D175;
		   }
	   }else if(md == Diameter.D3){
		   if(od == Diameter.D2){
			   return Diameter.D25;
		   }
		   if(od == Diameter.D15){
			   return Diameter.D2;
		   }
		   if(od == Diameter.D125){
			   return Diameter.D175;
		   }
		   if(od == Diameter.D1 || od == Diameter.D05){
			   return Diameter.D15;
		   }
	   }else if(md == Diameter.D25){
		   if(od == Diameter.D15){
			   return Diameter.D2;
		   }
		   if(od == Diameter.D125){
			   return Diameter.D175;
		   }
		   if(od == Diameter.D1 || od == Diameter.D05){
			   return Diameter.D15;
		   }
	   }else if(md == Diameter.D2){
		   if(od == Diameter.D15 || od == Diameter.D125){
			   return Diameter.D175;
		   }
		   if(od == Diameter.D1 || od == Diameter.D05){
			   return Diameter.D15;
		   }
	   }
	   return null;
   }
   
   public MainCutSheetInfo mainCutSheetInfoForChain(DrawingArea area, List<PipeFitting> chainList, boolean isWeld){
	   AreaBody areaBody = area.getAreaBody();
//	   int roundScale = area.getAreaOptions().getTakeoutRounding();
//	   long roundFactor = Math.round(Math.pow(2, roundScale));
//	   BigDecimal bdRoundFactor = new BigDecimal(roundFactor);

	   MainCutSheetInfo cInfo = new MainCutSheetInfo();
	   for(int pipeIndex = 0; pipeIndex < chainList.size() - 1; pipeIndex++){
			Pipe nextMain = areaBody.getPipeFromChain(chainList, pipeIndex); 
			Integer id = nextMain.getId();
           if(id  != null){
           	cInfo.setId(id);
           	Diameter d = nextMain.getDiameter();
           	cInfo.setDiameter(d);
           }
		}
		List<OutletInfo> outlets = cInfo.getOutlets();
		BigDecimal length = CommonDecimal.Zero.getMeasure();
		Pipe pipe = null;
		for(int i = 0; i < chainList.size() - 1; i++){
			if(i > 0){
				PipeFitting pf =  chainList.get(i);
				List<OutletInfo> pfOutlets = this.outletInfosForFitting(area, length, pipe, pf, isWeld);
	   			outlets.addAll(pfOutlets);
			}
			pipe = areaBody.getPipeFromChain(chainList, i);
			BigDecimal pipeLength = pipe.getAfterTakeout();
			length = length.add(pipeLength);
		}
		cInfo.setCutLength(length);
			/*
			
			PipeFitting startFitting = chainList.get(0);
			Pipe startPipe = areaBody.getPipeFromChain(chainList, 0);
			PipeAttachment startAttachment = areaBody.getAttachmentToFitting(startPipe, startFitting);
			
			PipeFitting endFitting = chainList.get(chainList.size() - 1);
			Pipe endPipe = areaBody.getPipeFromChain(chainList, chainList.size() - 2);
			PipeAttachment endAttachment = areaBody.getAttachmentToFitting(endPipe, endFitting);
			
			DwgPoint startCenter = startFitting.getCenter();
			DwgPoint endCenter = endFitting.getCenter();
			Double span = planeGeo.distance(startCenter, endCenter);
			BigDecimal startTakeout = startAttachment.getTakeout();
			double dStartTakeout = startTakeout != null ? startTakeout.doubleValue() : 0.0;
			BigDecimal endTakeout = endAttachment.getTakeout();
			double dEndTakeout = endTakeout != null ? endTakeout.doubleValue() : 0.0;
			Double dLength = span - dStartTakeout - dEndTakeout;
			BigDecimal bdLength = (roundScale > 0) ?
	   			(new BigDecimal(Math.round(roundFactor*dLength))).divide(bdRoundFactor) :
	   			(new BigDecimal(Math.round(dLength)));
	   		cInfo.setCutLength(bdLength);
	   		
	   		List<OutletInfo> outlets = cInfo.getOutlets();
	   		for(int i = 1; i < (chainList.size() - 1); i++){
	   			OutletInfo outlet = this.outletInfoForFitting(area, startCenter, dStartTakeout,
	   					chainList.get(i), areaBody.getPipeFromChain(chainList, i - 1));
	   			if(!isWeld){
		   			Diameter mechd = mechanicalDiameter(cInfo.getDiameter(), outlet.getDiameter());
		   			outlet.setDiameter(mechd);
	   			}
	   			outlets.add(outlet);
	   		}
	   		*/
	 
		
 		return cInfo;
   }
   
   public MainCutSheetInfo mainCutSheetInfoForJump(Pipe e){
  		MainCutSheetInfo cInfo = new MainCutSheetInfo();
  		Integer id = e.getId();
  		Diameter d = e.getDiameter();
  		cInfo.setId(id);
  		cInfo.setDiameter(d);
  		return cInfo;
   }

   
   /**
    * Iterates over ordered edges list and calculates the numeric ids
    * for the main pipes and for the branches. The ids are set on the list
    * and a branch map is created in the CutSheet sub-object.
    * @param dwg the drawing
    * @param area the ares
    */

   private void numberEdges2(final FloorDrawing dwg,
		   final DrawingArea area){
       logger.info("+numberEdges");
    AreaBody areaBody = area.getAreaBody();
   	double ignoreSize = //dwg.getOptionsRoot().getShortPipe();
   		area.getAreaOptions().getShortPipe().doubleValue();
   	int branchNumber = -1; // initialized to compile - otherwise should not be used
    int ignoreNumber = -1;
   	int mainNumber = area.getAreaOptions().getMainStartNo() == null ? 1 : area.getAreaOptions().getMainStartNo();
   	Map<Integer, Pipe> branchLookup = new HashMap<Integer, Pipe>();
   	
	String prefix = area.getAreaOptions().getMainLabel();
	if(prefix == null){
		prefix = dwg.getOptionsRoot().getMainPrefix();
	}
	if(prefix == null){
		prefix = "M-";
	}

//	List<Pipe> chain = new ArrayList<Pipe>();
   	for(Pipe e : areaBody.getEdgesInOrder()){
		PipeFitting source = areaBody.getStartFitting(e);
   		if(e.getDesignation() == Designation.Main){
   			DrawingLayer dl = dwg.getOptionsRoot().findLayer(e.getLayerName());
   			List<MainCutSheetInfo> mainGrCutSheetList = null;
   			List<CutSheetInfo> mainThCutSheetList = null;
   			boolean isWeld = false;
   			if(dl != null){
   				if(dl.getSubType() == Attachment.threaded){
   					mainThCutSheetList = area.getAreaCutSheet().getMainThreadedList();
   				}else if(dl.getSubType() == Attachment.grooved){
   					mainGrCutSheetList = area.getAreaCutSheet().getMainGroovedList();
   				}else{
   					isWeld = true;
   					mainGrCutSheetList = area.getAreaCutSheet().getMainWeldedList();
   				}
   			}
   					

   			logger.info("found main pipe");
   			if(e.isVertical()){
				e.setId(mainNumber);		
				mainNumber++;
				if(mainGrCutSheetList != null){
					logger.info("adding grvd jump " + mainNumber);
					MainCutSheetInfo cs = this.mainCutSheetInfoForJump(e);
					cs.setPrefix(prefix);
					mainGrCutSheetList.add(cs);
				}
				if(mainThCutSheetList != null){
   	   				logger.info("adding thrd jump " + mainNumber);
   	   				CutSheetInfo cs = area.cutSheetInfoForPipe(e);
   	   				mainThCutSheetList.add(cs);
   	   			}
				continue;
   			}
   			if(!areaBody.isInitialGroovedPipe(e) && !areaBody.isTerminalGroovedPipe(e)){
				logger.info("ignoring middle pipe");
   				continue; // continues on both sides
   			}
   			// break on at least one side
   			PipeFitting chainStart = areaBody.isInitialGroovedPipe(e) ? areaBody.getStartFitting(e)
   					: areaBody.getEndFitting(e);
   			Direction chainDir = areaBody.getAttachmentToFitting(e, chainStart).getDirectionInFitting();
   			List<PipeFitting> chainList = this.getPipeFittingList();
   			chainList.clear();
   			areaBody.fillChainList(chainList, chainStart, chainDir, false); // longest unbroken chain
//   			chain.clear();
   			boolean isIgnored = true;
   			boolean isNumbered = false;
   			Pipe maxSpanPipe = null;
   			for(int pipeIndex = 0; pipeIndex < chainList.size() - 1; pipeIndex++){
   				Pipe nextMain = areaBody.getPipeFromChain(chainList, pipeIndex); 
   				if(maxSpanPipe == null || nextMain.getSpan().compareTo(maxSpanPipe.getSpan()) > 0){
   					maxSpanPipe = nextMain;
   				}
                if(!nextMain.isIgnored()){
                	isIgnored = false; // chain cannot be ignored
                }
                if(nextMain.getId() != null){
                	isNumbered = true; // chain has been numbered already
                }
   			}
   			if(!isNumbered && !isIgnored){
   				maxSpanPipe.setId(mainNumber);
				mainNumber++;
   				
	   			// populate main cut-sheet report
   	   			if(mainThCutSheetList != null){
   	   				logger.info("adding threaded cutsheet");
   	   				CutSheetInfo cs = area.cutSheetInfoForPipe(e);
   	   				mainThCutSheetList.add(cs);
   	   			}else if(mainGrCutSheetList != null){
   	   				logger.info("adding grooved/welded cutsheet");
   	   				MainCutSheetInfo cs = this.mainCutSheetInfoForChain(area, chainList, isWeld);
   	   				cs.setPrefix(prefix);
   	   				mainGrCutSheetList.add(cs);
   	   			}
   			}else{
   				logger.info("Skipping: isNumbered=" + isNumbered + ",isIgnored=" + isIgnored);
   			}
   		}else{
   			// branch cut-sheet report
   			if(isIgnoredBranch(areaBody, e, ignoreSize)){
   				e.setIgnored(true);
   				continue;
   			}
   			if(areaBody.isOnMain(source)){
   				branchNumber = calcBranchNumber(area, e, branchLookup);
   				branchLookup.put(branchNumber, e);
   			}
   			e.setId(branchNumber);
   		}
   	}
       logger.info("-numberEdges");
   }
   /**
    * Creates an entry in the cut-sheet Branch Map.
    * Determines branch number for the next branch start encountered in the iteration.
    * If the next branch matches one already in the branch map then its number is used
    * and the multiplicity counter is incremented. Otherwise a new entry in
    * the branch map is created with the branch number which is higher by one
    * than the highest existing one.
    * 2 Branches are considered equal if they have the same attachment type
    * to the main and if their shapes are metrically equivalent
    * (see AreaBody.branchStartsEqual for details)
    * @param area the area
    * @param e the next branch start
    * @param branchLookup all branch starts visited so far - indexed by branch number
    * @return the calculated branch number
    */
   private int calcBranchNumber(final DrawingArea area, final Pipe e, final Map<Integer, Pipe> branchLookup){
   	AreaBody areaBody = area.getAreaBody();
   	AreaCutSheet cutSheet = area.getAreaCutSheet();
   	int branchNumber;
   	BranchInfo info;
   	PipeFitting startPt = areaBody.getStartFitting(e);

   	for(Map.Entry<Integer, BranchInfo> entry : cutSheet.getBranchMap().entrySet()){
       	branchNumber = entry.getKey();
       	info = entry.getValue();
       	Pipe brStart = info.getOrigin();
       	PipeFitting brStartPt = areaBody.getStartFitting(brStart);
       	if( !startPt.getFitting().getAttachment().equals(
       			brStartPt.getFitting().getAttachment())){
       		continue; // mismatch on start attachment type
       	}
       	Pipe sampleBranchStart = branchLookup.get(branchNumber);
       	if(areaBody.branchStartsEqual(e, sampleBranchStart)){

       		info.setMultiplicity(info.getMultiplicity() + 1);
       		return branchNumber;
       	}
       }
   	// no match found
   	SortedMap<Integer, BranchInfo> branchMap = cutSheet.getBranchMap();
   	if(branchMap.isEmpty()){
   		branchNumber = area.getAreaOptions().getBranchStartNo() == null ? 1 : area.getAreaOptions().getBranchStartNo();
   	}else{
   	    branchNumber = branchMap.lastKey().intValue() + 1;
   	}
   	info = new BranchInfo();
   	info.setMultiplicity(1);
   	info.setOrigin(e);
   	PipeAttachment pa = e.getStartAttachment();
   	Direction dif = pa.getDirectionInFitting();
   	Fitting f = startPt.getFitting();
   	Attachment a = f.attachment(dif);
   	if(a == null){
   		a = f.getAttachment();
   	}
   	info.setOriginalAttachment(a);
   	cutSheet.getBranchMap().put(branchNumber, info);
   	return branchNumber;
   }

   /**
    * Tests if a branch pipe is ignored, i.e.,
    * is short and either is a leaf or is followed by short leafs.
    * @param areaBody area containing the tested pipe
    * @param e the tested pipe
    * @param ignoreSize defines what short means
    * @return <code>true</code> if it is
    */
   private boolean isIgnoredBranch(final AreaBody areaBody, final Pipe e, final double ignoreSize){
   	PipeFitting target = areaBody.getEndFitting(e); //e.getEntEnd();//areaBody.getPointGraph().getEdgeTarget(e);
   	PipeFitting source = areaBody.getStartFitting(e);//areaBody.getPointGraph().getEdgeSource(e);
   	if(PlaneGeo.distance(source, target) > ignoreSize){
   		return false; // this is not short
   	}
   	for(Pipe e2 : areaBody.getPipeGraph().edgesOf(target)){
   		if(e2.getDesignation() == Designation.Branch &&  e2 != e){
   	    	PipeFitting target2 = areaBody.getEndFitting(e2);//e2.getEntEnd();//areaBody.getPointGraph().getEdgeTarget(e2);
   	    	if(areaBody.degreeOf(target2) > 1){
   	    		return false; // follower is not leaf
   	    	}
   	    	if(PlaneGeo.distance(target, target2) > ignoreSize){
   	    		return false; // follower is not short
   	    	}
   		}
   	}
   	return true;
   }

/**
 * Breaks and inserts couplings for long pipes in an area of a drawing.
 * Will do it only if treaded/grooved cut sizes are present.
 * @param dwg the drawing
 * @param areaBody the area
 */
   private Readiness subdividePipes(final FloorDrawing dwg, final DrawingArea area){
       logger.info("+subdividePipes");
  //     AreaBody areaBody = area.getAreaBody();
       Readiness ret = Readiness.Ready;
       // collect cutting options
       AreaOptions options = // dwg.getOptionsRoot();
       		area.getAreaOptions();
       List<Long> threadedSizes = options.getBranchCutList();
       List<Long> groovedSizes = options.getMainCutList();
       Long longPipe = options.getLongPipe();
       Long shortPipe = options.getShortPipe();
       Long cutSpace = options.getMainCutSpace();
       if(threadedSizes == null || groovedSizes == null || longPipe == null || shortPipe == null || cutSpace == null){
    	   ret = Readiness.BadLengthOptionFormat;
       }else{
	       if(threadedSizes.size() > 0){
	    	//   subdivideThreaded(dwg, area);
	       }
	       if(groovedSizes.size() > 0){
	    	   ret = subdivideAll(dwg, area);
	       }
       }
       logger.info("-subdividePipes->" + ret);
       return ret;
    }

   /**
    * Checks if a pipe continues (un-broken) beyond its start vertex.
    * @param areaBody the area
    * @param pipe the pipe
    * @return <code>true</code> if it does not continue
    *
   private boolean isInitialGroovedPipe(final AreaBody areaBody, final Pipe pipe){
       return isExtremeGroovedPipe(areaBody, pipe, areaBody.getStartFitting(pipe));
    }*/
/**
 * Checks if a pipe continues (un-broken) beyond its end vertex.
 * @param areaBody the area
 * @param pipe the pipe
 * @return <code>true</code> if it does not continue
 *
    private boolean isTerminalGroovedPipe(final AreaBody areaBody, final Pipe pipe){
	       return isExtremeGroovedPipe(areaBody, pipe, areaBody.getEndFitting(pipe));
	}*/
   
	/**
	 * Checks if a pipe continues (un-broken) beyond one of its extreme vertices.
	 * @param areaBody the area
	 * @param pipe the pipe
	 * @param pf the vertex
	 * @return <code>true</code> if it does not continue
	 *
    private boolean isExtremeGroovedPipe(final AreaBody areaBody, final Pipe pipe, final PipeFitting pf){
		 PipeFitting startPf = areaBody.getStartFitting(pipe);
		 Fitting f = pf.getFitting();
         if(!f.isHoleBased()){
        	 return true;
         }
         // mechanical or welded.
         // find how is 'pipe' attached to fitting 'f'.
         // If 'tee' or cross with different diameters then main run will be E-W
         PipeAttachment pa = (pf == startPf) ? pipe.getStartAttachment()
        		 : pipe.getEndAttachment();
		 Direction pd = pa.getDirectionInFitting();
         if(f.getType().getEndCount() == 3
        		 || f.getDiameterList().size() > 1){
        	 return !pd.equalOrOpposite(Direction.E);
         }
         // must be welded cross with equal diameters.
         // we just need a way to choose main-run.
         // find first incoming(?)
         int firstIndex = areaBody.getEdgesInOrder().size();
         Direction firstDirection = null;
         for(Direction d : f.getType().getDirections()){
        	 Pipe p = areaBody.getPipe(pf, d);
        	 PipeFitting end = areaBody.getEndFitting(p);
        	 if(end != pf){
        		 continue;
        	 }
        	 // incoming
        	 int index = areaBody.getEdgesInOrder().indexOf(p);
        	 if(index < firstIndex){
        		 firstIndex = index;
        		 firstDirection = d;
        	 }
         }
    	 return !pd.equalOrOpposite(firstDirection);
    }*/
/*
   private void subdivideOneTreaded(final DrawingOptions options, final AreaBody areaBody, final Pipe bp, final double maxSize, final double cutSize){
 //      logger.info("+subdivideOneTreaded(" + bp + ")");
       PipeFitting start = areaBody.getStartFitting(bp);
       PipeFitting end = areaBody.getEndFitting(bp);
       List<Point> cuts = getThreadCutList();
       planeGeo.subdivide(DwgPoint.class, start, end, maxSize, cutSize, cuts);
       if(cuts.isEmpty()){
 //          logger.info("+subdivideOneTreaded->No cuts");
           return; // nothing to do
       }
       List<PipeFitting> breakups = areaBody.subdivideGraph(bp, cuts);
       updateBreakups(options, areaBody, bp, start, end, breakups);
//       logger.info("-subdivideOneTreaded->" + cuts.size() + " cuts");
   }
   */
   /*
    * Adjust locations of vertices on a grooved chain so that linear distances
    * from the start point are whole numbers.
    * @param start start of the chain
    * @param end end of the chain
    * @param avoids vertices on the chain
    *
   private void alignAvoids(PipeFitting start, PipeFitting end, List<PipeFitting> avoids){
	   // create unit vector for the segment we divide
	   double[] u = PlaneGeo.difference(end, start);
	   PlaneGeo.streachInPlace(u, 1.0/PlaneGeo.norm(u));
	   for(PipeFitting avoid : avoids){
		   long dist = Math.round(PlaneGeo.distance(start, avoid));
		   double x = start.x(0) + u[0]*dist;
		   double y = start.x(1) + u[1]*dist;
		   avoid.getCenter().setX(x);
		   avoid.getCenter().setY(y);
	   }
   }*/
   /*
    * Subdivide one grooved pipe.
    * This operation assumes that between-holes pieces of a grooved pipe are ordered consistently.
    * @param areaBody
    * @param m
    * @param maxPipe
    * @param sizes
    * @param margin
    *
   private boolean subdivideOneGrooved(final DrawingOptions options, final AreaBody areaBody, final Pipe m, final double maxPipe, final Collection<Double> sizes, final double margin){
       logger.info("+subdivideOneGrooved(" + m + ")");
       boolean ret = true;
       PipeFitting trueEndFitting = areaBody.getEndFitting(m);
       List<PipeFitting> avoids = getAvoidedPoints();
       avoids.clear();
       List<Pipe> subs = getPipeSubdivision();
       subs.clear();
       subs.add(m);
       Pipe next = m;
       while(!areaBody.isTerminalGroovedPipe(next)){
       	avoids.add( trueEndFitting);
        Direction endDirection = next.getEndAttachment().getDirectionInFitting();
       	Direction oppositeDirection = trueEndFitting.getFitting().getType().antipode(endDirection);
       	next = areaBody.getPipe(trueEndFitting, oppositeDirection);
       	subs.add(next);
       	trueEndFitting = areaBody.getEndFitting(next);
       }
       //alignAvoids(areaBody.getStartFitting(m), trueEndFitting, avoids);

       Map<Point, List<Point>> cutMap = getSubMap();
       ret = planeGeo.subdivide(DwgPoint.class, areaBody.getStartFitting(m), trueEndFitting, maxPipe, sizes, avoids, margin, cutMap);
       if(ret){
	       for(Pipe sub : subs){
	       	 List<Point> cuts = cutMap.get(areaBody.getStartFitting(sub));
	       	 if(cuts != null){
	       		PipeFitting subStart = areaBody.getStartFitting(sub);
	       		PipeFitting subEnd = areaBody.getEndFitting(sub);
	       		List<PipeFitting> breakups = areaBody.subdivideGraph(sub, cuts);
	            updateBreakups(options, areaBody, sub, subStart, subEnd, breakups);
	       	}
	       }
       }
       logger.info("-subdivideOneGrooved->" + ret);
       return ret;
   }
   */
   private void updateBreakups(final DrawingOptions options, final AreaBody areaBody, final Pipe oldPipe, final PipeFitting start, final PipeFitting end, final List<PipeFitting> cuts){
//logger.debug("+updateBreakups:" + oldPipe + " old takeouts-" + oldPipe.getStartAttachment().getTakeout()
//		+ "," + oldPipe.getEndAttachment().getTakeout());
	   Pipe startPipe = areaBody.getPipeGraph().getEdge(start, cuts.get(0));
       Pipe endPipe = areaBody.getPipeGraph().getEdge(end, cuts.get(cuts.size() - 1));
       // transfer attachment info from old pipe
       startPipe.setStartAttachment(oldPipe.getStartAttachment());
       endPipe.setEndAttachment(oldPipe.getEndAttachment());
//	   List<Pipe> pipes = new ArrayList<Pipe>();
	   DrawingLayer layer = options.findLayer(oldPipe.getLayerName());
       for(PipeFitting cut : cuts){
    	   List<Pipe> pipes = getPipeList();
    	   pipes.clear();
    	   pipes.addAll(areaBody.getPipeGraph().edgesOf(cut));
    	   Attachment attachment = Attachment.threaded;
    	   if(layer.getType() == Designation.Main){
    		   if(layer.getSubType() == Attachment.grooved||layer.getSubType() == Attachment.weldedGroove){
    			   attachment = Attachment.grooved;
    		   }
    	   }
           setupOneFitting(options, areaBody, cut, Type.Coupling, attachment, layer.getVendor(), pipes);
       }
//logger.debug("-updateBreakups: new start takeouts" + startPipe.getStartAttachment().getTakeout()
//		+ "," + startPipe.getEndAttachment().getTakeout());
//logger.debug("-updateBreakups: new end takeouts" + endPipe.getStartAttachment().getTakeout()
//		+ "," + endPipe.getEndAttachment().getTakeout());
   }
   private void updateBreakups2(final DrawingLayer layer,
		   final AreaBody areaBody,
		   final PipeAttachment oldPipeStartAttachment,
		   final PipeAttachment oldPipeEndAttachment,
		   final PipeFitting start,
		   final PipeFitting end, final List<PipeFitting> cuts){

	   Pipe startPipe = areaBody.getPipeGraph().getEdge(start, cuts.get(0));
	   Pipe endPipe = areaBody.getPipeGraph().getEdge(end, cuts.get(cuts.size() - 1));
	   // transfer attachment info from old pipe
	   startPipe.setStartAttachment(oldPipeStartAttachment);
	   endPipe.setEndAttachment(oldPipeEndAttachment);
	   for(PipeFitting cut : cuts){
     	   List<Pipe> pipes = getPipeList();
     	   pipes.clear();
     	   pipes.addAll(areaBody.getPipeGraph().edgesOf(cut));
     	   Attachment attachment = Attachment.threaded;
     	   if(layer.getType() == Designation.Main){
     		   if(layer.getSubType() == Attachment.grooved||layer.getSubType() == Attachment.weldedGroove){
     			   attachment = Attachment.grooved;
     		   }
     	   }
           setupOneFitting(null, areaBody, cut, Type.Coupling, attachment, layer.getVendor(), pipes);
        }
    }

	public PlaneGeo getPlaneGeo() {
		return planeGeo;
	}

	public void setPlaneGeo(final PlaneGeo planeGeo) {
		this.planeGeo = planeGeo;
	}

	public TakeoutRepository getTakeout() {
		return takeout;
	}

	public void setTakeout(final TakeoutRepository takeout) {
		this.takeout = takeout;
	}
	public Fitting.Factory getFittingFactory() {
		return fittingFactory;
	}
	public void setFittingFactory(final Fitting.Factory fittingFactory) {
		this.fittingFactory = fittingFactory;
	}
	public PipeConfig getPipeConfig() {
		return pipeConfig;
	}
	public void setPipeConfig(PipeConfig pipeConfig) {
		this.pipeConfig = pipeConfig;
	}

}
