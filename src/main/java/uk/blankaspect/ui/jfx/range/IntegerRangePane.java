/*====================================================================*\

IntegerRangePane.java

Class: integer-range pane.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.range;

//----------------------------------------------------------------------


// IMPORTS


import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.geometry.Pos;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import javafx.scene.layout.HBox;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.range2.IntegerRange;

import uk.blankaspect.ui.jfx.button.LinkUnlinkButton;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

//----------------------------------------------------------------------


// CLASS: INTEGER-RANGE PANE


public class IntegerRangePane
	extends HBox
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent controls. */
	private static final	double	H_GAP	= 6.0;

	/** Miscellaneous strings. */
	private static final	String	TO_STR						= "to";
	private static final	String	UNEXPECTED_FACTORY_TYPE_STR	= "Unexpected type of spinner value factory";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The range that is determined by {@link #lowerEndpointSpinner} and {@link #upperEndpointSpinner}. */
	private	SimpleObjectProperty<IntegerRange>				range;

	/** The spinner for the lower endpoint. */
	private	Spinner<Integer>								lowerEndpointSpinner;

	/** The value factory for the {@linkplain #lowerEndpointSpinner lower-endpoint spinner}. */
	private	SpinnerValueFactory.IntegerSpinnerValueFactory	lowerValueFactory;

	/** The spinner for the upper endpoint. */
	private	Spinner<Integer>								upperEndpointSpinner;

	/** The value factory for the {@linkplain #upperEndpointSpinner upper-endpoint spinner}. */
	private	SpinnerValueFactory.IntegerSpinnerValueFactory	upperValueFactory;

	/** The button for linking and unlinking the lower and upper endpoints. */
	private	LinkUnlinkButton								linkButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public IntegerRangePane(
		int	minValue,
		int	maxValue)
	{
		// Call alternative constructor
		this(minValue, maxValue, minValue, maxValue, 0, false);
	}

	//------------------------------------------------------------------

	public IntegerRangePane(
		int		minValue,
		int		maxValue,
		boolean	linkable)
	{
		// Call alternative constructor
		this(minValue, maxValue, minValue, maxValue, 0, linkable);
	}

	//------------------------------------------------------------------

	public IntegerRangePane(
		int	minValue,
		int	maxValue,
		int	numDigits)
	{
		// Call alternative constructor
		this(minValue, maxValue, minValue, maxValue, numDigits, false);
	}

	//------------------------------------------------------------------

	public IntegerRangePane(
		int	minValue,
		int	maxValue,
		int	numDigits,
		boolean	linkable)
	{
		// Call alternative constructor
		this(minValue, maxValue, minValue, maxValue, numDigits, linkable);
	}

	//------------------------------------------------------------------

	public IntegerRangePane(
		int	minValue,
		int	maxValue,
		int	lowerEndpoint,
		int	upperEndpoint)
	{
		// Call alternative constructor
		this(minValue, maxValue, lowerEndpoint, upperEndpoint, 0, false);
	}

	//------------------------------------------------------------------

	public IntegerRangePane(
		int		minValue,
		int		maxValue,
		int		lowerEndpoint,
		int		upperEndpoint,
		boolean	linkable)
	{
		// Call alternative constructor
		this(minValue, maxValue, lowerEndpoint, upperEndpoint, 0, linkable);
	}

	//------------------------------------------------------------------

	public IntegerRangePane(
		int	minValue,
		int	maxValue,
		int	lowerEndpoint,
		int	upperEndpoint,
		int	numDigits)
	{
		// Call alternative constructor
		this(minValue, maxValue, lowerEndpoint, upperEndpoint, numDigits, false);
	}

	//------------------------------------------------------------------

	public IntegerRangePane(
		int		minValue,
		int		maxValue,
		int		lowerEndpoint,
		int		upperEndpoint,
		int		numDigits,
		boolean	linkable)
	{
		// Validate arguments
		if (minValue > maxValue)
			throw new IllegalArgumentException("Minimum and maximum values out of order");

		// Initialise instance variables
		range = new SimpleObjectProperty<>(IntegerRange.ZERO);

		// Set properties
		setSpacing(H_GAP);
		setAlignment(Pos.CENTER_LEFT);

		// If number of digits was not specified, use number of digits in maximum value
		if (numDigits <= 0)
			numDigits = NumberUtils.getNumDecDigitsInt(maxValue);

		// Create procedure to update range
		IProcedure0 updateRange = () ->
				range.set(IntegerRange.of(lowerEndpointSpinner.getValue(), upperEndpointSpinner.getValue()));

		// Create spinner for lower endpoint
		lowerEndpointSpinner =
				SpinnerFactory.integerSpinner(minValue, maxValue, clamp(lowerEndpoint, minValue, maxValue), numDigits);
		if (!(lowerEndpointSpinner.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory lvf))
			throw new UnexpectedRuntimeException(UNEXPECTED_FACTORY_TYPE_STR);
		lowerValueFactory = lvf;
		lowerEndpointSpinner.valueProperty().addListener((observable, oldValue, value) ->
		{
			if (linkable && linkButton.isSelected())
				upperValueFactory.setValue(value);
			else
			{
				int upperBound = upperValueFactory.getValue();
				if (value > upperBound)
				{
					value = upperBound;
					lowerValueFactory.setValue(value);
					lowerEndpointSpinner.getEditor().setText(Integer.toString(value));
				}
				upperValueFactory.setMin(value);
			}

			updateRange.invoke();
		});

		// Create spinner for upper endpoint
		upperEndpointSpinner =
				SpinnerFactory.integerSpinner(minValue, maxValue, clamp(upperEndpoint, minValue, maxValue), numDigits);
		if (!(upperEndpointSpinner.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory uvf))
			throw new UnexpectedRuntimeException(UNEXPECTED_FACTORY_TYPE_STR);
		upperValueFactory = uvf;
		upperEndpointSpinner.valueProperty().addListener((observable, oldValue, value) ->
		{
			if (linkable && linkButton.isSelected())
				lowerValueFactory.setValue(value);
			else
			{
				int lowerBound = lowerValueFactory.getValue();
				if (value < lowerBound)
				{
					value = lowerBound;
					upperValueFactory.setValue(value);
					upperEndpointSpinner.getEditor().setText(Integer.toString(value));
				}
				lowerValueFactory.setMax(value);
			}

			updateRange.invoke();
		});

		// Add children to this pane
		getChildren().addAll(lowerEndpointSpinner, Labels.hNoShrink(TO_STR), upperEndpointSpinner);

		// Create link button
		if (linkable)
		{
			// Create button
			linkButton = LinkUnlinkButton.horizontal();
			linkButton.selectedProperty().addListener((observable, oldSelected, selected) ->
			{
				if (selected)
				{
					lowerValueFactory.setMax(maxValue);
					upperValueFactory.setMin(minValue);

					upperValueFactory.setValue(lowerValueFactory.getValue());
				}
				else
				{
					lowerValueFactory.setMax(upperValueFactory.getValue());
					upperValueFactory.setMin(lowerValueFactory.getValue());
				}
			});

			// Add button to this pane
			getChildren().add(linkButton);
		}

		// Update range
		updateRange.invoke();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	private static int clamp(
		int	value,
		int	lowerBound,
		int	upperBound)
	{
		return Math.min(Math.max(lowerBound, value), upperBound);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public LinkUnlinkButton linkButton()
	{
		return linkButton;
	}

	//------------------------------------------------------------------

	public IntegerRange range()
	{
		return range.get();
	}

	//------------------------------------------------------------------

	public ReadOnlyObjectProperty<IntegerRange> rangeProperty()
	{
		return range;
	}

	//------------------------------------------------------------------

	public void setRange(
		IntegerRange	range)
	{
		if (range == null)
			setRange(0, 0);
		else
			setRange(range.lowerEndpoint(), range.upperEndpoint());
	}

	//------------------------------------------------------------------

	public void setRange(
		int	lowerEndpoint,
		int	upperEndpoint)
	{
		// Get minimum and maximum values
		int minValue = lowerValueFactory.getMin();
		int maxValue = upperValueFactory.getMax();

		// Set values of spinners to clamped endpoints
		upperValueFactory.setValue(clamp(upperEndpoint, minValue, maxValue));
		lowerValueFactory.setValue(clamp(lowerEndpoint, minValue, maxValue));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
