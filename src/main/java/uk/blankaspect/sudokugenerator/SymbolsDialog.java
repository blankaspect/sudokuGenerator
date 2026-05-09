/*====================================================================*\

SymbolsDialog.java

Class: symbols dialog.

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

import javafx.scene.layout.HBox;

import javafx.stage.Window;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.label.Labels;

//----------------------------------------------------------------------


// CLASS: SYMBOLS DIALOG


/**
 * This class implements a modal dialog in which the symbols of a puzzle may be edited.
 */

class SymbolsDialog
	extends SimpleModalDialog<char[]>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent components of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(8.0, 12.0, 8.0, 12.0);

	/** Miscellaneous strings. */
	private static final	String	EDIT_SYMBOLS_STR	= "Edit symbols of puzzle";
	private static final	String	SYMBOLS_STR			= "Symbols";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	char[]		result;
	private	SymbolsPane	symbolsPane;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private SymbolsDialog(
		Window			owner,
		Puzzle.Order	order,
		char[]			symbols)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, EDIT_SYMBOLS_STR);

		// Pane: symbols
		symbolsPane = new SymbolsPane();
		symbolsPane.symbols(order, symbols);

		// Create control pane
		HBox controlPane = new HBox(CONTROL_PANE_H_GAP, Labels.hNoShrink(SYMBOLS_STR), symbolsPane);
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
			result = symbolsPane.symbols();

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

		// Create procedure to update 'OK' button
		IProcedure0 updateOkButton = () -> okButton.setDisable(!symbolsPane.symbolsValid());

		// Update 'OK' button when content of symbols field changes
		symbolsPane.symbolsField().textProperty().addListener(observable -> updateOkButton.invoke());

		// Update 'OK' button
		updateOkButton.invoke();

		// Apply new style sheet to scene
		applyStyleSheet();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static char[] show(
		Window			owner,
		Puzzle.Order	order,
		char[]			symbols)
	{
		return new SymbolsDialog(owner, order, symbols).showDialog();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected char[] getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

	@Override
	protected void onWindowShown()
	{
		// Call superclass method
		super.onWindowShown();

		// Move caret to end of symbols field
		symbolsPane.symbolsField().end();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
