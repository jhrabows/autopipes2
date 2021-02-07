package org.autopipes.takeout;
/**
 * Interface to a bean which has a diameter.
 * This is what the takeout package needs to know about real pipes.
 * @author Janek
 *
 */
public interface Diametrisable {
    Diameter getDiameter();
    void setDiameter(Diameter d);
}
