/*====================================================================*\

PreferencesDialog.java

Class: preferences dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;

import javafx.stage.Window;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.BaseException;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.container.LabelTitledPane;
import uk.blankaspect.ui.jfx.container.PaneStyle;

import uk.blankaspect.ui.jfx.control.ControlUtils;

import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.dropdownlist.SimpleDropDownList;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;
import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.tabbedpane.TabPaneUtils;

//----------------------------------------------------------------------


// CLASS: PREFERENCES DIALOG


/**
 * This class implements a modal dialog in which the user preferences of the application may be edited.
 */

class PreferencesDialog
	extends SimpleModalDialog<Preferences>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	ORDER_PROTOTYPE_TEXT	= "0".repeat(3);

	private static final	int		MIN_EDIT_HISTORY_MAX_SIZE	= 1;
	private static final	int		MAX_EDIT_HISTORY_MAX_SIZE	= 9999;

	/** The horizontal gap between adjacent components in a container. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The vertical gap between adjacent components in a container. */
	private static final	double	CONTROL_V_GAP	= 8.0;

	/** The margins around a check box. */
	private static final	Insets	CHECK_BOX_MARGINS	= new Insets(2.0, 0.0, 2.0, 0.0);

	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(12.0);

	private static final	Insets	PUZZLE_PANE_PADDING	= new Insets(6.0);

	private static final	Insets	TABBED_PANE_HEADER_PADDING	= new Insets(2.0, 2.0, 0.0, 2.0);

	private static final	double	MIN_TAB_WIDTH	= 64.0;

	/** Miscellaneous strings. */
	private static final	String	PREFERENCES_STR				= "Preferences";
	private static final	String	COLOUR_SCHEME_STR			= "Colour scheme";
	private static final	String	GRID_FONT_STR				= "Grid font";
	private static final	String	GRID_STR					= "Grid";
	private static final	String	IGNORE_CASE_IN_GRID_STR		= "Ignore letter case when typing in a grid";
	private static final	String	DEFAULT_SYMBOLS_STR			= "Default symbols";
	private static final	String	SYMBOLS_STR					= "Symbols";
	private static final	String	ORDER_STR					= "Order";
	private static final	String	EDIT_HISTORY_STR			= "Edit history";
	private static final	String	MAX_SIZE_OF_HISTORY_STR		= "Maximum size of history";
	private static final	String	CLEAR_HISTORY_ON_SAVE_STR	= "Clear history on save";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			PaneStyle.ColourKey.PANE_BORDER,
			CssSelector.builder()
					.cls(StyleClass.PREFERENCES_DIALOG_ROOT)
					.desc(StyleClass.TABBED_PANE)
					.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.cls(StyleClass.PREFERENCES_DIALOG_ROOT)
						.desc(StyleClass.TABBED_PANE)
						.build())
				.borders(Side.BOTTOM)
				.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	PREFERENCES_DIALOG_ROOT	= StyleConstants.APP_CLASS_PREFIX + "preferences-dialog-root";

		String	TABBED_PANE	= StyleConstants.CLASS_PREFIX + "tabbed-pane";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	INCORRECT_NUM_SYMBOLS =
				"An order-%s puzzle must have %d symbols.";

		String	DUPLICATE_SYMBOL =
				"The symbol '%s' occurs more than once.";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int				selectedTabIndex;
	private static	Puzzle.Order	puzzleOrder			= Puzzle.DEFAULT_ORDER;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Preferences							result;
	private	EnumMap<Puzzle.Order, char[]>		symbols;
	private	TabPane								tabPane;
	private	CheckBox							gridFontCheckBox;
	private	SimpleDropDownList<String>			gridFontNameList;
	private	SimpleDropDownList<FontInfo.Style>	gridFontStyleList;
	private	CheckBox							ignoreCaseInGridCheckBox;
	private	CollectionSpinner<Puzzle.Order>		orderSpinner;
	private	SymbolsPane							symbolsPane;
	private	Spinner<Integer>					editHistoryMaxSizeSpinner;
	private	CheckBox							editHistoryClearOnSaveCheckBox;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(PreferencesDialog.class, COLOUR_PROPERTIES, RULE_SETS);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private PreferencesDialog(
		Window		owner,
		Preferences	preferences)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getName(), null, PREFERENCES_STR);

		// Initialise instance variables
		symbols = Puzzle.Order.defaultSymbolMap();
		for (Puzzle.Order order : preferences.symbols().keySet())
			symbols.put(order, preferences.symbols().get(order).clone());

		// Set style class on root node of scene graph
		getScene().getRoot().getStyleClass().add(StyleClass.PREFERENCES_DIALOG_ROOT);

		// Create tabbed pane
		tabPane = new TabPane();
		tabPane.setBorder(SceneUtils.createSolidBorder(getColour(PaneStyle.ColourKey.PANE_BORDER), Side.BOTTOM));
		tabPane.setTabMinWidth(MIN_TAB_WIDTH);
		tabPane.getStyleClass().add(StyleClass.TABBED_PANE);

		// Set tabbed pane as content of dialog
		setContent(tabPane);

		// Set padding around header of tabbed pane
		ControlUtils.onSkin(tabPane, () -> TabPaneUtils.setHeaderAreaPadding(tabPane, TABBED_PANE_HEADER_PADDING));

		// Add tabs to tabbed pane
		for (TabId tabId : TabId.values())
			tabPane.getTabs().add(tabId.createTab());

		// Select tab
		tabPane.getSelectionModel().select(selectedTabIndex);

		// Get theme ID
		StyleManager styleManager = StyleManager.INSTANCE;
		String themeId = styleManager.getThemeId();

		// Create tab content: appearance
		getTab(TabId.APPEARANCE).setContent(createAppearancePane(preferences));

		// Create tab content: puzzle
		getTab(TabId.PUZZLE).setContent(createPuzzlePane(preferences));

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Update symbols
			symbols.put(puzzleOrder, symbolsPane.symbols());

			// Validate symbols for each order
			for (Puzzle.Order order : symbols.keySet())
			{
				int expectedNumSymbols = order.numSymbols();
				char[] syms = symbols.get(order);
				try
				{
					if (syms.length != expectedNumSymbols)
						throw new BaseException(ErrorMsg.INCORRECT_NUM_SYMBOLS, order, expectedNumSymbols);
					Character symbol = SymbolsPane.findDuplicateSymbol(syms);
					if (symbol != null)
						throw new BaseException(ErrorMsg.DUPLICATE_SYMBOL, symbol);
				}
				catch (BaseException e)
				{
					orderSpinner.setItem(order);
					ErrorDialog.show(this, SYMBOLS_STR, e);
					return;
				}
			}

			// Set result
			result = new Preferences(gridFont(), ignoreCaseInGridCheckBox.isSelected(), symbols,
									 editHistoryMaxSizeSpinner.getValue(),
									 editHistoryClearOnSaveCheckBox.isSelected());

			// Close dialog
			requestClose();
		});
		addButton(okButton, HPos.RIGHT);

		// Create button: cancel
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Create procedure to update 'OK' button
		IProcedure0 updateOkButton = () -> okButton.setDisable(!symbolsPane.symbolsValid());

		// When order changes, update expected number of symbols, length limiter of symbols field, content of symbols
		// field and 'OK' button
		orderSpinner.itemProperty().addListener((observable, oldOrder, order) ->
		{
			// Update symbols for old order
			if (oldOrder != null)
				symbols.put(oldOrder, symbolsPane.symbols());

			// Update dialog state
			puzzleOrder = order;

			// Update symbols pane
			symbolsPane.symbols(order, symbols.get(order));

			// Update 'OK' button
			updateOkButton.invoke();
		});

		// Update 'OK' button when content of symbols field changes
		symbolsPane.symbolsField().textProperty().addListener(observable -> updateOkButton.invoke());

		// Update font of symbols field when puzzle tab is selected
		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, tab) ->
		{
			if (tab.getUserData() == TabId.PUZZLE)
				symbolsPane.symbolsField().setFont(gridFont());
		});

		// Set initial order
		orderSpinner.setItem(puzzleOrder);

		// When window is closed, save index of selected tab and restore old theme
		setOnHiding(event ->
		{
			// Save index of selected tab
			selectedTabIndex = tabPane.getSelectionModel().getSelectedIndex();

			// If dialog was not accepted, restore old theme
			if ((result == null) && !Objects.equals(themeId, styleManager.getThemeId()))
				selectTheme(themeId);
		});

		// Apply new style sheet to scene
		applyStyleSheet();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Preferences show(
		Window		owner,
		Preferences	preferences)
	{
		return new PreferencesDialog(owner, preferences).showDialog();
	}

	//------------------------------------------------------------------

	private static void selectTheme(
		String	themeId)
	{
		if (themeId != null)
		{
			// Update theme
			StyleManager.INSTANCE.selectTheme(themeId);

			// Reapply style sheet to the scenes of all JavaFX windows
			StyleManager.INSTANCE.reapplyStylesheet();
		}
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
	protected Preferences getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Tab getTab(
		TabId	tabId)
	{
		return tabPane.getTabs().get(tabId.ordinal());
	}

	//------------------------------------------------------------------

	private FontInfo gridFont()
	{
		FontInfo fontInfo = null;
		if (gridFontCheckBox.isSelected())
		{
			String name = gridFontNameList.item();
			if (name != null)
				fontInfo = new FontInfo(name, gridFontStyleList.item());
		}
		return fontInfo;
	}

	//------------------------------------------------------------------

	private GridPane createAppearancePane(
		Preferences	preferences)
	{
		// Create control pane
		GridPane controlPane = new GridPane();
		controlPane.setHgap(CONTROL_H_GAP);
		controlPane.setVgap(12.0);
		controlPane.setAlignment(Pos.CENTER);
		controlPane.setPadding(CONTROL_PANE_PADDING);

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		controlPane.getColumnConstraints().add(column);

		// Initialise row index
		int row = 0;

		// Spinner: theme
		StyleManager styleManager = StyleManager.INSTANCE;
		CollectionSpinner<String> themeSpinner =
				CollectionSpinner.leftRightH(HPos.CENTER, true, styleManager.getThemeIds(), styleManager.getThemeId(),
											 null, id -> styleManager.findTheme(id).name());
		themeSpinner.itemProperty().addListener((observable, oldId, id) -> selectTheme(id));
		controlPane.addRow(row++, new Label(COLOUR_SCHEME_STR), themeSpinner);

		// Check box: grid font
		FontInfo gridFont = preferences.gridFont();
		gridFontCheckBox = new CheckBox(" " + GRID_FONT_STR);
		gridFontCheckBox.setSelected(gridFont != null);

		// Drop-down list: font name
		List<String> fontNames = Font.getFamilies();
		fontNames.sort(String.CASE_INSENSITIVE_ORDER);
		gridFontNameList = new SimpleDropDownList<>(fontNames);
		String fontName = (gridFont == null) ? null : gridFont.name();
		int index = (fontName == null) ? -1 : fontNames.indexOf(fontName);
		if (index < 0)
			index = fontNames.indexOf(Font.getDefault().getFamily());
		if (index >= 0)
			gridFontNameList.selectIndex(index);

		// Drop-down list: font style
		gridFontStyleList = new SimpleDropDownList<>(FontInfo.Style.values());
		gridFontStyleList.selectItem((gridFont == null) ? FontInfo.Style.REGULAR : gridFont.style());

		// Pane: grid font
		HBox gridFontPane = new HBox(8.0, gridFontNameList, gridFontStyleList);
		gridFontPane.setAlignment(Pos.CENTER_LEFT);
		gridFontPane.disableProperty().bind(gridFontCheckBox.selectedProperty().not());
		controlPane.addRow(row++, gridFontCheckBox, gridFontPane);

		// Return pane
		return controlPane;
	}

	//------------------------------------------------------------------

	private VBox createPuzzlePane(
		Preferences	preferences)
	{
		// Check box: ignore case in grid
		ignoreCaseInGridCheckBox = new CheckBox(IGNORE_CASE_IN_GRID_STR);
		ignoreCaseInGridCheckBox.setSelected(preferences.ignoreCaseInGrid());

		// Control pane: grid
		HBox gridControlPane = new HBox(ignoreCaseInGridCheckBox);
		gridControlPane.setAlignment(Pos.CENTER);
		gridControlPane.setPadding(CONTROL_PANE_PADDING);

		// Titled pane: grid
		LabelTitledPane gridPane = new LabelTitledPane(GRID_STR, gridControlPane);
		gridPane.setAlignment(Pos.CENTER);

		// Control pane: symbols
		GridPane symbolsControlPane = new GridPane();
		symbolsControlPane.setHgap(CONTROL_H_GAP);
		symbolsControlPane.setVgap(CONTROL_V_GAP);
		symbolsControlPane.setAlignment(Pos.CENTER);
		symbolsControlPane.setPadding(CONTROL_PANE_PADDING);

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		symbolsControlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		symbolsControlPane.getColumnConstraints().add(column);

		// Initialise row index
		int row = 0;

		// Spinner: order
		orderSpinner =
				CollectionSpinner.leftRightH(HPos.CENTER, true, Puzzle.Order.class, null, ORDER_PROTOTYPE_TEXT, null);
		orderSpinner.setItem(null);
		symbolsControlPane.addRow(row++, new Label(ORDER_STR), orderSpinner);

		// Pane: symbols
		symbolsPane = new SymbolsPane();
		symbolsControlPane.addRow(row++, new Label(SYMBOLS_STR), symbolsPane);

		// Titled pane: default symbols
		LabelTitledPane defaultSymbolsPane = new LabelTitledPane(DEFAULT_SYMBOLS_STR, symbolsControlPane);
		defaultSymbolsPane.setAlignment(Pos.CENTER);
		VBox.setVgrow(defaultSymbolsPane, Priority.ALWAYS);

		// Control pane: edit history
		GridPane editHistoryControlPane = new GridPane();
		editHistoryControlPane.setHgap(CONTROL_H_GAP);
		editHistoryControlPane.setVgap(CONTROL_V_GAP);
		editHistoryControlPane.setAlignment(Pos.CENTER);
		editHistoryControlPane.setPadding(CONTROL_PANE_PADDING);

		// Initialise column constraints
		column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		editHistoryControlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		editHistoryControlPane.getColumnConstraints().add(column);

		// Reset row index
		row = 0;

		// Spinner: maximum size of edit history
		editHistoryMaxSizeSpinner =
				SpinnerFactory.integerSpinner(MIN_EDIT_HISTORY_MAX_SIZE, MAX_EDIT_HISTORY_MAX_SIZE,
											  preferences.editHistoryMaxSize(),
											  NumberUtils.getNumDecDigitsInt(MAX_EDIT_HISTORY_MAX_SIZE));
		editHistoryControlPane.addRow(row++, new Label(MAX_SIZE_OF_HISTORY_STR), editHistoryMaxSizeSpinner);

		// Check box: clear edit history on save
		editHistoryClearOnSaveCheckBox = new CheckBox(CLEAR_HISTORY_ON_SAVE_STR);
		editHistoryClearOnSaveCheckBox.setSelected(preferences.editHistoryClearOnSave());
		GridPane.setMargin(editHistoryClearOnSaveCheckBox, CHECK_BOX_MARGINS);
		editHistoryControlPane.add(editHistoryClearOnSaveCheckBox, 1, row++);

		// Titled pane: edit history
		LabelTitledPane editHistoryPane = new LabelTitledPane(EDIT_HISTORY_STR, editHistoryControlPane);
		editHistoryPane.setAlignment(Pos.CENTER);

		// Create and return outer pane
		VBox pane = new VBox(6.0, gridPane, defaultSymbolsPane, editHistoryPane);
		pane.setPadding(PUZZLE_PANE_PADDING);
		return pane;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: TAB IDENTIFIER


	private enum TabId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		APPEARANCE
		(
			"Appearance"
		),

		PUZZLE
		(
			"Puzzle"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TabId(
			String	text)
		{
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Tab createTab()
		{
			Tab tab = new Tab(text);
			tab.setUserData(this);
			tab.setClosable(false);
			return tab;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
