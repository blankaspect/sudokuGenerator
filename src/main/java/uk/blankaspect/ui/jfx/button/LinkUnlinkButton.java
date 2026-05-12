/*====================================================================*\

LinkUnlinkButton.java

Class: link/unlink button.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.button;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.List;

import javafx.geometry.Orientation;

import javafx.scene.Group;
import javafx.scene.Node;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

//----------------------------------------------------------------------


// CLASS: LINK/UNLINK BUTTON


public class LinkUnlinkButton
	extends GraphicButton
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	double	MARGIN_D1	= 0.0;
	private static final	double	MARGIN_D2	= 2.0;

	private static final	double	LINK_STROKE_WIDTH	= 2.0;
	private static final	double	LINK_RADIUS			= 3.5;
	private static final	double	LINK_LINE_LENGTH	= 3.0;
	private static final	double	LINK_ARC_SIZE		= 2.0 * LINK_RADIUS;
	private static final	double	LINK_SIZE_D1		= 2.0 * LINK_RADIUS + LINK_LINE_LENGTH;
	private static final	double	LINK_SIZE_D2		= 2.0 * LINK_RADIUS;
	private static final	double	LINK_GAP			= 3.0;
	private static final	double	LINK_OVERLAP		= 3.0;

	private static final	double	SIZE_D1	= 2.0 * MARGIN_D1 + 2.0 * LINK_SIZE_D1 + LINK_STROKE_WIDTH + LINK_GAP;
	private static final	double	SIZE_D2	= 2.0 * MARGIN_D2 + LINK_SIZE_D2 + LINK_STROKE_WIDTH;

	/** CSS style classes. */
	private interface StyleClass
	{
		String	ICON				= StyleConstants.CLASS_PREFIX + "icon";
		String	LINK_UNLINK_BUTTON	= StyleConstants.CLASS_PREFIX + "link-unlink-button";
	}

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.LINK_UNLINK_BUTTON).pseudo(PseudoClassKey.INACTIVE)
					.desc(GraphicButton.StyleClass.INNER_VIEW)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.BORDER,
			CssSelector.builder()
					.cls(StyleClass.LINK_UNLINK_BUTTON).pseudo(PseudoClassKey.INACTIVE)
					.desc(GraphicButton.StyleClass.INNER_VIEW)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.FILL,
			Color.TRANSPARENT,
			CssSelector.builder()
					.cls(StyleClass.LINK_UNLINK_BUTTON)
					.desc(StyleClass.ICON)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.ICON_STROKE,
			CssSelector.builder()
					.cls(StyleClass.LINK_UNLINK_BUTTON)
					.desc(StyleClass.ICON)
					.build()
		)
	);

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	BACKGROUND	= PREFIX + "background";
		String	BORDER		= PREFIX + "border";
		String	ICON_STROKE	= PREFIX + "icon.stroke";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Orientation	orientation;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(LinkUnlinkButton.class, COLOUR_PROPERTIES);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public LinkUnlinkButton(
		Orientation	orientation)
	{
		// Call superclass constructor
		super(new Rectangle(width(orientation), height(orientation), Color.TRANSPARENT));

		// Initialise instance variables
		this.orientation = orientation;

		// Set properties
		setSelectable(true);
		setHighlightIfSelected(false);
		setBackgroundColour(getColour(ColourKey.BACKGROUND));
		setBorderColour(getColour(ColourKey.BORDER));
		getStyleClass().add(StyleClass.LINK_UNLINK_BUTTON);

		// Update button view
		update();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static LinkUnlinkButton horizontal()
	{
		return new LinkUnlinkButton(Orientation.HORIZONTAL);
	}

	//------------------------------------------------------------------

	public static LinkUnlinkButton vertical()
	{
		return new LinkUnlinkButton(Orientation.VERTICAL);
	}

	//------------------------------------------------------------------

	private static double width(
		Orientation	orientation)
	{
		return switch (orientation)
		{
			case HORIZONTAL -> SIZE_D1;
			case VERTICAL   -> SIZE_D2;
		};
	}

	//------------------------------------------------------------------

	private static double height(
		Orientation	orientation)
	{
		return switch (orientation)
		{
			case HORIZONTAL -> SIZE_D2;
			case VERTICAL   -> SIZE_D1;
		};
	}

	//------------------------------------------------------------------

	private static Rectangle createLink(
		double	x,
		double	y,
		double	width,
		double	height)
	{
		Rectangle shape = new Rectangle(x, y, width, height);
		shape.setArcWidth(LINK_ARC_SIZE);
		shape.setArcHeight(LINK_ARC_SIZE);
		shape.setFill(Color.TRANSPARENT);
		shape.setStroke(getColour(ColourKey.ICON_STROKE));
		shape.setStrokeWidth(LINK_STROKE_WIDTH);
		shape.getStyleClass().add(StyleClass.ICON);
		return shape;
	}

	//------------------------------------------------------------------

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

	@Override
	protected Node graphic()
	{
		// If button has not been initialised, return placeholder graphic
		if (orientation == null)
			return getGraphic();

		// Get dimensions
		double width = width(orientation);
		double height = height(orientation);

		// Create background
		Rectangle background = new Rectangle(width, height, Color.TRANSPARENT);

		// Calculate coordinates and dimensions of links
		double size_d1 = 0.0;
		double size_d2 = 0.0;
		switch (orientation)
		{
			case HORIZONTAL:
				size_d1 = width;
				size_d2 = height;
				break;

			case VERTICAL:
				size_d1 = height;
				size_d2 = width;
				break;
		};

		double a1 = 0.5 * (size_d1 - 2.0 * LINK_SIZE_D1 - LINK_GAP);
		double a2 = a1 + LINK_SIZE_D1 + LINK_GAP;
		if (isSelected())
		{
			double d = 0.5 * (LINK_GAP + LINK_OVERLAP);
			a1 += d;
			a2 -= d;
		}
		double b = 0.5 * (size_d2 - LINK_SIZE_D2);

		double x1 = 0.0;
		double x2 = 0.0;
		double y1 = 0.0;
		double y2 = 0.0;
		double w = 0.0;
		double h = 0.0;
		switch (orientation)
		{
			case HORIZONTAL:
			{
				x1 = a1;
				x2 = a2;
				y1 = y2 = b;
				w = LINK_SIZE_D1;
				h = LINK_SIZE_D2;
				break;
			}

			case VERTICAL:
			{
				x1 = x2 = b;
				y1 = a1;
				y2 = a2;
				w = LINK_SIZE_D2;
				h = LINK_SIZE_D1;
				break;
			}
		}

		// Create links
		Rectangle link1 = createLink(x1, y1, w, h);
		Rectangle link2 = createLink(x2, y2, w, h);

		// Create and return group
		return new Group(background, link1, link2);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
