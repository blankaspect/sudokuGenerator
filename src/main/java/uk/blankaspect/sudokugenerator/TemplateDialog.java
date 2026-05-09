/*====================================================================*\

TemplateDialog.java

Class: template dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Group;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;

import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.Shape;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.function.IFunction1;
import uk.blankaspect.common.function.IFunction2;
import uk.blankaspect.common.function.IProcedure0;
import uk.blankaspect.common.function.IProcedure2;

import uk.blankaspect.common.geometry.VHDirection;

import uk.blankaspect.common.misc.EditList;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.random.IPrng;
import uk.blankaspect.common.random.PrngXoshiro256ss;

import uk.blankaspect.ui.jfx.button.Buttons;
import uk.blankaspect.ui.jfx.button.GraphicButton;

import uk.blankaspect.ui.jfx.container.PaneStyle;

import uk.blankaspect.ui.jfx.dialog.DialogState;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.dropdownlist.SimpleDropDownList;

import uk.blankaspect.ui.jfx.icon.Icons;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.shape.Shapes;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;
import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.tooltip.TooltipDecorator;

import uk.blankaspect.ui.jfx.widtheq.RegionWidthEqualiser;

//----------------------------------------------------------------------


// CLASS: TEMPLATE DIALOG


class TemplateDialog
	extends SimpleModalDialog<List<Puzzle.Entry>>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The minimum preferred number of entries. */
	private static final	int		MIN_PREF_NUM_ENTRIES	= GenerationDialog.MIN_NUM_ENTRIES;

	/** The prototype text for the <i>value</i> drop-down list. */
	private static final	String	VALUE_PROTOTYPE_TEXT	= "0".repeat(4);

	/** The preferred width and height of the puzzle pane. */
	private static final	double	PUZZLE_PANE_SIZE	= 360.0;

	/** The horizontal gap between adjacent components in a container. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The vertical gap between adjacent components in a container. */
	private static final	double	CONTROL_V_GAP	= 8.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(8.0, 12.0, 8.0, 12.0);

	/** The factor by which the height of the default font is multiplied in determining the size of the icon of the
		<i>add template</i> button. */
	private static final	double	ADD_TEMPLATE_BUTTON_ICON_SIZE_FACTOR	= 0.85;

	/** The padding around the <i>number of entries</i>. */
	private static final	Insets	NUM_ENTRIES_LABEL_PADDING	= new Insets(3.0, 6.0, 3.0, 6.0);

	/** Miscellaneous strings. */
	private static final	String	CREATE_TEMPLATE_STR		= "Create template";
	private static final	String	SYMMETRY_STR			= "Symmetry";
	private static final	String	PREF_NUM_ENTRIES_STR	= "Preferred number of entries";
	private static final	String	SYMBOL_STR				= "Symbol";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			PaneStyle.ColourKey.PANE_BORDER,
			CssSelector.builder()
					.cls(StyleClass.TEMPLATE_DIALOG_ROOT)
					.desc(StyleClass.CONTROL_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.GRAPHIC_BUTTON_BORDER,
			CssSelector.builder()
					.cls(StyleClass.TEMPLATE_DIALOG_ROOT)
					.desc(GraphicButton.StyleClass.GRAPHIC_BUTTON).pseudo(GraphicButton.PseudoClassKey.INACTIVE)
					.desc(GraphicButton.StyleClass.INNER_VIEW)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.NUM_ENTRIES_LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.TEMPLATE_DIALOG_ROOT)
					.desc(StyleClass.NUM_ENTRIES_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.NUM_ENTRIES_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.TEMPLATE_DIALOG_ROOT)
					.desc(StyleClass.NUM_ENTRIES_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.NUM_ENTRIES_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.TEMPLATE_DIALOG_ROOT)
					.desc(StyleClass.NUM_ENTRIES_LABEL)
					.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	TEMPLATE_DIALOG_ROOT	= StyleConstants.APP_CLASS_PREFIX + "template-dialog-root";

		String	CONTROL_PANE		= StyleConstants.CLASS_PREFIX + "control-pane";
		String	NUM_ENTRIES_LABEL	= StyleConstants.CLASS_PREFIX + "num-entries-label";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	CLEAR_TEMPLATE_BUTTON_CROSS		= PREFIX + "clearTemplateButton.cross";
		String	CLEAR_TEMPLATE_BUTTON_DISC		= PREFIX + "clearTemplateButton.disc";
		String	GRAPHIC_BUTTON_BORDER			= PREFIX + "graphicButton.border";
		String	GRAPHIC_BUTTON_ICON				= PREFIX + "graphicButton.icon";
		String	NUM_ENTRIES_LABEL_BACKGROUND	= PREFIX + "numEntriesLabel.background";
		String	NUM_ENTRIES_LABEL_BORDER		= PREFIX + "numEntriesLabel.border";
		String	NUM_ENTRIES_LABEL_TEXT			= PREFIX + "numEntriesLabel.text";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state	= new State();
	private static	IPrng	prng	= new PrngXoshiro256ss();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<Puzzle.Entry>			result;
	private	List<List<Puzzle.Entry>>	templates;
	private	int							selectedIndex;
	private	EditList					editList;
	private	Puzzle						puzzle;
	private	TemplateView				templateView;
	private	CollectionSpinner<Symmetry>	symmetrySpinner;
	private	Spinner<Integer>			prefNumEntriesSpinner;
	private	SimpleDropDownList<Integer>	valueDropDownList;
	private	GraphicButton				previousTemplateButton;
	private	GraphicButton				nextTemplateButton;
	private	Label						numEntriesLabel;
	private	Label						templateIndexLabel;
	private	Button						okButton;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(TemplateDialog.class, COLOUR_PROPERTIES);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private TemplateDialog(
		Window	owner,
		Puzzle	sourcePuzzle)
	{
		// Call superclass constructor
		super(owner, CREATE_TEMPLATE_STR, state.locator(), state.getSize());

		// Initialise instance variables
		templates = new ArrayList<>();
		selectedIndex = -1;
		editList = new EditList(SudokuGeneratorApp.instance().preferences().editHistoryMaxSize());

		// Set properties
		setResizable(true);
		setMinButtonWidth(6.0);

		// Set style class on root node of scene graph
		getScene().getRoot().getStyleClass().add(StyleClass.TEMPLATE_DIALOG_ROOT);

		// Create document
		Puzzle.Order puzzleOrder = sourcePuzzle.puzzleOrder();
		PuzzleDocument document = new PuzzleDocument(puzzleOrder);
		puzzle = document.puzzle();
		puzzle.symbols(sourcePuzzle.symbols());
		puzzle.setValues(sourcePuzzle.entries());

		// Create puzzle pane
		PuzzlePane puzzlePane = new PuzzlePane(document)
		{
			@Override
			protected void onGridDoubleClick(
				MouseEvent	event)
			{
				int index = selectedIndex();
				if (index >= 0)
				{
					templateView.setSelectionValue((puzzle.value(index) == 0) ? valueDropDownList.item() : 0);
					event.consume();
				}
			}
		};
		puzzlePane.setPrefSize(PUZZLE_PANE_SIZE, PUZZLE_PANE_SIZE);
		puzzlePane.symmetry(state.symmetry);

		// Create template view
		templateView = new TemplateView(puzzlePane);
		VBox.setVgrow(templateView, Priority.ALWAYS);

		// Create control pane
		GridPane controlPane = new GridPane();
		controlPane.setHgap(CONTROL_H_GAP);
		controlPane.setVgap(CONTROL_V_GAP);
		controlPane.setAlignment(Pos.CENTER);
		controlPane.setPadding(CONTROL_PANE_PADDING);
		controlPane.setBorder(SceneUtils.createSolidBorder(getColour(PaneStyle.ColourKey.PANE_BORDER)));
		controlPane.getStyleClass().add(StyleClass.CONTROL_PANE);
		VBox.setMargin(controlPane, new Insets(0.0, 4.0, 0.0, 4.0));

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

		// Create spinner: symmetry
		symmetrySpinner = CollectionSpinner.leftRightH(HPos.CENTER, true, Symmetry.class, state.symmetry, null, null);
		symmetrySpinner.itemProperty().addListener((observable, oldSymmetry, symmetry) ->
				puzzlePane.symmetry(symmetry));
		controlPane.addRow(row++, new Label(SYMMETRY_STR), symmetrySpinner);

		// Spinner: preferred number of entries
		int numCells = puzzle.numRows() * puzzle.numColumns();
		int maxPrefNumEntries = numCells - 1;
		int initialPrefNumEntries = state.prefNumEntries.get(puzzleOrder);
		prefNumEntriesSpinner =
				SpinnerFactory.integerSpinner(MIN_PREF_NUM_ENTRIES, maxPrefNumEntries, initialPrefNumEntries,
											  NumberUtils.getNumDecDigitsInt(maxPrefNumEntries));
		controlPane.addRow(row++, new Label(PREF_NUM_ENTRIES_STR), prefNumEntriesSpinner);

		// Create procedure to homogenise entries
		IProcedure0 homogeniseEntries = () ->
		{
			int value = valueDropDownList.item();
			for (Puzzle.Entry entry : puzzle.entries())
				puzzle.setValue(entry.index(), value);
		};

		// Drop-down list: value
		String symbols = new String(puzzle.symbols());
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < symbols.length(); i++)
			items.add(i + 1);
		valueDropDownList = new SimpleDropDownList<>(VALUE_PROTOTYPE_TEXT, items, item ->
				(item == null) ? null : symbols.substring(item - 1, item));
		valueDropDownList.item(state.values.get(puzzleOrder));
		valueDropDownList.itemProperty().addListener(observable ->
		{
			homogeniseEntries.invoke();
			puzzlePane.redraw();
		});
		controlPane.addRow(row++, new Label(SYMBOL_STR), valueDropDownList);

		// Homogenise entries
		homogeniseEntries.invoke();

		// If puzzle is not empty, add it to list of templates
		List<Puzzle.Entry> entries = puzzle.entries();
		if (!entries.isEmpty())
		{
			templates.add(entries);
			selectedIndex = 0;
		}

		// Create content pane
		VBox contentPane = new VBox(templateView, controlPane);
		contentPane.setAlignment(Pos.TOP_CENTER);

		// Set content pane as content
		setContent(contentPane);

		// Create icon for 'add template' button
		double textHeight = TextUtils.textHeight();
		Shape addIcon = Shapes.plus01(ADD_TEMPLATE_BUTTON_ICON_SIZE_FACTOR * textHeight);
		addIcon.setStroke(getColour(ColourKey.GRAPHIC_BUTTON_ICON));

		// Create factory for graphic buttons
		IFunction2<GraphicButton, Group, String> graphicButtonFactory = (icon, text) ->
		{
			GraphicButton button = new GraphicButton(icon);
			button.setBorderColour(getColour(ColourKey.GRAPHIC_BUTTON_BORDER));
			TooltipDecorator.addTooltip(button, text);
			return button;
		};

		// Button: add template
		GraphicButton addTemplateButton = graphicButtonFactory.invoke(Shapes.tile(addIcon, Math.ceil(textHeight)),
																	  Command.ADD_TEMPLATE.tooltipText());
		addTemplateButton.setOnAction(event -> onAddTemplate());
		HBox.setMargin(addTemplateButton, new Insets(0.0, 8.0, 0.0, 0.0));
		addButton(addTemplateButton, HPos.LEFT, false);

		// Create icon for 'previous template' button
		Shape previousIcon = Shapes.arrowhead01(VHDirection.LEFT, textHeight);
		previousIcon.setFill(getColour(ColourKey.GRAPHIC_BUTTON_ICON));

		// Button: previous template
		previousTemplateButton = graphicButtonFactory.invoke(Shapes.tile(previousIcon, Math.ceil(textHeight)),
															 Command.PREVIOUS_TEMPLATE.tooltipText());
		previousTemplateButton.setOnAction(event -> onPreviousTemplate());
		addButton(previousTemplateButton, HPos.LEFT, false);

		// Create icon for 'next template' button
		Shape nextIcon = Shapes.arrowhead01(VHDirection.RIGHT, textHeight);
		nextIcon.setFill(getColour(ColourKey.GRAPHIC_BUTTON_ICON));

		// Button: next template
		nextTemplateButton = graphicButtonFactory.invoke(Shapes.tile(nextIcon, Math.ceil(textHeight)),
														 Command.NEXT_TEMPLATE.tooltipText());
		nextTemplateButton.setOnAction(event -> onNextTemplate());
		addButton(nextTemplateButton, HPos.LEFT, false);

		// Button: clear template
		Group clearIcon = Icons.clear01(getColour(ColourKey.CLEAR_TEMPLATE_BUTTON_DISC),
										getColour(ColourKey.CLEAR_TEMPLATE_BUTTON_CROSS),
										0.8, 1.0);
		GraphicButton clearTemplateButton =
				graphicButtonFactory.invoke(clearIcon, Command.CLEAR_TEMPLATE.tooltipText());
		clearTemplateButton.setOnAction(event -> onClearTemplate());
		HBox.setMargin(clearTemplateButton, new Insets(0.0, 8.0, 0.0, 8.0));
		addButton(clearTemplateButton, HPos.LEFT, false);

		// Label: number of entries
		numEntriesLabel = new Label();
		numEntriesLabel.setMinWidth(Region.USE_PREF_SIZE);
		numEntriesLabel.setAlignment(Pos.CENTER_RIGHT);
		numEntriesLabel.setPadding(NUM_ENTRIES_LABEL_PADDING);
		numEntriesLabel.setTextFill(getColour(ColourKey.NUM_ENTRIES_LABEL_TEXT));
		numEntriesLabel.setBackground(SceneUtils.createColouredBackground(
				getColour(ColourKey.NUM_ENTRIES_LABEL_BACKGROUND)));
		numEntriesLabel.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.NUM_ENTRIES_LABEL_BORDER)));
		Insets insets = numEntriesLabel.getInsets();
		numEntriesLabel.setPrefWidth(TextUtils.textWidthCeil("0".repeat(NumberUtils.getNumDecDigitsInt(numCells)))
				+ insets.getLeft() + insets.getRight());
		numEntriesLabel.getStyleClass().add(StyleClass.NUM_ENTRIES_LABEL);
		HBox.setMargin(numEntriesLabel, new Insets(0.0, 4.0, 0.0, 0.0));
		addButton(numEntriesLabel, HPos.LEFT, false);

		// Label: template index
		templateIndexLabel = Labels.hNoShrink();
		addButton(templateIndexLabel, HPos.LEFT, false);

		// Create button: OK
		okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Set result
			result = puzzle.entries();

			// Close dialog
			requestClose();
		});
		addButton(okButton, HPos.RIGHT);

		// Create button: cancel
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Update template
		updateTemplate();

		// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, okButton);

		// Add key combinations for commands
		getScene().getAccelerators().put(Command.ADD_TEMPLATE.keyCombo,      this::onAddTemplate);
		getScene().getAccelerators().put(Command.CLEAR_TEMPLATE.keyCombo,    this::onClearTemplate);
		getScene().getAccelerators().put(Command.PREVIOUS_TEMPLATE.keyCombo, this::onPreviousTemplate);
		getScene().getAccelerators().put(Command.NEXT_TEMPLATE.keyCombo,     this::onNextTemplate);
		getScene().getAccelerators().put(Command.UNDO.keyCombo,              this::onUndo);
		getScene().getAccelerators().put(Command.REDO.keyCombo,              this::onRedo);

		// Request focus on template view when dialog is opened
		setOnShown(event -> templateView.requestFocus());

		// Save dialog state when dialog is closed
		setOnHiding(event ->
		{
			state.restoreAndUpdate(this, true);
			state.symmetry = symmetrySpinner.getItem();
			state.prefNumEntries.put(puzzleOrder, prefNumEntriesSpinner.getValue());
			state.values.put(puzzleOrder, valueDropDownList.item());
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static List<Puzzle.Entry> show(
		Window	owner,
		Puzzle	sourcePuzzle)
	{
		return new TemplateDialog(owner, sourcePuzzle).showDialog();
	}

	//------------------------------------------------------------------

	public static MapNode encodeState()
	{
		return state.encodeTree();
	}

	//------------------------------------------------------------------

	public static void decodeState(
		MapNode	rootNode)
	{
		state.decodeTree(rootNode);
	}

	//------------------------------------------------------------------

	private static List<Integer> symmetricalCells(
		int			r1,
		int			c1,
		int			numRows,
		int			numColumns,
		Symmetry	symmetry)
	{
		// Initialise list of cell indices
		List<Integer> cellIndices = new ArrayList<>();

		// Create procedure to convert row and column indices to cell index and add cell index to list
		IProcedure2<Integer, Integer> addCell = (row, column) ->
		{
			int index = row * numColumns + column;
			for (int i = 0; i < cellIndices.size(); i++)
			{
				if (cellIndices.get(i) == index)
				{
					index = -1;
					break;
				}
			}
			if (index >= 0)
				cellIndices.add(index);
		};

		// Add indices of cells that are symmetrical with the cell at the specified row and column
		int c2 = numColumns - c1 - 1;
		int r2 = numRows - r1 - 1;
		switch (symmetry)
		{
			case NONE                   -> { }
			case REFLECTION_V           ->   addCell.invoke(r1, c2);
			case REFLECTION_H           ->   addCell.invoke(r2, c1);
			case REFLECTION_VH          -> { addCell.invoke(r1, c2); addCell.invoke(r2, c1); addCell.invoke(r2, c2); }
			case REFLECTION_DIAGONAL_TL ->   addCell.invoke(c1, r1);
			case REFLECTION_DIAGONAL_TR ->   addCell.invoke(c2, r2);
			case REFLECTION_DIAGONALS   -> { addCell.invoke(c1, r1); addCell.invoke(c2, r2); addCell.invoke(r2, c2); }
			case ROTATION_2             ->   addCell.invoke(r2, c2);
			case ROTATION_4             -> { addCell.invoke(c1, r2); addCell.invoke(r2, c2); addCell.invoke(c2, r1); }
		}

		// Return list of cell indices
		return cellIndices;
	}

	//------------------------------------------------------------------

	private static List<Puzzle.Entry> createTemplate(
		Puzzle		puzzle,
		Symmetry	symmetry,
		int			prefNumEntries,
		int			value)
	{
		// Initialise set of entries
		List<Puzzle.Entry> entries = new ArrayList<>();

		// Initialise bit array of flags of entries
		int numRows = puzzle.numRows();
		int numColumns = puzzle.numColumns();
		BitSet entryFlags = new BitSet(numRows * numColumns);

		// Create a procedure to add an entry to a set
		IProcedure2<Integer, Integer> addEntry = (row, column) ->
		{
			int index = row * puzzle.numColumns() + column;
			if (!entryFlags.get(index))
			{
				entries.add(new Puzzle.Entry(index, value));
				entryFlags.set(index);
			}
		};

		// Generate a list of sets of entries
		List<List<Puzzle.Entry>> entrySets = new ArrayList<>();
		Symmetry.Dimensions rc = symmetry.principalDimensions(numRows);
		int entryCount = 0;
		while (entryCount < prefNumEntries)
		{
			// Clear set of entries
			entries.clear();

			// Set value on cell in principal region
			int row = prng.nextInt(rc.numRows());
			int column = prng.nextInt(rc.numColumns());
			addEntry.invoke(row, column);

			// Set values on corresponding cells in other regions
			for (int cellIndex : symmetricalCells(row, column, numRows, numColumns, symmetry))
				addEntry.invoke(cellIndex / numColumns, cellIndex % numColumns);

			// Add set of entries to list
			entrySets.add(List.copyOf(entries));

			// Increment count of entries
			entryCount += entries.size();
		}

		// Ensure that the combined size of the entry sets does not exceed the preferred number of entries
		int excess = entryCount - prefNumEntries;
		if (excess > 0)
			diminishEntrySets(entrySets, excess);

		// Return set of entries
		entries.clear();
		for (List<Puzzle.Entry> entrySet : entrySets)
			entries.addAll(entrySet);
		return entries;
	}

	//------------------------------------------------------------------

	private static void diminishEntrySets(
		List<List<Puzzle.Entry>>	entrySets,
		int							excess)
	{
		// Check arguments
		if (excess > 3)
			throw new UnexpectedRuntimeException();

		// Sort entry sets by size
		Comparator<List<Puzzle.Entry>> comparator = Comparator.comparingInt(List::size);
		entrySets.sort(comparator);

		// Create list of dummy entries for binary search
		List<Puzzle.Entry> dummyEntries = new ArrayList<>();
		Collections.addAll(dummyEntries, new Puzzle.Entry[excess]);

		// Search for an entry set whose size is equal to the excess
		int index = Collections.binarySearch(entrySets, dummyEntries, comparator);

		// If there is such a set, remove it from the list and return
		if (index >= 0)
		{
			entrySets.remove(index);
			return;
		}

		// Declare record: range of entry sets of a given size within the list of entry sets
		record Range(
			int		offset,
			boolean	notEmpty)
		{ }

		// Find the ranges of entry sets of a given size in the list of entry sets
		Range[] ranges = new Range[excess + 1];
		ranges[excess] = new Range(-index - 1, false);
		for (int i = excess - 1; i > 0; i--)
		{
			dummyEntries.remove(i);
			index = Collections.binarySearch(entrySets, dummyEntries, comparator);
			ranges[i] = (index < 0) ? new Range(-index - 1, false) : new Range(index, true);
		}

		// Try to remove a set of entry sets whose combined size is equal to the excess
		switch (excess)
		{
			case 2:
				// If possible, remove two size-1 entry sets
				if (ranges[1].notEmpty && (ranges[2].offset - ranges[1].offset >= 2))
				{
					entrySets.remove(ranges[1].offset + 1);
					entrySets.remove(ranges[1].offset);
					return;
				}
				break;

			case 3:
				if (ranges[1].notEmpty)
				{
					// If possible, remove a size-2 entry set and a size-1 entry set
					if (ranges[2].notEmpty)
					{
						entrySets.remove(ranges[2].offset);
						entrySets.remove(ranges[1].offset);
						return;
					}

					// If possible, remove three size-1 entry sets
					if (ranges[2].offset - ranges[1].offset >= 3)
					{
						entrySets.remove(ranges[1].offset + 2);
						entrySets.remove(ranges[1].offset + 1);
						entrySets.remove(ranges[1].offset);
						return;
					}
				}
				break;
		}

		// Remove the smallest entry set that will ensure that the combined size of the remaining entry sets does not
		// exceed the preferred number of entries
		entrySets.remove(ranges[excess].offset);
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
	protected List<Puzzle.Entry> getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

	@Override
	protected void onWindowShown()
	{
		// Call superclass method
		super.onWindowShown();

		// Set lower bound on width of dialog
		setMinWidth(prefWidth());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void updateTemplate()
	{
		// Clear edit list
		editList.clear();

		// Set template on puzzle
		if (selectedIndex >= 0)
		{
			// Set values on puzzle
			puzzle.setValues(templates.get(selectedIndex));

			// Redraw puzzle
			redrawPuzzle();
		}

		// Update index label
		templateIndexLabel.setText((selectedIndex + 1) + " / " + templates.size());

		// Update navigation buttons
		previousTemplateButton.setDisable(selectedIndex <= 0);
		nextTemplateButton.setDisable(selectedIndex >= templates.size() - 1);

		// Update 'number of entries' label and 'OK' button
		updateNumEntries();
	}

	//------------------------------------------------------------------

	private void updateNumEntries()
	{
		// Update 'number of entries' label
		numEntriesLabel.setText(Integer.toString(puzzle.numEntries()));

		// Update 'OK' button
		okButton.setDisable(puzzle.numEntries() == 0);
	}

	//------------------------------------------------------------------

	private void redrawPuzzle()
	{
		// Redraw puzzle
		templateView.puzzlePane.redraw();

		// Request focus on template view
		templateView.requestFocus();
	}

	//------------------------------------------------------------------

	private void onUndo()
	{
		EditList.IEdit edit = editList.removeUndo();
		if (edit != null)
		{
			// Undo last edit
			edit.undo();

			// Redraw puzzle
			redrawPuzzle();

			// Update 'number of entries' label and 'OK' button
			updateNumEntries();
		}
	}

	//------------------------------------------------------------------

	private void onRedo()
	{
		EditList.IEdit edit = editList.removeRedo();
		if (edit != null)
		{
			// Redo last edit
			edit.redo();

			// Redraw puzzle
			redrawPuzzle();

			// Update 'number of entries' label and 'OK' button
			updateNumEntries();
		}
	}

	//------------------------------------------------------------------

	private void onAddTemplate()
	{
		// Save current template
		List<Puzzle.Entry> entries = puzzle.entries();
		if (selectedIndex < 0)
		{
			if (!entries.isEmpty())
				templates.add(entries);
		}
		else
			templates.set(selectedIndex, entries);

		// Create template and add it to list
		templates.add(createTemplate(puzzle, symmetrySpinner.getItem(), prefNumEntriesSpinner.getValue(),
									 valueDropDownList.item()));

		// Select new template
		selectedIndex = templates.size() - 1;
		updateTemplate();
	}

	//------------------------------------------------------------------

	private void onClearTemplate()
	{
		// Get entries of puzzle
		List<Puzzle.Entry> entries = puzzle.entries();

		// Clear entries
		puzzle.clear();

		// Add edit to list
		editList.add(new EntriesEdit(entries, puzzle.entries()));

		// Redraw puzzle
		redrawPuzzle();

		// Update 'number of entries' label and 'OK' button
		updateNumEntries();
	}

	//------------------------------------------------------------------

	private void onPreviousTemplate()
	{
		if (selectedIndex > 0)
		{
			// Save current template
			templates.set(selectedIndex, puzzle.entries());

			// Select previous template
			--selectedIndex;
			updateTemplate();
		}
	}

	//------------------------------------------------------------------

	private void onNextTemplate()
	{
		if (selectedIndex < templates.size() - 1)
		{
			// Save current template
			templates.set(selectedIndex, puzzle.entries());

			// Select next template
			++selectedIndex;
			updateTemplate();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COMMAND


	public enum Command
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ADD_TEMPLATE
		(
			"Add template",
			"Ctrl+T",
			new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)
		),

		CLEAR_TEMPLATE
		(
			"Clear template",
			"Ctrl+Delete",
			new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN)
		),

		NEXT_TEMPLATE
		(
			"Next template",
			"Ctrl+PageDown",
			new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.CONTROL_DOWN)
		),

		PREVIOUS_TEMPLATE
		(
			"Previous template",
			"Ctrl+PageUp",
			new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.CONTROL_DOWN)
		),

		REDO
		(
			"Redo",
			"Ctrl+Y",
			new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN)
		),

		UNDO
		(
			"Undo",
			"Ctrl+Z",
			new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN)
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String			text;
		private	String			keyComboText;
		private	KeyCombination	keyCombo;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Command(
			String			text,
			String			keyComboText,
			KeyCombination	keyCombo)
		{
			// Initialise instance variables
			this.text = text;
			this.keyComboText = keyComboText;
			this.keyCombo = keyCombo;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private String tooltipText()
		{
			return text + " (" + keyComboText + ")";
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: DIALOG STATE


	private static class State
		extends DialogState
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Symmetry	DEFAULT_SYMMETRY	= Symmetry.REFLECTION_VH;
		private static final	Map<Puzzle.Order, Integer>	DEFAULT_VALUES;

		/** Keys of properties. */
		private interface PropertyKey
		{
			String	PREF_NUM_ENTRIES	= "prefNumEntries";
			String	SYMMETRY			= "symmetry";
			String	VALUES				= "values";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Symmetry					symmetry;
		private	Map<Puzzle.Order, Integer>	prefNumEntries;
		private	Map<Puzzle.Order, Integer>	values;

	////////////////////////////////////////////////////////////////////
	//  Static initialiser
	////////////////////////////////////////////////////////////////////

		static
		{
			DEFAULT_VALUES = new EnumMap<>(Puzzle.Order.class);
			for (Puzzle.Order order : Puzzle.Order.values())
				DEFAULT_VALUES.put(order, 1);
		}

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, true);

			// Initialise instance variables
			symmetry = DEFAULT_SYMMETRY;
			prefNumEntries = new EnumMap<>(GenerationParams.DEFAULT_NUM_ENTRIES);
			values = new EnumMap<>(DEFAULT_VALUES);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public MapNode encodeTree()
		{
			// Call superclass method
			MapNode rootNode = super.encodeTree();

			// Encode symmetry
			rootNode.addString(PropertyKey.SYMMETRY, symmetry.key());

			// Encode preferred number of entries
			MapNode prefNumEntriesNode = rootNode.addMap(PropertyKey.PREF_NUM_ENTRIES);
			for (Puzzle.Order order : prefNumEntries.keySet())
				prefNumEntriesNode.addInt(order.key(), prefNumEntries.get(order));

			// Encode values
			MapNode valuesNode = rootNode.addMap(PropertyKey.VALUES);
			for (Puzzle.Order order : values.keySet())
				valuesNode.addInt(order.key(), values.get(order));

			// Return root node
			return rootNode;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public void decodeTree(
			MapNode	rootNode)
		{
			// Call superclass method
			super.decodeTree(rootNode);

			// Decode symmetry
			symmetry = rootNode.getEnumValue(Symmetry.class, PropertyKey.SYMMETRY, Symmetry::key, DEFAULT_SYMMETRY);

			// Decode preferred number of entries
			String key = PropertyKey.PREF_NUM_ENTRIES;
			if (rootNode.hasMap(key))
			{
				MapNode node = rootNode.getMapNode(key);
				for (Puzzle.Order order : Puzzle.Order.values())
				{
					key = order.key();
					if (node.hasInt(key))
						prefNumEntries.put(order, node.getInt(key));
				}
			}

			// Decode values
			key = PropertyKey.VALUES;
			if (rootNode.hasMap(key))
			{
				MapNode node = rootNode.getMapNode(key);
				for (Puzzle.Order order : Puzzle.Order.values())
				{
					key = order.key();
					if (node.hasInt(key))
						values.put(order, node.getInt(key));
				}
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: VIEW OF A TEMPLATE


	private class TemplateView
		extends StackPane
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The padding around this view. */
		private static final	Insets	PADDING	= new Insets(6.0);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PuzzlePane	puzzlePane;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TemplateView(
			PuzzlePane	puzzlePane)
		{
			// Initialise instance variables
			this.puzzlePane = puzzlePane;

			// Set properties
			setPadding(PADDING);
			setFocusTraversable(true);
			getProperties().put(PuzzlePane.FOCUSABLE_PARENT, new Object());

			// Add children
			getChildren().add(puzzlePane);

			// Redraw puzzle when this view gains or loses focus
			focusedProperty().addListener(observable -> puzzlePane.redraw());

			// Request focus on this view when mouse is pressed on it
			addEventHandler(MouseEvent.MOUSE_PRESSED, event -> requestFocus());

			// Handle 'key pressed' events
			addEventHandler(KeyEvent.KEY_PRESSED, event ->
			{
				// Ignore key press if modifier key was also pressed
				if (event.isShiftDown() || event.isControlDown() || event.isAltDown())
					return;

				// Handle navigation keys and keys that clear a cell
				boolean handled = true;
				switch (event.getCode())
				{
					case SPACE,
						 DELETE    -> setSelectionValue(0);
					case UP        -> puzzlePane.incrementSelectionRow(-1);
					case DOWN      -> puzzlePane.incrementSelectionRow(1);
					case PAGE_UP   -> puzzlePane.incrementSelectionRow(-puzzlePane.numRows());
					case PAGE_DOWN -> puzzlePane.incrementSelectionRow(puzzlePane.numRows());
					case LEFT      -> puzzlePane.incrementSelectionColumn(-1);
					case RIGHT     -> puzzlePane.incrementSelectionColumn(1);
					case HOME      -> puzzlePane.incrementSelectionColumn(-puzzlePane.numColumns());
					case END       -> puzzlePane.incrementSelectionColumn(puzzlePane.numColumns());
					default        -> handled = false;
				}

				// If key event was handled, consume it
				if (handled)
					event.consume();
			});

			// Handle 'key typed' events
			addEventHandler(KeyEvent.KEY_TYPED, event ->
			{
				if (puzzle.editable())
				{
					int value = puzzle.valueForKeyTyped(event);
					if (value > 0)
					{
						setSelectionValue(valueDropDownList.item());
						event.consume();
					}
				}
			});

			// Display context menu on request
			addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event ->
			{
				// Create label-width equaliser to align key-combo labels across multiple menu items
				RegionWidthEqualiser labelWidthEqualiser = new RegionWidthEqualiser();

				// Create menu-item factory
				IFunction1<MenuItem, Command> menuItemFactory = command ->
				{
					Label label = new Label(command.text);
					labelWidthEqualiser.add(label);
					return new MenuItem(null, new HBox(10.0, label, new Label(command.keyComboText)));
				};

				// Create context menu
				ContextMenu menu = new ContextMenu();

				// Add menu item: undo
				MenuItem menuItem = menuItemFactory.invoke(Command.UNDO);
				menuItem.setDisable(!editList.canUndo());
				menuItem.setOnAction(event0 -> onUndo());
				menu.getItems().add(menuItem);

				// Add menu item: redo
				menuItem = menuItemFactory.invoke(Command.REDO);
				menuItem.setDisable(!editList.canRedo());
				menuItem.setOnAction(event0 -> onRedo());
				menu.getItems().add(menuItem);

				// Add separator
				menu.getItems().add(new SeparatorMenuItem());

				// Add menu item: add template
				menuItem = menuItemFactory.invoke(Command.ADD_TEMPLATE);
				menuItem.setOnAction(event0 -> onAddTemplate());
				menu.getItems().add(menuItem);

				// Add separator
				menu.getItems().add(new SeparatorMenuItem());

				// Add menu item: previous template
				menuItem = menuItemFactory.invoke(Command.PREVIOUS_TEMPLATE);
				menuItem.setDisable(selectedIndex <= 0);
				menuItem.setOnAction(event0 -> onPreviousTemplate());
				menu.getItems().add(menuItem);

				// Add menu item: next template
				menuItem = menuItemFactory.invoke(Command.NEXT_TEMPLATE);
				menuItem.setDisable(selectedIndex >= templates.size() - 1);
				menuItem.setOnAction(event0 -> onNextTemplate());
				menu.getItems().add(menuItem);

				// Add separator
				menu.getItems().add(new SeparatorMenuItem());

				// Add menu item: clear template
				menuItem = menuItemFactory.invoke(Command.CLEAR_TEMPLATE);
				menuItem.setOnAction(event0 -> onClearTemplate());
				menu.getItems().add(menuItem);

				// If menu was triggered by key press, relocate menu at top left of grid when it is shown
				if (event.isKeyboardTrigger())
				{
					menu.setOnShown(event0 ->
					{
						Bounds bounds = puzzlePane.gridBoundsOnScreen();
						menu.setAnchorX(bounds.getMinX());
						menu.setAnchorY(bounds.getMinY());
					});
				}

				// Update widths of principal labels of menu items
				menu.setOnShown(event0 -> labelWidthEqualiser.updateWidths());

				// Display context menu
				menu.show(SceneUtils.getWindow(this), event.getScreenX(), event.getScreenY());
			});
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setSelectionValue(
			int	value)
		{
			int index = puzzlePane.selectedIndex();
			byte oldValue = puzzle.value(index);
			if (oldValue != value)
			{
				// Get entries of puzzle
				List<Puzzle.Entry> entries = puzzle.entries();

				// Set value on selected cell
				puzzle.setValue(index, value);

				// Set value on symmetrical cells
				int numColumns = puzzle.numColumns();
				List<Integer> cellIndices = symmetricalCells(index / numColumns, index % numColumns, puzzle.numRows(),
															 numColumns, puzzlePane.symmetry());
				for (int cellIndex : cellIndices)
					puzzle.setValue(cellIndex, value);

				// Add edit to list
				editList.add(new EntriesEdit(entries, puzzle.entries()));

				// Redraw puzzle
				puzzlePane.redraw();

				// Update 'number of entries' label and 'OK' button
				updateNumEntries();
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: AN EDIT OF THE ENTRIES OF A PUZZLE


	private class EntriesEdit
		implements EditList.IEdit
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<Puzzle.Entry>	oldEntries;
		private	List<Puzzle.Entry>	newEntries;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EntriesEdit(
			List<Puzzle.Entry>	oldEntries,
			List<Puzzle.Entry>	newEntries)
		{
			// Initialise instance variables
			this.oldEntries = oldEntries;
			this.newEntries = newEntries;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : EditList.IEdit interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void undo()
		{
			puzzle.setValues(oldEntries);
		}

		//--------------------------------------------------------------

		@Override
		public void redo()
		{
			puzzle.setValues(newEntries);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
