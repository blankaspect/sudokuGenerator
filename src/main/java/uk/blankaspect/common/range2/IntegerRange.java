/*====================================================================*\

IntegerRange.java

Record: integer range.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.range2;

//----------------------------------------------------------------------


// RECORD: INTEGER RANGE


public record IntegerRange(
	int lowerEndpoint,
	int upperEndpoint)
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		IntegerRange	ZERO	= new IntegerRange(0, 0);

	private static final	String	OUT_SEPARATOR	= ", ";

	private static final	String	SEPARATOR_REGEX	= " *, *";

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static IntegerRange of(
		int lowerEndpoint,
		int upperEndpoint)
	{
		// Validate arguments
		if (lowerEndpoint > upperEndpoint)
			throw new IllegalArgumentException("Endpoints out of order");

		// Create new range and return it
		return new IntegerRange(lowerEndpoint, upperEndpoint);
	}

	//------------------------------------------------------------------

	public static IntegerRange copy(
		IntegerRange	range)
	{
		return new IntegerRange(range.lowerEndpoint, range.upperEndpoint);
	}

	//------------------------------------------------------------------

	/**
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 */

	public static IntegerRange from(
		String	str)
	{
		// Split string at separator
		String[] strs = str.split(SEPARATOR_REGEX, -1);
		if (strs.length != 2)
			throw new IllegalArgumentException("Malformed string representation");

		// Parse endpoints; create new range and return it
		return new IntegerRange(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(
		Object	obj)
	{
		if (this == obj)
			return true;

		return (obj instanceof IntegerRange other) && (lowerEndpoint == other.lowerEndpoint)
				&& (upperEndpoint == other.upperEndpoint);
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		int sum = lowerEndpoint + upperEndpoint;
		return sum * (sum + 1) / 2 + lowerEndpoint;
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return new String(lowerEndpoint + OUT_SEPARATOR + upperEndpoint);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int interval()
	{
		return upperEndpoint - lowerEndpoint + 1;
	}

	//------------------------------------------------------------------

	public boolean contains(
		int	value)
	{
		return (value >= lowerEndpoint) && (value <= upperEndpoint);
	}

	//------------------------------------------------------------------

	public int clamp(
		int	value)
	{
		return Math.min(Math.max(lowerEndpoint, value), upperEndpoint);
	}

	//------------------------------------------------------------------

	public int value(
		double	fraction)
	{
		return lowerEndpoint + (int)Math.round((double)(upperEndpoint - lowerEndpoint) * fraction);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
