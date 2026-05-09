/*====================================================================*\

CopyTextDialog.java

Class: 'copy text' dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import javafx.scene.layout.HBox;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

//----------------------------------------------------------------------


// CLASS: 'COPY TEXT' DIALOG


class CopyTextDialog
	extends SimpleModalDialog<Boolean>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(6.0, 32.0, 6.0, 32.0);

	/** Miscellaneous strings. */
	private static final	String	COPY_TEXT_STR		= "Copy text to clipboard";
	private static final	String	SEPARATE_BLOCKS_STR	= "Separate blocks";

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	SEPARATE_BLOCKS	= "separateBlocks";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	boolean	separateBlocks;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Boolean	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CopyTextDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, COPY_TEXT_STR);

		// Check box: separate blocks
		CheckBox separateBlocksCheckBox = new CheckBox(SEPARATE_BLOCKS_STR);
		separateBlocksCheckBox.setSelected(separateBlocks);

		// Create control pane
		HBox controlPane = new HBox(separateBlocksCheckBox);
		controlPane.setAlignment(Pos.CENTER_LEFT);
		controlPane.setPadding(CONTROL_PANE_PADDING);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Update dialog state
			separateBlocks = separateBlocksCheckBox.isSelected();

			// Set result
			result = separateBlocks;

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

		// Fire 'request to close window' event if Escape is pressed
		setRequestCloseOnEscape();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Boolean show(
		Window	owner)
	{
		return new CopyTextDialog(owner).showDialog();
	}

	//------------------------------------------------------------------

	public static MapNode encodeState()
	{
		// Create root node
		MapNode rootNode = new MapNode();

		// Encode 'separate blocks' flag
		rootNode.addBoolean(PropertyKey.SEPARATE_BLOCKS, separateBlocks);

		// Return root node
		return rootNode;
	}

	//------------------------------------------------------------------

	public static void decodeState(
		MapNode	rootNode)
	{
		// Decode 'separate blocks' flag
		String key = PropertyKey.SEPARATE_BLOCKS;
		if (rootNode.hasBoolean(key))
			separateBlocks = rootNode.getBoolean(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected Boolean getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
