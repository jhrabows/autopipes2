package org.autopipes.takeout;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Jaxb bean which stores information related to a particular diameter.
 * That diameter is named <code>outlet</code> although the bean stores non-outlet info as well.
 * @author Janek
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { })
@XmlRootElement(name="takeoutinfo")
public class TakeoutInfo {
	private Diameter outlet;
	private Boolean main;
//	private Diameter drillLimit;
	private SortedMap<Attachment, Diameter> drillLimits;
	private BigDecimal targetHole;
//	private SortedMap<Vendor, BigDecimal> byVendor;
	private SortedMap<Diameter, Cut> byDiameter;
	private SortedMap<Angle, GroovedCut> groovedByAngle;

    /*
    * Defines attachment-specific drill limit for main outlets.
    * @return limit-by-attachment map
    */
	public SortedMap<Attachment, Diameter> getDrillLimits() {
		if(drillLimits == null){
			drillLimits = new TreeMap<Attachment, Diameter>();
		}
		return drillLimits;
	}
	public void setDrillLimits(final SortedMap<Attachment, Diameter> drillLimits) {
		this.drillLimits = drillLimits;
	}

	/**
	 * Defines vendor-independent takouts where this diameter is a source.
	 * @return takeouts-by-target_diameter map
	 */
	public SortedMap<Diameter, Cut> getByDiameter() {
		if(byDiameter == null){
			byDiameter = new TreeMap<Diameter, Cut>();
		}
		return byDiameter;
	}
	public void setByDiameter(final SortedMap<Diameter, Cut> byDiameter) {
		this.byDiameter = byDiameter;
	}
	
	public SortedMap<Angle, GroovedCut> getGroovedByAngle() {
		if(groovedByAngle == null){
			groovedByAngle = new TreeMap<Angle, GroovedCut>();
		}
		return groovedByAngle;
	}
	
	public void setGroovedByAngle(SortedMap<Angle, GroovedCut> groovedByAngle) {
		this.groovedByAngle = groovedByAngle;
	}
	
	/**
	 * Flag indicating that this diameter may be used by main
	 * @return
	 */
	public Boolean getMain() {
		return main;
	}
	public void setMain(final Boolean main) {
		this.main = main;
	}
	/**
	 * The largest hole diameter that may be drilled into a pipe of this size.
	 * @return the limit diameter
	 */
	public Diameter getDrillLimit(Attachment attachment) {
		return drillLimits.get(attachment);
	}
	/*
	public void setDrillLimit(final Diameter drillLimit) {
		this.drillLimit = drillLimit;
	}
	*/
	/**
	 * The diameter that has to be drilled in the target pipe to fit this pipe.
	 * @return the required diameter
	 */
	public BigDecimal getTargetHole() {
		return targetHole;
	}
	public void setTargetHole(final BigDecimal targetHole) {
		this.targetHole = targetHole;
	}
    /**
     * Returns actual diameter drilled in the target pipe.
     * Returns <code>null</code> if target is smaller than the hole.
     * If the target is equal to the hole the return is adjusted.
     * @param targetPipeDiameter
     * @return the adjusted hole diameter or <code>null</code>.
     */
	public BigDecimal getAdjustedTargetHole(final BigDecimal targetPipeDiameter){
		int comp = targetPipeDiameter.compareTo(targetHole);
		BigDecimal ret = targetHole;
		if(comp < 0){
			ret = null; // target too small
		}else if(comp == 0){
			// source is 1.5 and target 2: take average
			ret = ret.add(targetPipeDiameter).multiply(Diameter.D05.getMeasure());
		}
		return ret;
	}
	public Diameter getOutlet() {
		return outlet;
	}
	public void setOutlet(final Diameter outlet) {
		this.outlet = outlet;
	}
	
	  /**
	   * All cutting info related to a grooved source pipe of a given diameter
	   * attached to a target grooved pipe at a given angle.
	   */
	  @XmlAccessorType(XmlAccessType.FIELD)
	  @XmlType(name = "groovedcut", propOrder = { })
	  @XmlRootElement(name="groovedcut")
	public static class GroovedCut implements Serializable{
		private static final long serialVersionUID = 1L;
		
		private SortedMap<Vendor, BigDecimal> byVendor;
		private SortedMap<Diameter, BigDecimal> byDiameter;

		public SortedMap<Vendor, BigDecimal> getByVendor() {
			if(byVendor == null){
				byVendor = new TreeMap<Vendor, BigDecimal>();
			}
			return byVendor;
		}
		public void setByVendor(final SortedMap<Vendor, BigDecimal> byVendor) {
			this.byVendor = byVendor;
		}

		public SortedMap<Diameter, BigDecimal> getByDiameter() {
			if(byDiameter == null){
				byDiameter = new TreeMap<Diameter, BigDecimal>();
			}
			return byDiameter;
		}
		public void setByDiameter(final SortedMap<Diameter, BigDecimal> byDiameter) {
			this.byDiameter = byDiameter;
		}
	  }

	  /**
	   * All cutting info related to a threaded source pipe of a given diameter
	   * attached to a target pipe of a given diameter
	   */
	  @XmlAccessorType(XmlAccessType.FIELD)
	  @XmlType(name = "takeoutcut", propOrder = { })
	  @XmlRootElement(name="takeoutcut")
	  public static class Cut implements Serializable{
		private static final long serialVersionUID = 3063947597215541704L;

	    private SortedMap<Angle, BigDecimal> byAngle;
	    private Map<Attachment, BigDecimal> byAttachment;

		public SortedMap<Angle, BigDecimal> getByAngle() {
			if(byAngle == null){
				byAngle = new TreeMap<Angle, BigDecimal>();
			}
			return byAngle;
		}
		public void setByAngle(final SortedMap<Angle, BigDecimal> byAngle) {
			this.byAngle = byAngle;
		}
		public Map<Attachment, BigDecimal> getByAttachment() {
			if(byAttachment == null){
				byAttachment = new HashMap<Attachment, BigDecimal>();
			}
			return byAttachment;
		}
		public void setByAttachment(final Map<Attachment, BigDecimal> byAttachment) {
			this.byAttachment = byAttachment;
		}
	  } // end of Cut class


}

