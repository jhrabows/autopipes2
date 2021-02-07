package org.autopipes.takeout;

import java.math.BigDecimal;
import java.util.List;

/**
 * Enumeration of possible pipe diameters.
 * @author Janek
 *
 */
public enum Diameter {
	  D05(0, 50),
	  D075(0, 75),
	  D1(1, 0),
	  D125(1, 25),
	  D15(1, 50),
	  D175(1, 75),
	  D2(2, 0),
	  D25(2, 50),
	  D275(2, 75),
	  D3(3, 0),
	  D35(3, 50),
	  D4(4, 0),
	  D45(4, 50),
	  D6(6, 0),
	  D8(8, 0);
	  private final BigDecimal measure;
	  private String display;

/**
 * Main constructor from a diameter. The diameter is passed as an integral
 * part in inches and a fractional part in 100th of an inch.
 * @param rounded integral integral portion of the diameter
 * @param fraction fractional portion of the diameter
 */
		Diameter(final int rounded, final int fraction){
			BigDecimal decFrac = (new BigDecimal(fraction)).divide(new BigDecimal(100));
			measure = (new BigDecimal(rounded)).add(decFrac);

			display = rounded > 0 ? "" + rounded : "";
			if(fraction == 25){
				display += '\u00BC';
			}else if(fraction == 50){
				display += '\u00BD';
			}else if(fraction == 75){
				display += '\u00BE';
			}
		}

		/**
		 * Decimal representation of the diameter.
		 * Primarily for debugging.
		 * @return the decimal diameter digits
		 */
		@Override
		public String toString(){
			  return measure.toString();
		}

		public int compareTo(final BigDecimal that){
			return getMeasure().compareTo(that);
		}

        /**
         * Returns underlying decimal value.
         * @return the value
         */
		public BigDecimal getMeasure() {
			return measure;
		}

        /**
         * Architectural representation of the diameter.
         * Uses unicode to represent fractions.
         * @return the architectural representation
         */
		public String getDisplay() {
			return display;
		}


		public static boolean checkDiametrisables(final List<? extends Diametrisable> diametrisables){
	        return scanDiametrisables(diametrisables, false);
		}
		public static void orderDiametrisables(final List<? extends Diametrisable> diametrisables){
			scanDiametrisables(diametrisables, true);
		}
		private static boolean scanDiametrisables(final List<? extends Diametrisable> diametrisables, final boolean alter){
	      if(diametrisables.size() >= 2){
	    	  if(diametrisables.get(0).getDiameter().compareTo(diametrisables.get(1).getDiameter()) < 0){
	    		  if(alter){
	    			  TakeoutRepository.swapListItems(diametrisables, 0, 1);
	    		  }else{
	    			  return true;
	    		  }
	    	  }
	      }
	      if(diametrisables.size() == 4){
	    	  if(diametrisables.get(2).getDiameter().compareTo(diametrisables.get(3).getDiameter()) < 0){
	              if(alter){
	            	  TakeoutRepository.swapListItems(diametrisables, 2, 3);
	              }else{
	    		      return true;
	              }
	    	  }
	    	  if(diametrisables.get(0).getDiameter().compareTo(diametrisables.get(2).getDiameter()) < 0){
	              if(alter){
	            	  TakeoutRepository.swapListItems(diametrisables, 0, 2);
	            	  TakeoutRepository.swapListItems(diametrisables, 1, 3);
	              }else{
	    		      return true;
	              }
	    	  }
	      }
	      return false;
		}

}
