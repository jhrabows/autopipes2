package org.autopipes.takeout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.autopipes.takeout.Fitting.Direction;
import org.autopipes.takeout.Fitting.Type;
import org.autopipes.takeout.TakeoutInfo.Cut;
import org.autopipes.takeout.TakeoutInfo.GroovedCut;
import org.autopipes.util.CommonDecimal;

/**
 * Jaxb bean representing full takeout database.
 * @author Janek
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { })
@XmlRootElement(name="root")
public class TakeoutRepository {
	@XmlElement(name="info")
	protected List<TakeoutInfo> infoList;
	@XmlElement(name="headDiameter")
	protected Diameter headDiameter = Diameter.D1;
	@XmlElement(name="minGroovedDiameter")
	protected Diameter minGroovedDiameter = Diameter.D15;
	@XmlElement(name="minGroovedOutlet")
	protected Diameter minGroovedOutlet = Diameter.D2;

	@XmlTransient
	protected Map<Diameter, TakeoutInfo> infoLookup;

    public static<T> void swapListItems(final List<T> list, final int i, final int j){
    	T tmp = list.get(i);
    	list.set(i, list.get(j));
    	list.set(j, tmp);
    }

	public void initLookup(){
		infoLookup = new HashMap<Diameter, TakeoutInfo>();
		for(TakeoutInfo ti : infoList){
			infoLookup.put(ti.getOutlet(), ti);
		}
	}
	public BigDecimal locateTakeout(final Fitting fitting, final Direction direction){
        if(fitting.getType().getEndCount() == 1){
        	return CommonDecimal.Zero.getMeasure();
        }
		if(fitting.getAttachment() != Attachment.threaded){
			// grooved or welded
        	return locateGroovedTakeout(fitting, direction);
        }
        return locateThreadedTakeout(fitting, direction);
	}
	private BigDecimal locateGroovedTakeout(final Fitting fitting,
			final Direction direction){
		if(fitting.getType() == Type.Coupling){
			// No takeout for grooved couplings
			return CommonDecimal.Zero.getMeasure();
		}
		Attachment attachment = fitting.getAttachment();
		if(attachment == Attachment.grooved){
			// lookup info by (single) fitting diameter or bigger diameter of a reducer
			// (the direction is not used)
			Diameter groovedDiameter = fitting.diameter(Direction.E);
			TakeoutInfo info = takeoutInfo(groovedDiameter);
			// lookup cut by angle
			Angle angle = fitting.getType().getBaseAngle().complement();
			GroovedCut grcut = info.getGroovedByAngle().get(angle);
			if(grcut == null){
				return null;
			}
			// check for lookup by secondary diameter
			Diameter diameter = null;
			if(fitting.getType() == Type.Ell45){
				// secondary is equal to primary
				diameter = groovedDiameter;
			}else if(fitting.getType() == Type.Reducer){
				// secondary is the smaller one
				diameter = fitting.diameter(Direction.W);
			}
			if(diameter != null){
				return grcut.getByDiameter().get(diameter);				
			}
			// 90deg - lookup by vendor
			return grcut.getByVendor().get(fitting.getVendor());
		}
		// mechanical or welded tee/cross  (hole based)
		// No takout for main directions - The pipe is not even cut
		if(direction == Direction.E || direction == Direction.W){
			return CommonDecimal.Zero.getMeasure();
		}
		Attachment sideAttachment = fitting.attachment(direction);
		if(sideAttachment != null){
			attachment = sideAttachment;
		}
		// lookup info by the N-S (side) diameter
		Diameter cutPipeDiameter = fitting.diameter(direction);
		TakeoutInfo info = takeoutInfo(cutPipeDiameter);
		// lookup cut by the E-W (main) diameter
		Cut cut = info.getByDiameter().get(fitting.diameter(Direction.E));
		return cut == null ? null : cut.getByAttachment().get(attachment);
	}

	/**
	 */
	private BigDecimal locateThreadedTakeout(final Fitting fitting, final Direction direction){
		Diameter cutPipeDiameter = fitting.diameter(direction);
		Direction adjacentDirection = fitting.adjacentDirectionsMaxDiam(direction).get(0);
		Diameter adjacentDiameter = fitting.diameter(adjacentDirection);
		Angle angle = fitting.getType().angle(direction, adjacentDirection);

		TakeoutInfo info = takeoutInfo(cutPipeDiameter);
		TakeoutInfo.Cut cut = info == null ? null : info.getByDiameter().get(adjacentDiameter);
		// store lookup based on 180-complement
		return cut == null ? null : cut.getByAngle().get(angle.complement());
	}

	public boolean isMechanicalAllowed(Diameter primary, Diameter secondary){
		boolean ret = false;
		Diameter minGrooved = this.getMinGroovedDiameter();
		if(primary.compareTo(minGrooved) >= 0){
        	TakeoutInfo info = this.takeoutInfo(primary);
        	Diameter maxHole = info.getDrillLimit(Attachment.mechanical);
        	if(maxHole.compareTo(secondary) >= 0){
        		ret = true;
        	}
		}
		return ret;
	}

    public TakeoutInfo takeoutInfo(final Diameter diameter){
    	return infoLookup.get(diameter);
    }

	public List<TakeoutInfo> getInfoList() {
		if(infoList == null){
			infoList = new ArrayList<TakeoutInfo>();
		}
		return infoList;
	}

	public void setInfoList(final List<TakeoutInfo> infoList) {
		this.infoList = infoList;
	}
	public Diameter getHeadDiameter() {
		return headDiameter;
	}
	public void setHeadDiameter(final Diameter headDiameter) {
		this.headDiameter = headDiameter;
	}

	public Diameter getMinGroovedDiameter() {
		return minGroovedDiameter;
	}

	public void setMinGroovedDiameter(final Diameter minGroovedDiameter) {
		this.minGroovedDiameter = minGroovedDiameter;
	}
	public Diameter getMinGroovedOutlet() {
		return minGroovedOutlet;
	}

	public void setMinGroovedOutlet(Diameter minGroovedOutlet) {
		this.minGroovedOutlet = minGroovedOutlet;
	}

}
