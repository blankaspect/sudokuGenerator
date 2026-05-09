/*====================================================================*\

GenerationMode.java

Enumeration: puzzle-generation modes.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

//----------------------------------------------------------------------


// ENUMERATION: PUZZLE-GENERATION MODES


enum GenerationMode
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	ADDITIVE
	(
		"additive1",
		"add1",
		"Additive",
		GenerationParams.Additive.class,
		0
	),

	SUBTRACTIVE
	(
		"subtractive1",
		"sub1",
		"Subtractive",
		GenerationParams.Subtractive.class,
		0
	);

	private static final	String	VERSION_STRING_SEPARATOR	= "-";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private String								key;
	private String								prefix;
	private String								text;
	private	Class<? extends GenerationParams>	paramsClass;
	private	int									version;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private GenerationMode(
		String								key,
		String								prefix,
		String								text,
		Class<? extends GenerationParams>	paramsClass,
		int									version)
	{
		// Initialise instance variables
		this.key = key;
		this.prefix = prefix;
		this.text = text;
		this.paramsClass = paramsClass;
		this.version = version;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String key()
	{
		return key;
	}

	//------------------------------------------------------------------

	public String prefix()
	{
		return prefix;
	}

	//------------------------------------------------------------------

	public int version()
	{
		return version;
	}

	//------------------------------------------------------------------

	public String versionString()
	{
		return prefix + VERSION_STRING_SEPARATOR + version;
	}

	//------------------------------------------------------------------

	public GenerationParams newParams()
	{
		try
		{
			return paramsClass.getConstructor().newInstance();
		}
		catch (Throwable e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
