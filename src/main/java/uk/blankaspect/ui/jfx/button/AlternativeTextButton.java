/*====================================================================*\

AlternativeTextButton.java

Class: alternative-text button.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.button;

//----------------------------------------------------------------------


// IMPORTS


import java.util.EnumSet;
import java.util.List;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.StackPane;

import uk.blankaspect.ui.jfx.label.Labels;

//----------------------------------------------------------------------


// CLASS: ALTERNATIVE-TEXT BUTTON


public class AlternativeTextButton<T>
	extends Button
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	SimpleIntegerProperty	selectedIndex;
	private	StackPane				pane;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public AlternativeTextButton(
		T...	items)
	{
		// Call alternative constructor
		this(null, null, items);
	}

	//------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public AlternativeTextButton(
		Function<T, String>		converter,
		T...					items)
	{
		// Call alternative constructor
		this(null, converter, items);
	}

	//------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public AlternativeTextButton(
		T						initialItem,
		Function<T, String>		converter,
		T...					items)
	{
		// Call alternative constructor
		this(List.of(items), initialItem, converter);
	}

	//------------------------------------------------------------------

	public AlternativeTextButton(
		Iterable<? extends T>	items)
	{
		// Call alternative constructor
		this(items, null, null);
	}

	//------------------------------------------------------------------

	public AlternativeTextButton(
		Iterable<? extends T>	items,
		Function<T, String>		converter)
	{
		// Call alternative constructor
		this(items, null, converter);
	}

	//------------------------------------------------------------------

	public AlternativeTextButton(
		Iterable<? extends T>	items,
		T						initialItem,
		Function<T, String>		converter)
	{
		// Initialise container for labels
		pane = new StackPane();

		// Create a label for each item and add label to container
		int index = 0;
		int initialIndex = -1;
		for (T item : items)
		{
			// Test for null item
			if (item == null)
				throw new IllegalArgumentException("An item is null");

			// Create label
			Label label = Labels.hNoShrink((converter == null) ? item.toString() : converter.apply(item));
			label.setUserData(item);
			label.setVisible(false);
			pane.getChildren().add(label);

			// Initialise selected index
			if ((initialIndex < 0) && item.equals(initialItem))
				initialIndex = index;

			// Increment item index
			++index;
		}

		// Test for items
		if (index == 0)
			throw new IllegalArgumentException("No items");

		// Initialise selected index
		if (initialIndex < 0)
			initialIndex = 0;
		selectedIndex = new SimpleIntegerProperty(initialIndex);

		// Make selected label visible
		pane.getChildren().get(initialIndex).setVisible(true);

		// Set container as graphic of button
		setGraphic(pane);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static <E extends Enum<E>> AlternativeTextButton<E> forEnum(
		Class<E>	enumClass)
	{
		return forEnum(enumClass, null, null);
	}

	//------------------------------------------------------------------

	public static <E extends Enum<E>> AlternativeTextButton<E> forEnum(
		Class<E>	enumClass,
		E			initialItem)
	{
		return forEnum(enumClass, initialItem, null);
	}

	//------------------------------------------------------------------

	public static <E extends Enum<E>> AlternativeTextButton<E> forEnum(
		Class<E>			enumClass,
		Function<E, String>	converter)
	{
		return forEnum(enumClass, null, converter);
	}

	//------------------------------------------------------------------

	public static <E extends Enum<E>> AlternativeTextButton<E> forEnum(
		Class<E>			enumClass,
		E					initialItem,
		Function<E, String>	converter)
	{
		return new AlternativeTextButton<>(EnumSet.allOf(enumClass), initialItem, converter);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int selectedIndex()
	{
		return selectedIndex.get();
	}

	//------------------------------------------------------------------

	public ReadOnlyIntegerProperty selectedIndexProperty()
	{
		return selectedIndex;
	}

	//------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public T selectedItem()
	{
		return (T)pane.getChildren().get(selectedIndex.get()).getUserData();
	}

	//------------------------------------------------------------------

	public boolean selectIndex(
		int	index)
	{
		// Validate index
		List<Node> children = pane.getChildren();
		if ((index < 0) || (index >= children.size()))
			throw new IndexOutOfBoundsException(index);

		// Update selected index
		int currentIndex = selectedIndex.get();
		if (currentIndex != index)
		{
			// Show label corresponding to index
			children.get(currentIndex).setVisible(false);
			children.get(index).setVisible(true);

			// Update instance variable
			selectedIndex.set(index);

			// Indicate selected index changed
			return true;
		}

		// Indicate selected index unchanged
		return false;
	}

	//------------------------------------------------------------------

	public boolean selectItem(
		T	item)
	{
		int index = 0;
		for (Node child : pane.getChildren())
		{
			if (child.getUserData().equals(item))
				return selectIndex(index);
			++index;
		}
		return false;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
