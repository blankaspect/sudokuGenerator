/*====================================================================*\

PuzzlePane.java

Class: container for a representation of a sudoku puzzle.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.image.WritableImage;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import javafx.scene.transform.Transform;

import javafx.stage.Popup;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IFunction1;
import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.ui.jfx.font.FontUtils;

import uk.blankaspect.ui.jfx.icon.Icons;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: CONTAINER FOR A REPRESENTATION OF A SUDOKU PUZZLE


class PuzzlePane
	extends StackPane
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		Object	FOCUSABLE_PARENT	= new Object();

	private static final	double	HORIZONTAL_GLYPH_FRACTION	= 0.8;
	private static final	double	VERTICAL_GLYPH_FRACTION		= 1.0;

	private static final	double	GRID_LINE_WIDTH_0		= 1.0;
	private static final	double	GRID_LINE_WIDTH_1		= 3.0;
	private static final	double	DELTA_GRID_LINE_WIDTH	= GRID_LINE_WIDTH_1 - GRID_LINE_WIDTH_0;

	private static final	double	SYMMETRY_LINE_WIDTH_0	= 1.0;
	private static final	double	SYMMETRY_LINE_WIDTH_1	= GRID_LINE_WIDTH_1;

	private static final	Map<Puzzle.Order, Double>	ROTATION_INDICATOR_RADIUS_FRACTIONS;

	private static final	double	ROTATION_INDICATOR_ARROWHEAD_FACTOR1	= 0.001;
	private static final	double	ROTATION_INDICATOR_ARROWHEAD_FACTOR2	= 0.015;
	private static final	double	ROTATION_INDICATOR_ARROWHEAD_FACTOR3	= 0.03;
	private static final	double	ROTATION_INDICATOR_ARROWHEAD_FACTOR4	= 0.05;

	private static final	double	ROTATION2_INDICATOR_ANGLE	= 165.0;

	private static final	double	ROTATION4_INDICATOR_ANGLE	= 75.0;

	private static final	int[]	FONT_SIZES	=
			{ 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 40, 44, 48, 54, 60 };

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.CLEAR_CELL_ICON_DISC,
			CssSelector.builder()
					.cls(StyleClass.VALUE_SELECTION_PANE)
					.desc(Icons.StyleClass.CLEAR01_DISC)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.CLEAR_CELL_ICON_CROSS,
			CssSelector.builder()
					.cls(StyleClass.VALUE_SELECTION_PANE)
					.desc(Icons.StyleClass.CLEAR01_CROSS)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.VALUE_SELECTOR_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.VALUE_SELECTION_PANE)
					.desc(StyleClass.CLEAR_CELL_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.VALUE_SELECTOR_BACKGROUND_HIGHLIGHTED,
			CssSelector.builder()
					.cls(StyleClass.VALUE_SELECTION_PANE)
					.desc(StyleClass.CLEAR_CELL_PANE).pseudo(FxPseudoClass.HOVERED)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.VALUE_SELECTOR_GRID,
			CssSelector.builder()
					.cls(StyleClass.VALUE_SELECTION_PANE)
					.desc(StyleClass.CLEAR_CELL_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.VALUE_SELECTOR_BORDER,
			CssSelector.builder()
					.cls(StyleClass.VALUE_SELECTION_PANE)
					.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.cls(StyleClass.VALUE_SELECTION_PANE)
						.build())
				.borderWidths(2, 2, 2, 2)
				.build(),
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.cls(StyleClass.VALUE_SELECTION_PANE)
						.desc(StyleClass.CLEAR_CELL_PANE)
						.build())
				.borders(Side.TOP)
				.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	CLEAR_CELL_PANE			= StyleConstants.CLASS_PREFIX + "clear-cell-pane";
		String	VALUE_SELECTION_PANE	= StyleConstants.CLASS_PREFIX + "value-selection-pane";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	BACKGROUND								= PREFIX + "background";
		String	BACKGROUND_HIGHLIGHTED					= PREFIX + "background.highlighted";
		String	BACKGROUND_SELECTED						= PREFIX + "background.selected";
		String	BACKGROUND_SELECTED_FOCUSED				= PREFIX + "background.selected.focused";
		String	GRID									= PREFIX + "grid";
		String	GRID_FOCUSED							= PREFIX + "grid.focused";
		String	SYMMETRY_LINE							= PREFIX + "symmetryLine";
		String	SYMMETRY_LINE_FOCUSED					= PREFIX + "symmetryLine.focused";
		String	TEXT									= PREFIX + "text";
		String	TEXT_EDITABLE							= PREFIX + "text.editable";
		String	VALUE_SELECTOR_BACKGROUND				= PREFIX + "valueSelector.background";
		String	VALUE_SELECTOR_BACKGROUND_DISABLED		= PREFIX + "valueSelector.background.disabled";
		String	VALUE_SELECTOR_BACKGROUND_HIGHLIGHTED	= PREFIX + "valueSelector.background.highlighted";
		String	VALUE_SELECTOR_BORDER					= PREFIX + "valueSelector.border";
		String	VALUE_SELECTOR_GRID						= PREFIX + "valueSelector.grid";
		String	VALUE_SELECTOR_TEXT						= PREFIX + "valueSelector.text";
		String	VALUE_SELECTOR_TEXT_DISABLED			= PREFIX + "valueSelector.text.disabled";

		String	CLEAR_CELL_ICON_CROSS					= PREFIX + "clearCellIcon.cross";
		String	CLEAR_CELL_ICON_DISC					= PREFIX + "clearCellIcon.disc";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	FontInfo	fontInfo;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	PuzzleDocument			document;
	private	int						order;
	private	int						numRows;
	private	int						numColumns;
	private	double					totalGridLineWidth;
	private	int						selectedIndex;
	private	List<Integer>			highlightedIndices;
	private	List<FontSize>			fontSizes;
	private	Symmetry				symmetry;
	private	InvalidationListener	themeListener;
	private	Canvas					grid;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(PuzzlePane.class, COLOUR_PROPERTIES, RULE_SETS);

		// Initialise map of rotation-indicator radius fractions
		ROTATION_INDICATOR_RADIUS_FRACTIONS = new EnumMap<>(Puzzle.Order.class);
		ROTATION_INDICATOR_RADIUS_FRACTIONS.put(Puzzle.Order._2, 0.25);
		ROTATION_INDICATOR_RADIUS_FRACTIONS.put(Puzzle.Order._3, 0.278);
		ROTATION_INDICATOR_RADIUS_FRACTIONS.put(Puzzle.Order._4, 0.25);
		ROTATION_INDICATOR_RADIUS_FRACTIONS.put(Puzzle.Order._5, 0.3);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PuzzlePane()
	{
		// Create grid
		grid = new Canvas();
		getChildren().add(grid);

		// Select cell when primary mouse button is pressed on grid
		grid.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->
		{
			Puzzle puzzle = document.puzzle();
			if (puzzle.editable() && (event.getButton() == MouseButton.PRIMARY))
			{
				// Select cell
				double cellSize = grid.getWidth() / (double)numColumns;
				int x = (int)Math.floor(event.getX() / cellSize);
				int y = (int)Math.floor(event.getY() / cellSize);
				int cellIndex = y * numColumns + x;
				selectedIndex(cellIndex);

				// Display pop-up for selecting an available value
				if (event.isControlDown() && (symmetry == null))
				{
					// Create pop-up
					Popup popUp = createValueSelectionPopUp(false);

					// Display pop-up
					popUp.show(SceneUtils.getWindow(this), event.getScreenX() + 2.0, event.getScreenY() + 2.0);
				}
			}
		});

		// Handle event when primary mouse button is double-clicked on grid
		grid.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
		{
			if (document.puzzle().editable() && (event.getButton() == MouseButton.PRIMARY)
					&& (event.getClickCount() == 2))
				onGridDoubleClick(event);
		});

		// Redraw grid when theme changes
		themeListener = observable -> redraw();
		StyleManager.INSTANCE.themeProperty().addListener(new WeakInvalidationListener(themeListener));
	}

	//------------------------------------------------------------------

	public PuzzlePane(
		PuzzleDocument	document)
	{
		// Call alternative constructor
		this();

		// Set document on puzzle pane
		document(document, newState());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void setFont(
		FontInfo	fontInfo)
	{
		PuzzlePane.fontInfo = fontInfo;
	}

	//------------------------------------------------------------------

	public static State newState()
	{
		return new State(0, Collections.emptyList(), Collections.emptyList(), null);
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
	protected void layoutChildren()
	{
		// Update dimensions of grid
		double width = getWidth();
		double height = getHeight();
		double gridSize = calcGridSize(calcCellSize(width, height));
		grid.setWidth(gridSize);
		grid.setHeight(gridSize);

		// Update location of grid
		double x = Math.floor(0.5 * (width - gridSize));
		double y = Math.floor(0.5 * (height - gridSize));
		grid.relocate(x, y);

		// Redraw grid
		redraw();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int numRows()
	{
		return numRows;
	}

	//------------------------------------------------------------------

	public int numColumns()
	{
		return numColumns;
	}

	//------------------------------------------------------------------

	public int selectedIndex()
	{
		return selectedIndex;
	}

	//------------------------------------------------------------------

	public void selectedIndex(
		int	index)
	{
		if (selectedIndex != index)
		{
			selectedIndex = index;
			redraw();
			SudokuGeneratorApp.instance().updateMenuItems();
		}
	}

	//------------------------------------------------------------------

	public void highlightIndices(
		int...	indices)
	{
		highlightedIndices.clear();
		for (int index : indices)
			highlightedIndices.add(index);
		redraw();
	}

	//------------------------------------------------------------------

	public void clearHighlightIndices()
	{
		if (!highlightedIndices.isEmpty())
		{
			highlightedIndices.clear();
			redraw();
		}
	}

	//------------------------------------------------------------------

	public Symmetry symmetry()
	{
		return symmetry;
	}

	//------------------------------------------------------------------

	public void symmetry(
		Symmetry	symmetry)
	{
		if (this.symmetry != symmetry)
		{
			this.symmetry = symmetry;
			redraw();
		}
	}

	//------------------------------------------------------------------

	public void incrementSelectionRow(
		int	increment)
	{
		if (document.puzzle().editable())
		{
			int row = Math.min(Math.max(0, selectedIndex / numColumns + increment), numRows - 1);
			selectedIndex(row * numColumns + selectedIndex % numColumns);
		}
	}

	//------------------------------------------------------------------

	public void incrementSelectionColumn(
		int	increment)
	{
		if (document.puzzle().editable())
		{
			int column = Math.min(Math.max(0, selectedIndex % numColumns + increment), numColumns - 1);
			selectedIndex(selectedIndex / numColumns * numColumns + column);
		}
	}

	//------------------------------------------------------------------

	public Bounds gridBoundsOnScreen()
	{
		return grid.localToScreen(grid.getLayoutBounds());
	}

	//------------------------------------------------------------------

	public Bounds cellBoundsOnScreen(
		int	index)
	{
		Bounds bounds = gridBoundsOnScreen();
		double cellSize = calcCellSize(grid.getWidth(), grid.getHeight());

		double dx = indexToOffset(index % numColumns, cellSize);
		double dy = indexToOffset(index / numColumns, cellSize);
		return new BoundingBox(bounds.getMinX() + dx, bounds.getMinY() + dy, cellSize, cellSize);
	}

	//------------------------------------------------------------------

	public WritableImage gridImage(
		double	scaleX,
		double	scaleY)
	{
		SnapshotParameters params = new SnapshotParameters();
		params.setTransform(Transform.scale(scaleX, scaleY));
		return grid.snapshot(params, null);
	}

	//------------------------------------------------------------------

	public void invalidateFontSizes()
	{
		fontSizes.clear();
	}

	//------------------------------------------------------------------

	public void invalidateSymbolSize()
	{
		ValueSelectionPane.symbolSize = 0.0;
	}

	//------------------------------------------------------------------

	public Popup createValueSelectionPopUp(
		boolean	keyboardControl)
	{
		// Create pop-up
		Popup popUp = new Popup();
		popUp.setAutoHide(true);

		// Create value-selection pane and set it as content of pop-up
		Puzzle puzzle = document.puzzle();
		int cellIndex = selectedIndex;
		ValueSelectionPane valueSelectionPane = new ValueSelectionPane(keyboardControl, cellIndex, value ->
		{
			// Hide pop-up
			popUp.hide();

			// Set selected value on puzzle
			byte oldValue = puzzle.value(cellIndex);
			if (oldValue != value)
			{
				puzzle.setValue(cellIndex, value);
				redraw();
				document.cellValueChanged(cellIndex, oldValue, value);
			}
		});
		popUp.getContent().add(valueSelectionPane);

		// Add style sheet to scene of pop-up when pop-up is displayed
		popUp.setOnShowing(event -> StyleManager.INSTANCE.addStyleSheet(popUp.getScene()));

		// Return pop-up
		return popUp;
	}

	//------------------------------------------------------------------

	public State state()
	{
		return new State(selectedIndex, highlightedIndices, fontSizes, symmetry);
	}

	//------------------------------------------------------------------

	public void document(
		PuzzleDocument	document,
		State			state)
	{
		// Update instance variables
		this.document = document;
		order = document.puzzle().order();
		numRows = numColumns = order * order;
		totalGridLineWidth = (double)(numColumns + 1) * GRID_LINE_WIDTH_0 + (double)(order + 1) * DELTA_GRID_LINE_WIDTH;
		selectedIndex = state.selectedIndex;
		highlightedIndices = new ArrayList<>(state.highlightedIndices);
		fontSizes = new ArrayList<>(state.fontSizes);
		symmetry = state.symmetry;

		// Set properties
		setMinSize(totalGridLineWidth, totalGridLineWidth);

		// Redraw grid
		requestLayout();
	}

	//------------------------------------------------------------------

	protected void redraw()
	{
		// Get graphics context of grid and save it
		GraphicsContext gc = grid.getGraphicsContext2D();
		gc.save();

		// Calculate size of a cell and size of grid
		double width = grid.getWidth();
		double height = grid.getHeight();
		double cellSize = calcCellSize(width, height);
		double gridSize = calcGridSize(cellSize);

		// Create function to convert horizontal or vertical index of cell to offset from left or top of grid
		IFunction1<Double, Integer> indexToOffset = index -> indexToOffset(index, cellSize);

		// If there are cells, draw grid, lines of symmetry and cells ...
		if (cellSize > 0.0)
		{
			// Fill background of grid
			gc.setFill(getColour(ColourKey.BACKGROUND));
			gc.fillRect(GRID_LINE_WIDTH_1, GRID_LINE_WIDTH_1, gridSize - 2.0 * GRID_LINE_WIDTH_1,
						gridSize - 2.0 * GRID_LINE_WIDTH_1);

			// If there are no highlighted cells, fill background of selected cell ...
			Puzzle puzzle = document.puzzle();
			if (highlightedIndices.isEmpty())
			{
				if (puzzle.editable() && (selectedIndex >= 0) && hasFocus())
				{
					double x = indexToOffset.invoke(selectedIndex % numColumns);
					double y = indexToOffset.invoke(selectedIndex / numColumns);
					gc.setFill(getColour(
							hasFocus() ? ColourKey.BACKGROUND_SELECTED_FOCUSED : ColourKey.BACKGROUND_SELECTED));
					gc.fillRect(x, y, cellSize, cellSize);
				}
			}

			// ... otherwise, fill backgrounds of highlighted cells
			else
			{
				gc.setFill(getColour(ColourKey.BACKGROUND_HIGHLIGHTED));
				for (int index : highlightedIndices)
				{
					double x = indexToOffset.invoke(index % numColumns);
					double y = indexToOffset.invoke(index / numColumns);
					gc.fillRect(x, y, cellSize, cellSize);
				}
			}

			// Draw grid lines
			gc.setLineCap(StrokeLineCap.BUTT);
			gc.setStroke(getColour(hasFocus() ? ColourKey.GRID_FOCUSED : ColourKey.GRID));
			for (int i = 0; i <= numColumns; i++)
			{
				double c = indexToOffset.invoke(i);
				if (i % order == 0)
				{
					c -= 0.5 * GRID_LINE_WIDTH_1;
					gc.setLineWidth(GRID_LINE_WIDTH_1);
				}
				else
				{
					c -= 0.5 * GRID_LINE_WIDTH_0;
					gc.setLineWidth(GRID_LINE_WIDTH_0);
				}
				gc.strokeLine(c, 0.0, c, gridSize);
				gc.strokeLine(0.0, c, gridSize, c);
			}

			// Draw lines of symmetry
			if (symmetry != null)
			{
				Color colour = getColour(hasFocus() ? ColourKey.SYMMETRY_LINE_FOCUSED : ColourKey.SYMMETRY_LINE);
				gc.setStroke(colour);
				gc.setFill(colour);
				gc.setLineWidth(SYMMETRY_LINE_WIDTH_0);

				double c1 = GRID_LINE_WIDTH_1;
				double c2 = gridSize - GRID_LINE_WIDTH_1;
				double cMid = 0.5 * (c1 + c2);
				switch (symmetry)
				{
					case NONE:
						// do nothing
						break;

					case REFLECTION_V:
						if (puzzle.order() % 2 == 0)
							gc.setLineWidth(SYMMETRY_LINE_WIDTH_1);
						gc.strokeLine(cMid, c1, cMid, c2);
						break;

					case REFLECTION_H:
						if (puzzle.order() % 2 == 0)
							gc.setLineWidth(SYMMETRY_LINE_WIDTH_1);
						gc.strokeLine(c1, cMid, c2, cMid);
						break;

					case REFLECTION_VH:
						if (puzzle.order() % 2 == 0)
							gc.setLineWidth(SYMMETRY_LINE_WIDTH_1);
						gc.strokeLine(cMid, c1, cMid, c2);
						gc.strokeLine(c1, cMid, c2, cMid);
						break;

					case REFLECTION_DIAGONAL_TL:
						gc.strokeLine(c1, c1, c2, c2);
						break;

					case REFLECTION_DIAGONAL_TR:
						gc.strokeLine(c2, c1, c1, c2);
						break;

					case REFLECTION_DIAGONALS:
						gc.strokeLine(c1, c1, c2, c2);
						gc.strokeLine(c2, c1, c1, c2);
						break;

					case ROTATION_2:
					{
						// Draw arcs
						double radius = ROTATION_INDICATOR_RADIUS_FRACTIONS.get(puzzle.puzzleOrder()) * gridSize;
						double d = 2.0 * radius;
						double c = 0.5 * gridSize;
						double a = c - radius;
						double angle = 0.0;
						int numArcs = 2;
						for (int i = 0; i < numArcs; i++)
						{
							gc.strokeArc(a, a, d, d, angle, ROTATION2_INDICATOR_ANGLE, ArcType.OPEN);
							angle += 360.0 / (double)numArcs;
						}

						// Draw arrowheads
						int numVertices = 3;
						double[] x = new double[numVertices];
						double[] y = new double[numVertices];

						double d1 = ROTATION_INDICATOR_ARROWHEAD_FACTOR1 * gridSize;
						double d2 = ROTATION_INDICATOR_ARROWHEAD_FACTOR2 * gridSize;
						double d3 = ROTATION_INDICATOR_ARROWHEAD_FACTOR3 * gridSize;
						double d4 = ROTATION_INDICATOR_ARROWHEAD_FACTOR4 * gridSize;

						x[0] = a + d1;
						x[1] = x[0] - d2;
						x[2] = x[0] + d2;
						y[0] = c - d3;
						y[1] = y[2] = y[0] + d4;
						gc.fillPolygon(x, y, numVertices);

						x[0] = a + d - d1;
						x[1] = x[0] - d2;
						x[2] = x[0] + d2;
						y[0] = c + d3;
						y[1] = y[2] = y[0] - d4;
						gc.fillPolygon(x, y, numVertices);
						break;
					}

					case ROTATION_4:
					{
						// Draw arcs
						double radius = ROTATION_INDICATOR_RADIUS_FRACTIONS.get(puzzle.puzzleOrder()) * gridSize;
						double d = 2.0 * radius;
						double c = 0.5 * gridSize;
						double a = c - radius;
						double angle = 0.0;
						int numArcs = 4;
						for (int i = 0; i < numArcs; i++)
						{
							gc.strokeArc(a, a, d, d, angle, ROTATION4_INDICATOR_ANGLE, ArcType.OPEN);
							angle += 360.0 / (double)numArcs;
						}

						// Draw arrowheads
						int numVertices = 3;
						double[] x = new double[numVertices];
						double[] y = new double[numVertices];

						double d1 = ROTATION_INDICATOR_ARROWHEAD_FACTOR1 * gridSize;
						double d2 = ROTATION_INDICATOR_ARROWHEAD_FACTOR2 * gridSize;
						double d3 = ROTATION_INDICATOR_ARROWHEAD_FACTOR3 * gridSize;
						double d4 = ROTATION_INDICATOR_ARROWHEAD_FACTOR4 * gridSize;

						x[0] = a + d1;
						x[1] = x[0] - d2;
						x[2] = x[0] + d2;
						y[0] = c - d3;
						y[1] = y[2] = y[0] + d4;
						gc.fillPolygon(x, y, numVertices);

						x[0] = a + d - d1;
						x[1] = x[0] - d2;
						x[2] = x[0] + d2;
						y[0] = c + d3;
						y[1] = y[2] = y[0] - d4;
						gc.fillPolygon(x, y, numVertices);

						x[0] = c + d3;
						x[1] = x[2] = x[0] - d4;
						y[0] = a + d1;
						y[1] = y[0] - d2;
						y[2] = y[0] + d2;
						gc.fillPolygon(x, y, numVertices);

						x[0] = c - d3;
						x[1] = x[2] = x[0] + d4;
						y[0] = a + d - d1;
						y[1] = y[0] - d2;
						y[2] = y[0] + d2;
						gc.fillPolygon(x, y, numVertices);
						break;
					}
				}
			}

			// Get font size
			FontSize fontSize = findFontSize(cellSize);

			// If cells are too small for text, draw placeholders in cells instead of symbols ...
			if (fontSize == null)
			{
				double size = cellSize / 3.0;
				gc.setFill(getColour(puzzle.editable() ? ColourKey.TEXT_EDITABLE : ColourKey.TEXT));
				for (int i = 0; i < numRows; i++)
				{
					double y = indexToOffset.invoke(i) + size;
					for (int j = 0; j < numColumns; j++)
					{
						int value = puzzle.value(i, j) & 0xFF;
						if (value > 0)
						{
							double x = indexToOffset.invoke(j) + size;
							gc.fillOval(x, y, size, size);
						}
					}
				}
			}

			// ... otherwise, draw symbols in cells
			else
			{
				Font font = fontSize.font();
				gc.setFont(font);
				gc.setTextBaseline(VPos.TOP);
				gc.setFill(getColour(puzzle.editable() ? ColourKey.TEXT_EDITABLE : ColourKey.TEXT));

				char[] symbols = puzzle.symbols();
				for (int i = 0; i < numRows; i++)
				{
					double y = indexToOffset.invoke(i) + 0.5 * (cellSize - fontSize.maxGlyphHeight);
					for (int j = 0; j < numColumns; j++)
					{
						int value = puzzle.value(i, j) & 0xFF;
						if (value > 0)
						{
							double x = indexToOffset.invoke(j);
							String text = Character.toString(symbols[value - 1]);
							gc.fillText(text, x + 0.5 * (cellSize - TextUtils.textWidth(font, text)), y);
						}
					}
				}
			}
		}

		// ... otherwise, fill grid
		else
		{
			gc.setFill(getColour(hasFocus() ? ColourKey.GRID_FOCUSED : ColourKey.GRID));
			gc.fillRect(0.0, 0.0, gridSize, gridSize);
		}

		// Restore graphics context of grid
		gc.restore();
	}

	//------------------------------------------------------------------

	protected void onGridDoubleClick(
		MouseEvent	event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private boolean hasFocus()
	{
		Parent parent = getParent();
		return (parent != null) && parent.getProperties().containsKey(FOCUSABLE_PARENT) && parent.isFocused();
	}

	//------------------------------------------------------------------

	private double calcCellSize(
		double	width,
		double	height)
	{
		return Math.max(0.0, Math.floor((Math.min(width, height) - totalGridLineWidth) / (double)numColumns));
	}

	//------------------------------------------------------------------

	private double calcGridSize(
		double	cellSize)
	{
		return totalGridLineWidth + (double)numColumns * cellSize;
	}

	//------------------------------------------------------------------

	private double indexToOffset(
		int		index,
		double	cellSize)
	{
		return (double)index * cellSize + (double)(index + 1) * GRID_LINE_WIDTH_0
					+ (double)(index / order + 1) * DELTA_GRID_LINE_WIDTH;
	}

	//------------------------------------------------------------------

	private FontSize findFontSize(
		double	cellSize)
	{
		// Initialise font sizes, if necessary
		if (fontSizes.isEmpty())
		{
			for (int size : FONT_SIZES)
				fontSizes.add(FontSize.init(size));
		}

		// Find font size for given cell size
		double maxGlyphWidth = cellSize * HORIZONTAL_GLYPH_FRACTION;
		double maxGlyphHeight = cellSize * VERTICAL_GLYPH_FRACTION;
		char[] symbols = document.puzzle().symbols();
		int index = 0;
		while (index < fontSizes.size())
		{
			FontSize fontSize = fontSizes.get(index);
			if (fontSize.maxGlyphHeight < 0.0)
			{
				double maxWidth = 0.0;
				double maxHeight = 0.0;
				Text node = new Text();
				node.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
				node.setFont(fontSize.font());
				for (char symbol : symbols)
				{
					node.setText(Character.toString(symbol));
					Bounds bounds = node.getLayoutBounds();
					maxWidth = Math.max(maxWidth, bounds.getWidth());
					maxHeight = Math.max(maxHeight, bounds.getHeight());
				}
				fontSize = new FontSize(fontSize.size, maxWidth, maxHeight);
				fontSizes.set(index, fontSize);
			}
			if ((fontSize.maxGlyphWidth > maxGlyphWidth) || (fontSize.maxGlyphHeight > maxGlyphHeight))
				break;
			++index;
		}
		return (index == 0) ? null : fontSizes.get(index - 1);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: PUZZLE-PANE STATE


	public record State(
		int				selectedIndex,
		List<Integer>	highlightedIndices,
		List<FontSize>	fontSizes,
		Symmetry		symmetry)
	{ }

	//==================================================================


	// RECORD: FONT SIZE


	private record FontSize(
		int		size,
		double	maxGlyphWidth,
		double	maxGlyphHeight)
	{

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static FontSize init(
			int	size)
		{
			return new FontSize(size, -1.0, -1.0);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Font font()
		{
			return (fontInfo == null) ? Font.font((double)size) : fontInfo.font((double)size);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: VALUE-SELECTION PANE


	private class ValueSelectionPane
		extends VBox
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	BORDER_WIDTH	= 2.0;
		private static final	double	GRID_LINE_WIDTH	= 1.0;

		private static final	double	CELL_MARGIN	= 2.0;

		private static final	Insets	CLEAR_CELL_PANE_PADDING	= new Insets(4.0);

	////////////////////////////////////////////////////////////////////
	//  Class variables
	////////////////////////////////////////////////////////////////////

		private static	double	symbolSize;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	double	cellSize;
		private	double	gridSize;
		private	int		available;
		private	int		hoveredIndex;
		private	int		selectedIndex;
		private	Font	font;
		private	Canvas	grid;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ValueSelectionPane(
			boolean					keyboardControl,
			int						cellIndex,
			IProcedure1<Integer>	onSelected)
		{
			// Initialise instance variables
			hoveredIndex = -1;
			selectedIndex = -1;
			font = FontUtils.defaultFont(1.25);

			// Set properties
			setAlignment(Pos.TOP_CENTER);
			setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.VALUE_SELECTOR_BORDER), BORDER_WIDTH));
			getStyleClass().add(StyleClass.VALUE_SELECTION_PANE);

			// Initialise bit array of available values
			Puzzle puzzle = document.puzzle();
			available = puzzle.availableValues(cellIndex);
			byte cellValue = puzzle.value(cellIndex);
			if (cellValue > 0)
				available |= 1 << cellValue - 1;

			// Calculate size of largest symbol glyph
			if (symbolSize == 0.0)
			{
				double maxSize = 0.0;
				for (char symbol : puzzle.symbols())
				{
					Text node = new Text(Character.toString(symbol));
					node.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
					node.setFont(font);
					Bounds bounds = node.getLayoutBounds();
					maxSize = Math.max(maxSize, bounds.getWidth());
					maxSize = Math.max(maxSize, bounds.getHeight());
				}
				symbolSize = Math.ceil(maxSize);
			}

			// Initialise remaining instance variables
			cellSize = 2.0 * CELL_MARGIN + symbolSize;
			gridSize = (double)(order - 1) * GRID_LINE_WIDTH + (double)order * cellSize;
			grid = new Canvas(gridSize, gridSize);

			// Add grid to pane
			getChildren().add(grid);

			// Case: control navigation and selection with keyboard
			if (keyboardControl)
			{
				// Initialise selected cell
				selectedIndex = 0;

				// Request focus on this pane
				setFocusTraversable(true);
				requestFocus();

				// Create procedure to increment or decrement row of selected cell
				IProcedure1<Integer> incSelectionRow = increment ->
				{
					int row = Math.min(Math.max(0, selectedIndex / order + increment), order - 1);
					selectedIndex = row * order + selectedIndex % order;
					redraw();
				};

				// Create procedure to increment or decrement column of selected cell
				IProcedure1<Integer> incSelectionColumn = increment ->
				{
					int column = Math.min(Math.max(0, selectedIndex % order + increment), order - 1);
					selectedIndex = selectedIndex / order * order + column;
					redraw();
				};

				// Handle 'key pressed' events
				addEventHandler(KeyEvent.KEY_PRESSED, event ->
				{
					boolean handled = true;
					switch (event.getCode())
					{
						case UP    -> incSelectionRow.invoke(-1);
						case DOWN  -> incSelectionRow.invoke(1);
						case LEFT  -> incSelectionColumn.invoke(-1);
						case RIGHT -> incSelectionColumn.invoke(1);
						case ENTER ->
						{
							if (valueAvailable(selectedIndex))
								onSelected.invoke(selectedIndex + 1);
						}
						default    -> handled = false;
					}
					if (handled)
						event.consume();
				});

				// Handle 'key typed' events
				addEventHandler(KeyEvent.KEY_TYPED, event ->
				{
					int value = puzzle.valueForKeyTyped(event);
					if (value > 0)
					{
						if (valueAvailable(value - 1))
							onSelected.invoke(value);
						event.consume();
					}
				});
			}

			// Case: control navigation and selection with mouse
			else
			{
				// Add 'clear cell' pane if target cell is not empty
				if (cellValue > 0)
				{
					// Create 'clear cell' icon
					Group clearIcon = Icons.clear01(getColour(ColourKey.CLEAR_CELL_ICON_DISC),
													getColour(ColourKey.CLEAR_CELL_ICON_CROSS));

					// Create 'clear cell' pane
					StackPane clearCellPane = new StackPane(clearIcon);
					clearCellPane.setPadding(CLEAR_CELL_PANE_PADDING);
					clearCellPane.setBackground(SceneUtils
							.createColouredBackground(getColour(ColourKey.VALUE_SELECTOR_BACKGROUND)));
					clearCellPane.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.VALUE_SELECTOR_GRID)));
					clearCellPane.getStyleClass().add(StyleClass.CLEAR_CELL_PANE);

					// Set handler for 'mouse pressed' events
					clearCellPane.setOnMousePressed(event -> onSelected.invoke(0));

					// Add 'clear cell' pane to parent
					getChildren().add(clearCellPane);
				}

				// Create procedure to update hovered cell in response to mouse event
				IProcedure1<MouseEvent> updateHovered = event ->
				{
					// Update index of hovered cell
					double cellSize = gridSize / (double)order;
					int x = (int)Math.floor(event.getX() / cellSize);
					int y = (int)Math.floor(event.getY() / cellSize);
					hoveredIndex = ((x >= 0) && (x < order) && (y >= 0) && (y < order)) ? y * order + x : -1;

					// Redraw grid
					redraw();
				};

				// Set handlers for mouse events
				grid.setOnMouseEntered(event -> updateHovered.invoke(event));
				grid.setOnMouseExited(event -> updateHovered.invoke(event));
				grid.setOnMouseMoved(event -> updateHovered.invoke(event));
				grid.setOnMousePressed(event ->
				{
					updateHovered.invoke(event);
					if ((hoveredIndex >= 0) && valueAvailable(hoveredIndex))
						onSelected.invoke(hoveredIndex + 1);
				});
			}

			// Redraw grid
			redraw();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private boolean valueAvailable(
			int	cellIndex)
		{
			return ((available & 1 << cellIndex) != 0);
		}

		//--------------------------------------------------------------

		private void redraw()
		{
			// Get graphics context of grid and save it
			GraphicsContext gc = grid.getGraphicsContext2D();
			gc.save();

			// Fill background of grid
			gc.setFill(getColour(ColourKey.VALUE_SELECTOR_BACKGROUND));
			gc.fillRect(0.0, 0.0, gridSize, gridSize);

			// Create function to convert horizontal or vertical index of cell to offset from left or top of grid
			IFunction1<Double, Integer> indexToOffset = index -> (double)index * (GRID_LINE_WIDTH + cellSize);

			// Fill background of selected cell
			if (selectedIndex >= 0)
			{
				gc.setFill(getColour(valueAvailable(selectedIndex)
											? ColourKey.VALUE_SELECTOR_BACKGROUND_HIGHLIGHTED
											: ColourKey.VALUE_SELECTOR_BACKGROUND_DISABLED));
				double x = indexToOffset.invoke(selectedIndex % order);
				double y = indexToOffset.invoke(selectedIndex / order);
				gc.fillRect(x, y, cellSize, cellSize);
			}

			// Fill background of hovered cell
			else if ((hoveredIndex >= 0) && valueAvailable(hoveredIndex))
			{
				gc.setFill(getColour(ColourKey.VALUE_SELECTOR_BACKGROUND_HIGHLIGHTED));
				double x = indexToOffset.invoke(hoveredIndex % order);
				double y = indexToOffset.invoke(hoveredIndex / order);
				gc.fillRect(x, y, cellSize, cellSize);
			}

			// Draw grid lines
			gc.setLineCap(StrokeLineCap.BUTT);
			gc.setStroke(getColour(ColourKey.VALUE_SELECTOR_GRID));
			for (int i = 1; i < order; i++)
			{
				double c = indexToOffset.invoke(i) - 0.5 * GRID_LINE_WIDTH;
				gc.strokeLine(c, 0.0, c, gridSize);
				gc.strokeLine(0.0, c, gridSize, c);
			}

			// Draw symbols in cells
			gc.setTextBaseline(VPos.TOP);
			gc.setFont(font);
			char[] symbols = document.puzzle().symbols();
			for (int i = 0; i < order; i++)
			{
				for (int j = 0; j < order; j++)
				{
					int index = i * order + j;
					String text = Character.toString(symbols[index]);
					Text node = new Text(text);
					node.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
					node.setFont(font);
					Bounds bounds = node.getLayoutBounds();
					double x = indexToOffset.invoke(j);
					double y = indexToOffset.invoke(i);
					gc.setFill(getColour(((available & 1 << index) == 0)
									? ColourKey.VALUE_SELECTOR_TEXT_DISABLED
									: ColourKey.VALUE_SELECTOR_TEXT));
					gc.fillText(text, x + 0.5 * (cellSize - bounds.getWidth()),
								y + 0.5 * (cellSize - bounds.getHeight()));
				}
			}

			// Restore graphics context of grid
			gc.restore();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
