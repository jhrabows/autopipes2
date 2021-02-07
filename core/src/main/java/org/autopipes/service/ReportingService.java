package org.autopipes.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.DrawingLayer;
import org.autopipes.model.DwgEntity;
import org.autopipes.model.DwgPoint;
import org.autopipes.model.DwgSize;
import org.autopipes.model.FloorDrawing;
import org.autopipes.model.Pipe;
import org.autopipes.model.PipeConfig;
import org.autopipes.model.PipeFitting;
import org.autopipes.model.AreaBody.Defect;
import org.autopipes.model.AreaBody.PointInfo;
import org.autopipes.model.AreaCutSheet.CutSheetInfo;
import org.autopipes.model.AreaCutSheet.MainCutSheetComparator;
import org.autopipes.model.DrawingLayer.Designation;
import org.autopipes.takeout.Attachment;
import org.autopipes.takeout.Diameter;
import org.autopipes.takeout.Fitting.Direction;
import org.autopipes.util.CommonDecimal;
import org.autopipes.util.PlaneGeo;
/**
 * Service class containing
 * formatting logic for decorations send back to AutoCAD for rendering.
 * @author janh
 *
 */
public class ReportingService {
    private static Logger logger = Logger.getLogger(ReportingService.class);

    private PipeConfig pipeConfig;
    private PlaneGeo planeGeo;
    private MainCutSheetComparator mainComp;
    
    public MainCutSheetComparator getMainComp(){
    	if(mainComp == null){
    		mainComp = new MainCutSheetComparator();
    	}
    	return mainComp;
    }
  
    public void sortThreadedMain(List<CutSheetInfo> mainList){
		Collections.sort(mainList, getMainComp());
    }
    
    /**
     * Produces entire text associated with branch segments and jumps
     * and the length-portion of the main segment text.
     * @param dwg
     * @param area
     */
    public void renderSizes(final FloorDrawing dwg, final DrawingArea area, boolean doMain, boolean doBranch, boolean doMainE2E, boolean doBranchE2E){
        logger.info("+renderSizes" + (doMain ? " M" : "") + (doMainE2E ? "e2e" : "") + (doBranch ? " B" : "") + (doBranchE2E ? "e2e" : ""));
        if(doMain || doBranch){
        	double h = dwg.getDwgTextSize();
        	List<String> textList = new ArrayList<String>();
            for(Pipe e : area.getAreaBody().getEdgesInOrder()){
            	if(e.isIgnored()){
            		continue; // skip ignorables
            	}
            	boolean mainPipe = e.getDesignation() == Designation.Main;
            	if(!(doMain && mainPipe || doBranch && !mainPipe)){
            		continue; // suppress based on user input
            	}
            	textList.clear();
            	if(e.isVertical()){
            		if(e.getDesignation() == Designation.Branch){
    	        		Diameter jd = e.getDiameter();
    	            	String jlabel = jd.getDisplay() + "\"";
    	            	renderJump(dwg, area, e, jlabel);
            		}
                	continue;
                }
            	boolean startsAbove;
            	int rounding = 0;
            	if(e.getDesignation() == Designation.Branch){
            		// diameter for branches only
                	Diameter d = e.getDiameter();
                	textList.add(d.getDisplay() + "\"");
                	startsAbove = true;
            	}else{
            		startsAbove = false;
        			DrawingLayer layer = dwg.getOptionsRoot().findLayer(e.getLayerName());
        			if(layer.getSubType() == Attachment.grooved
        					|| layer.getSubType() == Attachment.threaded // round threaded main too
        					|| layer.getSubType() == Attachment.weldedGroove
        					|| layer.getSubType() == Attachment.welded){
        				
        				rounding = area.getAreaOptions().getTakeoutRounding();
        			}
            	}
            	// we have changed meaning of doE2E to mean doC2C (do span)
            	BigDecimal span = e.getSpan(),
            			aftercut = e.getAfterTakeout();
            	boolean aftercutPresentAndDifferent = aftercut != null && aftercut.compareTo(span) != 0,
            			includeC2C = false;
            	if(!aftercutPresentAndDifferent){
            		includeC2C = true;
            	}else{
            		if(mainPipe){
            			includeC2C = doMainE2E;
            		}else{
            			includeC2C = doBranchE2E;
            		}
            	}
            	if(includeC2C){
                	textList.add(footInch(span, 0));
            	}

            	// After cut: show if provided and different than P2P
            	//if(mainPipe && doMainE2E || !mainPipe && doBranchE2E){
        		if(aftercutPresentAndDifferent){
            		textList.add(footInch(aftercut, rounding));
        		}
            	//}
            	
                double verticalShift = verticalShift(startsAbove, textList.size(), h);
                DwgEntity text = makeTiltedText(area.getAreaBody().getStartFitting(e).getCenter(), area.getAreaBody().getEndFitting(e).getCenter(), textList,
                		verticalShift, pipeConfig.getTextSideShift(), h, pipeConfig.getTextWidth());
        		renderEntity(dwg, area, text);
            }
        }
        logger.info("-renderSizes");
    }
    private DwgEntity makeTiltedText(final DwgPoint start, final DwgPoint end, final List<String> textList,
    		final double verticalShift, final int sideShift, final double h, final double w){
    	DwgPoint center = new DwgPoint();
    	double tilt = planeGeo.tilt(start, end);
    	center.setX(0.5*(start.getX()
    			+ end.getX())
    			- verticalShift*Math.sin(tilt)
    			- sideShift*Math.cos(tilt));
    	center.setY(0.5*(start.getY()
    			+ end.getY())
    			+ verticalShift*Math.cos(tilt)
    			- sideShift*Math.sin(tilt)
    			);
    	// reposition center
    	DwgEntity ret;
    	if(textList.size() == 1){
    		ret = DwgEntity.createText(center, h, textList.get(0));
    	}else{
    		DwgSize s = new DwgSize();
    		s.setH(h);
    		s.setW(w);
    		ret = DwgEntity.createMultiText(center, s, textList);
    	}
        ret.rotate(tilt);
        return ret;
    }

    /**
     * Produces id-labels for main segments and jumps.
     * A segment label is one line. A jump label is a 2-line text
     * consisting of a segment label and a jump-indicator text.
     * Segment labels appear above just above main segments terminated by fittings.
     * Jump labels are shifted away from jump symbols.
     * @param dwg
     * @param area
     */
    public void renderMainLabelsOld(final FloorDrawing dwg, final DrawingArea area){
        logger.info("+renderMainLabels");
    	double h = dwg.getDwgTextSize();
    	String prefix = area.getAreaOptions().getMainLabel();
    	if(prefix == null){
    		prefix = dwg.getOptionsRoot().getMainPrefix();
    	}
    	if(prefix == null){
    		prefix = "M-";
    	}
//        Pipe startMainPipe = null;
    	List<String> textList = new ArrayList<String>();
        for(Pipe e : area.getAreaBody().getEdgesInOrder()){
        	if(e.getDesignation() != Designation.Main){
        		continue;  // skip branches
        	}
        	if(e.isVertical()){
            	String jumpLabel = e.getDiameter().getDisplay() + "\"" + prefix + e.getId();
        	    renderJump(dwg, area, e, jumpLabel);
        	    continue;
        	}
        	if(!area.getAreaBody().isInitialMainPipe(e) || e.getId() < 0){
        		continue; // not start or start of ignored chain
        	}
        	Pipe nextMainPipe = e;
        	while(!area.getAreaBody().isTerminalMainPipe(nextMainPipe)){
        		PipeFitting endFitting = area.getAreaBody().getEndFitting(nextMainPipe);
                Direction endDirection = nextMainPipe.getEndAttachment().getDirectionInFitting();
               	Direction oppositeDirection = endFitting.getFitting().getType().antipode(endDirection);
               	nextMainPipe = area.getAreaBody().getPipe(endFitting, oppositeDirection);  //getContinuationPipe(p, pf) fitting.getOpposite(m);
            }
        	// reached true end

        	textList.clear();
        	//
        	Diameter d = e.getDiameter();
        	String label = d.getDisplay() + "\"" + prefix + e.getId();
        	textList.add(label);
        	DwgEntity txt = makeTiltedText(area.getAreaBody().getStartFitting(e).getCenter(),
        		area.getAreaBody().getEndFitting(nextMainPipe).getCenter(),
        		textList, pipeConfig.getTextVerticalUpShift1(), pipeConfig.getTextSideShift(), h, 0.0);
    		renderEntity(dwg, area, txt);
        }
        logger.info("-renderMainLabels");
    }
    
    public void renderMainLabels(final FloorDrawing dwg, final DrawingArea area){
        logger.info("+renderMainLabels");
    	double h = dwg.getDwgTextSize();
    	String prefix = area.getAreaOptions().getMainLabel();
    	if(prefix == null){
    		prefix = dwg.getOptionsRoot().getMainPrefix();
    	}
    	if(prefix == null){
    		prefix = "M-";
    	}
//        Pipe startMainPipe = null;
    	List<String> textList = new ArrayList<String>();
    	List<PipeFitting> chainList = new ArrayList<PipeFitting>();
        for(Pipe e : area.getAreaBody().getEdgesInOrder()){
        	if(e.getDesignation() != Designation.Main){
        		continue;  // skip branches
        	}
        	if(e.isVertical()){
            	String jumpLabel = e.getDiameter().getDisplay() + "\"" + prefix + e.getId();
        	    renderJump(dwg, area, e, jumpLabel);
        	    continue;
        	}
   	//		PipeFitting chainStart = area.getAreaBody().getChainStart(e);
        	
        	
        	if(e.getId() == null){
        		continue; // no Id
        	}
        	PipeFitting start = area.getAreaBody().getStartFitting(e);
        	PipeFitting end = area.getAreaBody().getEndFitting(e);

        	textList.clear();
        	//
        	Diameter d = e.getDiameter();
        	String label = d.getDisplay() + "\"" + prefix + e.getId();
        	textList.add(label);
        	DwgEntity txt = makeTiltedText(start.getCenter(),
        			end.getCenter(),
        		textList, pipeConfig.getTextVerticalUpShift1(), pipeConfig.getTextSideShift(), h, 0.0);
    		renderEntity(dwg, area, txt);
        }
        logger.info("-renderMainLabels");
    }

    private void renderJump(final FloorDrawing dwg, final DrawingArea a, final Pipe jump,
    		final String label){
        DwgPoint c = new DwgPoint();
        c.setX(a.getAreaBody().getStartFitting(jump).getCenter().getX()
        		+ pipeConfig.getJumpTextShift()*Math.cos(pipeConfig.getJumpTextDirection()*Math.PI));
        c.setY(a.getAreaBody().getStartFitting(jump).getCenter().getY()
        		+ pipeConfig.getJumpTextShift()*Math.sin(pipeConfig.getJumpTextDirection()*Math.PI));
        List<String> lines = new ArrayList<String>();
        lines.add(label);
        DwgSize size = new DwgSize();
        size.setH(dwg.getDwgTextSize());
        size.setW((double)pipeConfig.getTextWidth());
        String jumpIndicator = dwg.getOptionsRoot().getJumpIndicator() == null ? "jump" : dwg.getOptionsRoot().getJumpIndicator();
        lines.add(jumpIndicator);
        DwgEntity txt = DwgEntity.createMultiText(c, size, lines);
        DwgEntity tracer = DwgEntity.createSegment(a.getAreaBody().getStartFitting(jump).getCenter(), c);
        renderEntity(dwg, a, txt);
        renderEntity(dwg, a, tracer);
    }
/**
 * Puts branch labels on the rendering collection.
 * Iterates over ordered collection of all edges, bypassing main edges.
 * Retrieves branch id from a branch root-pipe, then continues to the last pipe
 * on this branch - this is where the label will be located.
 * @param dwg - the drawing
 * @param area - the area
 */
	public void renderBranchLabels(final FloorDrawing dwg, final DrawingArea area){
        logger.info("+renderBranchLabels");
		Pipe lastBranchEdge = null;
		int lastBranchId = 0;
        for(Pipe edge : area.getAreaBody().getEdgesInOrder()){
        	if(edge.getDesignation() == Designation.Main){
        		continue;
        	}
        	PipeFitting start = area.getAreaBody().getStartFitting(edge);
        	if(area.getAreaBody().isOnMain(start)){
        		// start of a new main
                renderBranchLabel(dwg, area, lastBranchEdge, lastBranchId);
                lastBranchId = edge.isIgnored() ? 0 : edge.getId();
        	}
        	lastBranchEdge = edge;
        }
        renderBranchLabel(dwg, area, lastBranchEdge, lastBranchId);
        logger.info("-renderBranchLabels");
	}
	public void renderBreakups(final FloorDrawing dwg, final DrawingArea area, boolean doMain, boolean doBranch){
        logger.info("+renderBreakups" + (doMain ? " M" : "") + (doBranch ? " B" : ""));
        if(doMain || doBranch){
            for(Map.Entry<DwgPoint, PointInfo> entry : area.getAreaBody().getPointMap().entrySet()){
                PointInfo info = entry.getValue();
                DwgPoint dp = entry.getKey();
                boolean isMainPoint = area.getAreaBody().isOnMain(dp);
                if(doMain && isMainPoint || doBranch && !isMainPoint){
                    for(DwgEntity cup : info.getCouplings()){
                    	if(cup.getId() == null || cup.getId().longValue() == 0){
                    		renderEntity(dwg, area, cup);
                    	}
                    }
                }
            }
        }
        logger.info("-renderBreakups");
    }
/**
 * Creates an Acad text for a branch label and puts it on the rendering collection.
 * The text size will be twice the default drawing text size.
 * @param dwg drawing of the configuration used
 * @param area are of the configuration and rendering used
 * @param edge branch pipe used for label location
 * @param id id of the branch. If id is <code>0</code> no label is generated.
 */
    private void renderBranchLabel(final FloorDrawing dwg, final DrawingArea area, final Pipe edge, final int id){
        if(edge == null){
        	return; // skip first iteration
        }
        if(id == 0){
        	return; // entire branch is to be ignored
        }
    	double h = dwg.getDwgTextSize();
    	String prefix = area.getAreaOptions().getBranchLabel();
    	if(prefix == null){
    		prefix = dwg.getOptionsRoot().getBranchPrefix();
    	}
    	if(prefix == null){
    		prefix = "#";
    	}

		DwgEntity labelTxt = DwgEntity.createText(area.getAreaBody().getEndFitting(edge).getCenter(), 2*h,
				prefix + id);
		renderEntity(dwg, area, labelTxt);
    }

    public void renderAreaIdStamp(final FloorDrawing dwg, final DrawingArea area){
        if(area.getAreaBody().getCenter() == null){
        	return; // area is empty
        }
		// id stamp
		Map<String, String> properties = new HashMap<String, String> ();
		properties.put(DrawingArea.AreaTag.areaId.getTag(), area.getAreaId().toString());
		properties.put(DrawingArea.AreaTag.areaName.getTag(), area.getAreaName());
		properties.put(DrawingArea.AreaTag.mainStartNo.getTag(), area.getAreaOptions().getMainStartNo().toString());
		properties.put(DrawingArea.AreaTag.branchStartNo.getTag(), area.getAreaOptions().getBranchStartNo().toString());
		properties.put(DrawingArea.AreaTag.mainLabel.getTag(), area.getAreaOptions().getMainLabel());
		properties.put(DrawingArea.AreaTag.branchLabel.getTag(), area.getAreaOptions().getBranchLabel());
		properties.put(DrawingArea.AreaTag.takeoutRounding.getTag(), area.getAreaOptions().getTakeoutRounding().toString());

		properties.put(DrawingArea.AreaTag.shortLimit.getTag(), area.getAreaOptions().getIgnoreShorterThan());
		properties.put(DrawingArea.AreaTag.cutLimit.getTag(), area.getAreaOptions().getCutLongerThan());
		properties.put(DrawingArea.AreaTag.cutClearance.getTag(), area.getAreaOptions().getMainCutClearance());
		properties.put(DrawingArea.AreaTag.cutSizeB.getTag(), area.getAreaOptions().getBranchCut());
		properties.put(DrawingArea.AreaTag.cutSizeM.getTag(), area.getAreaOptions().getMainCut());
		
		DwgPoint p = null;
		if(area.getAreaBody().getRaiser() != null){
			p = area.getAreaBody().getRaiser().getCenter();
		}else{
        	p = area.getAreaBody().getCenter();
        }
		DwgEntity idTxt = DwgEntity.createAreaIdStamp(p, properties);
        area.getRendering().add(idTxt);
    }
    /**
     * Puts an Acad entity object on the rendering collection,
     * assigning it output layer (if defined) 
     * @param dwg the drawing where the output layer is defined
     * @param a the area which holds the rendering collection
     * @param e the Acad entity
     */
    private void renderEntity(final FloorDrawing dwg, final DrawingArea a, final DwgEntity e){
    	DrawingLayer out = dwg.getOptionsRoot().getOutputLayer();
    	if(out != null){
    		e.setLy(out.getName());
    	}
    	a.getRendering().add(e);
    }
    private void renderErrorText(final FloorDrawing dwg, final DrawingArea a, final DwgEntity e){
        String ec = dwg.getOptionsRoot().getErrorColor();
        e.setColor(ec);
    	e.rotate(pipeConfig.getErrorTilt()*Math.PI);
    	renderEntity(dwg, a, e);
    }
    public void renderAreaStatus(final FloorDrawing dwg, final DrawingArea area){
        if(area.getAreaBody().getCenter() == null){
        	return; // area is empty
        }
    	double h = dwg.getDwgTextSize();
    	// graph connectivity
    	if(area.getAreaBody().getCenters().size() > 1){
    		for(DwgPoint center : area.getAreaBody().getCenters()){
    			DwgEntity errTxt = DwgEntity.createText(center, h,
    					"Separated part of area " + area.getAreaId());
    			renderErrorText(dwg, area, errTxt);
    		}
    	}

    	// raiser
    	if(area.getAreaBody().getRaiser() == null){
			DwgEntity errTxt = DwgEntity.createText(area.getAreaBody().getCenter(), h,
					"Area " + area.getAreaId() + ": raiser undefined");
			renderErrorText(dwg, area, errTxt);
    	}
    	// render info defects
    	for(Map.Entry<DwgPoint, PointInfo> entry : area.getAreaBody().getPointMap().entrySet()){
            Defect status = entry.getValue().getStatus();
            if(status != Defect.noDefects){
    			DwgEntity errTxt = DwgEntity.createText(entry.getKey(), h, status.toString()
						+ " for area " + area.getAreaId());
    			renderErrorText(dwg, area, errTxt);
    		}
    	}
    }

    public static String footInch(final BigDecimal size, int rounding) {
      long factor = Math.round(Math.pow(10, rounding));
      BigDecimal[] dr = size.divideAndRemainder(CommonDecimal.Dozen.getMeasure());
      BigDecimal bdInches =
        (new BigDecimal(Math.round(factor*dr[1].doubleValue()))).movePointLeft(rounding);
      if(bdInches.compareTo(CommonDecimal.Dozen.getMeasure()) == 0){ // case when size is little less than a whole foot
    	  bdInches = CommonDecimal.Zero.getMeasure();
    	  dr[0] = dr[0].add(CommonDecimal.One.getMeasure());
      }
      BigInteger feet = dr[0].toBigInteger();
      return feet.toString() + "\'-" + inchFraction(bdInches);
    }

    /**
     * Converts a decimal length to fractional inch string representation.
     *
     * @param inches the length
     *
     * @return the representation
     */
    public static String inchFraction(final BigDecimal inches) {
      String ret;
      BigDecimal[] dr = inches.divideAndRemainder(CommonDecimal.One.getMeasure());
      BigDecimal wholeinchesdec = dr[0];
      BigInteger wholeinches = wholeinchesdec.toBigInteger();
      BigDecimal bdFrac = roundToFraction(dr[1]);
      if(bdFrac.equals(CommonDecimal.One.getMeasure())){// case of little under 1"
    	  wholeinches = wholeinches.add(CommonDecimal.One.getMeasure().toBigInteger());
    	  bdFrac = CommonDecimal.Zero.getMeasure();
      }
      String frac = fraction(bdFrac);

      if(frac.equals("")) {
        ret = wholeinches.toString();
      }
      else {
    	  // whole inch must show even when it is 0
        ret = wholeinches.toString() + frac;
      }

      return ret + "\"";
    }
    /**
     * Rounds decimal to the nearest half
     * @param frac decimal reminder
     * @return closest to frac among 0, 1/2,  1.
     */
    public static BigDecimal roundToFraction(final BigDecimal frac){
    	BigDecimal ret = CommonDecimal.Zero.getMeasure();
    	BigDecimal[] q = {CommonDecimal.Zero.getMeasure(),
    			CommonDecimal.Half.getMeasure(),
    			CommonDecimal.One.getMeasure()};
    	for(int i = 0; i < (q.length - 1); i++){
    		if(frac.compareTo(q[i]) == 0){
    			ret = q[i];
    			break;
    		}else if((frac.compareTo(q[i]) >= 0) && (frac.compareTo(q[i + 1]) < 0)){
    			BigDecimal before = frac.subtract(q[i]);
    			BigDecimal after = q[i+1].subtract(frac);
    			ret = after.compareTo(before) > 0 ? q[i] : q[i + 1];
    			break;
    		}
    	}
    	return ret;
    }
    /**
     * Converts a decimal reminder to a string representation.
     * The remainder must be a multiple of 1/4, >= 0 and < 1.
     *
     * @param frac the reminder
     *
     * @return the representation
     */
    public static String fraction(final BigDecimal frac)
    {
      String ret;

      if(frac.equals(CommonDecimal.Zero.getMeasure())){
        ret = "";
      }
      else if(frac.equals(CommonDecimal.Quarter.getMeasure())) {
        ret = "\u00bc";
      }
      else if(frac.equals(CommonDecimal.Half.getMeasure())) {
        ret = "\u00bd";
      }
      else if(frac.equals(CommonDecimal.ThreeQuarters.getMeasure())) {
        ret = "\u00be";
      }
      else {
        throw new IllegalArgumentException(
          "Not a whole quarter fraction:" + frac);
      }

      return ret;
    }
    private double verticalShift(final boolean startsAbove, final int lineCnt, final double textSize) {
      double ret = 0;

      if(!startsAbove) {
        if(lineCnt == 1) {
          ret =  pipeConfig.getTextVerticalDownShift1();//-11; // -12
        }
        else {
          ret = pipeConfig.getTextVerticalDownShift2(); //-2; // -3;
        }
      } else {
        if(lineCnt == 1) {
          ret = pipeConfig.getTextVerticalUpShift1();//3;
        }
        else {
          ret = textVerticalUpShift2(textSize);
        }
      }
      return ret;
    }
    
    private double textVerticalUpShift2(final double txtSize) {
    	return pipeConfig.getTextVerticalUpShift2_5()
    	  + ((pipeConfig.getTextVerticalUpShift2_7() - pipeConfig.getTextVerticalUpShift2_5())/(7.0 - 5.0))*(txtSize - 5.0);
    }

    public double edgeTilt(final DwgEntity e){
    	return planeGeo.tilt(e.getEntStart(), e.getEntEnd());
    }
	public PlaneGeo getPlaneGeo() {
		return planeGeo;
	}
	public void setPlaneGeo(final PlaneGeo planeGeo) {
		this.planeGeo = planeGeo;
	}

	public PipeConfig getPipeConfig() {
		return pipeConfig;
	}
	public void setPipeConfig(PipeConfig pipeConfig) {
		this.pipeConfig = pipeConfig;
	}
	
}
