/*====================================================================*\

ChangeIndicatorTitleBar.java

Class: title bar that has a change indicator.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.toolbar;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import javafx.scene.Node;

import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.ToolBar;

import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.scene.paint.Color;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.ui.jfx.label.ChangeIndicatorLabel;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

//----------------------------------------------------------------------


// CLASS: TITLE BAR THAT HAS A CHANGE INDICATOR


/**
 * This class implements a title bar: a horizontal control that contains a {@linkplain ChangeIndicatorLabel
 * change-indicator label} on the left and a toolbar on the right.
 */

public class ChangeIndicatorTitleBar
	extends Control
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The gap between the label and the tool bar of a title bar. */
	private static final	double	GAP	= 16.0;

	/** The padding around the content of a title bar. */
	private static final	Insets	PADDING	= new Insets(1.0, 4.0, 1.0, 0.0);

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.DEFAULT_TEXT,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR_TITLE_BAR)
					.desc(ChangeIndicatorLabel.StyleClass.CHANGE_INDICATOR_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.DEFAULT_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR_TITLE_BAR)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.DEFAULT_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR_TITLE_BAR)
					.desc(ChangeIndicatorLabel.StyleClass.CHANGE_INDICATOR_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.DEFAULT_BORDER,
			CssSelector.builder()
					.cls(StyleClass.CHANGE_INDICATOR_TITLE_BAR)
					.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.cls(StyleClass.CHANGE_INDICATOR_TITLE_BAR)
						.build())
				.borders(Side.TOP, Side.BOTTOM)
				.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	CHANGE_INDICATOR_TITLE_BAR	= StyleConstants.CLASS_PREFIX + "change-indicator-title-bar";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	DEFAULT_BACKGROUND	= PREFIX + "default.background";
		String	DEFAULT_BORDER		= PREFIX + "default.border";
		String	DEFAULT_TEXT		= PREFIX + "default.text";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The node that provides the skin for this title bar. */
	private	View	view;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(ChangeIndicatorTitleBar.class, COLOUR_PROPERTIES, RULE_SETS);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a title bar with a default text colour and background colour.
	 */

	public ChangeIndicatorTitleBar()
	{
		// Call alternative constructor
		this(getColour(ColourKey.DEFAULT_TEXT), getColour(ColourKey.DEFAULT_BACKGROUND),
			 getColour(ColourKey.DEFAULT_BORDER));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a title bar with the specified text colour and background.
	 *
	 * @param textColour
	 *          the text colour of the change-indicator label.
	 * @param backgroundColour
	 *          the background colour of the title bar.
	 * @param borderColour
	 *          the border colour of the title bar.
	 */

	public ChangeIndicatorTitleBar(
		Color	textColour,
		Color	backgroundColour,
		Color	borderColour)
	{
		// Create view
		view = new View();
		view.setBackground(SceneUtils.createColouredBackground(backgroundColour));
		view.setBorder(SceneUtils.createSolidBorder(borderColour, Side.TOP, Side.BOTTOM));
		textColour(textColour);
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
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	protected Skin<ChangeIndicatorTitleBar> createDefaultSkin()
	{
		return new Skin<>()
		{
			@Override
			public ChangeIndicatorTitleBar getSkinnable()
			{
				return ChangeIndicatorTitleBar.this;
			}

			@Override
			public Node getNode()
			{
				return view;
			}

			@Override
			public void dispose()
			{
				// do nothing
			}
		};
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the view of this title bar.
	 *
	 * @return the view of this title bar.
	 */

	public HBox view()
	{
		return view;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the text of the change-indicator label this title bar.
	 *
	 * @return the text of the change-indicator label of this title bar.
	 */

	public String text()
	{
		return view.label.getText();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the text of the change-indicator label of this title bar to the specified value.
	 *
	 * @param text
	 *          the text that will be set on the change-indicator label of this title bar.
	 */

	public void text(
		String	text)
	{
		view.label.setText(text);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the change-indicator label of this title bar.
	 *
	 * @return the change-indicator label of this title bar.
	 */

	public ChangeIndicatorLabel label()
	{
		return view.label;
	}

	//------------------------------------------------------------------

	/**
	 * Sets the text colour of the change-indicator label.
	 *
	 * @param colour
	 *          the value to which the text colour of the change-indicator label will be set.
	 */

	public void textColour(
		Color	colour)
	{
		view.label.setTextFill(colour);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified node to the toolbar of this title bar.
	 *
	 * @param node
	 *          the node that will be added to the toolbar of this title bar.
	 */

	public void addToolbarNode(
		Node	node)
	{
		view.toolbar.getItems().add(node);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified node to the toolbar of this title bar at the specified index in the toolbar's list of items.
	 *
	 * @param index
	 *          the index at which {@code node} will be added to the toolbar's list of items.
	 * @param node
	 *          the node that will be added to the toolbar of this title bar.
	 */

	public void addToolbarNode(
		int		index,
		Node	node)
	{
		view.toolbar.getItems().add(index, node);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: TITLE-BAR VIEW


	/**
	 * This class implements a node that provides the skin for a title bar.
	 */

	private class View
		extends HBox
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The change-indicator label. */
		private	ChangeIndicatorLabel	label;

		/** The toolbar. */
		private	ToolBar					toolbar;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a title-bar view.
		 */

		private View()
		{
			// Set properties
			setSpacing(GAP);
			setAlignment(Pos.CENTER_LEFT);
			setPadding(PADDING);
			getStyleClass().add(StyleClass.CHANGE_INDICATOR_TITLE_BAR);

			// Create label
			label = new ChangeIndicatorLabel();
			label.setTextFill(getColour(ColourKey.DEFAULT_TEXT));
			HBox.setHgrow(label, Priority.ALWAYS);

			// Create toolbar
			toolbar = new ToolBar();
			toolbar.setPadding(Insets.EMPTY);
			toolbar.setBackground(Background.EMPTY);

			// Add children to this container
			getChildren().addAll(label, toolbar);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
