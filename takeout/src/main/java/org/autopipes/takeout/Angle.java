package org.autopipes.takeout;
/**
 * Commonly encountered angles between 2 pipes collected in one place.
 * @author Janek
 *
 */
public enum Angle {
	deg0(0) { @Override
	public Angle complement(){ return Angle.deg180; }},
	deg45(0.25*Math.PI){ @Override
	public Angle complement(){ return Angle.deg135; }},
	deg90(0.5*Math.PI){ @Override
	public Angle complement(){ return Angle.deg90; }},
	deg135(0.75*Math.PI){ @Override
	public Angle complement(){ return Angle.deg45; }},
	deg180(Math.PI){ @Override
	public Angle complement(){ return Angle.deg0; }};

	private  final double measure;

	private Angle(final double radians){
		measure = radians;
	}

	public double getMeasure() {
		return measure;
	}
	public abstract Angle complement();
}
