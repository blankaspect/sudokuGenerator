/*====================================================================*\

RegionWidthEqualiser.java

Class: region-width equaliser.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.widtheq;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

import javafx.scene.Parent;

import javafx.scene.layout.Region;

//----------------------------------------------------------------------


// CLASS: REGION-WIDTH EQUALISER


/**
 * This class provides a means of managing a collection of {@linkplain Region regions} so that the preferred widths of
 * all the regions in the collection are equal to the preferred width of the widest region.  Widths are equalised by
 * adjusting the preferred width of each managed region.
 * <p>
 * The widths of regions are typically updated from the {@link Parent#computePrefWidth(double) computePrefWidth(double)}
 * method of the parent of the regions.  For example:
 * </p>
 * <pre>
 * RegionWidthEqualiser rwe = new RegionWidthEqualiser();
 * Label label1 = new Label("Label");
 * rwe.add(label1);
 * Label label2 = new Label("Longer label");
 * rwe.add(label2);
 * VBox pane = new VBox(5.0, box1, box2)
 * {
 *     &#x40;Override
 *     protected double computePrefWidth(double height)
 *     {
 *         // Reset widths of regions (necessary only if the widths may change after the regions have been laid out).
 *         rwe.resetWidths();
 *
 *         // Update widths of regions
 *         rwe.updateWidths();
 *
 *         // Call superclass method
 *         return super.computePrefWidth(height);
 *    }
 * };
 * </pre>
 */

public class RegionWidthEqualiser
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** A list of the regions whose widths are managed by this region-width equaliser. */
	private	List<Region>	regions;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a region-width equaliser.
	 */

	public RegionWidthEqualiser()
	{
		// Initialise instance variables
		regions = new ArrayList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Adds the specified region to the list of managed regions.
	 *
	 * @param region
	 *          the region that will be added to the list of managed regions.
	 */

	public void add(Region region)
	{
		regions.add(region);
	}

	//------------------------------------------------------------------

	/**
	 * Removes the specified region from the list of managed regions.
	 *
	 * @param region
	 *          the region that will be removed from the list of managed regions.
	 */

	public void remove(Region region)
	{
		regions.remove(region);
	}

	//------------------------------------------------------------------

	/**
	 * Removes all regions from the list of managed regions.
	 */

	public void clear()
	{
		regions.clear();
	}

	//------------------------------------------------------------------

	/**
	 * Resets the preferred widths of the managed regions.
	 */

	public void resetWidths()
	{
		for (Region region : regions)
		{
			region.setPrefWidth(Region.USE_COMPUTED_SIZE);
			region.getParent().layout();
		}
	}

	//------------------------------------------------------------------

	/**
	 * Sets the preferred widths of the managed regions to the preferred width of the widest region.
	 *
	 * @return {@code true} if the width of one or more regions was set.
	 */

	public boolean updateWidths()
	{
		// Find maximum preferred width of regions
		double maxWidth = 0.0;
		for (Region region : regions)
		{
			double width = region.prefWidth(-1.0);
			if (maxWidth < width)
				maxWidth = width;
		}

		// Set preferred width of regions to maximum preferred width
		boolean widthSet = false;
		if (maxWidth > 0.0)
		{
			maxWidth = Math.ceil(maxWidth);
			for (Region region : regions)
			{
				if (region.getPrefWidth() < maxWidth)
				{
					region.setPrefWidth(maxWidth);
					widthSet = true;
				}
			}
		}

		// Return flag to indicate whether the preferred width of a region was set
		return widthSet;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
