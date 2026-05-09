/*====================================================================*\

ChangeIndicatorLabel.java

Class: change-indicator label.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.label;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.css.PseudoClass;

import javafx.geometry.Insets;

import javafx.scene.input.ContextMenuEvent;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;

import javafx.stage.Window;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IFunction0;
import uk.blankaspect.common.function.IProcedure2;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.popup.CopyPopUp;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

//----------------------------------------------------------------------


// CLASS: CHANGE-INDICATOR LABEL


/**
 * This class implements a label that has a <i>changed</i> property and a graphic indicator that reflects the state of
 * the <i>changed</i> property.
 */

public class ChangeIndicatorLabel
	extends OverlayLabel.Primary
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The gap between the text and graphic. */
	private static final	double	GRAPHIC_TEXT_GAP	= 6.0;

	/** The padding around the label. */
	private static final	Insets	PADDING	= new Insets(1.0, 6.0, 1.0, 6.0);

	/** The size of the <i>changed</i> indicator. */
	private static final	double	CHANGED_INDICATOR_SIZE	= 6.0;

	/** The pseudo-class that is associated with the <i>changed</i> state. */
	private static final	PseudoClass	PSEUDO_CLASS_CHANGED	= PseudoClass.getPseudoClass(PseudoClassKey.CHANGED);

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.TEXT,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.POPUP_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR_LABEL_POPUP)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.POPUP_BORDER,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR_LABEL_POPUP)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.INDICATOR_UNCHANGED_FILL,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.INDICATOR_CHANGED_FILL,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR).pseudo(PseudoClassKey.CHANGED)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.INDICATOR_UNCHANGED_STROKE,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.INDICATOR_CHANGED_STROKE,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR).pseudo(PseudoClassKey.CHANGED)
					.build()
		)
	);

	/** CSS style classes. */
	public interface StyleClass
	{
		String	CHANGE_INDICATOR_LABEL			= StyleConstants.CLASS_PREFIX + "change-indicator-label";
		String	CHANGE_INDICATOR_LABEL_POPUP	= CHANGE_INDICATOR_LABEL + "-popup";
		String	CHANGE_INDICATOR				= CHANGE_INDICATOR_LABEL + "-indicator";
	}

	/** Keys of CSS pseudo-classes. */
	private interface PseudoClassKey
	{
		String	CHANGED	= "changed";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	BACKGROUND					= PREFIX + "background";
		String	INDICATOR_CHANGED_FILL		= PREFIX + "indicator.changed.fill";
		String	INDICATOR_CHANGED_STROKE	= PREFIX + "indicator.changed.stroke";
		String	INDICATOR_UNCHANGED_FILL	= PREFIX + "indicator.unchanged.fill";
		String	INDICATOR_UNCHANGED_STROKE	= PREFIX + "indicator.unchanged.stroke";
		String	POPUP_BACKGROUND			= PREFIX + "popup.background";
		String	POPUP_BORDER				= PREFIX + "popup.border";
		String	TEXT						= PREFIX + "text";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** Flag: if {@code true}, the model that is represented by this label has changed. */
	private	SimpleBooleanProperty	changed;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(MethodHandles.lookup().lookupClass(), COLOUR_PROPERTIES);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of an empty label.
	 */

	public ChangeIndicatorLabel()
	{
		// Initialise instance variables
		changed = new SimpleBooleanProperty(false);

		// Set properties
		setMaxWidth(Double.MAX_VALUE);
		setGraphicTextGap(GRAPHIC_TEXT_GAP);
		setPadding(PADDING);
		setTextFill(getColour(ColourKey.TEXT));
		setBackground(SceneUtils.createColouredBackground(getColour(ColourKey.BACKGROUND)));
		setPopUpBackgroundColour(getColour(ColourKey.POPUP_BACKGROUND));
		setPopUpBorderColour(getColour(ColourKey.POPUP_BORDER));
		setPopUpStyleClass(StyleClass.CHANGE_INDICATOR_LABEL_POPUP);
		getStyleClass().add(StyleClass.CHANGE_INDICATOR_LABEL);

		// Create procedure to update colours of indicator
		IProcedure2<Rectangle, Boolean> indicatorUpdater = (indicator, changed) ->
		{
			indicator.setFill(changed ? getColour(ColourKey.INDICATOR_CHANGED_FILL)
									  : getColour(ColourKey.INDICATOR_UNCHANGED_FILL));
			indicator.setStroke(changed ? getColour(ColourKey.INDICATOR_CHANGED_STROKE)
										: getColour(ColourKey.INDICATOR_UNCHANGED_STROKE));
			indicator.pseudoClassStateChanged(PSEUDO_CLASS_CHANGED, changed);
		};

		// Create function to provide indicator
		IFunction0<Rectangle> indicatorSource = () ->
		{
			Rectangle indicator = new Rectangle(CHANGED_INDICATOR_SIZE, CHANGED_INDICATOR_SIZE);
			indicator.setStrokeWidth(1.0);
			indicator.getStyleClass().add(StyleClass.CHANGE_INDICATOR);
			indicatorUpdater.invoke(indicator, isChanged());
			return indicator;
		};

		// Set graphic source
		setGraphicSource(indicatorSource);

		// Set graphic
		Rectangle indicator = indicatorSource.invoke();
		setGraphic(indicator);

		// Change colours of indicator to reflect 'changed' flag
		changed.addListener((observable, oldChanged, newChanged) -> indicatorUpdater.invoke(indicator, newChanged));

		// Display context menu on request
		addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event ->
		{
			// Get text of label
			String text = getText();

			// If text is not blank, display 'copy text' pop-up
			if (!StringUtils.isNullOrBlank(text))
			{
				// Create pop-up for 'copy text' action
				Window window = SceneUtils.getWindow(this);
				CopyPopUp popUp = CopyPopUp.text(window, () -> text);

				// Display pop-up
				popUp.show(window, event.getScreenX(), event.getScreenY());
			}
		});
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a label with the specified text.
	 *
	 * @param text
	 *          the text of the label.
	 */

	public ChangeIndicatorLabel(
		String	text)
	{
		// Call alternative constructor
		this();

		// Set text
		setText(text);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the colour that is associated with the specified key in the colour map of the current theme of the
	 * {@linkplain StyleManager style manager}.
	 *
	 * @param  key
	 *           the key of the desired colour.
	 * @return the colour that is associated with {@code key} in the colour map of the current theme of the style
	 *         manager, or {@link StyleManager#DEFAULT_COLOUR} if there is no such colour.
	 */

	private static Color getColour(
		String	key)
	{
		return StyleManager.INSTANCE.getColourOrDefault(key);
	}

	//------------------------------------------------------------------


////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the value of the <i>changed</i> flag.
	 *
	 * @return the value of the <i>changed</i> flag.
	 */

	public boolean isChanged()
	{
		return changed.get();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the <i>changed</i> flag to the specified value.
	 *
	 * @param changed
	 *          the value to which the <i>changed</i> flag will be set.
	 */

	public void setChanged(
		boolean	changed)
	{
		this.changed.set(changed);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the <i>changed</i> flag as a property.
	 *
	 * @return the <i>changed</i> flag as a property.
	 */

	public BooleanProperty changedProperty()
	{
		return changed;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
