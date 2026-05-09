/*====================================================================*\

SimpleDropDownList.java

Class: simple drop-down list.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.dropdownlist;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.InvalidationListener;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import javafx.scene.Group;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import javafx.stage.Popup;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.geometry.VHDirection;

import uk.blankaspect.ui.jfx.button.GraphicButton;

import uk.blankaspect.ui.jfx.listview.ListViewStyle;
import uk.blankaspect.ui.jfx.listview.ListViewUtils;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.shape.Shapes;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.FxStyleClass;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: SIMPLE DROP-DOWN LIST


public class SimpleDropDownList<T>
	extends HBox
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The opacity of a disabled component. */
	private static final	double	DISABLED_OPACITY	= 0.4;

	/** The padding around the label. */
	private static final	Insets	LABEL_PADDING	= new Insets(3.0, 8.0, 3.0, 8.0);

	/** The preferred number of rows of the list view. */
	private static final	int		LIST_VIEW_NUM_ROWS	= 10;

	/** The gap between the text and the graphic of a cell of the list view. */
	private static final	double	LIST_VIEW_CELL_GRAPHIC_TEXT_GAP	= 6.0;

	/** The padding at the top and bottom of a cell of the list view. */
	private static final	double	LIST_VIEW_CELL_VERTICAL_PADDING	= 3.0;

	/** The logical size of a <i>tick</i> icon. */
	private static final	double	LIST_VIEW_TICK_ICON_SIZE	= 0.85 * TextUtils.textHeight();

	/** The key combination that causes the list view to be displayed. */
	private static final	KeyCombination	KEY_COMBO_LIST_TRIGGER	=
			new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN);

	/** The factor by which the height of the default font is multiplied in determining the size of the icon of the
		list-view trigger button. */
	private static final	double	BUTTON_ICON_SIZE_FACTOR	= 0.8;

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.SIMPLE_DROP_DOWN_LIST)
					.desc(StyleClass.LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.SIMPLE_DROP_DOWN_LIST)
					.desc(StyleClass.LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.SIMPLE_DROP_DOWN_LIST)
					.desc(StyleClass.LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.LIST_BUTTON_ICON,
			CssSelector.builder()
					.cls(StyleClass.SIMPLE_DROP_DOWN_LIST)
					.desc(StyleClass.LIST_BUTTON_ICON)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.LIST_BUTTON_PANE_BORDER,
			CssSelector.builder()
					.cls(StyleClass.SIMPLE_DROP_DOWN_LIST)
					.desc(StyleClass.LIST_BUTTON_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.LIST_VIEW_TICK,
			CssSelector.builder()
					.cls(StyleClass.LIST_VIEW_TICK)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ListViewStyle.ColourKey.CELL_BACKGROUND_SELECTED_FOCUSED,
			CssSelector.builder()
					.cls(StyleClass.LIST_VIEW).pseudo(FxPseudoClass.FOCUSED)
					.desc(FxStyleClass.LIST_CELL)
							.pseudo(FxPseudoClass.FILLED, FxPseudoClass.HOVERED, FxPseudoClass.EVEN)
					.build(),
			CssSelector.builder()
					.cls(StyleClass.LIST_VIEW).pseudo(FxPseudoClass.FOCUSED)
					.desc(FxStyleClass.LIST_CELL)
							.pseudo(FxPseudoClass.FILLED, FxPseudoClass.HOVERED, FxPseudoClass.ODD)
					.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.cls(StyleClass.SIMPLE_DROP_DOWN_LIST)
						.desc(StyleClass.LIST_BUTTON_PANE)
						.build())
				.borders(Side.TOP, Side.RIGHT, Side.BOTTOM)
				.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	SIMPLE_DROP_DOWN_LIST	= StyleConstants.CLASS_PREFIX + "simple-drop-down-list";

		String	LABEL				= SIMPLE_DROP_DOWN_LIST + "-label";
		String	LIST_BUTTON_ICON	= StyleConstants.CLASS_PREFIX + "list-button-icon";
		String	LIST_BUTTON_PANE	= StyleConstants.CLASS_PREFIX + "list-button-pane";
		String	LIST_VIEW			= SIMPLE_DROP_DOWN_LIST + "-list-view";
		String	LIST_VIEW_TICK		= SIMPLE_DROP_DOWN_LIST + "-list-view-tick";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	LABEL_BACKGROUND		= PREFIX + "label.background";
		String	LABEL_BORDER			= PREFIX + "label.border";
		String	LABEL_TEXT				= PREFIX + "label.text";
		String	LIST_BUTTON_ICON		= PREFIX + "listButton.icon";
		String	LIST_BUTTON_PANE_BORDER	= PREFIX + "listButtonPane.border";
		String	LIST_VIEW_TICK			= PREFIX + "listView.tick";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	IConverter<T>			converter;
	private	SimpleObjectProperty<T>	item;
	private	int						itemIndex;
	private	ObservableList<T>		items;
	private	Label					label;
	private	GraphicButton			button;
	private	ListView<String>		listView;
	private	Popup					popUp;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class and its dependencies with the style manager
		StyleManager.INSTANCE.register(SimpleDropDownList.class, COLOUR_PROPERTIES, RULE_SETS,
									   ListViewStyle.class);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SimpleDropDownList(
		Collection<? extends T>	items)
	{
		// Call alternative constructor
		this(null, items, null);
	}

	//------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public SimpleDropDownList(
		T...	items)
	{
		// Call alternative constructor
		this(null, List.of(items), null);
	}

	//------------------------------------------------------------------

	public SimpleDropDownList(
		String			prototypeText,
		IConverter<T>	converter)
	{
		// Initialise instance variables
		this.converter = (converter == null) ? SimpleDropDownList::itemToString : converter;
		item = new SimpleObjectProperty<>();
		itemIndex = -1;
		items = FXCollections.observableArrayList();

		// Set properties
		setSpacing(-1.0);
		setAlignment(Pos.CENTER_LEFT);
		setMaxWidth(Region.USE_PREF_SIZE);
		getStyleClass().add(StyleClass.SIMPLE_DROP_DOWN_LIST);

		// Create label
		label = new Label();
		label.setMinWidth(Region.USE_PREF_SIZE);
		label.setPadding(LABEL_PADDING);
		label.setTextFill(getColour(ColourKey.LABEL_TEXT));
		label.setBackground(SceneUtils.createColouredBackground(getColour(ColourKey.LABEL_BACKGROUND)));
		label.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.LABEL_BORDER)));
		Insets insets = label.getInsets();
		label.setPrefWidth(TextUtils.textWidthCeil(prototypeText) + insets.getLeft() + insets.getRight());
		label.getStyleClass().add(StyleClass.LABEL);
		HBox.setHgrow(label, Priority.ALWAYS);

		// Create list view
		double cellHeight = TextUtils.textHeight() + 2.0 * LIST_VIEW_CELL_VERTICAL_PADDING + 1.0;
		listView = new ListView<>();
		listView.setFixedCellSize(cellHeight);
		listView.setCellFactory(listView0 ->
		{
			// Create cell
			ListCell<String> cell = new ListCell<>()
			{
				Group	marker;
				Shape	blank;

				// Constructor
				{
					// Initialise instance variables
					Shape tickIcon = Shapes.tick01(LIST_VIEW_TICK_ICON_SIZE);
					tickIcon.setStroke(getColour(ColourKey.LIST_VIEW_TICK));
					tickIcon.getStyleClass().add(StyleClass.LIST_VIEW_TICK);
					marker = Shapes.tile(tickIcon);
					Bounds bounds = marker.getLayoutBounds();
					blank = new Rectangle(bounds.getWidth(), bounds.getHeight(), Color.TRANSPARENT);

					// Set properties
					setGraphicTextGap(LIST_VIEW_CELL_GRAPHIC_TEXT_GAP);
				}

				@Override
				protected void updateItem(
					String	item,
					boolean	empty)
				{
					// Call superclass method
					super.updateItem(item, empty);

					// Set graphic
					setGraphic((empty || (getIndex() != itemIndex)) ? blank : marker);

					// Set text
					setText(empty ? null : item);
				}
			};

			// Return cell
			return cell;
		});
		listView.getStyleClass().addAll(StyleClass.LIST_VIEW, ListViewStyle.StyleClass.LIST_VIEW);
		listView.prefWidthProperty().bind(widthProperty());

		// Create procedure to update value of combo box from selection in list view
		IProcedure0 updateValueFromList = () ->
		{
			// Update value and label if item was selected in list view
			int index = listView.getSelectionModel().getSelectedIndex();
			if (index >= 0)
			{
				// Update instance variables
				itemIndex = index;
				item.set(items.get(index));

				// Update label
				label.setText(listView.getItems().get(index));
			}

			// Hide pop-up
			popUp.hide();
		};

		// Handle 'key pressed' event on list view
		listView.setOnKeyPressed(event ->
		{
			// Update value
			if (event.getCode() == KeyCode.ENTER)
				updateValueFromList.invoke();

			// Hide pop-up
			else if (event.getCode() == KeyCode.ESCAPE)
				popUp.hide();

			// Consume event
			event.consume();
		});

		// If a key is typed on the list view, scroll the list view to the first item whose text starts with the typed
		// character
		listView.setOnKeyTyped(event ->
		{
			String str = event.getCharacter();
			if (!KeyEvent.CHAR_UNDEFINED.equals(str))
			{
				str = str.toLowerCase();
				List<String> listViewItems = listView.getItems();
				int numItems = listViewItems.size();
				for (int i = 0; i < numItems; i++)
				{
					if (listViewItems.get(i).toLowerCase().startsWith(str))
					{
						listView.getSelectionModel().clearAndSelect(i);
						if (numItems > LIST_VIEW_NUM_ROWS)
							ListViewUtils.scrollToCentred(listView, i);
						break;
					}
				}
				event.consume();
			}
		});

		// Update value if mouse is clicked on list view
		listView.setOnMouseClicked(event ->
		{
			if (event.getButton() == MouseButton.PRIMARY)
			{
				// Update value
				updateValueFromList.invoke();

				// Consume event
				event.consume();
			}
		});

		// Create pop-up for list view
		popUp = new Popup();
		popUp.getContent().add(listView);
		popUp.setAutoHide(true);
		popUp.setOnHidden(event ->
		{
			// Enable button
			button.setDisable(false);

			// Request focus on button
			button.requestFocus();
		});

		// Create procedure to display list view in pop-up window
		IProcedure0 showListView = () ->
		{
			// Disable button
			button.setDisable(true);

			// Set preferred height of list view
			int numRows = Math.min(Math.max(1, items.size()), LIST_VIEW_NUM_ROWS);
			double height = (double)numRows * cellHeight + 2.0;
			listView.setPrefHeight(height);

			// Clear selection in list view
			listView.getSelectionModel().clearSelection();

			// Create list of string representations of items
			ObservableList<String> strings = FXCollections.observableArrayList();
			for (T item : items)
			{
				String text = this.converter.toText(item);
				strings.add((text == null) ? "" : text);
			}

			// Set string representations of items on list view
			listView.setItems(strings);

			// Display pop-up
			Bounds bounds = label.localToScreen(label.getLayoutBounds());
			popUp.show(label, bounds.getMinX() - 1.0, bounds.getMaxY());
		};

		// Create icon for button that triggers list view
		double textHeight = TextUtils.textHeight();
		Shape buttonIcon = Shapes.arrowhead01(VHDirection.DOWN, BUTTON_ICON_SIZE_FACTOR * textHeight);
		buttonIcon.setFill(getColour(ColourKey.LIST_BUTTON_ICON));
		buttonIcon.getStyleClass().add(StyleClass.LIST_BUTTON_ICON);

		// Create button that triggers list view
		button = new GraphicButton(Shapes.tile(buttonIcon, Math.ceil(textHeight)));
		button.setDisable(true);
		button.setOnAction(event -> showListView.invoke());
		button.prefHeightProperty().bind(label.heightProperty());

		// Handle 'key pressed' event on button
		button.addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			// Show list-view pop-up
			if (KEY_COMBO_LIST_TRIGGER.match(event))
			{
				// Show list-view pop-up
				showListView.invoke();

				// Select first item in list view
				if (!listView.getItems().isEmpty())
				{
					listView.getSelectionModel().select(0);
					listView.refresh();
				}

				// Consume event
				event.consume();
			}

			// Hide list-view pop-up
			else if (event.getCode() == KeyCode.ESCAPE)
			{
				// Hide pop-up
				popUp.hide();

				// Consume event
				event.consume();
			}
		});

		// Disable button if list of items is empty
		items.addListener((InvalidationListener) observable -> button.setDisable(items.isEmpty()));

		// Create pane to provide three-sided border around button
		StackPane buttonPane = new StackPane(button);
		buttonPane.setPadding(new Insets(0.0, 0.0, 0.0, 1.0));
		buttonPane.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.LIST_BUTTON_PANE_BORDER),
														  Side.TOP, Side.RIGHT, Side.BOTTOM));
		buttonPane.getStyleClass().add(StyleClass.LIST_BUTTON_PANE);

		// Reduce opacity of border around button when combo box is disabled
		disabledProperty().addListener((observable, oldDisabled, disabled) ->
				buttonPane.setOpacity(disabled ? DISABLED_OPACITY : 1.0));

		// Create outer pane for button
		StackPane outerButtonPane = new StackPane(buttonPane, button);
		outerButtonPane.setMaxHeight(Region.USE_PREF_SIZE);

		// Add children to this component
		getChildren().addAll(label, outerButtonPane);
	}

	//------------------------------------------------------------------

	public SimpleDropDownList(
		String					prototypeText,
		Collection<? extends T>	items,
		IConverter<T>			converter)
	{
		// Call alternative constructor
		this((prototypeText == null)
				? items.stream()
						.map((converter == null) ? SimpleDropDownList::itemToString : converter::toText)
						.max(Comparator.comparingDouble(text -> TextUtils.textWidthCeil(text))).orElse(null)
				: prototypeText,
			 converter);

		// Set items
		items(items);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a string representation of the specified item.
	 *
	 * @param  <T>
	 *           the type of {@code item}.
	 * @param  item
	 *           the item whose string representation is desired.
	 * @return a string representation of {@code item}, or {@code null} if {@code item} is {@code null}.
	 */

	private static <T> String itemToString(
		T	item)
	{
		return (item == null) ? null : item.toString();
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
	public void requestFocus()
	{
		button.requestFocus();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public T item()
	{
		return item.get();
	}

	//------------------------------------------------------------------

	public void item(
		T	item)
	{
		// Update instance variables
		itemIndex = items.indexOf(item);
		this.item.set(item);

		// Update label
		label.setText(converter.toText(item));
	}

	//------------------------------------------------------------------

	public ReadOnlyObjectProperty<T> itemProperty()
	{
		return item;
	}

	//------------------------------------------------------------------

	public int itemIndex()
	{
		return itemIndex;
	}

	//------------------------------------------------------------------

	public GraphicButton button()
	{
		return button;
	}

	//------------------------------------------------------------------

	public List<T> items()
	{
		return Collections.unmodifiableList(items);
	}

	//------------------------------------------------------------------

	public void items(
		Collection<? extends T>	items)
	{
		// Validate argument
		if (items == null)
			throw new IllegalArgumentException("Null items");

		// Update instance variable
		this.items.setAll(items);

		// Clear selection
		selectIndex(-1);
	}

	//------------------------------------------------------------------

	public void selectIndex(
		int	index)
	{
		// Get value from list
		T value = (index < 0) ? null : items.get(index);

		// Update instance variables
		itemIndex = index;
		this.item.set(value);

		// Update label
		label.setText(converter.toText(value));
	}

	//------------------------------------------------------------------

	public void selectItem(
		T	item)
	{
		selectIndex(items.indexOf(item));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// INTERFACE: CONVERTER


	/**
	 * This interface defines the methods that are used by a {@link SimpleDropDownList} to convert an item to a textual
	 * representation.
	 *
	 * @param <T>
	 *          the type of the item.
	 */

	@FunctionalInterface
	public interface IConverter<T>
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns a textual representation of the specified item.
		 *
		 * @param  item
		 *           the item for which a textual representation is desired.
		 * @return a textual representation of the specified item, or {@code null} if {@code item} is not valid.
		 */

		String toText(
			T	item);

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
