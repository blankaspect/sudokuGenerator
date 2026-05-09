/*====================================================================*\

Preferences.java

Class: user preferences.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

//----------------------------------------------------------------------


// CLASS: USER PREFERENCES


class Preferences
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	DEFAULT_EDIT_HISTORY_MAX_SIZE	= 200;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	FontInfo						gridFont;
	private	boolean							ignoreCaseInGrid;
	private	EnumMap<Puzzle.Order, char[]>	symbols;
	private	int								editHistoryMaxSize;
	private	boolean							editHistoryClearOnSave;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Preferences()
	{
		// Initialise instance variables
		ignoreCaseInGrid = true;
		symbols = Puzzle.Order.defaultSymbolMap();
		editHistoryMaxSize = DEFAULT_EDIT_HISTORY_MAX_SIZE;
	}

	//------------------------------------------------------------------

	public Preferences(
		FontInfo					gridFont,
		boolean						ignoreCaseInGrid,
		Map<Puzzle.Order, char[]>	symbols,
		int							editHistoryMaxSize,
		boolean						editHistoryClearOnSave)
	{
		// Initialise instance variables
		this.gridFont = gridFont;
		this.ignoreCaseInGrid = ignoreCaseInGrid;
		this.symbols = new EnumMap<>(symbols);
		this.editHistoryMaxSize = editHistoryMaxSize;
		this.editHistoryClearOnSave = editHistoryClearOnSave;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public FontInfo gridFont()
	{
		return gridFont;
	}

	//------------------------------------------------------------------

	public void gridFont(
		FontInfo	fontInfo)
	{
		gridFont = fontInfo;
	}

	//------------------------------------------------------------------

	public boolean ignoreCaseInGrid()
	{
		return ignoreCaseInGrid;
	}

	//------------------------------------------------------------------

	public void ignoreCaseInGrid(
		boolean	ignoreCaseInGrid)
	{
		this.ignoreCaseInGrid = ignoreCaseInGrid;
	}

	//------------------------------------------------------------------

	public Map<Puzzle.Order, char[]> symbols()
	{
		return Collections.unmodifiableMap(symbols);
	}

	//------------------------------------------------------------------

	public void symbols(
		Map<Puzzle.Order, char[]>	symbols)
	{
		for (Puzzle.Order order : symbols.keySet())
			this.symbols.put(order, symbols.get(order).clone());
	}

	//------------------------------------------------------------------

	public int editHistoryMaxSize()
	{
		return editHistoryMaxSize;
	}

	//------------------------------------------------------------------

	public void editHistoryMaxSize(
		int	maxSize)
	{
		editHistoryMaxSize = maxSize;
	}

	//------------------------------------------------------------------

	public boolean editHistoryClearOnSave()
	{
		return editHistoryClearOnSave;
	}

	//------------------------------------------------------------------

	public void editHistoryClearOnSave(
		boolean	clearOnSave)
	{
		editHistoryClearOnSave = clearOnSave;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
