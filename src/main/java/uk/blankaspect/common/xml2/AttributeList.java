/*====================================================================*\

AttributeList.java

Class: list of XML attributes.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.xml2;

//----------------------------------------------------------------------


// IMPORTS


import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import uk.blankaspect.common.function.IFunction1;

//----------------------------------------------------------------------


// CLASS: LIST OF XML ATTRIBUTES


public class AttributeList
	implements Iterable<Attribute>
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<Attribute>	attributes;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public AttributeList()
	{
		// Initialise instance variables
		attributes = new ArrayList<>();
	}

	//------------------------------------------------------------------

	public AttributeList(
		Iterable<? extends Attribute>	attributes)
	{
		// Call alternative constructor
		this();

		// Add attributes
		add(attributes);
	}

	//------------------------------------------------------------------

	public AttributeList(
		Attribute...	attributes)
	{
		// Call alternative constructor
		this();

		// Add attributes
		add(attributes);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Iterable interface
////////////////////////////////////////////////////////////////////////

	@Override
	public Iterator<Attribute> iterator()
	{
		return attributes.iterator();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public List<Attribute> getAttributes()
	{
		return Collections.unmodifiableList(attributes);
	}

	//------------------------------------------------------------------

	public void clear()
	{
		attributes.clear();
	}

	//------------------------------------------------------------------

	public void add(
		String	name,
		boolean	value)
	{
		attributes.add(new Attribute(name, Boolean.toString(value)));
	}

	//------------------------------------------------------------------

	public void add(
		String	name,
		int		value)
	{
		attributes.add(new Attribute(name, Integer.toString(value)));
	}

	//------------------------------------------------------------------

	public void add(
		String	name,
		long	value)
	{
		attributes.add(new Attribute(name, Long.toString(value)));
	}

	//------------------------------------------------------------------

	public void add(
		String	name,
		double	value)
	{
		attributes.add(new Attribute(name, Double.toString(value)));
	}

	//------------------------------------------------------------------

	public void add(
		String			name,
		double			value,
		NumberFormat	format)
	{
		attributes.add(new Attribute(name, format.format(value)));
	}

	//------------------------------------------------------------------

	public void add(
		String	name,
		Object	value)
	{
		attributes.add(new Attribute(name, value.toString()));
	}

	//------------------------------------------------------------------

	public void add(
		String 	name,
		Object	value,
		boolean	escape)
	{
		attributes.add(new Attribute(name, value.toString(), escape));
	}

	//------------------------------------------------------------------

	public <T> void add(
		String 					name,
		T						value,
		IFunction1<String, T>	converter)
	{
		attributes.add(new Attribute(name, converter.invoke(value)));
	}

	//------------------------------------------------------------------

	public <T> void add(
		String 					name,
		T						value,
		boolean					escape,
		IFunction1<String, T>	converter)
	{
		attributes.add(new Attribute(name, converter.invoke(value), escape));
	}

	//------------------------------------------------------------------

	public void add(
		String	name,
		String	value)
	{
		attributes.add(new Attribute(name, value));
	}

	//------------------------------------------------------------------

	public void add(
		String	name,
		String	value,
		boolean	escape)
	{
		attributes.add(new Attribute(name, escape ? XmlUtils.escape(value) : value));
	}

	//------------------------------------------------------------------

	public void add(
		Iterable<? extends Attribute> attributes)
	{
		for (Attribute attribute : attributes)
			this.attributes.add(attribute);
	}

	//------------------------------------------------------------------

	public void add(
		Attribute...	attributes)
	{
		for (Attribute attribute : attributes)
			this.attributes.add(attribute);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
