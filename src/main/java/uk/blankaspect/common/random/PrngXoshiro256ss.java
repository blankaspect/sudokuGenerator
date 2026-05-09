/*====================================================================*\

PrngXoshiro256ss.java

Class: xoshiro256** pseudo-random number generator.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.random;

//----------------------------------------------------------------------


// IMPORTS


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

//----------------------------------------------------------------------


// CLASS: XOSHIRO256** PSEUDO-RANDOM NUMBER GENERATOR


/**
 * This class implements a <a href="https://prng.di.unimi.it/xoshiro256starstar.c">xoshiro256**</a> pseudo-random number
 * generator that uses four 64-bit words (256 bits) for its state.  It was devised by David Blackman and Sebastiano
 * Vigna and is a member of the <a href="https://en.wikipedia.org/wiki/Xorshift">xorshift</a> family of generators.
 */

public class PrngXoshiro256ss
	implements IPrng
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The number of 64-bit words that constitute the state of a PRNG. */
	public static final		int		NUM_STATE_WORDS	= 4;

	/** The number of bytes that constitute the state of a PRNG. */
	public static final		int		NUM_STATE_BYTES	= NUM_STATE_WORDS * Long.BYTES;

	/** The name of the hash function that is applied to a seed. */
	private static final	String	SEED_HASH_NAME	= "SHA-256";

	/** The hash function that is used with a {@code byte}-array seed. */
	private static final	MessageDigest	SEED_HASH;

	/** Miscellaneous strings. */
	private static final	String	NULL_STATE_STR		= "Null state";
	private static final	String	LENGTH_OF_STATE_STR	= "Length of state must be ";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The seed with which the pseudo-random number generator was initialised. */
	private	SeedKind	seedKind;

	/** The {@code byte}-array seed with which the pseudo-random number generator was initialised. */
	private	byte[]		seedBytes;

	/** The {@code long} seed with which the pseudo-random number generator was initialised. */
	private	long		seedLong;

	/** The state of this pseudo-random number generator. */
	private	long[]		state;

	/** The initial state of this pseudo-random number generator. */
	private	long[]		initialState;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Initialise hash function that is applied to a {@code byte}-array seed
		try
		{
			SEED_HASH = MessageDigest.getInstance(SEED_HASH_NAME);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new UnexpectedRuntimeException(e);
		}

		// Check length of hash value
		if (SEED_HASH.digest(new byte[1]).length != NUM_STATE_BYTES)
			throw new UnexpectedRuntimeException(SEED_HASH_NAME + ": unexpected length of hash value");
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a <i>xoshiro256**</i> pseudo-random number generator and initialises it with a
	 * pseudo-randomly generated seed.
	 */

	public PrngXoshiro256ss()
	{
		// Call alternative constructor
		this(null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a <i>xoshiro256**</i> pseudo-random number generator and initialises it with the
	 * specified seed.
	 *
	 * @param seed
	 *          the seed with which the pseudo-random number generator will be initialised.
	 */

	public PrngXoshiro256ss(
		long	seed)
	{
		// Initialise instance variables
		state = new long[NUM_STATE_WORDS];
		initialState = new long[NUM_STATE_WORDS];

		// Set seed
		seed(seed);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an <i>xoshiro256**</i> pseudo-random number generator and initialises it with the
	 * specified seed.
	 *
	 * @param seed
	 *          the seed with which the pseudo-random number generator will be initialised.  If it is {@code null} or
	 *          its length is 0, a pseudo-randomly generated seed will be used.
	 */

	public PrngXoshiro256ss(
		byte[]	seed)
	{
		// Initialise instance variables
		state = new long[NUM_STATE_WORDS];
		initialState = new long[NUM_STATE_WORDS];

		// Set seed
		seed(seed);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if the specified long-integer array is a viable state for a <i>xoshiro256**</i>
	 * pseudo-random number generator.  The specified state is deemed viable if none of the elements of the array is
	 * zero.
	 *
	 * @param  state
	 *           the state that will be tested.
	 * @return {@code true} if {@code state} is a viable state for a <i>xoshiro256**</i> pseudo-random number generator.
	 * @throws IllegalArgumentException
	 *           if {@code state} is {@code null} or the length of {@code state} is not {@link #NUM_STATE_WORDS}.
	 */

	public static boolean isViableState(
		long[]	state)
	{
		// Validate argument
		if (state == null)
			throw new IllegalArgumentException(NULL_STATE_STR);
		if (state.length != NUM_STATE_WORDS)
			throw new IllegalArgumentException(LENGTH_OF_STATE_STR + NUM_STATE_WORDS);

		// Test for any zero words
		for (int i = 0; i < NUM_STATE_WORDS; i++)
		{
			if (state[i] == 0)
				return false;
		}
		return true;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified byte array is a viable state for a <i>xoshiro256**</i> pseudo-random number
	 * generator.  The specified state is deemed viable if the specified array is viewed as a sequence of 64-bit words
	 * and none of the words is zero.
	 *
	 * @param  state
	 *           the state that will be tested.
	 * @return {@code true} if {@code state} is a viable state for a <i>xoshiro256**</i> pseudo-random number generator.
	 * @throws IllegalArgumentException
	 *           if {@code state} is {@code null} or the length of {@code state} is not {@link #NUM_STATE_BYTES}.
	 */

	public static boolean isViableState(
		byte[]	state)
	{
		// Validate argument
		if (state == null)
			throw new IllegalArgumentException(NULL_STATE_STR);
		if (state.length != NUM_STATE_BYTES)
			throw new IllegalArgumentException(LENGTH_OF_STATE_STR + NUM_STATE_BYTES);

		// Test for any zero words
		for (int i = 0; i < NUM_STATE_WORDS; i++)
		{
			boolean zero = true;
			int index = i * Long.BYTES;
			int endIndex = index + Long.BYTES;
			for (int j = index; j < endIndex; j++)
			{
				if (state[j] != 0)
				{
					zero = false;
					break;
				}
			}
			if (zero)
				return false;
		}
		return true;
	}

	//------------------------------------------------------------------

	/**
	 * Copies PRNG state from the source array to the destination array.
	 *
	 * @param source
	 *          the source state.
	 * @param dest
	 *          the destination state.
	 */

	private static void copyState(
		long[]	source,
		long[]	dest)
	{
		System.arraycopy(source, 0, dest, 0, NUM_STATE_WORDS);
	}

	//------------------------------------------------------------------

	/**
	 * Applies the SHA-256 hash function repeatedly to the specified bytes until the result is a viable state for a
	 * <i>xoshiro256**</i> pseudo-random number generator, and returns the result.
	 *
	 * @param  data
	 *           the data to which the hash function will be applied.
	 * @return a viable state for a <i>xoshiro256**</i> pseudo-random number generator that is generated by applying the
	 *         SHA-256 hash function to {@code data}.
	 */

	private static byte[] bytesToState(
		byte[]	data)
	{
		byte[] bytes = data;
		while (true)
		{
			SEED_HASH.reset();
			bytes = SEED_HASH.digest(bytes);
			if (isViableState(bytes))
				break;
		}
		return bytes;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IPrng interface
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	public int nextUInt()
	{
		return (int)(nextULong() & 0xFFFF_FFFFL);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public int nextInt()
	{
		return (int)(nextULong() & 0x7FFF_FFFFL);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException
	 *           if {@code bound} is negative.
	 */

	@Override
	public int nextInt(
		int	bound)
	{
		// Validate arguments
		if (bound < 0)
			throw new IllegalArgumentException("Bound is negative");

		// Calculate product of next random integer and bound; return upper 31 bits of result
		long product = (nextULong() & 0x7FFF_FFFFL) * (long)bound;
		return (int)(product >>> 31);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public long nextULong()
	{
		long result = Long.rotateLeft(state[1] * 5, 7) * 9;

		long t = state[1] << 17;

		state[2] ^= state[0];
		state[3] ^= state[1];
		state[1] ^= state[2];
		state[0] ^= state[3];

		state[2] ^= t;

		state[3] = Long.rotateLeft(state[3], 45);

		return result;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public long nextLong()
	{
		return nextULong() & 0x7FFF_FFFF_FFFF_FFFFL;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the next double value in the interval [0, 1).  The value is obtained by converting a 64-bit integer value
	 * to an IEEE 754 double-format bit field.  The 64-bit value is deemed to represent a binary fraction of the
	 * form 0.b[63]...b[0].  It is normalised by shifting it to the left until the msb is 1.  The msb is then discarded
	 * because the significand has an implied leading '1' bit; the remaining bits are right-shifted into bits 51..0, and
	 * the exponent is set in bits 62..52.
	 *
	 * @return the next double value in the interval [0, 1).
	 */

	@Override
	public double nextDouble()
	{
		// Get a random 64-bit integer
		long value = nextULong();

		// Convert the integer to an 11-bit exponent and a normalised 52-bit significand
		if (value != 0)
		{
			long exponent = 1022;
			while (value > 0)
			{
				value <<= 1;
				--exponent;
			}
			value <<= 1;
			value >>>= 12;
			value |= exponent << 52;
		}

		// Convert the bit field to a double and return it
		return Double.longBitsToDouble(value);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public void nextBytes(
		byte[]	buffer)
	{
		nextBytes(buffer, 0, buffer.length);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException
	 *           if
	 *           <ul>
	 *             <li>
	 *               {@code buffer} is {@code null}, or
	 *             </li>
	 *             <li>
	 *               {@code offset} is negative or beyond the end of {@code buffer}, or
	 *             </li>
	 *             <li>
	 *               {@code length} is negative, or the sum of {@code offset} and {@code length} is beyond the end of
	 *               {@code buffer}.
	 *             </li>
	 *           </ul>
	 */

	@Override
	public void nextBytes(
		byte[]	buffer,
		int		offset,
		int		length)
	{
		// Validate arguments
		if (buffer == null)
			throw new IllegalArgumentException("Null buffer");
		if ((offset < 0) || (offset > buffer.length))
			throw new IllegalArgumentException("Offset out of bounds: " + offset);
		if ((length < 0) || (length > buffer.length - offset))
			throw new IllegalArgumentException("Length out of bounds: " + length);

		// Get unsigned longs, split each long into bytes and store bytes in buffer
		long value = 0;
		int i = 0;
		int endOffset = offset + length;
		while (offset < endOffset)
		{
			if (i == 0)
			{
				value = nextULong();
				i = Long.BYTES;
			}
			--i;
			buffer[offset++] = (byte)value;
			value >>>= Byte.SIZE;
		}
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException
	 *           if {@code numIndices} is negative.
	 */

	@Override
	public int[] permutedIndices(
		int	numIndices)
	{
		// Validate argument
		if (numIndices < 0)
			throw new IllegalArgumentException("Number of indices is negative");

		// Initialise array of indices
		int[] indices = new int[numIndices];

		// Permute indices
		for (int i = 0; i < numIndices; i++)
		{
			int j = nextInt(i + 1);
			indices[i] = indices[j];
			indices[j] = i;
		}

		// Return indices
		return indices;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public SeedKind seedKind()
	{
		return seedKind;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public byte[] seedBytes()
	{
		return seedBytes.clone();
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public long seedLong()
	{
		return seedLong;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * @param seed
	 *          the seed with which this pseudo-random number generator will be initialised.  If it is {@code null} or
	 *          its length is 0, a pseudo-randomly generated seed will be used.
	 */

	@Override
	public void seed(
		byte[]	seed)
	{
		// Case: initialise state with pseudo-randomly generated seed
		byte[] bytes = null;
		if ((seed == null) || (seed.length == 0))
		{
			// Update instance variables
			seedKind = SeedKind.NONE;
			seedBytes = null;
			seedLong = 0;

			// Get source of seed
			SecureRandom prng = new SecureRandom();

			// Generate random seed
			while (true)
			{
				bytes = prng.generateSeed(NUM_STATE_BYTES);
				if (isViableState(bytes))
					break;
			}
		}

		// Case: initialise state with hash of seed
		else
		{
			// Update instance variables
			seedKind = SeedKind.BYTES;
			seedBytes = seed.clone();
			seedLong = 0;

			// Apply hash function to seed
			bytes = bytesToState(seed);
		}

		// Initialise state
		initState(bytes);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public void seed(
		long	seed)
	{
		// Update instance variables
		seedKind = SeedKind.LONG;
		seedBytes = null;
		seedLong = seed;

		// Extract bytes of seed
		long value = seed;
		byte[] bytes = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++)
		{
			bytes[i] = (byte)value;
			value >>>= Byte.SIZE;
		}

		// Initialise state with hash of bytes of seed
		initState(bytesToState(bytes));
	}

	//------------------------------------------------------------------

	/**
	 * Returns a new instance of a pseudo-random number generator that was initialised with an {@linkplain #nextULong()
	 * unsigned long integer} from this PRNG.
	 *
	 * @return a new instance of a pseudo-random number generator that was initialised with an unsigned long integer
	 *         from this PRNG.
	 */

	@Override
	public PrngXoshiro256ss child()
	{
		return new PrngXoshiro256ss(nextULong());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a copy of the current state of this pseudo-random number generator.
	 *
	 * @return a copy of the current state of this pseudo-random number generator.
	 */

	public long[] state()
	{
		return state.clone();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the state of this pseudo-random number generator to the specified value.
	 * <p>
	 * The specified state is not checked for viability (that is, whether all the elements of {@code state} are
	 * non-zero).  Such a check can be performed by calling {@link #isViableState(long[])} before calling this method.
	 * </p>
	 *
	 * @param state
	 *          the value to which the state of this pseudo-random number generator will be set.
	 */

	public void state(
		long[]	state)
	{
		// Validate argument
		if (state == null)
			throw new IllegalArgumentException(NULL_STATE_STR);
		if (state.length != NUM_STATE_WORDS)
			throw new IllegalArgumentException(LENGTH_OF_STATE_STR + NUM_STATE_WORDS);

		// Update instance variables
		copyState(state, this.state);
		copyState(state, initialState);
	}

	//------------------------------------------------------------------

	/**
	 * <p style="margin-bottom: 0.25em;">
	 * Resets the state of this pseudo-random number generator to the state that existed immediately after the most
	 * recent of the following:
	 * </p>
	 * <ul style="margin-top: 0.25em;">
	 *   <li>the generator was constructed,</li>
	 *   <li>its seed was set with {@link #seed(byte[])} or {@link #seed(long)}, or</li>
	 *   <li>its state was set with {@link #state(long[])}.</li>
	 * </ul>
	 *
	 * @see #seed(byte[])
	 * @see #seed(long)
	 * @see #state(int[])
	 */

	public void reset()
	{
		copyState(initialState, state);
	}

	//------------------------------------------------------------------

	/**
	 * Initialises the state of this pseudo-random number generator with the specified data.
	 *
	 * @param  data
	 *           the data with which the state of this pseudo-random number generator will be initialised.
	 * @throws IllegalArgumentException
	 *           if {@code data} is {@code null} or its length is incorrect.
	 */

	private void initState(
		byte[]	data)
	{
		// Validate argument
		if (data == null)
			throw new IllegalArgumentException("Null data");
		if (data.length != NUM_STATE_BYTES)
			throw new IllegalArgumentException("Data length must be " + NUM_STATE_BYTES);

		// Fill state with supplied data
		int index = 0;
		for (int i = 0; i < state.length; i++)
		{
			long value = 0;
			for (int j = 0; j < Long.BYTES; j++)
			{
				value <<= 8;
				value |= data[index++] & 0xFF;
			}
			state[i] = value;
		}

		// Update initial state
		copyState(state, initialState);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
