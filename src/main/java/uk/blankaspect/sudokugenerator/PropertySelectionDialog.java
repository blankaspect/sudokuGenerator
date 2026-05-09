/*====================================================================*\

PropertySelectionDialog.java

Class: property-selection dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import javafx.stage.Window;

import uk.blankaspect.common.function.IFunction0;
import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

//----------------------------------------------------------------------


// CLASS: PROPERTY-SELECTION DIALOG


/**
 * This class implements a modal dialog in which properties of a puzzle may be selected.
 */

class PropertySelectionDialog
	extends SimpleModalDialog<Set<Puzzle.Key>>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent components of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The vertical gap between adjacent components of the control pane. */
	private static final	double	CONTROL_PANE_V_GAP	= 8.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(6.0, 8.0, 6.0, 8.0);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Set<Puzzle.Key>	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private PropertySelectionDialog(
		Window	owner,
		String	title,
		Puzzle	puzzle)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, title);

		// Create control pane
		GridPane controlPane = new GridPane();
		controlPane.setHgap(CONTROL_PANE_H_GAP);
		controlPane.setVgap(CONTROL_PANE_V_GAP);
		controlPane.setAlignment(Pos.CENTER);
		controlPane.setPadding(CONTROL_PANE_PADDING);

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		controlPane.getColumnConstraints().add(column);

		// Create check box for each property of puzzle and add it to control pane
		EnumMap<Puzzle.Key, CheckBox> checkBoxes = new EnumMap<>(Puzzle.Key.class);
		int row = 0;
		for (Puzzle.Key key : Puzzle.Key.values())
		{
			if (puzzle.hasProperty(key))
			{
				CheckBox checkBox = new CheckBox(key.toString());
				checkBoxes.put(key, checkBox);
				controlPane.add(checkBox, 1, row++);
			}
		}

		// Create function to return set of selected properties
		IFunction0<EnumSet<Puzzle.Key>> selectedProperties = () ->
		{
			EnumSet<Puzzle.Key> properties = EnumSet.noneOf(Puzzle.Key.class);
			for (Puzzle.Key key : checkBoxes.keySet())
			{
				if (checkBoxes.get(key).isSelected())
					properties.add(key);
			}
			return properties;
		};

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Set result
			result = selectedProperties.invoke();

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
		IProcedure0 updateOkButton = () -> okButton.setDisable(selectedProperties.invoke().isEmpty());

		// Update 'OK' button if 'selected' state of a check box changes
		checkBoxes.values().stream().forEach(checkBox ->
				checkBox.selectedProperty().addListener(observable -> updateOkButton.invoke()));

		// Update 'OK' button
		updateOkButton.invoke();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Set<Puzzle.Key> show(
		Window	owner,
		String	title,
		Puzzle	puzzle)
	{
		return new PropertySelectionDialog(owner, title, puzzle).showDialog();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected Set<Puzzle.Key> getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
