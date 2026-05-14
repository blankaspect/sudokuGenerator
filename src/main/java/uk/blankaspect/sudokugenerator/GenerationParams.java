/*====================================================================*\

GenerationParams.java

Class: puzzle-generation parameter set.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import uk.blankaspect.common.basictree.MapNode;

//----------------------------------------------------------------------


// CLASS: PUZZLE-GENERATION PARAMETER SET


abstract class GenerationParams
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	Map<Puzzle.Order, Integer>	DEFAULT_NUM_ENTRIES	= new EnumMap<>(Map.of
	(
		Puzzle.Order._2,   4,
		Puzzle.Order._3,  24,
		Puzzle.Order._4, 112,
		Puzzle.Order._5, 340
	));

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	MAX_FILL_TIME			= "maxFillTime";
		String	NUM_ENTRIES				= "numEntries";
		String	NUM_THREADS				= "numThreads";
		String	RANDOMISE_VERIFICATION	= "randomiseVerification";
		String	SEED					= "seed";
		String	VERIFY_INCREMENTALLY	= "verifyIncrementally";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected GenerationParams()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract MapNode encode();

	//------------------------------------------------------------------

	public abstract void decode(
		MapNode	rootNode);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: COMMON PARAMETERS


	abstract static class Common
		extends GenerationParams
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	boolean	DEFAULT_RANDOMISE_VERIFICATION	= true;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Map<Puzzle.Order, Integer>	numEntries;
		private	Long						seed;
		private	boolean						randomiseVerification;
		private	int							numThreads;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		protected Common()
		{
			// Call alternative constructor
			this(DEFAULT_NUM_ENTRIES, null, DEFAULT_RANDOMISE_VERIFICATION, 0);
		}

		//--------------------------------------------------------------

		protected Common(
			Map<Puzzle.Order, Integer>	numEntries,
			Long						seed,
			boolean						randomiseVerification,
			int							numThreads)
		{
			// Initialise instance variables
			this.numEntries = new EnumMap<>(numEntries);
			this.seed = seed;
			this.randomiseVerification = randomiseVerification;
			this.numThreads = numThreads;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public MapNode encode()
		{
			// Create root node
			MapNode rootNode = new MapNode();

			// Encode number of entries
			MapNode numEntriesNode = rootNode.addMap(PropertyKey.NUM_ENTRIES);
			for (Puzzle.Order order : numEntries.keySet())
				numEntriesNode.addInt(order.key(), numEntries.get(order));

			// Encode seed
			if (seed != null)
				rootNode.addLong(PropertyKey.SEED, seed);

			// Encode 'randomise verification' flag
			rootNode.addBoolean(PropertyKey.RANDOMISE_VERIFICATION, randomiseVerification);

			// Encode number of threads
			if (numThreads > 0)
				rootNode.addInt(PropertyKey.NUM_THREADS, numThreads);

			// Return root node
			return rootNode;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public void decode(
			MapNode	rootNode)
		{
			// Decode number of entries
			String key = PropertyKey.NUM_ENTRIES;
			if (rootNode.hasMap(key))
			{
				MapNode numEntriesNode = rootNode.getMapNode(key);
				for (Puzzle.Order order : Puzzle.Order.values())
				{
					key = order.key();
					if (numEntriesNode.hasInt(key))
						numEntries.put(order, numEntriesNode.getInt(key));
				}
			}

			// Decode seed
			key = PropertyKey.SEED;
			if (rootNode.hasIntOrLong(key))
				seed = rootNode.getIntOrLong(key);

			// Decode 'randomise verification' flag
			randomiseVerification =
					rootNode.getBoolean(PropertyKey.RANDOMISE_VERIFICATION, DEFAULT_RANDOMISE_VERIFICATION);

			// Decode number of threads
			key = PropertyKey.NUM_THREADS;
			if (rootNode.hasInt(key))
				numThreads = rootNode.getInt(key);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Map<Puzzle.Order, Integer> numEntries()
		{
			return Collections.unmodifiableMap(numEntries);
		}

		//--------------------------------------------------------------

		public int numEntries(
			Puzzle.Order	order)
		{
			return numEntries.get(order);
		}

		//--------------------------------------------------------------

		public Long seed()
		{
			return seed;
		}

		//--------------------------------------------------------------

		public boolean randomiseVerification()
		{
			return randomiseVerification;
		}

		//--------------------------------------------------------------

		public int numThreads()
		{
			return numThreads;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PARAMETER SET, ADDITIVE GENERATION


	static class Additive
		extends Common
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Additive()
		{
		}

		//--------------------------------------------------------------

		public Additive(
			Map<Puzzle.Order, Integer>	numEntries,
			Long						seed,
			boolean						randomiseVerification,
			int							numThreads)
		{
			// Call superclass constructor
			super(numEntries, seed, randomiseVerification, numThreads);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PARAMETER SET, SUBTRACTIVE GENERATION


	static class Subtractive
		extends Common
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Map<Puzzle.Order, Integer>	DEFAULT_MAX_FILL_TIMES	= new EnumMap<>(Map.of
		(
			Puzzle.Order._2,    0,
			Puzzle.Order._3,    0,
			Puzzle.Order._4,  500,
			Puzzle.Order._5, 1000
		));

		private static final	boolean	DEFAULT_VERIFY_INCREMENTALLY	= true;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Map<Puzzle.Order, Integer>	maxFillTimes;
		private	boolean						verifyIncrementally;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Subtractive()
		{
			// Initialise instance variables
			maxFillTimes = new EnumMap<>(DEFAULT_MAX_FILL_TIMES);
			verifyIncrementally = DEFAULT_VERIFY_INCREMENTALLY;
		}

		//--------------------------------------------------------------

		public Subtractive(
			Map<Puzzle.Order, Integer>	numEntries,
			Long						seed,
			boolean						randomiseVerification,
			int							numThreads,
			Map<Puzzle.Order, Integer>	maxFillTimes,
			boolean						verifyIncrementally)
		{
			// Call superclass constructor
			super(numEntries, seed, randomiseVerification, numThreads);

			// Initialise remaining instance variables
			this.maxFillTimes = new EnumMap<>(maxFillTimes);
			this.verifyIncrementally = verifyIncrementally;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public MapNode encode()
		{
			// Call superclass method
			MapNode rootNode = super.encode();

			// Encode maximum fill times
			MapNode maxFillTimeNode = rootNode.addMap(PropertyKey.MAX_FILL_TIME);
			for (Puzzle.Order order : maxFillTimes.keySet())
				maxFillTimeNode.addInt(order.key(), maxFillTimes.get(order));

			// Encode 'verify incrementally' flag
			rootNode.addBoolean(PropertyKey.VERIFY_INCREMENTALLY, verifyIncrementally);

			// Return root node
			return rootNode;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public void decode(
			MapNode	rootNode)
		{
			// Call superclass method
			super.decode(rootNode);

			// Decode maximum fill times
			String key = PropertyKey.MAX_FILL_TIME;
			if (rootNode.hasMap(key))
			{
				MapNode maxFillTimeNode = rootNode.getMapNode(key);
				for (Puzzle.Order order : Puzzle.Order.values())
				{
					key = order.key();
					if (maxFillTimeNode.hasInt(key))
						maxFillTimes.put(order, maxFillTimeNode.getInt(key));
				}
			}

			// Decode 'verify incrementally' flag
			verifyIncrementally = rootNode.getBoolean(PropertyKey.VERIFY_INCREMENTALLY, DEFAULT_VERIFY_INCREMENTALLY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Map<Puzzle.Order, Integer> maxFillTimes()
		{
			return Collections.unmodifiableMap(maxFillTimes);
		}

		//--------------------------------------------------------------

		public int maxFillTime(
			Puzzle.Order	order)
		{
			return maxFillTimes.get(order);
		}

		//--------------------------------------------------------------

		public boolean verifyIncrementally()
		{
			return verifyIncrementally;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
