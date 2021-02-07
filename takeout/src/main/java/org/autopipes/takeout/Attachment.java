package org.autopipes.takeout;
/**
 * Enumerates both main-types specified for the drawing layer and also fitting types defined by manufactures and standards.
 * The association between layer and fitting types is as follows:
 * 
 * threaded -> threaded
 * grooved -> grooved or mechanical
 * welded -> threaded or welded
 * weldedGroove -> grooved or weldedGroove(between another grooved layer) or welded (between another threaded layer)
 * 
 * @author Janek
 *
 */
public enum Attachment {
	  threaded, // Layer type (implied for branch layer). Also threaded fitting type.
	  grooved, // Layer type. Also grooved fitting type which requires vendor info.
      welded, // Layer type. Also welded tee/cross with threaded ending.
      weldedGroove, // Layer type. Also welded tee/cross with grooved ending.
      mechanical // Not a layer type. Mechanical tee/cross with treaded ending.
}
