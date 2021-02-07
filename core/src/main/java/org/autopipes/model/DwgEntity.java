package org.autopipes.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.autopipes.model.DrawingLayer.Designation;


/**
 * Literal representation of a geometric entity in an AutoCAD drawing.
 * Different kinds are distinguished by <i>cls</i> property which
 * matches AutoCAD object name.
 * Contains factory method for each kind.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctEntity", propOrder = {

})
public class DwgEntity {
/**
 * Identifying string of the Area Id Stamp box.
 */
	public static final String AREA_ID_TOKEN = "AREA-CONFIG";


	public enum AcClass{
		AcDbArc(true),
		AcDbCircle(true),
		AcDbMText(true),
		AcDbLine(true),
		AcDbPoint(true),
		AcDbText(true),
		AcDbBlockReference(true);

		private final boolean processed;

		AcClass(final boolean processed){
			this.processed = processed;
		}

		public boolean isProcessed() {
			return processed;
		}
	}

    @XmlElement(name = "ent-start")
    protected DwgPoint entStart;
    @XmlElement(name = "ent-end")
    protected DwgPoint entEnd;
    @XmlElement(name = "ent-ang")
    protected DwgAng entAng;
    @XmlElement(name = "ent-caption")
    protected String entCaption;
    @XmlElement(name = "ent-size")
    protected DwgSize entSize;
    @XmlAttribute(required = true)
    protected AcClass cls;
    @XmlAttribute
    protected String color;
    @XmlAttribute
    protected Long id;
    @XmlAttribute(required = true)
    protected String ly;
    @XmlAttribute
    protected String name;
 //   @XmlElement
    protected Map<String, String> attributes;
    @XmlTransient
    protected DrawingLayer layer;

/*    public static double crossProduct(final DwgEntity e1, final DwgEntity e2){
    	PlaneGeo.Point[] trip = tripple(e1, e2);
    	if(trip == null){
    		throw new IllegalArgumentException("Angle cannot be determined: segments have no points in common");
    	}
    	return PlaneGeo.crossProduct(trip);
    }*/
	/**
	 * If passed segment entities have a point in common builds a 3 vertex point-sequence
	 * ordered by having the common point in the middle.
	 * @param e1 first segment
	 * @param e2 second segment
	 * @return the 3-point sequence or null if the segments have no points in common.
	 */
    /*
    public static PlaneGeo.Point[] tripple(final DwgEntity e1, final DwgEntity e2){
        PlaneGeo.Point[] tripple = new PlaneGeo.Point[3];
        if(e1.getEntStart() == e2.getEntStart()){
        	tripple[0] = e1.getEntEnd();
        	tripple[1] = e1.getEntStart();
        	tripple[2] = e2.getEntEnd();
    	}else if(e1.getEntEnd() == e2.getEntEnd()){
        	tripple[0] = e1.getEntStart();
        	tripple[1] = e1.getEntEnd();
        	tripple[2] = e2.getEntStart();
    	}else if(e1.getEntStart() == e2.getEntEnd()){
        	tripple[0] = e1.getEntEnd();
        	tripple[1] = e1.getEntStart();
        	tripple[2] = e2.getEntStart();
    	}else if(e1.getEntEnd() == e2.getEntStart()){
        	tripple[0] = e1.getEntStart();
        	tripple[1] = e1.getEntEnd();
        	tripple[2] = e2.getEntEnd();
    	}else{
    		tripple = null;
    	}
        return tripple;
    }*/



    public static DwgEntity createCircle(final DwgPoint center, final double radius){
    	DwgEntity ret = new DwgEntity();
    	ret.setCls(AcClass.AcDbCircle);
    	DwgSize s = new DwgSize();
    	s.setH(radius);
    	ret.setEntSize(s);
    	ret.setEntStart(center);
    	return ret;
    }
    public static DwgEntity createSegment(final DwgPoint start, final DwgPoint end){
    	DwgEntity ret = new DwgEntity();
    	ret.setCls(AcClass.AcDbLine);
    	ret.setEntStart(start);
    	ret.setEntEnd(end);
    	return ret;
    }
    public static DwgEntity createBlockReference(final DwgPoint point,
    		final String name, final Map<String, String> attributes){
    	DwgEntity ret = new DwgEntity();
    	ret.setCls(AcClass.AcDbBlockReference);
    	ret.setEntStart(point);
    	ret.setName(name);
    	ret.setAttributes(attributes);
    	return ret;
    }
    public static DwgEntity createPoint(final DwgPoint point){
    	DwgEntity ret = new DwgEntity();
    	ret.setCls(AcClass.AcDbPoint);
    	ret.setEntStart(point);
    	return ret;
    }
    public static DwgEntity createText(final DwgPoint location, final double size, final String msg){
    	DwgEntity ret = new DwgEntity();
    	ret.setCls(AcClass.AcDbText);
    	ret.setEntStart(location);
    	DwgSize s = new DwgSize();
    	s.setH(size);
    	ret.setEntSize(s);
    	ret.setEntCaption(msg);
    	return ret;
    }

    public static DwgEntity createMultiText(final DwgPoint location, final DwgSize size, final List<String> msg){
    	DwgEntity ret = new DwgEntity();
    	ret.setCls(AcClass.AcDbMText);
    	ret.setEntStart(location);
    	ret.setEntSize(size);
    	StringBuilder sb = new StringBuilder();
        for(int i = 0; i < msg.size(); i++)
        {
          if(i > 0)
          {
            sb.append("\\P");
          }

          sb.append(msg.get(i));
        }
        ret.setEntCaption(sb.toString());
    	return ret;
    }
    /**
     * Factory method for a special textual box containing
     * area identification properties.
     * @param location position point of the box
     * @param size dimensions of the box
     * @param id area id
     * @param name area name
     * @return
     */
    public static DwgEntity createAreaIdStamp(final DwgPoint location, final Map<String, String> properties){
        return createBlockReference(location, AREA_ID_TOKEN, properties);
        /*
    	List<String> lines = new ArrayList<String>();
    	lines.add(AREA_ID_TOKEN);
    	for(Map.Entry<String, String> property : properties.entrySet()){
    		lines.add(property.getKey() + '=' + property.getValue());
    	}
    	return createMultiText(location, size, lines);
    	*/
    }
    /**
     * Define display orientation of a textual entity.
     * @param angle
     * @return
     */
    public DwgEntity rotate(final double angle){
        DwgAng ang = new DwgAng();
        ang.setAlpha(angle);
        setEntAng(ang);
    	return this;
    }
    /**
     * Swaps the start and end points.
     * Does not effect one-oint entities.
     */
    /*
    public void reverse(){
    	if(entEnd != null){
    	    DwgPoint p = entStart;
    	    entStart = entEnd;
    	    entEnd = p;
    	}
    }*/
    /**
     * Cuts this segment entity into subsegments at provided cut-points.
     * @param cuts the cut-points
     * @return the subsegments
     */
    /*
    public List<DwgEntity> subdivide(final Collection<DwgPoint> cuts){
    	List<DwgEntity> ret = new ArrayList<DwgEntity>();
    	List<DwgPoint> ends = new ArrayList<DwgPoint>();
    	ends.addAll(cuts);
    	ends.add(getEntEnd());
    	DwgPoint last = getEntStart();
    	for(DwgPoint cutend : ends){
			DwgEntity part = new DwgEntity();
			part.setCls(getCls());
			part.setEntStart(last);
			part.setEntEnd(cutend);
			part.setLy(getLy());
			part.setLayer(getLayer());
            ret.add(part);
            last = cutend;
    	}
    	return ret;
    }*/

	@Override
	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(ly);
		ret.append(':');
		if(entCaption != null){
			ret.append(entCaption);
		}
		return ret.toString();
	}
    /**
     * Flag which determines if this entity carries information relevant
     * to the drawing's structure (i.e. is not for display only)
     * @return <code>true</code> if it is relevant
     */
    public boolean isStructure(){
    	boolean ret = false;
    	if(cls == AcClass.AcDbLine){
    		if(layer != null && layer.getType() != null
    			&& (layer.getType() == Designation.Main || layer.getType() == Designation.Branch)){
    			ret = true;
    		}
    	}else if(cls == AcClass.AcDbCircle
    			|| cls == AcClass.AcDbArc
    			|| cls == AcClass.AcDbPoint
    			|| cls == AcClass.AcDbBlockReference){
    		ret = true;
    	}//
    	return ret;
    }
    /**
     * Checks if this entity is either a text or a multi-text.
     *
     * @return <code>true</code> if it is
     */
  public boolean isText(){
    	return cls == AcClass.AcDbMText || cls == AcClass.AcDbText;
    }

  public DwgPoint getEntStart() {
        return entStart;
    }

  public void setEntStart(final DwgPoint value) {
        entStart = value;
    }

  public DwgPoint getEntEnd() {
        return entEnd;
    }

  public void setEntEnd(final DwgPoint value) {
        entEnd = value;
    }

  public DwgAng getEntAng() {
    //	if(entAng != null){
    //	    entAng.setUnit(angularUnit);
    //	}
        return entAng;
    }

  public void setEntAng(final DwgAng value) {
        entAng = value;
    }

  public String getEntCaption() {
        return entCaption;
    }

  public void setEntCaption(final String value) {
        entCaption = value;
    }

  public DwgSize getEntSize() {
        return entSize;
    }

  public void setEntSize(final DwgSize value) {
        entSize = value;
    }

  public AcClass getCls() {
        return cls;
    }

  public void setCls(final AcClass value) {
        cls = value;
    }

  public String getColor() {
        return color;
    }

  public void setColor(final String value) {
        color = value;
    }

  public Long getId() {
        return id;
    }

  public void setId(final Long value) {
        id = value;
    }

  public String getLy() {
        return ly;
    }

  public void setLy(final String value) {
        ly = value;
    }

  public DrawingLayer getLayer() {
	return layer;
  }
  public void setLayer(final DrawingLayer layer) {
	this.layer = layer;
  }
public String getName() {
	return name;
}
public void setName(final String name) {
	this.name = name;
}
public Map<String, String> getAttributes() {
    if (attributes == null) {
    	attributes = new HashMap<String, String>();
    }
	return attributes;
}
public void setAttributes(final Map<String, String> attributes) {
	this.attributes = attributes;
}
}
