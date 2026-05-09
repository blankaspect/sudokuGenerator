/*====================================================================*\

ErrorLogger.java

Class: logger of XML parsing errors.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.xml2;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

//----------------------------------------------------------------------


// CLASS: LOGGER OF XML PARSING ERRORS


public class ErrorLogger
	implements ErrorHandler
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	ERROR_STR		= "Error";
	private static final	String	FATAL_ERROR_STR	= "Fatal error";
	private static final	String	WARNING_STR		= "Warning";
	private static final	String	LINE_STR		= "Line ";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<String>	errorStrings;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ErrorLogger()
	{
		errorStrings = new ArrayList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ErrorHandler interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void error(
		SAXParseException	exception)
	{
		append(ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	@Override
	public void fatalError(
		SAXParseException	exception)
	{
		append(FATAL_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	@Override
	public void warning(
		SAXParseException	exception)
	{
		append(WARNING_STR, exception);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean isEmpty()
	{
		return errorStrings.isEmpty();
	}

	//------------------------------------------------------------------

	public void clear()
	{
		errorStrings.clear();
	}

	//------------------------------------------------------------------

	public String[] getErrorStrings()
	{
		return errorStrings.toArray(String[]::new);
	}

	//------------------------------------------------------------------

	private void append(
		String				typeStr,
		SAXParseException	exception)
	{
		StringBuilder buffer = new StringBuilder(64);
		buffer.append(LINE_STR);
		buffer.append(exception.getLineNumber());
		buffer.append(": [");
		buffer.append(typeStr);
		buffer.append("] ");
		buffer.append(exception.getMessage());
		errorStrings.add(buffer.toString());
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
