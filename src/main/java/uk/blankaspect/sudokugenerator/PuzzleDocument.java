/*====================================================================*\

PuzzleDocument.java

Class: document for a sudoku puzzle.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.nio.file.attribute.FileTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.atomic.AtomicLong;

import java.util.concurrent.locks.ReentrantLock;

import javafx.concurrent.Task;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Point2D;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import javafx.stage.Window;

import uk.blankaspect.common.collection.ArraySet;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;

import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.function.IProcedure3;

import uk.blankaspect.common.misc.EditList;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.dialog.ButtonInfo;
import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.MessageDialog;
import uk.blankaspect.ui.jfx.dialog.NotificationDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleProgressDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.task.AbstractSoftCancelTask;

//----------------------------------------------------------------------


// CLASS: DOCUMENT FOR A SUDOKU PUZZLE


class PuzzleDocument
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	enum SubmenuId
	{
		TRANSFORM
	}

	private static final	String	TEMP_NAME_PREFIX	= "unnamed-";

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR				= "...";
	private static final	String	NUM_ENTRIES_STR				= "Number of puzzle entries : ";
	private static final	String	NO_SOLUTION_FOUND_STR		= "No solution was found.";
	private static final	String	PUZZLE_STR					= "The puzzle ";
	private static final	String	NO_SOLUTION_STR				= "is valid but has no solution.";
	private static final	String	MULTIPLE_SOLUTIONS_STR		= "is valid but has multiple solutions.";
	private static final	String	UNIQUE_SOLUTION_STR			= "has a unique solution.";
	private static final	String	NO_UNIQUE_SOLUTION_STR		= "The puzzle does not have a unique solution.";
	private static final	String	COMPLETE_STR				= "is complete, with no conflicting entries.";
	private static final	String	CONFLICTING_ENTRIES_STR		= "contains conflicting entries.";
	private static final	String	FIND_ENTRY_QUESTION_STR		=
			"Do you want to find the entry for the selected cell?";
	private static final	String	SEARCHING_FOR_ENTRY_STR		= "Searching for entry";
	private static final	String	CHECK_PUZZLE_STR			= "Check puzzle";
	private static final	String	CHECKING_PUZZLE_STR			= "Checking puzzle";
	private static final	String	GENERATE_PUZZLE_STR			= "generate puzzle";
	private static final	String	CLEAR_PUZZLE_STR			= "clear puzzle";
	private static final	String	COPY_PUZZLE_STR				= "copy puzzle";
	private static final	String	TEXT_COPIED_STR				=
			"A textual representation of the puzzle was copied to the clipboard.";
	private static final	String	TRANSFORM_STR				= "Transform";

	/** Error messages. */
	private interface ErrorMsg
	{
		String	FAILED_TO_GET_FILE_TIMESTAMP =
				"Failed to get the timestamp of the file.";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int	unnamedIndex;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Puzzle			puzzle;
	private	Puzzle			savedPuzzle;
	private	EditList		editList;
	private	PuzzleView		view;
	private	List<Puzzle>	solutions;
	private	int				solutionIndex;
	private	Path			file;
	private	FileKind		fileKind;
	private	FileTime		timestamp;
	private	String			tempName;
	private	ReentrantLock	lock;
	private	Path			exportHtmlFile;
	private	Path			exportImageFile;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PuzzleDocument()
	{
		// Call alternative constructor
		this(new Puzzle(Puzzle.DEFAULT_ORDER, false), false);
	}

	//------------------------------------------------------------------

	public PuzzleDocument(
		Puzzle.Order	order)
	{
		// Call alternative constructor
		this(new Puzzle(order, true), true);
	}

	//------------------------------------------------------------------

	public PuzzleDocument(
		Puzzle	puzzle)
	{
		// Call alternative constructor
		this(puzzle, true);
	}

	//------------------------------------------------------------------

	public PuzzleDocument(
		Puzzle		puzzle,
		Path		file,
		FileKind	fileKind)
		throws FileException
	{
		// Call alternative constructor
		this(puzzle, false);

		// Initialise remaining instance variables
		this.file = file;
		this.fileKind = fileKind;
		timestamp = getTimestamp(file);
	}

	//------------------------------------------------------------------

	private PuzzleDocument(
		Puzzle	puzzle,
		boolean	hasTempName)
	{
		// Initialise instance variables
		this.puzzle = puzzle;
		editList = new EditList(SudokuGeneratorApp.instance().preferences().editHistoryMaxSize());
		solutions = Collections.emptyList();
		solutionIndex = -1;
		if (hasTempName)
			tempName = "<" + TEMP_NAME_PREFIX + ++unnamedIndex + ">";
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static FileTime getTimestamp(
		Path	file)
		throws FileException
	{
		try
		{
			return Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS);
		}
		catch (IOException e)
		{
			throw new FileException(ErrorMsg.FAILED_TO_GET_FILE_TIMESTAMP, file);
		}
	}

	//------------------------------------------------------------------

	public static Menu createTransformSubmenu()
	{
		Menu submenu = new Menu(TRANSFORM_STR);
		submenu.setUserData(SubmenuId.TRANSFORM);
		for (Command command : Command.TRANSFORM_COMMANDS)
			submenu.getItems().add(command.newMenuItem());
		return submenu;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Puzzle puzzle()
	{
		return puzzle;
	}

	//------------------------------------------------------------------

	public void view(
		PuzzleView	view)
	{
		this.view = view;
	}

	//------------------------------------------------------------------

	public Path file()
	{
		return file;
	}

	//------------------------------------------------------------------

	public void file(
		Path	file)
	{
		this.file = file;
	}

	//------------------------------------------------------------------

	public FileKind fileKind()
	{
		return fileKind;
	}

	//------------------------------------------------------------------

	public FileTime timestamp()
	{
		return timestamp;
	}

	//------------------------------------------------------------------

	public void timestamp(
		FileTime	timestamp)
	{
		this.timestamp = timestamp;
	}

	//------------------------------------------------------------------

	public ReentrantLock lock()
	{
		if (lock == null)
			lock = new ReentrantLock(true);
		return lock;
	}

	//------------------------------------------------------------------

	public Path exportHtmlFile()
	{
		return exportHtmlFile;
	}

	//------------------------------------------------------------------

	public void exportHtmlFile(
		Path	file)
	{
		exportHtmlFile = file;
	}

	//------------------------------------------------------------------

	public Path exportImageFile()
	{
		return exportImageFile;
	}

	//------------------------------------------------------------------

	public void exportImageFile(
		Path	file)
	{
		exportImageFile = file;
	}

	//------------------------------------------------------------------

	public String displayName()
	{
		return (file == null)
					? (tempName == null)
							? ""
							: tempName
					: file.getFileName().toString();
	}

	//------------------------------------------------------------------

	public String pathame()
	{
		return (file == null) ? null : PathUtils.absString(file);
	}

	//------------------------------------------------------------------

	public boolean hasSolutions()
	{
		return !solutions.isEmpty();
	}

	//------------------------------------------------------------------

	public SolutionInfo solutionInfo()
	{
		return new SolutionInfo(solutionIndex, solutions.size());
	}

	//------------------------------------------------------------------

	public String statusText()
	{
		return puzzle.hasProperty(Puzzle.Key.GENERATION_INFO) ? puzzle.property(Puzzle.Key.GENERATION_INFO) : "";
	}

	//------------------------------------------------------------------

	public boolean isChanged()
	{
		return editList.isChanged();
	}

	//------------------------------------------------------------------

	public void cellValueChanged(
		int	index,
		int	oldValue,
		int	newValue)
	{
		// Add edit to list
		editList.add(new EntryEdit(index, oldValue, newValue));

		// Update UI
		updateUI();
	}

	//------------------------------------------------------------------

	public void updateFile(
		Path		file,
		FileKind	fileKind)
		throws FileException
	{
		// Clear list of edits
		if (SudokuGeneratorApp.instance().preferences().editHistoryClearOnSave())
			editList.clear();
		else
			editList.reset();

		// Update instance variables
		this.file = file;
		if (fileKind != null)
			this.fileKind = fileKind;
		timestamp = getTimestamp(file);
	}

	//------------------------------------------------------------------

	public void updateSubmenu(
		Menu		submenu,
		SubmenuId	id)
	{
		// Update 'disabled' state of submenu
		submenu.setDisable(switch (id)
		{
			case TRANSFORM -> hasSolutions() || !puzzle.hasEntries();
		});
	}

	//------------------------------------------------------------------

	public void updateMenuItem(
		MenuItem	menuItem,
		Command		command)
	{
		// Update text of menu item
		String text = switch (command)
		{
			case REDO -> editList.canRedo() ? command.text + " " + editList.getRedo().text() : command.text;
			case UNDO -> editList.canUndo() ? command.text + " " + editList.getUndo().text() : command.text;
			default   -> null;
		};
		if (text != null)
			menuItem.setText(text);

		// Update 'disabled' state of menu item
		int selectedIndex = view.selectedIndex();
		menuItem.setDisable(switch (command)
		{
			case CHOOSE_SYMBOL      -> hasSolutions() || (selectedIndex < 0);
			case CLOSE_SOLUTIONS    -> !hasSolutions();
			case DELETE_PROPERTIES  -> hasSolutions() || !puzzle.hasProperties();
			case FIND_ENTRY         -> hasSolutions() || (selectedIndex < 0) || (puzzle.value(selectedIndex) > 0);
			case FLIP_HORIZONTALLY,
				 FLIP_VERTICALLY,
				 ROTATE_90_DEG_ACW,
				 ROTATE_90_DEG_CW,
				 ROTATE_180_DEG,
				 HOMOGENISE_ENTRIES -> hasSolutions() || !puzzle.hasEntries();
			case REDO               -> hasSolutions() || !editList.canRedo();
			case UNDO               -> hasSolutions() || !editList.canUndo();
			default                 -> hasSolutions();
		});

		// Update action-event handler of menu item
		EventHandler<ActionEvent> eventHandler = switch (command)
		{
			case CHECK              -> event -> onCheckPuzzle();
			case CHOOSE_SYMBOL      -> event -> onChooseSymbol();
			case CLEAR              -> event -> onClearPuzzle();
			case CLOSE_SOLUTIONS    -> event -> onCloseSolutions();
			case COPY_TEXT          -> event -> onCopyText();
			case COUNT_ENTRIES      -> event -> onCountEntries();
			case CREATE_TEMPLATE    -> event -> onCreateTemplate();
			case DELETE_PROPERTIES  -> event -> onDeleteProperties();
			case EDIT_SYMBOLS       -> event -> onEditSymbols();
			case FIND_ENTRY         -> event -> onFindEntry();
			case FLIP_HORIZONTALLY,
				 FLIP_VERTICALLY,
				 ROTATE_90_DEG_ACW,
				 ROTATE_90_DEG_CW,
				 ROTATE_180_DEG     -> event -> onTransformEntries(command);
			case GENERATE           -> event -> onGeneratePuzzle();
			case HOMOGENISE_ENTRIES -> event -> onHomogeniseEntries();
			case REDO               -> event -> onRedo();
			case SOLVE              -> event -> onSolvePuzzle();
			case UNDO               -> event -> onUndo();
		};
		menuItem.setOnAction(eventHandler);
	}

	//------------------------------------------------------------------

	public void showPreviousSolution()
	{
		if (solutionIndex > 0)
		{
			// Set puzzle to previous solution
			puzzle = solutions.get(--solutionIndex).clone();

			// Redraw view
			view.redraw();

			// Update UI
			updateUI();
		}
	}

	//------------------------------------------------------------------

	public void showNextSolution()
	{
		if (solutionIndex < solutions.size() - 1)
		{
			// Set puzzle to next solution
			puzzle = solutions.get(++solutionIndex).clone();

			// Redraw view
			view.redraw();

			// Update UI
			updateUI();
		}
	}

	//------------------------------------------------------------------

	public void copyPuzzle(
		Puzzle	source)
	{
		// Check puzzle order
		if (source.order() != puzzle.order())
			throw new IllegalArgumentException("Incorrect puzzle order");

		// Get entries and properties of source puzzle
		List<Puzzle.Entry> entries = source.entries();
		Map<Puzzle.Key, String> properties = source.properties();

		// Add edit to list
		editList.add(new EntriesPropertiesEdit(puzzle.entries(), puzzle.properties(), entries, properties,
											   COPY_PUZZLE_STR));

		// Set entries and properties on puzzle
		puzzle.setValues(entries);
		puzzle.properties(properties);
	}

	//------------------------------------------------------------------

	public boolean homogeniseEntries()
	{
		// Display dialog for selecting value
		Integer value = HomogeniseEntriesDialog.show(window(), puzzle);

		// If dialog was accepted, set all entries to selected value
		boolean accepted = (value != null);
		if (accepted)
		{
			// Get entries of puzzle
			List<Puzzle.Entry> entries = puzzle.entries();

			// Set all entries to selected value
			for (Puzzle.Entry entry : entries)
				puzzle.setValue(entry.index(), value);

			// Add edit to list
			editList.add(new EntriesEdit(entries, puzzle.entries(), Command.HOMOGENISE_ENTRIES.text.toLowerCase()));

			// Redraw view
			view.redraw();

			// Update UI
			updateUI();
		}

		// Return 'accepted' flag
		return accepted;
	}

	//------------------------------------------------------------------

	public void onUndo()
	{
		EditList.IEdit edit = editList.removeUndo();
		if (edit != null)
		{
			// Undo last edit
			edit.undo();

			// Redraw view
			view.redraw();

			// Update UI
			updateUI();
		}
	}

	//------------------------------------------------------------------

	public void onRedo()
	{
		EditList.IEdit edit = editList.removeRedo();
		if (edit != null)
		{
			// Redo last edit
			edit.redo();

			// Redraw view
			view.redraw();

			// Update UI
			updateUI();
		}
	}

	//------------------------------------------------------------------

	public void onChooseSymbol()
	{
		if (view.selectedIndex() >= 0)
			view.selectCellValue(false);
	}

	//------------------------------------------------------------------

	public void onFindEntry()
	{
		// Test whether an entry is selected
		int selectedIndex = view.selectedIndex();
		if ((selectedIndex < 0) || (puzzle.value(selectedIndex) > 0))
			return;

		// Search for conflicting entries
		Puzzle.IndexPair indices = puzzle.findConflictingEntries();

		// If there are no conflicting entries, search for entry for selected cell ...
		if (indices == null)
		{
			// Highlight selected cell
			view.highlightIndices(selectedIndex);

			// Create locator to align top centre of message dialog with top centre of main window
			SimpleDialog.ILocator locator = (width, height) ->
			{
				Window window = window();
				return new Point2D(window.getX() + 0.5 * (window.getWidth() - width), window.getY());
			};

			// Display message dialog to confirm search
			int result = new MessageDialog(window(), Command.FIND_ENTRY.text, MessageIcon32.QUESTION.get(),
										   FIND_ENTRY_QUESTION_STR, locator, ButtonInfo.right(SimpleDialog.OK_STR),
										   ButtonInfo.right(SimpleDialog.CANCEL_STR))
					.showDialog();

			// Remove highlighting from selected cell
			view.clearHighlightIndices();

			// If dialog was not accepted, return
			if (result != 0)
				return;

			// Create task to search for entry for selected cell
			Task<List<Puzzle>> task = new AbstractSoftCancelTask<>()
			{
				{
					updateTitle(Command.FIND_ENTRY.text);
					updateMessage(SEARCHING_FOR_ENTRY_STR + " " + ELLIPSIS_STR);
					updateProgress(-1, 1);
				}

				@Override
				protected List<Puzzle> call()
					throws Exception
				{
					AtomicLong solutionsCount = new AtomicLong();
					return puzzle.clone().solve(2, true, solutionsCount, this::isCancelled);
				}

				@Override
				protected void succeeded()
				{
					List<Puzzle> solutions = getValue();
					if (solutions.size() == 1)
					{
						// Get cell value from solution
						byte value = solutions.get(0).value(selectedIndex);

						// Set cell value
						puzzle.setValue(selectedIndex, value);

						// Add edit to list
						editList.add(new EntryEdit(selectedIndex, 0, value));

						// Redraw view
						view.redraw();

						// Update UI
						updateUI();
					}
					else
					{
						NotificationDialog.show(window(), getTitle(), MessageIcon32.WARNING.get(),
												NO_UNIQUE_SOLUTION_STR);
					}
				}

				@Override
				protected void failed()
				{
					if (!isCancelled())
						ErrorDialog.show(window(), getTitle(), getException());
				}
			};

			// Create progress dialog for task
			new SimpleProgressDialog(window(), task, SimpleProgressDialog.CancelMode.NO_INTERRUPT);

			// Execute task on background thread
			SudokuGeneratorApp.instance().executeTask(task);
		}

		// ... otherwise, highlight conflicting cells and report conflicting entries
		else
			reportConflictingCells(indices.i1(), indices.i2());
	}

	//------------------------------------------------------------------

	public void onSolvePuzzle()
	{
		// Test whether puzzle has solutions
		if (hasSolutions())
			return;

		// Search for conflicting entries
		Puzzle.IndexPair indices = puzzle.findConflictingEntries();

		// If there are no conflicting entries, search for solutions to puzzle ...
		if (indices == null)
		{
			// Display dialog for parameters for search for solutions; perform search
			List<Puzzle> result = SolutionDialog.show(window(), puzzle);

			// If search was complete, report 'no solutions found' or show first solution
			if (result != null)
			{
				// If no solutions were found, report result ...
				if (result.isEmpty())
				{
					NotificationDialog.show(window(), Command.SOLVE.text, MessageIcon32.INFORMATION.get(),
											NO_SOLUTION_FOUND_STR);
				}

				// ... otherwise, set solutions and show first solution
				else
				{
					// Prevent solutions from being edited
					for (Puzzle solution : result)
						solution.editable(false);

					// Save puzzle
					savedPuzzle = puzzle;

					// Set solutions
					solutions = result;

					// Show first solution
					solutionIndex = -1;
					showNextSolution();
				}
			}
		}

		// ... otherwise, highlight conflicting cells and report conflicting entries
		else
			reportConflictingCells(indices.i1(), indices.i2());
	}

	//------------------------------------------------------------------

	public void onCloseSolutions()
	{
		// Clear solutions and invalidate solution index
		solutions = Collections.emptyList();
		solutionIndex = -1;

		// Restore puzzle
		if (savedPuzzle != null)
		{
			// Restore puzzle and invalidate saved puzzle
			puzzle = savedPuzzle;
			savedPuzzle = null;

			// Redraw view
			view.redraw();
		}

		// Update UI
		updateUI();
	}

	//------------------------------------------------------------------

	public void onGeneratePuzzle()
	{
		// If document is template, create provisional document and generate puzzle on it ...
		if (fileKind == FileKind.TEMPLATE)
		{
			// Create provisional document
			PuzzleDocument document = new PuzzleDocument(puzzle.puzzleOrder());

			// Copy puzzle entries from this document to provisional document
			document.puzzle.setValues(puzzle.entries());

			// Display dialog for generating puzzle; if puzzle was generated, add provisional document to application
			if (GenerationDialog.show(window(), document.puzzle))
			{
				document.editList.setChanged();
				SudokuGeneratorApp.instance().addDocument(document);
			}
		}

		// ... otherwise, generate puzzle on this document
		else
		{
			// Get entries and properties of puzzle for edit
			List<Puzzle.Entry> entries = puzzle.entries();
			Map<Puzzle.Key, String> properties = puzzle.properties();

			// Display dialog for generating puzzle; if puzzle was generated, add edit to list and update view
			if (GenerationDialog.show(window(), puzzle))
			{
				// Add edit to list
				editList.add(new EntriesPropertiesEdit(entries, properties, puzzle.entries(), puzzle.properties(),
													   GENERATE_PUZZLE_STR));

				// Redraw view
				view.redraw();

				// Update UI
				updateUI();
			}
		}
	}

	//------------------------------------------------------------------

	public void onCreateTemplate()
	{
		// Display dialog for creating template
		List<Puzzle.Entry> entries = TemplateDialog.show(window(), puzzle);

		// If template was created, set its entries on puzzle
		if (entries != null)
		{
			// Get properties of puzzle for edit
			Map<Puzzle.Key, String> oldProperties = puzzle.properties();

			// Remove generation-information property from puzzle
			puzzle.removeProperty(Puzzle.Key.GENERATION_INFO);

			// Add edit to list
			editList.add(new EntriesPropertiesEdit(puzzle.entries(), oldProperties, entries, puzzle.properties(),
												   Command.CREATE_TEMPLATE.text.toLowerCase()));

			// Set entries on puzzle
			puzzle.setValues(entries);

			// Redraw view
			view.redraw();

			// Update UI
			updateUI();
		}
	}

	//------------------------------------------------------------------

	public void onCountEntries()
	{
		NotificationDialog.show(window(), Command.COUNT_ENTRIES.text, MessageIcon32.INFORMATION.get(),
								NUM_ENTRIES_STR + puzzle.numEntries());
	}

	//------------------------------------------------------------------

	public void onCheckPuzzle()
	{
		// Search for conflicting entries
		Puzzle.IndexPair indices = puzzle.findConflictingEntries();

		// If there are no conflicting entries, search for solutions to puzzle ...
		if (indices == null)
		{
			// Create task to search for solutions to puzzle
			Task<Integer> task = new AbstractSoftCancelTask<>()
			{
				{
					updateTitle(CHECK_PUZZLE_STR);
					updateMessage(CHECKING_PUZZLE_STR + " " + ELLIPSIS_STR);
					updateProgress(-1, 1);
				}

				@Override
				protected Integer call()
					throws Exception
				{
					AtomicLong solutionsCount = new AtomicLong();
					return puzzle.clone().solve(2, true, solutionsCount, this::isCancelled).size();
				}

				@Override
				protected void succeeded()
				{
					MessageIcon32 icon = null;
					String message = null;
					switch (getValue())
					{
						case 0:
							icon = MessageIcon32.WARNING;
							message = NO_SOLUTION_STR;
							break;

						case 1:
							icon = MessageIcon32.INFORMATION;
							message = puzzle.isComplete() ? COMPLETE_STR : UNIQUE_SOLUTION_STR;
							break;

						default:
							icon = MessageIcon32.WARNING;
							message = MULTIPLE_SOLUTIONS_STR;
							break;
					}
					NotificationDialog.show(window(), getTitle(), icon.get(), PUZZLE_STR + message);
				}

				@Override
				protected void failed()
				{
					if (!isCancelled())
						ErrorDialog.show(window(), getTitle(), getException());
				}
			};

			// Create progress dialog for task
			new SimpleProgressDialog(window(), task, SimpleProgressDialog.CancelMode.NO_INTERRUPT);

			// Execute task on background thread
			SudokuGeneratorApp.instance().executeTask(task);
		}

		// ... otherwise, highlight conflicting cells and report conflicting entries
		else
			reportConflictingCells(indices.i1(), indices.i2());
	}

	//------------------------------------------------------------------

	public void onDeleteProperties()
	{
		if (puzzle.hasProperties())
		{
			// Display dialog for selecting properties
			String text = Command.DELETE_PROPERTIES.text;
			Set<Puzzle.Key> keys = PropertySelectionDialog.show(window(), text, puzzle);

			// If properties were selected, remove them from puzzle
			if (keys != null)
			{
				// Get properties of puzzle for edit
				Map<Puzzle.Key, String> properties = puzzle.properties();

				// Remove properties
				for (Puzzle.Key key : keys)
					puzzle.removeProperty(key);

				// Add edit to list
				editList.add(new PropertiesEdit(properties, puzzle.properties(), text.toLowerCase()));

				// Update UI
				updateUI();
			}
		}
	}

	//------------------------------------------------------------------

	public void onClearPuzzle()
	{
		// Get entries and properties of puzzle for edit
		List<Puzzle.Entry> entries = puzzle.entries();
		Map<Puzzle.Key, String> properties = puzzle.properties();

		// If puzzle has entries or properties, clear them
		if (!entries.isEmpty() || !properties.isEmpty())
		{
			// Clear puzzle
			puzzle.clear();

			// Add edit to list
			editList.add(new EntriesPropertiesEdit(entries, properties, puzzle.entries(), puzzle.properties(),
												   CLEAR_PUZZLE_STR));

			// Redraw view
			view.redraw();

			// Update UI
			updateUI();
		}
	}

	//------------------------------------------------------------------

	public void onEditSymbols()
	{
		// Get symbols for edit
		char[] symbols = puzzle.symbols();

		// Display dialog for editing symbols
		char[] newSymbols = SymbolsDialog.show(window(), puzzle.puzzleOrder(), symbols);

		// If dialog was accepted, set new symbols on puzzle
		if (newSymbols != null)
		{
			// Add edit to list
			editList.add(new SymbolsEdit(symbols, newSymbols));

			// Set new symbols on puzzle
			puzzle.symbols(newSymbols);

			// Invalidate symbol size in value-selection pop-up
			view.invalidateSymbolSize();

			// Redraw view
			view.redraw();

			// Update UI
			updateUI();
		}
	}

	//------------------------------------------------------------------

	public void onCopyText()
	{
		// Display dialog
		Boolean result = CopyTextDialog.show(window());
		if (result == null)
			return;

		// Create textual representation of puzzle and put it on system clipboard
		try
		{
			// Create textual representation of puzzle
			String text = puzzle.toText(result);

			// Put text on clipboard
			ClipboardUtils.putTextThrow(text);

			// Report success
			NotificationDialog.show(window(), Command.COPY_TEXT.text, MessageIcon32.INFORMATION.get(), TEXT_COPIED_STR);
		}
		catch (BaseException e)
		{
			ErrorDialog.show(window(), Command.COPY_TEXT.text, e);
		}
	}

	//------------------------------------------------------------------

	public void onHomogeniseEntries()
	{
		if (puzzle.hasEntries())
			homogeniseEntries();
	}

	//------------------------------------------------------------------

	private void onTransformEntries(
		Command	command)
	{
		// Get entries of puzzle
		List<Puzzle.Entry> entries = puzzle.entries();
		if (entries.isEmpty())
			return;

		// Initialise list of transformed entries
		List<Puzzle.Entry> transformedEntries = new ArraySet<>();

		// Create a procedure to create an entry and add it to the list of transformed entries
		int numRows = puzzle.numRows();
		int numColumns = puzzle.numColumns();
		IProcedure3<Integer, Integer, Integer> addEntry = (row, column, value) ->
				transformedEntries.add(new Puzzle.Entry(row * numColumns + column, value));

		// Transform entries
		for (Puzzle.Entry entry : entries)
		{
			int index = entry.index();
			int r1 = index / numColumns;
			int c1 = index % numColumns;
			int c2 = numColumns - c1 - 1;
			int r2 = numRows - r1 - 1;
			switch (command)
			{
				case FLIP_HORIZONTALLY -> addEntry.invoke(r1, c2, entry.value());
				case FLIP_VERTICALLY   -> addEntry.invoke(r2, c1, entry.value());
				case ROTATE_90_DEG_ACW -> addEntry.invoke(c2, r1, entry.value());
				case ROTATE_90_DEG_CW  -> addEntry.invoke(c1, r2, entry.value());
				case ROTATE_180_DEG    -> addEntry.invoke(r2, c2, entry.value());
				default                -> { }
			}
		}

		// Set transformed entries on puzzle
		puzzle.setValues(transformedEntries);

		// Add edit to list
		editList.add(new EntriesEdit(entries, transformedEntries, command.text.toLowerCase()));

		// Redraw view
		view.redraw();

		// Update UI
		updateUI();
	}

	//------------------------------------------------------------------

	private Window window()
	{
		return SceneUtils.getWindow(view);
	}

	//------------------------------------------------------------------

	private void updateUI()
	{
		SudokuGeneratorApp.instance().updateUI();
	}

	//------------------------------------------------------------------

	private void reportConflictingCells(
		int	index1,
		int	index2)
	{
		// Highlight conflicting cells
		view.highlightIndices(index1, index2);

		// Create locator to align top centre of message dialog with top centre of main window
		SimpleDialog.ILocator locator = (width, height) ->
		{
			Window window = window();
			return new Point2D(window.getX() + 0.5 * (window.getWidth() - width), window.getY());
		};

		// Display message dialog to report conflicting entries
		new MessageDialog(window(), Command.CHECK.text, MessageIcon32.ERROR.get(),
						  PUZZLE_STR + CONFLICTING_ENTRIES_STR, locator, ButtonInfo.right(SimpleDialog.OK_STR))
				.showDialog();

		// Remove highlighting from conflicting cells
		view.clearHighlightIndices();

		// Select conflicting cell with greater index
		view.selectedIndex(Math.max(index1, index2));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COMMAND


	/**
	 * This is an enumeration of commands that are associated with a puzzle document.
	 */

	public enum Command
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		CHECK
		(
			"Check",
			false,
			new KeyCodeCombination(KeyCode.F8)
		),

		CHOOSE_SYMBOL
		(
			"Choose symbol for selected cell",
			false,
			new KeyCodeCombination(KeyCode.F6)
		),

		CLEAR
		(
			"Clear",
			false,
			null
		),

		CLOSE_SOLUTIONS
		(
			"Close solutions",
			false,
			new KeyCodeCombination(KeyCode.ESCAPE)
		),

		COPY_TEXT
		(
			"Copy text",
			true,
			new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)
		),

		COUNT_ENTRIES
		(
			"Count entries",
			false,
			new KeyCodeCombination(KeyCode.F7)
		),

		CREATE_TEMPLATE
		(
			"Create template",
			true,
			new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)
		),

		DELETE_PROPERTIES
		(
			"Delete properties",
			true,
			null
		),

		EDIT_SYMBOLS
		(
			"Edit symbols",
			false,
			null
		),

		FIND_ENTRY
		(
			"Find entry for selected cell",
			false,
			new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)
		),

		FLIP_HORIZONTALLY
		(
			"Flip horizontally",
			false,
			null
		),

		FLIP_VERTICALLY
		(
			"Flip vertically",
			false,
			null
		),

		GENERATE
		(
			"Generate",
			true,
			new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN)
		),

		HOMOGENISE_ENTRIES
		(
			"Homogenise entries",
			true,
			null
		),

		REDO
		(
			"Redo",
			false,
			new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN)
		),

		ROTATE_180_DEG
		(
			"Rotate 180\u00B0",
			false,
			null
		),

		ROTATE_90_DEG_ACW
		(
			"Rotate 90\u00B0 anticlockwise",
			false,
			null
		),

		ROTATE_90_DEG_CW
		(
			"Rotate 90\u00B0 clockwise",
			false,
			null
		),

		SOLVE
		(
			"Solve",
			true,
			new KeyCodeCombination(KeyCode.F9)
		),

		UNDO
		(
			"Undo",
			false,
			new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN)
		);

		private static final	List<Command>	TRANSFORM_COMMANDS	= List.of
		(
			Command.ROTATE_90_DEG_ACW,
			Command.ROTATE_90_DEG_CW,
			Command.ROTATE_180_DEG,
			Command.FLIP_HORIZONTALLY,
			Command.FLIP_VERTICALLY
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The text that represents this command. */
		private	String			text;

		/** Flag: if {@code true}, the text of a menu item has a trailing ellipsis. */
		private	boolean			ellipsis;

		/** The key combination that invokes this command. */
		private	KeyCombination	keyCombo;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a command.
		 *
		 * @param text
		 *          the text that will represent the command.
		 * @param ellipsis
		 *          if {@code true}, the text of a menu item will have a trailing ellipsis.
		 * @param keyCombo
		 *          the key combination that will invoke the command.
		 */

		private Command(
			String			text,
			boolean			ellipsis,
			KeyCombination	keyCombo)
		{
			// Initialise instance variables
			this.text = text;
			this.ellipsis = ellipsis;
			this.keyCombo = keyCombo;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates and returns a new instance of a menu item for this command.
		 *
		 * @param  eventHandler
		 *           the action that will be performed by this command.
		 * @return a new instance of a menu item for this command.
		 */

		public MenuItem newMenuItem()
		{
			MenuItem menuItem = new MenuItem(ellipsis ? text + ELLIPSIS_STR : text);
			menuItem.setUserData(this);
			if (keyCombo != null)
				menuItem.setAccelerator(keyCombo);
			return menuItem;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: SOLUTION INFORMATION


	public record SolutionInfo(
		int	index,
		int	numSolutions)
	{ }

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: AN EDIT OF A SINGLE PUZZLE ENTRY


	private class EntryEdit
		implements EditList.IEdit
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "change entry";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	cellIndex;
		private	int	oldValue;
		private	int	newValue;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EntryEdit(
			int	cellIndex,
			int	oldValue,
			int	newValue)
		{
			// Initialise instance variables
			this.cellIndex = cellIndex;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : EditList.IEdit interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void undo()
		{
			puzzle.setValue(cellIndex, oldValue);
			view.selectedIndex(cellIndex);
		}

		//--------------------------------------------------------------

		@Override
		public void redo()
		{
			puzzle.setValue(cellIndex, newValue);
			view.selectedIndex(cellIndex);
		}

		//--------------------------------------------------------------

		@Override
		public String text()
		{
			return TEXT;
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
		private	String				text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EntriesEdit(
			List<Puzzle.Entry>	oldEntries,
			List<Puzzle.Entry>	newEntries,
			String				text)
		{
			// Initialise instance variables
			this.oldEntries = oldEntries;
			this.newEntries = newEntries;
			this.text = text;
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

		@Override
		public String text()
		{
			return text;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: AN EDIT OF THE PROPERTIES OF A PUZZLE


	private class PropertiesEdit
		implements EditList.IEdit
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Map<Puzzle.Key, String>	oldProperties;
		private	Map<Puzzle.Key, String>	newProperties;
		private	String					text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PropertiesEdit(
			Map<Puzzle.Key, String>	oldProperties,
			Map<Puzzle.Key, String>	newProperties,
			String					text)
		{
			// Initialise instance variables
			this.oldProperties = oldProperties;
			this.newProperties = newProperties;
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : EditList.IEdit interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void undo()
		{
			puzzle.properties(oldProperties);
		}

		//--------------------------------------------------------------

		@Override
		public void redo()
		{
			puzzle.properties(newProperties);
		}

		//--------------------------------------------------------------

		@Override
		public String text()
		{
			return text;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: AN EDIT OF THE ENTRIES AND PROPERTIES OF A PUZZLE


	private class EntriesPropertiesEdit
		implements EditList.IEdit
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<Puzzle.Entry>		oldEntries;
		private	Map<Puzzle.Key, String>	oldProperties;
		private	List<Puzzle.Entry>		newEntries;
		private	Map<Puzzle.Key, String>	newProperties;
		private	String					text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EntriesPropertiesEdit(
			List<Puzzle.Entry>		oldEntries,
			Map<Puzzle.Key, String>	oldProperties,
			List<Puzzle.Entry>		newEntries,
			Map<Puzzle.Key, String>	newProperties,
			String					text)
		{
			// Initialise instance variables
			this.oldEntries = oldEntries;
			this.oldProperties = oldProperties;
			this.newEntries = newEntries;
			this.newProperties = newProperties;
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : EditList.IEdit interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void undo()
		{
			puzzle.setValues(oldEntries);
			puzzle.properties(oldProperties);
		}

		//--------------------------------------------------------------

		@Override
		public void redo()
		{
			puzzle.setValues(newEntries);
			puzzle.properties(newProperties);
		}

		//--------------------------------------------------------------

		@Override
		public String text()
		{
			return text;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: AN EDIT OF THE SYMBOLS OF A PUZZLE


	private class SymbolsEdit
		implements EditList.IEdit
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "edit symbols";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	char[]	oldSymbols;
		private	char[]	newSymbols;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SymbolsEdit(
			char[]	oldSymbols,
			char[]	newSymbols)
		{
			// Initialise instance variables
			this.oldSymbols = oldSymbols;
			this.newSymbols = newSymbols;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : EditList.IEdit interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void undo()
		{
			puzzle.symbols(oldSymbols);
			view.redraw();
		}

		//--------------------------------------------------------------

		@Override
		public void redo()
		{
			puzzle.symbols(newSymbols);
			view.redraw();
		}

		//--------------------------------------------------------------

		@Override
		public String text()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
