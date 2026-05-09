/*====================================================================*\

FontInfo.java

Record: font information.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// RECORD: FONT INFORMATION


record FontInfo(
	String	name,
	Style	style)
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	NAME	= "name";
		String	STYLE	= "style";
	}

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static FontInfo decode(
		MapNode	rootNode)
	{
		// Decode name
		String name = rootNode.getString(PropertyKey.NAME, "");

		// Decode style
		Style style = rootNode.getEnumValue(Style.class, PropertyKey.STYLE, Style::key, Style.REGULAR);

		// Create font information and return it
		return new FontInfo(name, style);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Font font(
		double	size)
	{
		return Font.font(name, style.bold() ? FontWeight.BOLD : FontWeight.NORMAL,
						 style.italic() ? FontPosture.ITALIC : FontPosture.REGULAR, size);
	}

	//------------------------------------------------------------------

	public MapNode encode()
	{
		// Create root node
		MapNode rootNode = new MapNode();

		// Encode name
		rootNode.addString(PropertyKey.NAME, name);

		// Encode style
		rootNode.addString(PropertyKey.STYLE, style.key);

		// Return root node
		return rootNode;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: FONT STYLE


	enum Style
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		REGULAR
		(
			"Regular"
		),

		BOLD
		(
			"Bold"
		),

		ITALIC
		(
			"Italic"
		),

		BOLD_ITALIC
		(
			"Bold + italic"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Style(
			String	text)
		{
			// Initialise instance variables
			key = StringUtils.toCamelCase(name());
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private String key()
		{
			return key;
		}

		//--------------------------------------------------------------

		private boolean bold()
		{
			return (this == BOLD) || (this == BOLD_ITALIC);
		}

		//--------------------------------------------------------------

		private boolean italic()
		{
			return (this == ITALIC) || (this == BOLD_ITALIC);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
