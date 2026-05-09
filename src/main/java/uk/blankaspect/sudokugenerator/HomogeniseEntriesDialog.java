/*====================================================================*\

HomogeniseEntriesDialog.java

Class: 'homogenise entries' dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;

import javafx.scene.layout.HBox;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.DialogState;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.dropdownlist.SimpleDropDownList;

import uk.blankaspect.ui.jfx.label.Labels;

//----------------------------------------------------------------------


// CLASS: 'HOMOGENISE ENTRIES' DIALOG


class HomogeniseEntriesDialog
	extends SimpleModalDialog<Integer>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The prototype text for the <i>value</i> drop-down list. */
	private static final	String	VALUE_PROTOTYPE_TEXT	= "0".repeat(4);

	/** The horizontal gap between adjacent components of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(8.0, 24.0, 8.0, 24.0);

	/** Miscellaneous strings. */
	private static final	String	HOMOGENISE_ENTRIES_STR	= "Homogenise entries";
	private static final	String	SYMBOL_STR				= "Symbol";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state	= new State();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Integer	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private HomogeniseEntriesDialog(
		Window	owner,
		Puzzle	puzzle)
	{
		// Call superclass constructor
		super(owner, HOMOGENISE_ENTRIES_STR, state.locator(), null);

		// Drop-down list: value
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < puzzle.symbols().length; i++)
			items.add(i + 1);
		SimpleDropDownList<Integer> valueDropDownList = new SimpleDropDownList<>(VALUE_PROTOTYPE_TEXT, items, item ->
				(item == null) ? null : Character.toString(puzzle.symbols()[item - 1]));
		valueDropDownList.item(state.values.get(puzzle.puzzleOrder()));

		// Create control pane
		HBox controlPane = new HBox(CONTROL_PANE_H_GAP, Labels.hNoShrink(SYMBOL_STR), valueDropDownList);
		controlPane.setAlignment(Pos.CENTER_LEFT);
		controlPane.setPadding(CONTROL_PANE_PADDING);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Set result
			result = valueDropDownList.item();

			// Close dialog
			requestClose();
		});
		addButton(okButton, HPos.RIGHT);

		// Create button: cancel
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, okButton);

		// Save dialog state when dialog is closed
		setOnHiding(event ->
		{
			state.restoreAndUpdate(this, true);
			state.values.put(puzzle.puzzleOrder(), valueDropDownList.item());
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static MapNode encodeState()
	{
		return state.encodeTree();
	}

	//------------------------------------------------------------------

	public static void decodeState(
		MapNode	rootNode)
	{
		state.decodeTree(rootNode);
	}

	//------------------------------------------------------------------

	public static Integer show(
		Window	owner,
		Puzzle	puzzle)
	{
		return new HomogeniseEntriesDialog(owner, puzzle).showDialog();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected Integer getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: DIALOG STATE


	private static class State
		extends DialogState
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Map<Puzzle.Order, Integer>	DEFAULT_VALUES;

		/** Keys of properties. */
		private interface PropertyKey
		{
			String	VALUES	= "values";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Map<Puzzle.Order, Integer>	values;

	////////////////////////////////////////////////////////////////////
	//  Static initialiser
	////////////////////////////////////////////////////////////////////

		static
		{
			DEFAULT_VALUES = new EnumMap<>(Puzzle.Order.class);
			for (Puzzle.Order order : Puzzle.Order.values())
				DEFAULT_VALUES.put(order, 1);
		}

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, false);

			// Initialise instance variables
			values = new EnumMap<>(DEFAULT_VALUES);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public MapNode encodeTree()
		{
			// Call superclass method
			MapNode rootNode = super.encodeTree();

			// Encode values
			MapNode valuesNode = rootNode.addMap(PropertyKey.VALUES);
			for (Puzzle.Order order : values.keySet())
				valuesNode.addInt(order.key(), values.get(order));

			// Return root node
			return rootNode;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public void decodeTree(
			MapNode	rootNode)
		{
			// Call superclass method
			super.decodeTree(rootNode);

			// Decode values
			String key = PropertyKey.VALUES;
			if (rootNode.hasMap(key))
			{
				MapNode node = rootNode.getMapNode(key);
				for (Puzzle.Order order : Puzzle.Order.values())
				{
					key = order.key();
					if (node.hasInt(key))
						values.put(order, node.getInt(key));
				}
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
