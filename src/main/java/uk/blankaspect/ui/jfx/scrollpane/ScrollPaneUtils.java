/*====================================================================*\

ScrollPaneUtils.java

Class: scroll-pane-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.scrollpane;

//----------------------------------------------------------------------


// IMPORTS


import javafx.geometry.Orientation;

import javafx.scene.Node;

import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;

import javafx.scene.layout.Region;

import javafx.scene.paint.Color;

import uk.blankaspect.ui.jfx.control.ControlUtils;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.StyleSelector;

//----------------------------------------------------------------------


// CLASS: SCROLL-PANE-RELATED UTILITY METHODS


/**
 * This class contains utility methods that relate to JavaFX {@linkplain ScrollPane scroll panes}.
 */

public class ScrollPaneUtils
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private ScrollPaneUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the background of the viewport of the specified scroll pane to the specified colour.
	 * <p>
	 * The viewport belongs to the skin of the scroll pane and is not directly accessible from the scroll pane itself.
	 * It is accessed by calling {@link Node#lookup(String)} from a listener on {@link ScrollPane#skinProperty()}.
	 * </p>
	 *
	 * @param scrollPane
	 *          the target scroll pane.
	 * @param colour
	 *          the colour to which the background of the viewport of {@code scrollPane} will be set.
	 */

	public static void setViewportBackgroundColour(
		ScrollPane	scrollPane,
		Color		colour)
	{
		ControlUtils.onSkin(scrollPane, () ->
		{
			if (scrollPane.lookup(StyleSelector.SCROLL_PANE_VIEWPORT) instanceof Region viewport)
				viewport.setBackground(SceneUtils.createColouredBackground(colour));
		});
	}

	//------------------------------------------------------------------

	/**
	 * Returns the horizontal scroll bar of the specified scroll pane.
	 * <p>
	 * <b>NOTE</b>:<br/>
	 * This method requires that the skin of the scroll pane be initialised, which can be achieved by calling the method
	 * from a listener on {@link ScrollPane#skinProperty()}.
	 * </p>
	 *
	 * @param  scrollPane
	 *           the target scroll pane.
	 * @return the horizontal scroll bar of {@code scrollPane}.
	 */

	public static ScrollBar getHScrollBar(
		ScrollPane	scrollPane)
	{
		return getScrollBar(scrollPane, Orientation.HORIZONTAL);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the vertical scroll bar of the specified scroll pane.
	 * <p>
	 * <b>NOTE</b>:<br/>
	 * This method requires that the skin of the scroll pane be initialised, which can be achieved by calling the method
	 * from a listener on {@link ScrollPane#skinProperty()}.
	 * </p>
	 *
	 * @param  scrollPane
	 *           the target scroll pane.
	 * @return the vertical scroll bar of {@code scrollPane}.
	 */

	public static ScrollBar getVScrollBar(
		ScrollPane	scrollPane)
	{
		return getScrollBar(scrollPane, Orientation.VERTICAL);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the scroll bar of the specified scroll pane with the specified orientation.
	 * <p>
	 * <b>NOTE</b>:<br/>
	 * This method requires that the skin of the scroll pane be initialised, which can be achieved by calling the method
	 * from a listener on {@link ScrollPane#skinProperty()}.
	 * </p>
	 *
	 * @param  scrollPane
	 *           the target scroll pane.
	 * @param  orientation
	 *           the orientation of the required scroll bar.
	 * @return the scroll bar of {@code scrollPane} whose orientation is {@code orientation}.
	 */

	public static ScrollBar getScrollBar(
		ScrollPane	scrollPane,
		Orientation	orientation)
	{
		return scrollPane.lookupAll(StyleSelector.SCROLL_BAR).stream()
				.filter(node -> (node instanceof ScrollBar scrollBar) && (scrollBar.getOrientation() == orientation))
				.map(node -> (ScrollBar)node)
				.findFirst()
				.orElse(null);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
