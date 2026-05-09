/*====================================================================*\

PuzzleView.java

Class: view of a sudoku puzzle.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import javafx.geometry.Bounds;
import javafx.geometry.Insets;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;

import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.StackPane;

import javafx.stage.Popup;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

//----------------------------------------------------------------------


// CLASS: VIEW OF A SUDOKU PUZZLE


class PuzzleView
	extends StackPane
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The padding around a puzzle view. */
	private static final	Insets	PADDING	= new Insets(8.0);

	/** The key combination that triggers the value-selection pop-up. */
	private static final	KeyCombination	KEY_COMBO_SELECT_VALUE	=
			new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	PuzzleDocument	document;
	private	PuzzlePane		puzzlePane;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PuzzleView()
	{
		// Set properties
		setPadding(PADDING);
		setFocusTraversable(true);
		getProperties().put(PuzzlePane.FOCUSABLE_PARENT, new Object());

		// Create puzzle pane and add it to this view
		puzzlePane = new PuzzlePane();
		getChildren().add(puzzlePane);

		// Create temporary document and set it on this view
		document();

		// Redraw puzzle when this view gains or loses focus
		focusedProperty().addListener(observable -> puzzlePane.redraw());

		// Handle 'key pressed' events
		addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			// Get puzzle
			Puzzle puzzle = document.puzzle();

			// Display pop-up for selection of cell value
			if (KEY_COMBO_SELECT_VALUE.match(event))
			{
				// Create value-selection pop-up and display it below selected cell
				if (puzzle.editable())
					selectCellValue(true);

				// Consume event and return
				event.consume();
				return;
			}

			// Ignore key press if modifier key was also pressed
			if (event.isShiftDown() || event.isControlDown() || event.isAltDown())
				return;

			// Assume key event was handled
			boolean handled = true;

			// If puzzle is editable, handle navigation keys and keys that clear a cell ...
			if (puzzle.editable())
			{
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
			}

			// ... otherwise, handle keys that move through solutions
			else if (document.hasSolutions())
			{
				switch (event.getCode())
				{
					case LEFT  -> document.showPreviousSolution();
					case RIGHT -> document.showNextSolution();
					default    -> handled = false;
				}
			}

			// If key event was handled, consume it
			if (handled)
				event.consume();
		});

		// Handle 'key typed' events
		addEventHandler(KeyEvent.KEY_TYPED, event ->
		{
			Puzzle puzzle = document.puzzle();
			if (puzzle.editable())
			{
				int value = puzzle.valueForKeyTyped(event);
				if (value > 0)
				{
					setSelectionValue(value);
					event.consume();
				}
			}
		});

		// Display context menu on request
		addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event ->
		{
			// Create context menu
			ContextMenu menu = new ContextMenu();

			// Add items to menu
			menu.getItems().addAll(
				PuzzleDocument.Command.UNDO.newMenuItem(),
				PuzzleDocument.Command.REDO.newMenuItem(),
				new SeparatorMenuItem(),
				PuzzleDocument.Command.CHOOSE_SYMBOL.newMenuItem(),
				new SeparatorMenuItem(),
				PuzzleDocument.Command.FIND_ENTRY.newMenuItem(),
				PuzzleDocument.Command.SOLVE.newMenuItem(),
				PuzzleDocument.Command.CLOSE_SOLUTIONS.newMenuItem(),
				new SeparatorMenuItem(),
				SudokuGeneratorApp.Command.DUPLICATE.newMenuItem(),
				PuzzleDocument.Command.GENERATE.newMenuItem(),
				PuzzleDocument.Command.CREATE_TEMPLATE.newMenuItem(),
				new SeparatorMenuItem(),
				PuzzleDocument.Command.COUNT_ENTRIES.newMenuItem(),
				PuzzleDocument.Command.CHECK.newMenuItem(),
				new SeparatorMenuItem(),
				PuzzleDocument.createTransformSubmenu(),
				new SeparatorMenuItem(),
				PuzzleDocument.Command.HOMOGENISE_ENTRIES.newMenuItem(),
				new SeparatorMenuItem(),
				PuzzleDocument.Command.CLEAR.newMenuItem(),
				PuzzleDocument.Command.DELETE_PROPERTIES.newMenuItem(),
				new SeparatorMenuItem(),
				PuzzleDocument.Command.COPY_TEXT.newMenuItem(),
				SudokuGeneratorApp.Command.COPY_IMAGE.newMenuItem(),
				SudokuGeneratorApp.Command.PASTE.newMenuItem(),
				new SeparatorMenuItem(),
				PuzzleDocument.Command.EDIT_SYMBOLS.newMenuItem()
			);

			// Update menu items
			SudokuGeneratorApp.instance().updateMenuItems(menu.getItems());

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

			// Display context menu
			menu.show(SceneUtils.getWindow(this), event.getScreenX(), event.getScreenY());
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int selectedIndex()
	{
		return puzzlePane.selectedIndex();
	}

	//------------------------------------------------------------------

	public void selectedIndex(
		int	index)
	{
		puzzlePane.selectedIndex(index);
	}

	//------------------------------------------------------------------

	public void highlightIndices(
		int...	indices)
	{
		puzzlePane.highlightIndices(indices);
	}

	//------------------------------------------------------------------

	public void clearHighlightIndices()
	{
		puzzlePane.clearHighlightIndices();
	}

	//------------------------------------------------------------------

	public void invalidateFontSizes()
	{
		puzzlePane.invalidateFontSizes();
	}

	//------------------------------------------------------------------

	public void invalidateSymbolSize()
	{
		puzzlePane.invalidateSymbolSize();
	}

	//------------------------------------------------------------------

	public void redraw()
	{
		puzzlePane.redraw();
	}

	//------------------------------------------------------------------

	public PuzzlePane.State state()
	{
		return puzzlePane.state();
	}

	//------------------------------------------------------------------

	public void document()
	{
		document(new PuzzleDocument(), PuzzlePane.newState());
	}

	//------------------------------------------------------------------

	public void document(
		PuzzleDocument		document,
		PuzzlePane.State	viewState)
	{
		// Update instance variables
		this.document = document;

		// Set this view on document
		document.view(this);

		// Set document and view state on puzzle pane
		puzzlePane.document(document, viewState);
	}

	//------------------------------------------------------------------

	public void selectCellValue(
		boolean	keyboardControl)
	{
		// Create pop-up
		Popup popUp = puzzlePane.createValueSelectionPopUp(keyboardControl);

		// Display pop-up below selected cell
		int cellIndex = puzzlePane.selectedIndex();
		Bounds bounds = puzzlePane.cellBoundsOnScreen(cellIndex);
		Puzzle puzzle = document.puzzle();
		double dx = (cellIndex % puzzle.numColumns() % puzzle.order() == 0) ? 3.0 : 1.0;
		popUp.show(SceneUtils.getWindow(this), bounds.getMinX() - dx, bounds.getMaxY());
	}

	//------------------------------------------------------------------

	private void setSelectionValue(
		int	value)
	{
		int index = puzzlePane.selectedIndex();
		Puzzle puzzle = document.puzzle();
		byte oldValue = puzzle.value(index);
		if (oldValue != value)
		{
			puzzle.setValue(index, value);
			puzzlePane.redraw();
			document.cellValueChanged(index, oldValue, value);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
