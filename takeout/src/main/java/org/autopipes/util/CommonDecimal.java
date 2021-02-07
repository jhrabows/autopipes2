package org.autopipes.util;

import java.math.BigDecimal;
/**
 * Commonly used decimal values collected in one place.
 * @author Janek
 *
 */
public enum CommonDecimal{
	Zero(0),
	Quarter(25),
	Half(50),
	ThreeQuarters(75),
	One(100),
	Dozen(1200);

	private BigDecimal measure;
	CommonDecimal( final int measure){
		this.measure = new BigDecimal(measure);
		this.measure = this.measure.divide(new BigDecimal(100));
	}
	public BigDecimal getMeasure() {
		return measure;
	}
	public void setMeasure(final BigDecimal measure) {
		this.measure = measure;
	}
}
