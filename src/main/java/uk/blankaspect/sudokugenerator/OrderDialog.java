/*====================================================================*\

OrderDialog.java

Class: puzzle-order dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.EnumMap;
import java.util.Map;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;

import javafx.scene.input.KeyEvent;

import javafx.scene.layout.HBox;

import javafx.stage.Window;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.label.Labels;

//----------------------------------------------------------------------


// CLASS: PUZZLE-ORDER DIALOG


/**
 * This class implements a modal dialog in which the order of a puzzle may be selected.
 */

class OrderDialog
	extends SimpleModalDialog<Puzzle.Order>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent components of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 8.0;

	/** The padding around an <i>order</i> button. */
	private static final	Insets	ORDER_BUTTON_PADDING	= new Insets(3.0, 20.0, 3.0, 20.0);

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(4.0);

	/** Miscellaneous strings. */
	private static final	String	SELECT_ORDER_STR	= "Select order of puzzle";
	private static final	String	ORDER_STR			= "Order";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Puzzle.Order	order	= Puzzle.Order._3;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Puzzle.Order	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private OrderDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, SELECT_ORDER_STR);

		// Create control pane
		HBox controlPane = new HBox(CONTROL_PANE_H_GAP, Labels.hNoShrink(ORDER_STR));
		controlPane.setAlignment(Pos.CENTER_LEFT);
		controlPane.setPadding(CONTROL_PANE_PADDING);

		// Create 'order' buttons
		Map<Puzzle.Order, Button> orderButtons = new EnumMap<>(Puzzle.Order.class);
		for (Puzzle.Order order : Puzzle.Order.values())
		{
			Button button = Buttons.hExpansive(order.toString());
			button.setPadding(ORDER_BUTTON_PADDING);
			button.setOnAction(event ->
			{
				// Set result
				result = order;

				// Update saved state
				OrderDialog.order = order;

				// Close dialog
				requestClose();
			});
			orderButtons.put(order, button);
			controlPane.getChildren().add(button);
		}

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: cancel
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'request to close window' event if Escape is pressed
		setRequestCloseOnEscape();

		// When window is shown, request focus on previously selected 'order' button
		setOnShown(event -> orderButtons.get(order).requestFocus());

		// Fire 'order' button when corresponding number key is pressed
		addEventHandler(KeyEvent.KEY_TYPED, event ->
		{
			String str = event.getCharacter();
			if (!KeyEvent.CHAR_UNDEFINED.equals(str))
			{
				for (Puzzle.Order order : orderButtons.keySet())
				{
					Button button = orderButtons.get(order);
					if (button.getText().equals(str))
					{
						button.fire();
						break;
					}
				}
			}
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Puzzle.Order show(
		Window	owner)
	{
		return new OrderDialog(owner).showDialog();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected Puzzle.Order getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
