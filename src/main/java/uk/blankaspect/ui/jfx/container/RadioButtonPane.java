/*====================================================================*\

RadioButtonPane.java

Class: radio-button pane.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.container;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectProperty;

import javafx.geometry.Insets;

import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import uk.blankaspect.ui.jfx.style.StyleManager;

//----------------------------------------------------------------------


// CLASS: RADIO-BUTTON PANE


/**
 * This class implements a pane that contains a group of {@linkplain RadioButton radio buttons}.  The radio buttons may
 * be displayed in a single column or in multiple columns.
 *
 * @param <T>
 *          the type of an object that is associated with a radio button.
 */

public class RadioButtonPane<T>
	extends HBox
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent columns of radio buttons. */
	private static final	double	COLUMN_H_GAP	= 4.0;

	/** The vertical gap between adjacent radio buttons. */
	private static final	double	RADIO_BUTTON_V_GAP	= 8.0;

	/** The padding around the radio-button pane. */
	private static final	Insets	RADIO_BUTTON_PANE_PADDING	= new Insets(2.0, 5.0, 2.0, 5.0);

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of the dependencies of this class with the style manager
		StyleManager.INSTANCE.registerDependencies(PaneStyle.class);
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** A list of the radio buttons of this pane. */
	private	List<RadioButton>	radioButtons;

	/** The toggle group to which the radio buttons belong. */
	private	ToggleGroup			toggleGroup;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a pane that contains a group of radio buttons.
	 *
	 * @param buttonsPerColumn
	 *          the maximum number of radio buttons in a column.  If the value is zero or negative, there will be a
	 *          single column of buttons.
	 * @param items
	 *          the items that will be represented by radio buttons.
	 * @param selectedIndex
	 *          the index of the button that will be initially selected.  If the index is negative, no button will be
	 *          selected.
	 * @param converter
	 *          the function that supplies the string representation of an item that will be used for the label of the
	 *          item's radio button.  If it is {@code null}, a string representation is obtained by calling the item's
	 *          {@link Object#toString() toString()} method.
	 */

	public RadioButtonPane(
		int						buttonsPerColumn,
		Iterable<? extends T>	items,
		int						selectedIndex,
		Function<T, String>		converter)
	{
		// Set properties
		setSpacing(COLUMN_H_GAP);

		// Create radio buttons
		radioButtons = new ArrayList<>();
		toggleGroup = new ToggleGroup();
		VBox columnPane = null;
		int index = 0;
		int rowIndex = 0;
		for (T item : items)
		{
			// Create column pane
			if ((index == 0) || ((buttonsPerColumn > 0) && (rowIndex == buttonsPerColumn)))
			{
				columnPane = new VBox(RADIO_BUTTON_V_GAP);
				columnPane.setPadding(RADIO_BUTTON_PANE_PADDING);
				getChildren().add(columnPane);
				rowIndex = 0;
			}

			// Create radio button
			RadioButton radioButton = new RadioButton((converter == null) ? item.toString() : converter.apply(item));
			radioButton.setToggleGroup(toggleGroup);
			radioButton.setUserData(index);
			if (index == selectedIndex)
				toggleGroup.selectToggle(radioButton);
			columnPane.getChildren().add(radioButton);
			radioButtons.add(radioButton);

			// Increment indices
			++index;
			++rowIndex;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns an unmodifiable list of the radio buttons of this pane.
	 *
	 * @return an unmodifiable list of the radio buttons of this pane.
	 */

	public List<RadioButton> radioButtons()
	{
		return Collections.unmodifiableList(radioButtons);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the radio button that is currently selected.
	 *
	 * @return the radio button that is currently selected, or {@code null} if no button is selected.
	 */

	public RadioButton selectedButton()
	{
		return (RadioButton)toggleGroup.getSelectedToggle();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the selected radio button as a read-only property.
	 *
	 * @return the selected radio button as a read-only property.
	 */

	public ReadOnlyObjectProperty<Toggle> selectedButtonProperty()
	{
		return toggleGroup.selectedToggleProperty();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
