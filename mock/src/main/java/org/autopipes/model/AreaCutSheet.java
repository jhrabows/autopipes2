package org.autopipes.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.autopipes.model.PipeFitting.Jump;
import org.autopipes.takeout.Attachment;
import org.autopipes.takeout.Diameter;
import org.autopipes.takeout.Fitting;
import org.autopipes.takeout.Fitting.Type;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { })
@XmlRootElement(name = "cut-sheet")
public class AreaCutSheet {
//
// Inner classes
//
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "branch-info")
    public static class BranchInfo{
		@XmlElement(name = "branchId")
    	private int branchId;
		@XmlElement(name = "multiplicity")
    	private int multiplicity;
		@XmlElement(name = "origin")
    	private Pipe origin;
		@XmlElement(name = "originalAttachment")
    	private Attachment originalAttachment;
    	
    	// both of these contain edge information - second one is use only by DWR
		// TODO: change this to hash-map and add comparator to edge-multiplicity.
		// current set-up does not work since the comparator does not work when
		// the map is deserialized - it does not have fitting objects yet
    	//private TreeMap<CutSheetInfo, Integer> edgeMultiplicityMap;
		//@XmlTransient
		
		@XmlElement(name = "edges")
    	private List<EdgeMultiplicity> edgeMultiplicity;

		public int getBranchId() {
			return branchId;
		}
		public void setBranchId(final int branchId) {
			this.branchId = branchId;
		}
		public int getMultiplicity() {
			return multiplicity;
		}
		public void setMultiplicity(final int multiplicity) {
			this.multiplicity = multiplicity;
		}
		/*
		public SortedMap<CutSheetInfo, Integer> getEdgeMultiplicityMap() {
			if(edgeMultiplicityMap == null){
				edgeMultiplicityMap = new TreeMap<CutSheetInfo, Integer>();
			}
			return edgeMultiplicityMap;
		}
		*/
//		protected void setEdgeMultiplicityMap(final TreeMap<CutSheetInfo, Integer> edgeMultiplicityMap) {
//			this.edgeMultiplicityMap = edgeMultiplicityMap;
//		}
		public List<EdgeMultiplicity> getEdgeMultiplicity() {
			if(edgeMultiplicity == null){
				edgeMultiplicity = new ArrayList<EdgeMultiplicity>();
				/*
				for(Map.Entry<CutSheetInfo, Integer> entry : getEdgeMultiplicityMap().entrySet()){
					EdgeMultiplicity em = new EdgeMultiplicity();
					em.setEdgeInfo(entry.getKey());
					em.setCount(entry.getValue());
					edgeMultiplicity.add(em);
				}
				Collections.reverse(edgeMultiplicity);
				*/
			}
			return edgeMultiplicity;
		}
		public void setEdgeMultiplicity(final List<EdgeMultiplicity> edgeMultiplicity) {
			this.edgeMultiplicity = edgeMultiplicity;
		}
		public Pipe getOrigin() {
			return origin;
		}
		public void setOrigin(final Pipe origin) {
			this.origin = origin;
		}
		public Attachment getOriginalAttachment() {
			return originalAttachment;
		}
		public void setOriginalAttachment(Attachment originalAttachment) {
			this.originalAttachment = originalAttachment;
		}

    }
    public static class EdgeMultiplicity{
    	private CutSheetInfo edgeInfo;
    	private int count;

		public CutSheetInfo getEdgeInfo() {
			return edgeInfo;
		}
		public void setEdgeInfo(final CutSheetInfo edgeInfo) {
			this.edgeInfo = edgeInfo;
		}
		public int getCount() {
			return count;
		}
		public void setCount(final int count) {
			this.count = count;
		}
    }
    public static class EdgeMultiplicityComparator implements Comparator<EdgeMultiplicity>{
    	private final CutSheetComparator cutSheetComparator = new CutSheetComparator();
		public int compare(EdgeMultiplicity o1, EdgeMultiplicity o2) {
			CutSheetInfo c1 = o1.getEdgeInfo();
			CutSheetInfo c2 = o2.getEdgeInfo();
			return cutSheetComparator.compare(c1, c2);
		}
		public CutSheetComparator getCutSheetComparator() {
			return cutSheetComparator;
		}
    }
    
    public static class MainCutSheetComparator implements Comparator<CutSheetInfo>{
//		@Override
		public int compare(CutSheetInfo c1, CutSheetInfo c2) {
			return c1.getPipe().getId().compareTo(c2.getPipe().getId());
		}
    }

    public static class CutSheetComparator implements Comparator<CutSheetInfo>{
//		@Override
		public int compare(CutSheetInfo c1, CutSheetInfo c2) {
			int ret = (-1)*c1.getStartFitting().getAttachment().compareTo(
					c2.getStartFitting().getAttachment());
			if(ret != 0){
				return ret;
			}
			ret = (-1)*c1.getPipe().getDiameter().compareTo(
					c2.getPipe().getDiameter());
			if(ret != 0){
				return ret;
			}
			ret = c1.getEndFitting().getType().compareTo(
					c2.getEndFitting().getType());
			if(ret != 0){
				return ret;
			}
			return c1.getPipe().getEndAttachment().getDirectionInFitting().compareTo(
					c2.getPipe().getEndAttachment().getDirectionInFitting());
		}
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
	public static class OutletInfo {
		@XmlElement(name = "attachment")
    	protected Attachment attachment;
    	
		@XmlElement(name = "diam")
	    protected Diameter diameter;
		
		@XmlElement(name = "offset")
	    private BigDecimal offset;
		
		// for vertically positioned fittings we need to know the position within vertical pipe (top, bottom or middle)
		@XmlElement(name = "jumpLocation")
	    private Jump jumpLocation;

		// for horizontally positioned fitting we need to know if tee or cross
		// for tee left/right orientation of outlet is encoded as sign of the count
		@XmlElement(name = "sideCount")
		private int sideCount;
		
		public Attachment getAttachment() {
			return attachment;
		}

		public void setAttachment(Attachment attachment) {
			this.attachment = attachment;
		}
				
		public int getSideCount() {
			return sideCount;
		}

		public void setSideCount(int sideCount) {
			this.sideCount = sideCount;
		}

		public Jump getJumpLocation() {
			return jumpLocation;
		}

		public void setJumpLocation(Jump jumpLocation) {
			this.jumpLocation = jumpLocation;
		}

		public BigDecimal getOffset() {
			return offset;
		}

		public void setOffset(BigDecimal offset) {
			this.offset = offset;
		}

		public Diameter getDiameter() {
			return diameter;
		}

		public void setDiameter(Diameter diameter) {
			this.diameter = diameter;
		}
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
	public static class MainCutSheetInfo /*implements Comparable<CutSheetInfo>*/{
		@XmlElement(name = "id")
		protected Integer id;
		
		@XmlElement(name = "prefix")
		protected String prefix;
		
		@XmlElement(name = "diam")
	    protected Diameter diameter;
		
		@XmlElement(name = "cutLength")
	    private BigDecimal cutLength; // span of chain adjusted for takeout
		
		@XmlElement(name = "outlets")
    	private List<OutletInfo> outlets;
		@XmlElement(name = "outletsRTL")
    	private List<OutletInfo> outletsRTL;
		
		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public List<OutletInfo> getOutlets() {
			if(outlets == null){
				outlets = new ArrayList<OutletInfo>();
			}
			return outlets;
		}
		
		public void setOutlets(final List<OutletInfo> outlets) {
			this.outlets = outlets;
		}
		
		public List<OutletInfo> getOutletsRTL() {
			if(outletsRTL == null){
				outletsRTL = new ArrayList<OutletInfo>();
			}
			return outletsRTL;
		}
		
		public void setOutletsRTL(final List<OutletInfo> outletsRTL) {
			this.outletsRTL = outletsRTL;
		}
		
		public void provideRtlOrdering(){
			List<OutletInfo> list = getOutlets();
			int listSize = list.size();
			List<OutletInfo> rtlList = new ArrayList<OutletInfo>();
			
			for(int i = listSize - 1; i >= 0; i--){
				OutletInfo info = list.get(i);
				OutletInfo rtlInfo = new OutletInfo();
				rtlInfo.setDiameter(info.getDiameter());
				rtlInfo.setAttachment(info.getAttachment());
				rtlInfo.setSideCount(-1*info.getSideCount()); // indicate opposite side
				rtlInfo.setJumpLocation(info.getJumpLocation());
				BigDecimal offset = info.getOffset();
				BigDecimal fullLength = this.getCutLength();
				if(fullLength != null && offset != null){
					BigDecimal rtlOffset = fullLength.subtract(offset);
					rtlInfo.setOffset(rtlOffset);
				}
				rtlList.add(rtlInfo);
			}
			setOutletsRTL(rtlList);
		}

		public BigDecimal getCutLength() {
			return cutLength;
		}

		public void setCutLength(BigDecimal cutLength) {
			this.cutLength = cutLength;
		}

		public Diameter getDiameter() {
			return diameter;
		}

		public void setDiameter(Diameter diameter) {
			this.diameter = diameter;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
    }

    @XmlAccessorType(XmlAccessType.FIELD)
	public static class CutSheetInfo /*implements Comparable<CutSheetInfo>*/{
		@XmlElement(name = "pipe")
		private Pipe pipe;
		@XmlTransient
		private Fitting startFitting;
		@XmlTransient
		private Fitting endFitting;
		@XmlElement(name = "start")
		protected Integer startFittingId;
		@XmlElement(name = "end")
		protected Integer endFittingId;

		@Override
		/**
		 * 2 Objects are considered equal if
		 * <ul>
		 * <li>The 2 pipes are equal (as per Pipe.areEqual static call)</li>
		 * <li>The end fittings are equal</li>
		 * <li>Pipes are attached to end fittings the same way (bull vs run)
		 * <li>The start fitting attachments are equal.
		 * (This will result in unconditional distinction of the root pipe
		 * attached to a non-threaded main from any other pipe)</li>
		 * </ul>
		 */
		public boolean equals(final Object that){
			if(that == null || !(that instanceof CutSheetInfo)){
				return false;
			}
			CutSheetInfo thatInfo = (CutSheetInfo)that;
			if(Pipe.areEqualRounded(pipe, thatInfo.pipe)
					&& (endFitting == null && thatInfo.endFitting == null || endFitting != null && endFitting.equals(thatInfo.endFitting))
					&& (startFitting == null && thatInfo.startFitting == null
							|| startFitting != null && thatInfo.startFitting != null
							    && startFitting.getAttachment() == thatInfo.startFitting.getAttachment())
					){
				// check how ends are attached
				if(endFitting != null){
					Type t = endFitting.getType();
					PipeAttachment ea1 = pipe.getEndAttachment();
					PipeAttachment ea2 = thatInfo.pipe.getEndAttachment();
					if(ea1 == null && ea2 != null || ea1 != null && ea2 == null
							|| ea1 != null && ea2 != null
								&& !t.equivalentDirections(ea1.getDirectionInFitting(),
										ea2.getDirectionInFitting())){
						return false;
					}
				}
				return true;
			}
			return false;
		}
		@Override
		public int hashCode(){
			int ret = 7;
		//	ret = 31*ret + (startFitting == null ? 0 : startFitting.hashCode());
		//	ret = 31*ret + (endFitting == null ? 0 : endFitting.hashCode());
			ret = 31*ret + (pipe == null ? 0 : pipe.getHashCode());
			return ret;
		}

		public Pipe getPipe() {
			return pipe;
		}
		public void setPipe(final Pipe pipe) {
			this.pipe = pipe;
		}
		public Fitting getEndFitting() {
			return endFitting;
		}
		public void setEndFitting(final Fitting endFitting) {
			this.endFitting = endFitting;
		}
		public Fitting getStartFitting() {
			return startFitting;
		}
		public void setStartFitting(final Fitting startFitting) {
			this.startFitting = startFitting;
		}
	}
//
// Start of Main Class
//
	
//  @XmlElement(name = "branchMap")
  protected TreeMap<Integer, BranchInfo> branchMap;
  @XmlElement(name = "fitting")
  protected List<Fitting> fittings;
  
  @XmlElement(name = "mainThreaded")
  protected List<CutSheetInfo> mainThreadedList;
  
  @XmlElement(name = "mainGrooved")
  protected List<MainCutSheetInfo> mainGroovedList;
  
  @XmlElement(name = "mainWelded")
  protected List<MainCutSheetInfo> mainWeldedList;

  public List<MainCutSheetInfo> getMainGroovedList() {
		 if(mainGroovedList == null){
			 mainGroovedList = new ArrayList<MainCutSheetInfo>();
		 }
		 return mainGroovedList;
  }
		
  public void setMainGroovedList(List<MainCutSheetInfo> mainGroovedList) {
		this.mainGroovedList = mainGroovedList;
  }

  public List<MainCutSheetInfo> getMainWeldedList() {
		 if(mainWeldedList == null){
			 mainWeldedList = new ArrayList<MainCutSheetInfo>();
		 }
		 return mainWeldedList;
  }
		
  public void setMainWeldedList(List<MainCutSheetInfo> mainWeldedList) {
		this.mainWeldedList = mainWeldedList;
  }
  

  public List<CutSheetInfo> getMainThreadedList() {
	 if(mainThreadedList == null){
		 mainThreadedList = new ArrayList<CutSheetInfo>();
	 }
	 return mainThreadedList;
  }
	
	public void setMainThreadedList(List<CutSheetInfo> mainThreadedList) {
		this.mainThreadedList = mainThreadedList;
	}
	
	public SortedMap<Integer, BranchInfo> getBranchMap() {
		if(branchMap == null){
			branchMap = new TreeMap<Integer, BranchInfo>();
		}
		return branchMap;
	}

	public void setBranchMap(final TreeMap<Integer, BranchInfo> branchMap) {
		this.branchMap = branchMap;
	}
	
	public void orderCutSheet(){
		EdgeMultiplicityComparator emComp = new EdgeMultiplicityComparator();
		// sort branch cut-sheet
		for(BranchInfo bi : getBranchMap().values()){
			Collections.sort(bi.getEdgeMultiplicity(), emComp);
		}
		// sort main-threaded cut-sheet
		Collections.sort(getMainThreadedList(), emComp.getCutSheetComparator());
		// provide RTL ordering
		provideRtlOrdering(this.getMainGroovedList());
		provideRtlOrdering(this.getMainWeldedList());
	}
	protected void provideRtlOrdering(List<MainCutSheetInfo> cutSheetList){
		for(MainCutSheetInfo ci : cutSheetList){
			ci.provideRtlOrdering();
		}
	}
	/**
	 * Serialization compression.
	 * Build unique fitting list and replace fitting objects on the branch edge list
	 * with indexes to the fitting list.
	 */
	public void preSerialize(){
		fittings = new ArrayList<Fitting>();
		for(CutSheetInfo csi : getMainThreadedList()){
			preSerializeOne(csi);
		}
		for(BranchInfo bi : getBranchMap().values()){
			for(EdgeMultiplicity em : bi.getEdgeMultiplicity()){
				CutSheetInfo csi = em.getEdgeInfo();
				preSerializeOne(csi);
				/*
				for(int i = 0; i < 2; i++){
					Fitting f = (i == 0) ? csi.getStartFitting() : csi.getEndFitting();
					int idx = fittings.indexOf(f);
					if(idx < 0){
						idx = fittings.size();
						fittings.add(f);
					}
					if(i == 0){
						csi.startFittingId = idx;
					}else{
						csi.endFittingId = idx;
					}
				}
				*/
			}
		}
	}
	/**
	 * Deserialization decompression.
	 * Restores fitting objects on the branch edge list from their indexes
	 * on the fitting list.
	 */
	public void postDeserialize(){
		if(fittings == null){
			return;
		}
		for(CutSheetInfo csi : getMainThreadedList()){
			postDeserializeOne(csi);
		}
		for(BranchInfo bi : getBranchMap().values()){
			for(EdgeMultiplicity em : bi.getEdgeMultiplicity()){
				CutSheetInfo csi = em.getEdgeInfo();
				postDeserializeOne(csi);
			}
		}
	}
	private void postDeserializeOne(CutSheetInfo csi){
		for(int i = 0; i < 2; i++){
			Fitting f = fittings.get(
					(i == 0) ? csi.startFittingId : csi.endFittingId);
			if(i == 0){
				csi.startFitting = f;
			}else{
				csi.endFitting = f;
			}
		}
	}
	private void preSerializeOne(CutSheetInfo csi){
		for(int i = 0; i < 2; i++){
			Fitting f = (i == 0) ? csi.getStartFitting() : csi.getEndFitting();
			int idx = fittings.indexOf(f);
			if(idx < 0){
				idx = fittings.size();
				fittings.add(f);
			}
			if(i == 0){
				csi.startFittingId = idx;
			}else{
				csi.endFittingId = idx;
			}
		}
	}
}
