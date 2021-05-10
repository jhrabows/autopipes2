package org.autopipes.takeout;

import java.util.ArrayList;
import java.util.List;

import org.autopipes.takeout.Fitting.Type;

public class FittingFactory {
	// fitting cache
	private static final List<Fitting> allFittings = new ArrayList<Fitting>();
	
	/**
	 * Produces a fitting matching given type, attachment, vendor, and optionally
	 * a list of diametrisables in canonical order (see {@link orderDiametrisables# }).
	 * As a result of this operation, the diameter property of the diametrisables
	 * may be bumped up to a required minimum.
	 * @param type the type
	 * @param attachment type of fitting attachment
	 * @param vendor the vendor. <code>null</code> indicates theaded fitting.
	 * @param d the diametrisables or <code>null</code> if not provided
	 * @return the fitting.
	 */
	public static synchronized Fitting instanceOf(final Type type, final Attachment attachment, final Vendor vendor,
			final List<? extends Diametrisable> d, final List<Attachment> attList, Diameter minGroovedOutlet){
		int length = d == null ? 0 : d.size();
		Diameter[] diameters = new Diameter[length];
		for(int i = 0; i < length; i++){
		    diameters[i] = d.get(i).getDiameter();
		}
		Attachment[] attachments = attList != null ?  attList.toArray(new Attachment[0])
				: new Attachment[0];
		Attachment fittingAttachment = attachment;
		if(length > 0){
            assert(!Diameter.checkDiametrisables(d));
                // if grooved after all, other than cap, coupling, raiser or reducer,
                if(fittingAttachment == Attachment.grooved
                		&& (type.getEndCount() > 2 || type.getEndCount() == 2
                				&& type.getBaseAngle() != Angle.deg180)){
                	// we need to select main pipe which diameter will be used
                	// as the fitting diameter. For now we select the biggest.
                	Diameter groovedDiameter = null;
                	for(int i = 0; i < diameters.length; i++){
                		if(groovedDiameter == null || diameters[i].compareTo(groovedDiameter) > 0){
                			groovedDiameter = diameters[i];
                		}
                	}
         //       	Diameter minGroovedOutlet = takeout.getMinGroovedOutlet();
                	if(minGroovedOutlet.compareTo(groovedDiameter) > 0){
                		minGroovedOutlet = groovedDiameter; // outlet cannot be wider than the pipe
                	}
                	
                	for(int i = 0; i < diameters.length; i++){
                		if(diameters[i].compareTo(minGroovedOutlet) < 0){
        	                // bump diameters of branch pipes to configurable minimum
                			d.get(i).setDiameter(minGroovedOutlet);
                		}	                		
                		diameters[i] = groovedDiameter; // in grooved all fitting legs are equal
                		// regardless of actual pipe diameters
	                }
                }else if(fittingAttachment == Attachment.mechanical
                		&& type.getEndCount() == 4
                		&& (diameters[2].compareTo(diameters[3]) != 0)){
                	// make opposite legs of mechanical cross equal (pipe changes as well)
                	diameters[3] = diameters[2];
                	d.get(3).setDiameter(diameters[3]);
                }
//            }
		}
		Fitting probe = new Fitting(type, fittingAttachment, vendor, diameters, attachments);
		int idx = allFittings.indexOf(probe);
		if(idx < 0){
			allFittings.add(probe);
			return probe;
		}
		return allFittings.get(idx);
	}


}
