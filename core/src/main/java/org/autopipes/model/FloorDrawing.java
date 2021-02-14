package org.autopipes.model;

import java.util.Calendar;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;

	/**
	 * Jaxb bean which contains information which applies to the entire drawing.
	 * It is used in passing messages between AuoCAD client and the Autopipes servlet.
	 * The content of this bean is persisted in a database table but only some attributes
	 * may be queried individually - others are serialized into XML and saved in
	 * a single CLOB attribute (optionsRoot).
	 * The <code>area</code> is a SQL-transient attribute populated only in the
	 * reply message to the AutoCAD client. It contains status summaries for the areas.
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {})
	@XmlRootElement(name = "dwg-root")
    public class FloorDrawing {

		@XmlElement(name = "dwg-id")
	    protected Long id;
	    @XmlElement(name = "dwg-name", required = true)
	    protected String dwgName;
	    @XmlElement(name = "dwg-text-size")
	    protected Double dwgTextSize;
	    @XmlElement(name = "dwg-update-date")
	    @JsonIgnore // TODO: this should not be ignored but TS generator chokes on Calendar. Should change to Date
	    protected Calendar dwgUpdateDate;
	    @XmlElement(name = "options-root")
	    protected DrawingOptions optionsRoot;

	    protected HashMap<Long, DrawingArea> area;

	    public String getDwgName() {
	        return dwgName;
	    }

	    public void setDwgName(final String value) {
	        dwgName = value;
	    }

	    public Double getDwgTextSize() {
	        return dwgTextSize;
	    }

	    public void setDwgTextSize(final Double value) {
	        dwgTextSize = value;
	    }

	    public Calendar getDwgUpdateDate() {
	        return dwgUpdateDate;
	    }

	    public void setDwgUpdateDate(final Calendar value) {
	        dwgUpdateDate = value;
	    }

		public Long getId() {
			return id;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		public DrawingOptions getOptionsRoot() {
			return optionsRoot;
		}

		public void setOptionsRoot(final DrawingOptions optionsRoot) {
			this.optionsRoot = optionsRoot;
		}

		public HashMap<Long, DrawingArea> getArea() {
	        if (area == null) {
	        	area = new HashMap<Long, DrawingArea>();
	        }
			return area;
		}

		public void addArea(final DrawingArea area){
			getArea().put(area.getAreaId(), area);
		}
	}


