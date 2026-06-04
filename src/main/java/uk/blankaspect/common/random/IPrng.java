/*====================================================================*\

IPrng.java

Interface: pseudo-random number generator.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.random;

//----------------------------------------------------------------------


// INTERFACE: PSEUDO-RANDOM NUMBER GENERATOR


/**
 * This interface defines the methods that must be implemented by a pseudo-random number generator.
 */

public interface IPrng
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The kind of seed with which a pseudo-random number generator was last initialised. */
	public enum SeedKind
	{
		/**
		 * No seed: a pseudo-randomly generated seed was used.
		 */
		NONE,

		/**
		 * The seed was an array of {@code byte}s.
		 */
		BYTES,

		/**
		 * The seed was a single {@code long}.
		 */
		LONG
	}

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the next unsigned (ie, 32-bit) integer.
	 *
	 * @return the next unsigned (ie, 32-bit) integer.
	 */

	int nextUInt();

	//------------------------------------------------------------------

	/**
	 * Returns the next positive (ie, 31-bit) integer.
	 *
	 * @return the next positive (ie, 31-bit) integer.
	 */

	int nextInt();

	//------------------------------------------------------------------

	/**
	 * Returns the next positive (ie, 31-bit) integer, in the range from 0 (inclusive) to the specified upper bound
	 * (exclusive).
	 *
	 * @param  bound
	 *           the exclusive upper bound of the range that will contain the returned value.
	 * @return the next positive (ie, 31-bit) integer, in the range from 0 (inclusive) to {@code bound}
	 */

	int nextInt(
		int	bound);

	//------------------------------------------------------------------

	/**
	 * Returns the next unsigned (ie, 64-bit) long integer value.
	 *
	 * @return the next unsigned (ie, 64-bit) long integer value.
	 */

	long nextULong();

	//------------------------------------------------------------------

	/**
	 * Returns the next positive (ie, 63-bit) long integer value.
	 *
	 * @return the next positive (ie, 63-bit) long integer value.
	 */

	long nextLong();

	//------------------------------------------------------------------

	/**
	 * Returns the next double value in the interval [0, 1).
	 *
	 * @return the next double value in the interval [0, 1).
	 */

	double nextDouble();

	//------------------------------------------------------------------

	/**
	 * Generates sufficient bytes (8-bit integers) to fill the specified buffer, and stores the bytes in the buffer.
	 *
	 * @param buffer
	 *          the array that will be filled will random bytes.
	 */

	void nextBytes(
		byte[]	buffer);

	//------------------------------------------------------------------

	/**
	 * Generates the specified number of bytes (8-bit integers), and stores the bytes in the specified buffer starting
	 * at the specified offset.
	 *
	 * @param buffer
	 *          the array in which random bytes will be stored.
	 * @param offset
	 *          the offset to {@code buffer} at which the first random byte will be written.
	 * @param length
	 *          the number of random bytes that will be stored in {@code buffer}.
	 */

	void nextBytes(
		byte[]	buffer,
		int		offset,
		int		length);

	//------------------------------------------------------------------

	/**
	 * Returns the next Boolean value.
	 *
	 * @return the next Boolean value.
	 */

	default boolean nextBoolean()
	{
		return nextUInt() < 0;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the next value from an exponential distribution with the specified rate parameter.
	 *
	 * @param  lambda
	 *           the rate parameter of the exponential distribution.
	 * @return the next value from an exponential distribution whose rate parameter is {@code lambda}.
	 */

	default double nextExponential(
		double	lambda)
	{
		double x = 0.0;
		while (x == 0.0)
			x = nextDouble();
		return StrictMath.log(x) / -lambda;
	}

	//------------------------------------------------------------------

	/**
	 * Returns an array containing a random permutation of the specified number of zero-based indices.
	 *
	 * @param  numIndices
	 *           the number of zero-based indices that will be permuted.
	 * @return an array of indices, 0 to {@code numIndices}-1 inclusive, that have been randomly permuted.
	 */

	int[] permutedIndices(
		int	numIndices);

	//------------------------------------------------------------------

	/**
	 * Returns the kind of seed with which this pseudo-random number generator was initialised.
	 *
	 * @return the kind of seed with which this pseudo-random number generator was initialised.
	 * @see    #seedBytes()
	 * @see    #seedLong()
	 */

	SeedKind seedKind();

	//------------------------------------------------------------------

	/**
	 * Returns the {@code byte}-array seed with which this pseudo-random number generator was initialised.
	 *
	 * @return if {@link #seedKind()} returns {@link SeedKind#BYTES}, the {@code byte}-array seed with which this
	 *         pseudo-random number generator was initialised; otherwise, {@code null}.
	 * @see    #seedKind()
	 * @see    #seedLong()
	 */

	byte[] seedBytes();

	//------------------------------------------------------------------

	/**
	 * Returns the long-integer seed with which this pseudo-random number generator was initialised.
	 *
	 * @return if {@link #seedKind()} returns {@link SeedKind#LONG}, the long-integer seed with which this pseudo-random
	 *         number generator was initialised; otherwise, 0.
	 * @see    #seedKind()
	 * @see    #seedBytes()
	 */

	long seedLong();

	//------------------------------------------------------------------

	/**
	 * Initialises this pseudo-random number generator with the specified seed.
	 *
	 * @param seed
	 *          the seed with which this pseudo-random number generator will be initialised
	 */

	void seed(
		byte[]	seed);

	//------------------------------------------------------------------

	/**
	 * Initialises this pseudo-random number generator with the specified seed.
	 *
	 * @param seed
	 *          the seed with which this pseudo-random number generator will be initialised.
	 */

	void seed(
		long	seed);

	//------------------------------------------------------------------

	/**
	 * Returns a new instance of a pseudo-random number generator that was initialised with output from this generator.
	 *
	 * @return a new instance of a pseudo-random number generator that was initialised with output from this generator.
	 */

	IPrng child();

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
