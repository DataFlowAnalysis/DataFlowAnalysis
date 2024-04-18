package org.dataflowanalysis.analysis.pcm.informationflow.core.utils;

import org.apache.log4j.Logger;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;

/**
 * A Utils class providing functionality concerning a lattice.
 *
 */
public class IFLatticeUtils {

	private static final Logger logger = Logger.getLogger(IFLatticeUtils.class);

	private IFLatticeUtils() {
	}

	/**
	 * Returns if the first Literal is a lower level in the lattice than the second
	 * Literal. Assumes both Literals to be of the same lattice. Assumes the lattice
	 * to be ordered from lowest to highest.
	 * 
	 * @param first  the first Literal
	 * @param second the second Literal
	 * @return true, if the first Literal is a lower level than the second
	 */
	public static boolean isLowerLevel(Literal first, Literal second) {
		return compareLevels(first, second) < 0;
	}

	/**
	 * Returns if the first Literal is a higher level in the lattice than the second
	 * Literal. Assumes both Literals to be of the same lattice. Assumes the lattice
	 * to be ordered from lowest to highest.
	 * 
	 * @param first  the first Literal
	 * @param second the second Literal
	 * @return true, if the first Literal is a higher level than the second
	 */
	public static boolean isHigherLevel(Literal first, Literal second) {
		return compareLevels(first, second) > 0;
	}

	/**
	 * Compares the first Literal with the second Literal in the lattice. The result
	 * is negative if the first Literal is of a lower level, zero if the literals
	 * are of the same level and positive if the first Literal is of a lower level.
	 * Assumes both Literals to be of the same lattice. Assumes the lattice to be
	 * ordered from lowest to highest.
	 * 
	 * @param first  the first Literal
	 * @param second the second Literal
	 * @return the comparison result
	 */
	public static int compareLevels(Literal first, Literal second) {
		Enumeration enumeration = first.getEnum();
		if (!enumeration.equals(second.getEnum())) {
			String errorMsg = "The given Literals are not of the same Enumeration";
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}

		int firstIndex = enumeration.getLiterals().indexOf(first);
		int secondIndex = enumeration.getLiterals().indexOf(second);
		return firstIndex - secondIndex;
	}

	/**
	 * Returns the lowest level of the given lattice.
	 * 
	 * @param lattice the given lattice
	 * @return the lowest level
	 */
	public static Literal getLowestLevel(Enumeration lattice) {
		return lattice.getLiterals().get(0);
	}

}
