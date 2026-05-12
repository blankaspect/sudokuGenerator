/*====================================================================*\

Puzzle.java

Class: sudoku puzzle.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.concurrent.atomic.AtomicLong;

import java.util.stream.Collectors;

import javafx.scene.input.KeyEvent;

import uk.blankaspect.common.basictree.AbstractNode;
import uk.blankaspect.common.basictree.IntNode;
import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.NodeMessage;

import uk.blankaspect.common.collection.CollectionUtils;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.TaskCancelledException;
import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.function.IFunction0;
import uk.blankaspect.common.function.IFunction1;
import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.json.JsonGenerator;
import uk.blankaspect.common.json.JsonParser;
import uk.blankaspect.common.json.JsonUtils;

import uk.blankaspect.common.random.IPrng;
import uk.blankaspect.common.random.PrngXoshiro256ss;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.task.ICancellable;

import uk.blankaspect.common.thread.DaemonFactory;

//----------------------------------------------------------------------


// CLASS: SUDOKU PUZZLE


class Puzzle
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		Order	DEFAULT_ORDER	= Order._3;

	private static final	EnumMap<Order, int[][]>	CELL_INDICES_IN_BLOCK;

	private static final	long	MAX_NUM_GENERATION_ATTEMPTS	= Long.MAX_VALUE;

	private static final	int		GENERATE_CANCELLATION_TEST_LEVEL	= 5;

	private static final	char[]	TEXT_FILE_PLACEHOLDER_CHARS		= "._-~+=*^`,:;/?!<>$%&@|()[]".toCharArray();
	private static final	char[]	TEXT_FILE_COMMENT_PREFIX_CHARS	= "#;:!%&$@?/*+=~-^`<>.,_|[]{}".toCharArray();

	private static final	String	TEXT_FILE_COMMENT	= "Sudoku *";

	private static final	int[]	SYMBOL_CATEGORIES	=
	{
		Character.UPPERCASE_LETTER,
		Character.LOWERCASE_LETTER,
		Character.TITLECASE_LETTER,
		Character.OTHER_LETTER,
		Character.DECIMAL_DIGIT_NUMBER,
		Character.LETTER_NUMBER,
		Character.CONNECTOR_PUNCTUATION,
		Character.DASH_PUNCTUATION,
		Character.START_PUNCTUATION,
		Character.END_PUNCTUATION,
		Character.OTHER_PUNCTUATION,
		Character.MATH_SYMBOL,
		Character.CURRENCY_SYMBOL,
		Character.OTHER_SYMBOL
	};

	private static final	int[]	IGNORED_TEXT_FILE_CHAR_CATEGORIES	=
	{
		Character.SPACE_SEPARATOR,
		Character.LINE_SEPARATOR,
		Character.PARAGRAPH_SEPARATOR,
		Character.CONTROL,
		Character.FORMAT,
		Character.SURROGATE,
		Character.PRIVATE_USE,
		Character.UNASSIGNED
	};

	/** The identifier of a puzzle file. */
	private static final	String	ID	= "N6JE4781GLPM8O66IW2T8CUVG";

	/** The version of a puzzle file. */
	private static final	int		VERSION	= 0;

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	DESCRIPTION		= "description";
		String	DISPLAYED_VALUE	= "displayedValue";
		String	ID				= "id";
		String	INDEX			= "index";
		String	KIND			= "kind";
		String	ORDER			= "order";
		String	PROPERTIES		= "properties";
		String	PUZZLE			= "puzzle";
		String	ROWS			= "rows";
		String	SYMBOLS			= "symbols";
		String	VALUES			= "values";
		String	VERSION			= "version";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	NO_ORDER =
				"There is no puzzle order.";

		String	ORDER_OUT_OF_BOUNDS =
				"The puzzle order is out of bounds: ";

		String	INCORRECT_NUM_SYMBOLS =
				"The number of symbols is incorrect";

		String	NO_ROW_INDEX =
				"There is no row index.";

		String	ROW_INDEX_OUT_OF_BOUNDS =
				"The row index is out of bounds: ";

		String	CELL_VALUE_OUT_OF_BOUNDS =
				"The cell value is out of bounds: ";

		String	ERROR_READING_FILE =
				"An error occurred when reading the file.";

		String	ERROR_WRITING_FILE =
				"An error occurred when writing the file.";

		String	NOT_A_PUZZLE_FILE =
				"The file is not a sudoku puzzle.";

		String	MALFORMED_PUZZLE_FILE =
				"The puzzle file is malformed.";

		String	UNEXPECTED_PUZZLE_FILE_FORMAT =
				"The puzzle file does not have the expected format.";

		String	UNSUPPORTED_PUZZLE_FILE_VERSION =
				"Version %s of the puzzle file is not supported.";

		String	ERRORS_PARSING_FILE =
				"The following errors were found when parsing the file:%s";

		String	MALFORMED_FILE =
				"The file is malformed.";

		String	UNEXPECTED_NUMBER_OF_LINES =
				"The text does not have the expected number of significant lines.";

		String	UNEXPECTED_NUMBER_OF_CHARACTERS =
				"The line does not have the expected number of significant characters.";

		String	DUPLICATE_SYMBOL =
				"The header contains a duplicate symbol, '%s'.";

		String	SYMBOL_CONFLICTS_WITH_PLACEHOLDER =
				"A symbol conflicts with the placeholder character, '%s'.";

		String	INVALID_CHARACTER =
				"The character '%s' is neither a symbol nor a placeholder.";

		String	MAX_NUM_GENERATION_ATTEMPTS_REACHED =
				"The maximum number of attempts to generate a puzzle has been reached.";

		String	ERROR_GENERATING_PUZZLE =
				"An error occurred when generating a puzzle.";

		String	FAILED_TO_GENERATE_PUZZLE =
				"Failed to generate a puzzle under the given constraints.";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	/** The index of the last thread that was created for a background task. */
	private static	int	threadIndex;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Order				puzzleOrder;
	private	int					order;
	private	int					numRows;
	private	int					numColumns;
	private	int					cellsPerBlock;
	private	char[]				symbols;
	private	byte[]				values;
	private	boolean				editable;
	private	Map<Key, String>	properties;
	private	AvailableValues		availableValues;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Validate arrays related to text-file characters
		Order maxOrder = Order.MAX;
		if (TEXT_FILE_PLACEHOLDER_CHARS.length != maxOrder.numSymbols() + 1)
			throw new UnexpectedRuntimeException();
		if (TEXT_FILE_COMMENT_PREFIX_CHARS.length != maxOrder.numSymbols() + 2)
			throw new UnexpectedRuntimeException();

		// Initialise look-up table of cell indices in block
		CELL_INDICES_IN_BLOCK = new EnumMap<>(Order.class);
		for (Order puzzleOrder : Order.values())
		{
			// Initialise look-up table of cell indices in block: block_index -> cell_indices[]
			int numColumns = puzzleOrder.pow2();
			int numBlocks = puzzleOrder.pow2();
			int cellsPerBlock = puzzleOrder.pow2();
			int cellsPerBlockRow = puzzleOrder.pow3();
			int order = puzzleOrder.value;
			int[][] blocks = new int[numBlocks][];
			for (int block = 0; block < numBlocks; block++)
			{
				int[] indices = new int[cellsPerBlock];
				blocks[block] = indices;
				int index = block / order * cellsPerBlockRow + block % order * order;
				int k = 0;
				for (int i = 0; i < order; i++)
				{
					for (int j = index; j < index + order; j++)
						indices[k++] = j;
					index += numColumns;
				}
			}
			CELL_INDICES_IN_BLOCK.put(puzzleOrder, blocks);
		}
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Puzzle()
	{
		// Call alternative constructor
		this(DEFAULT_ORDER, true);
	}

	//------------------------------------------------------------------

	public Puzzle(
		Order	puzzleOrder,
		boolean	editable)
	{
		// Validate argument
		if (puzzleOrder == null)
			throw new IllegalArgumentException("Null puzzle order");

		// Initialise instance variables
		this.puzzleOrder = puzzleOrder;
		order = puzzleOrder.value;
		numRows = numColumns = cellsPerBlock = puzzleOrder.pow2();
		symbols = puzzleOrder.defaultSymbols.clone();
		values = new byte[numRows * numColumns];
		this.editable = editable;
		properties = Collections.emptyMap();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean isValidSymbol(
		char	ch)
	{
		// Test Unicode general category
		int category = Character.getType(ch);
		for (int i = 0; i < SYMBOL_CATEGORIES.length; i++)
		{
			if (category == SYMBOL_CATEGORIES[i])
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	public static int actualNumThreads(
		int	numThreads)
	{
		return (numThreads == 0) ? Math.max(1, Runtime.getRuntime().availableProcessors() - 1) : numThreads;
	}

	//------------------------------------------------------------------

	public static FileInfo fromText(
		Path	file)
		throws FileException
	{
		// Read file as text
		String text = null;
		try
		{
			text = Files.readString(file, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			throw new FileException(ErrorMsg.ERROR_READING_FILE, file, e);
		}

		// Parse text
		try
		{
			Puzzle puzzle = parseText(text);
			return new FileInfo(file, puzzle, ((puzzle.numEntries() > 1) && (puzzle.valueCardinality() == 1)));
		}
		catch (BaseException e)
		{
			throw new FileException(e, file);
		}
	}

	//------------------------------------------------------------------

	public static Puzzle parseText(
		String	text)
		throws BaseException
	{
		// Define record for line of text
		record Line(
			int		index,
			String	text)
		{ }

		// Split text into lines
		List<String> inLines = StringUtils.extractLines(text);

		// Remove ignored characters from each line; remove empty lines
		List<Line> lines = new ArrayList<>();
		StringBuilder buffer = new StringBuilder(128);
		for (int i = 0; i < inLines.size(); i++)
		{
			// Get next line
			String line = inLines.get(i);

			// Remove ignored characters from line
			buffer.setLength(0);
			for (int j = 0; j < line.length(); j++)
			{
				char ch = line.charAt(j);
				if (!isIgnoredTextFileChar(ch))
					buffer.append(ch);
			}

			// Add non-empty line to list
			if (buffer.length() > 0)
				lines.add(new Line(i, buffer.toString()));
		}

		// Test for blank text
		if (lines.isEmpty())
			throw new BaseException(ErrorMsg.MALFORMED_FILE);

		// Get comment-prefix character
		char commentPrefixChar = lines.get(0).text.charAt(0);

		// Remove comment from each line; remove resulting empty lines
		for (int i = lines.size() - 1; i >= 0; i--)
		{
			Line line = lines.get(i);
			int index = line.text.indexOf(commentPrefixChar);
			if (index > 0)
				lines.set(i, new Line(line.index, line.text.substring(0, index)));
			else if (index == 0)
				lines.remove(i);
		}

		// Test for expected number of lines; infer order of puzzle from number of lines
		int numLines = lines.size();
		Order puzzleOrder = null;
		for (Order order : Order.values())
		{
			if (numLines == order.value * order.value + 1)
			{
				puzzleOrder = order;
				break;
			}
		}
		if (puzzleOrder == null)
			throw new BaseException(ErrorMsg.UNEXPECTED_NUMBER_OF_LINES);

		// Create puzzle
		Puzzle puzzle = new Puzzle(puzzleOrder, true);

		// Parse lines
		int lineIndex = 0;
		try
		{
			char placeholder = '\0';
			int row = 0;
			while (lineIndex < lines.size())
			{
				// Get next line
				Line line = lines.get(lineIndex);
				int length = line.text.length();

				// Case: symbol-definition line
				if (lineIndex == 0)
				{
					// Test length of line
					if ((length != 1) && (length != puzzleOrder.numSymbols() + 1))
						throw new BaseException(ErrorMsg.UNEXPECTED_NUMBER_OF_CHARACTERS);

					// Extract placeholder from line
					placeholder = line.text.charAt(0);

					// Get symbols
					char[] symbols = (length == 1)
										? puzzleOrder.defaultSymbols.clone()
										: line.text.substring(1).toCharArray();

					// Test for duplicate symbol
					if (length > 1)
					{
						for (int i = 0; i < symbols.length - 1; i++)
						{
							char ch = symbols[i];
							for (int j = i + 1; j < symbols.length; j++)
							{
								if (ch == symbols[j])
									throw new BaseException(ErrorMsg.DUPLICATE_SYMBOL, ch);
							}
						}
					}

					// Test whether a symbol conflicts with placeholder
					for (int i = 0; i < symbols.length; i++)
					{
						if (symbols[i] == placeholder)
							throw new BaseException(ErrorMsg.SYMBOL_CONFLICTS_WITH_PLACEHOLDER, placeholder);
					}

					// Set symbols on puzzle
					puzzle.symbols = symbols;
				}

				// Case: puzzle entries
				else
				{
					// Test length of line
					if (length != puzzle.numColumns)
						throw new BaseException(ErrorMsg.UNEXPECTED_NUMBER_OF_CHARACTERS);

					// Parse puzzle entries
					for (int i = 0; i < length; i++)
					{
						char ch = line.text.charAt(i);
						int value = -1;
						if (ch == placeholder)
							value = 0;
						else
						{
							for (int j = 0; j < puzzle.symbols.length; j++)
							{
								if (ch == puzzle.symbols[j])
								{
									value = j + 1;
									break;
								}
							}
						}
						if (value < 0)
							throw new BaseException(ErrorMsg.INVALID_CHARACTER, ch);
						puzzle.setValue(row * puzzle.numColumns + i, value);
					}

					// Increment rows
					++row;
				}

				// Increment line index
				++lineIndex;
			}
		}
		catch (BaseException e)
		{
			throw new ParseException(e, lines.get(lineIndex).index);
		}

		// Return puzzle
		return puzzle;
	}

	//------------------------------------------------------------------

	public static FileInfo fromJson(
		Path		file,
		FileKind	expectedFileKind)
		throws FileException
	{
		// Read file as text
		String text = null;
		try
		{
			text = Files.readString(file);
		}
		catch (IOException e)
		{
			throw new FileException(ErrorMsg.ERROR_READING_FILE, file, e);
		}

		// Test for puzzle file
		if (!text.contains(ID))
			throw new FileException(ErrorMsg.NOT_A_PUZZLE_FILE, file);

		// Parse text of file
		AbstractNode root = null;
		try
		{
			root = JsonParser.builder().build().parse(text);
		}
		catch (JsonParser.ParseException e)
		{
			throw new FileException(ErrorMsg.MALFORMED_PUZZLE_FILE, file, e);
		}

		// Test for expected type of JSON value
		if (!(root instanceof MapNode rootNode))
			throw new FileException(ErrorMsg.NOT_A_PUZZLE_FILE, file);

		// Check ID
		String key = PropertyKey.ID;
		if (!rootNode.getString(key, "").equals(ID))
			throw new FileException(ErrorMsg.NOT_A_PUZZLE_FILE, file);

		// Check version
		key = PropertyKey.VERSION;
		if (!rootNode.hasInt(key))
			throw new FileException(ErrorMsg.UNEXPECTED_PUZZLE_FILE_FORMAT, file);
		int version = rootNode.getInt(key);
		if (version != VERSION)
			throw new FileException(ErrorMsg.UNSUPPORTED_PUZZLE_FILE_VERSION, file, version);

		// Decode file kind
		FileKind fileKind = rootNode.getEnumValue(FileKind.class, PropertyKey.KIND, FileKind::key, expectedFileKind);
		boolean template = (fileKind == FileKind.TEMPLATE);

		// Test for puzzle node
		key = PropertyKey.PUZZLE;
		if (!rootNode.hasMap(key))
			throw new FileException(ErrorMsg.MALFORMED_PUZZLE_FILE, file);

		// Declare class for decoding error
		class DecodingError extends Exception{};

		// Decode puzzle
		MapNode puzzleNode = rootNode.getMapNode(key);
		NodeMessage.List messages = new NodeMessage.List();
		try
		{
			// Decode order
			key = PropertyKey.ORDER;
			if (!puzzleNode.hasInt(key))
			{
				messages.add(puzzleNode, NodeMessage.Kind.FATAL, ErrorMsg.NO_ORDER);
				throw new DecodingError();
			}
			int order = puzzleNode.getInt(key);
			Puzzle.Order puzzleOrder = Order.forValue(order);
			if (puzzleOrder == null)
			{
				messages.add(puzzleNode.get(key), NodeMessage.Kind.FATAL, ErrorMsg.ORDER_OUT_OF_BOUNDS + order);
				throw new DecodingError();
			}

			// Create puzzle
			Puzzle puzzle = new Puzzle(puzzleOrder, true);

			// Decode symbols
			key = PropertyKey.SYMBOLS;
			if (puzzleNode.hasString(key))
			{
				String str = puzzleNode.getString(key);
				if (str.length() != puzzleOrder.numSymbols())
				{
					messages.add(puzzleNode.get(key), NodeMessage.Kind.FATAL, ErrorMsg.INCORRECT_NUM_SYMBOLS);
					throw new DecodingError();
				}
				puzzle.symbols = str.toCharArray();
			}

			// Decode displayed value
			key = PropertyKey.DISPLAYED_VALUE;
			int displayedValue = template ? puzzleNode.getInt(key, 0) : 0;
			if ((displayedValue < 0) || (displayedValue > puzzle.cellsPerBlock))
			{
				messages.add(puzzleNode.get(key), NodeMessage.Kind.FATAL,
							 ErrorMsg.CELL_VALUE_OUT_OF_BOUNDS + displayedValue);
				throw new DecodingError();
			}

			// Decode rows
			key = PropertyKey.ROWS;
			if (puzzleNode.hasList(key))
			{
				for (MapNode rowNode : puzzleNode.getListNode(key).mapNodes())
				{
					// Decode row index
					int rowIndex = -1;
					key = PropertyKey.INDEX;
					if (rowNode.hasInt(key))
					{
						rowIndex = rowNode.getInt(key);
						if ((rowIndex < 0) || (rowIndex >= puzzle.numRows))
						{
							messages.add(rowNode.get(key), NodeMessage.Kind.FATAL,
										 ErrorMsg.ROW_INDEX_OUT_OF_BOUNDS + rowIndex);
							throw new DecodingError();
						}
					}

					// Decode cell values
					key = PropertyKey.VALUES;
					if (rowNode.hasList(key))
					{
						// Test for row index
						if (rowIndex < 0)
						{
							messages.add(rowNode, NodeMessage.Kind.FATAL, ErrorMsg.NO_ROW_INDEX);
							throw new DecodingError();
						}

						// Decode cell values
						int index = rowIndex * puzzle.numColumns;
						for (IntNode valueNode : rowNode.getListNode(key).intNodes())
						{
							int value = valueNode.getValue();
							if ((value < 0) || (value > puzzle.cellsPerBlock))
							{
								messages.add(valueNode, NodeMessage.Kind.FATAL,
											 ErrorMsg.CELL_VALUE_OUT_OF_BOUNDS + value);
								throw new DecodingError();
							}
							if ((value > 0) && (displayedValue > 0))
								value = displayedValue;
							puzzle.setValue(index++, value);
						}
					}
				}
			}

			// Decode properties
			key = PropertyKey.PROPERTIES;
			if (puzzleNode.hasMap(key))
			{
				MapNode propertiesNode = puzzleNode.getMapNode(key);
				for (Key propertyKey : Key.values())
				{
					key = propertyKey.key;
					if (propertiesNode.hasString(key))
						puzzle.addProperty(propertyKey, propertiesNode.getString(key));
				}
			}

			// Return puzzle
			return new FileInfo(file, puzzle, template);
		}
		catch (DecodingError e)
		{
			// Concatenate messages
			String separator = "\n- ";
			String str = messages.getMessages().stream()
					.map(message -> message.toString(" : ", NodeMessage.Component.NODE, NodeMessage.Component.TEXT))
					.collect(Collectors.joining(separator, separator, ""));

			// Throw exception
			throw new FileException(ErrorMsg.ERRORS_PARSING_FILE, file, str);
		}
	}

	//------------------------------------------------------------------

	private static boolean isIgnoredTextFileChar(
		char	ch)
	{
		// Test Unicode general category
		int category = Character.getType(ch);
		for (int i = 0; i < IGNORED_TEXT_FILE_CHAR_CATEGORIES.length; i++)
		{
			if (category == IGNORED_TEXT_FILE_CHAR_CATEGORIES[i])
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	private static int[] flagsToIndices(
		int	flags,
		int	numIndices)
	{
		int[] indices = new int[numIndices];
		int index = 0;
		int i = 0;
		while (flags != 0)
		{
			if ((flags & 1) != 0)
				indices[i++] = index;
			++index;
			flags >>>= 1;
		}
		return indices;
	}

	//------------------------------------------------------------------

	private static void permuteIntArray(
		int[]	values,
		IPrng	prng)
	{
		for (int i = values.length - 1; i > 0; i--)
		{
			int j = prng.nextInt(i + 1);
			int temp = values[j];
			values[j] = values[i];
			values[i] = temp;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Cloneable interface
////////////////////////////////////////////////////////////////////////

	@Override
	public Puzzle clone()
	{
		try
		{
			Puzzle copy = (Puzzle)super.clone();
			copy.symbols = symbols.clone();
			copy.values = values.clone();
			copy.properties = properties.isEmpty() ? Collections.emptyMap() : new EnumMap<>(properties);
			if (availableValues != null)
				copy.availableValues = availableValues.clone();
			return copy;
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return toText(false);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int order()
	{
		return order;
	}

	//------------------------------------------------------------------

	public Puzzle.Order puzzleOrder()
	{
		return Puzzle.Order.forValue(order);
	}

	//------------------------------------------------------------------

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

	public int numSymbols()
	{
		return symbols.length;
	}

	//------------------------------------------------------------------

	public char symbol(
		int	index)
	{
		return symbols[index];
	}

	//------------------------------------------------------------------

	public char[] symbols()
	{
		return symbols.clone();
	}

	//------------------------------------------------------------------

	public void symbols(
		char[]	symbols)
	{
		if (symbols == null)
			symbols = puzzleOrder.defaultSymbols.clone();
		else
		{
			if (symbols.length != puzzleOrder.pow2())
				throw new IllegalArgumentException("Incorrect number of symbols");

			this.symbols = symbols.clone();
		}
	}

	//------------------------------------------------------------------

	public byte value(
		int	index)
	{
		return values[index];
	}

	//------------------------------------------------------------------

	public byte value(
		int	row,
		int	column)
	{
		return values[row * numColumns + column];
	}

	//------------------------------------------------------------------

	public void setValue(
		int	index,
		int	value)
	{
		if (availableValues == null)
			values[index] = (byte)value;
		else
		{
			int oldValue = values[index];
			if (value != oldValue)
			{
				// Calculate row, column and block indices
				int row = index / numColumns;
				int column = index % numColumns;
				int block = rowColumnToBlock(row, column);

				// Add old value to available values
				if (oldValue > 0)
				{
					int mask = 1 << oldValue - 1;
					availableValues.rows[row] |= mask;
					availableValues.columns[column] |= mask;
					availableValues.blocks[block] |= mask;
				}

				// Update value
				values[index] = (byte)value;

				// Remove new value from available values
				if (value > 0)
				{
					int mask = ~(1 << value - 1);
					availableValues.rows[row] &= mask;
					availableValues.columns[column] &= mask;
					availableValues.blocks[block] &= mask;
				}
			}
		}
	}

	//------------------------------------------------------------------

	public void setValues(
		Iterable<Entry>	entries)
	{
		// Clear all values
		clear();

		// Set values from entries
		for (Entry entry : entries)
			setValue(entry.index, entry.value);
	}

	//------------------------------------------------------------------

	public boolean editable()
	{
		return editable;
	}

	//------------------------------------------------------------------

	public Puzzle editable(
		boolean	editable)
	{
		// Update instance variable
		this.editable = editable;

		// Return this puzzle
		return this;
	}

	//------------------------------------------------------------------

	public boolean hasEntries()
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] > 0)
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	public int numEntries()
	{
		int numEntries = 0;
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] > 0)
				++numEntries;
		}
		return numEntries;
	}

	//------------------------------------------------------------------

	public List<Entry> entries()
	{
		List<Entry> entries = new ArrayList<>();
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] > 0)
				entries.add(new Entry(i, values[i]));
		}
		return entries;
	}

	//------------------------------------------------------------------

	public List<Integer> entryIndices()
	{
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] > 0)
				indices.add(i);
		}
		return indices;
	}

	//------------------------------------------------------------------

	public boolean hasHeterogeneousEntries()
	{
		int nonZeroValues = 0;
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] > 0)
				nonZeroValues |= 1 << values[i];
		}
		return (Integer.bitCount(nonZeroValues) > 1);
	}

	//------------------------------------------------------------------

	public boolean hasProperties()
	{
		return !properties.isEmpty();
	}

	//------------------------------------------------------------------

	public boolean hasProperty(
		Key	key)
	{
		return properties.containsKey(key);
	}

	//------------------------------------------------------------------

	public String property(
		Key	key)
	{
		return properties.get(key);
	}

	//------------------------------------------------------------------

	public Map<Key, String> properties()
	{
		return Map.copyOf(properties);
	}

	//------------------------------------------------------------------

	public void properties(
		Map<Key, String>	properties)
	{
		this.properties = properties.isEmpty() ? Collections.emptyMap() : new EnumMap<>(properties);
	}

	//------------------------------------------------------------------

	public void addProperty(
		Key		key,
		String	value)
	{
		// If map of properties is empty, replace it with modifiable map
		if (properties.isEmpty())
			properties = new EnumMap<>(Key.class);

		// Add property to map
		properties.put(key, value);
	}

	//------------------------------------------------------------------

	public void removeProperty(
		Key	key)
	{
		// Remove property from map
		properties.remove(key);

		// If map of properties is empty, replace it with unmodifiable empty map
		if (properties.isEmpty())
			properties = Collections.emptyMap();
	}

	//------------------------------------------------------------------

	public boolean isComplete()
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] == 0)
				return false;
		}
		return true;
	}

	//------------------------------------------------------------------

	public void clear()
	{
		// Clear values
		Arrays.fill(values, (byte)0);

		// Initialise available values
		if (availableValues != null)
			initAvailableValues();

		// Clear properties
		properties = Collections.emptyMap();
	}

	//------------------------------------------------------------------

	public String toText(
		boolean	separateBlocks)
	{
		// Find placeholder
		char placeholder = '\0';
		String str = new String(symbols);
		for (int i = 0; i < TEXT_FILE_PLACEHOLDER_CHARS.length; i++)
		{
			char ch = TEXT_FILE_PLACEHOLDER_CHARS[i];
			if (str.indexOf(ch) < 0)
			{
				placeholder = ch;
				break;
			}
		}

		// Find comment-prefix character
		char commentPrefixChar = '\0';
		str = placeholder + str;
		for (int i = 0; i < TEXT_FILE_COMMENT_PREFIX_CHARS.length; i++)
		{
			char ch = TEXT_FILE_COMMENT_PREFIX_CHARS[i];
			if (str.indexOf(ch) < 0)
			{
				commentPrefixChar = ch;
				break;
			}
		}

		// Initialise buffer
		StringBuilder buffer = new StringBuilder(1024);

		// Append initial comment line
		buffer.append(commentPrefixChar).append(' ').append(TEXT_FILE_COMMENT).append(order).append('\n');

		// Append symbol-definition line
		buffer.append(str).append('\n');

		// Compute maximum length of line
		int length = numColumns;
		if (separateBlocks)
			length += 2 * (order - 1);
		length = Math.max(length, str.length());

		// Append separator
		buffer.append(commentPrefixChar).append("-".repeat(length - 1)).append('\n');

		// Append puzzle rows
		for (int i = 0; i < numRows; i++)
		{
			if (separateBlocks && (i > 0) && (i % order == 0))
				buffer.append('\n');
			for (int j = 0; j < numColumns; j++)
			{
				if (separateBlocks && (j > 0) && (j % order == 0))
					buffer.append("  ");
				int value = value(i, j);
				buffer.append((value == 0) ? placeholder : symbols[value - 1]);
			}
			buffer.append('\n');
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public int valueCardinality()
	{
		int valueFlags = 0;
		for (int i = 0; i < values.length; i++)
		{
			int value = values[i];
			if (value > 0)
				valueFlags |= 1 << value - 1;
		}
		return Integer.bitCount(valueFlags);
	}

	//------------------------------------------------------------------

	public int availableValues(
		int	cellIndex)
	{
		// Initialise bit array of available values
		int available = (1 << symbols.length) - 1;

		// Remove values that appear in the same row as the target cell
		int index = cellIndex / numColumns * numColumns;
		for (int i = 0; i < numColumns; i++)
		{
			byte value = values[index];
			if (value > 0)
				available &= ~(1 << value - 1);
			++index;
		}

		// Remove values that appear in the same column as the target cell
		index = cellIndex % numColumns;
		for (int i = 0; i < numRows; i++)
		{
			byte value = values[index];
			if (value > 0)
				available &= ~(1 << value - 1);
			index += numColumns;
		}

		// Remove values that appear in the same block as the target cell
		int[] indices = cellIndicesOfBlock(cellIndex);
		for (int i = 0; i < indices.length; i++)
		{
			byte value = values[indices[i]];
			if (value > 0)
				available &= ~(1 << value - 1);
		}

		// Return available values
		return available;
	}

	//------------------------------------------------------------------

	public int valueForKeyTyped(
		KeyEvent	event)
	{
		String str = event.getCharacter();
		if (!KeyEvent.CHAR_UNDEFINED.equals(str) && !str.isEmpty() && Character.isBmpCodePoint(str.codePointAt(0)))
		{
			boolean ignoreCase = SudokuGeneratorApp.instance().preferences().ignoreCaseInGrid();
			if (ignoreCase)
				str = str.toUpperCase();
			char ch = str.charAt(0);
			for (int i = 0; i < symbols.length; i++)
			{
				char symbol = symbols[i];
				if ((ignoreCase ? Character.toUpperCase(symbol) : symbol) == ch)
					return i + 1;
			}
		}
		return 0;
	}

	//------------------------------------------------------------------

	public IndexPair findConflictingEntries()
	{
		int[] indices = new int[order * order];

		// Validate rows
		for (int i = 0; i < numRows; i++)
		{
			Arrays.fill(indices, -1);
			for (int j = 0; j < numColumns; j++)
			{
				int index = i * numColumns + j;
				int value = values[index];
				if (value > 0)
				{
					if (indices[--value] >= 0)
						return new IndexPair(indices[value], index);
					indices[value] = index;
				}
			}
		}

		// Validate columns
		for (int j = 0; j < numColumns; j++)
		{
			Arrays.fill(indices, -1);
			for (int i = 0; i < numRows; i++)
			{
				int index = i * numColumns + j;
				int value = values[index];
				if (value > 0)
				{
					if (indices[--value] >= 0)
						return new IndexPair(indices[value], index);
					indices[value] = index;
				}
			}
		}

		// Validate blocks
		int[][] blockIndices = CELL_INDICES_IN_BLOCK.get(puzzleOrder);
		for (int i = 0; i < blockIndices.length; i++)
		{
			Arrays.fill(indices, -1);
			int[] cellIndices = blockIndices[i];
			for (int j = 0; j < indices.length; j++)
			{
				int index = cellIndices[j];
				int value = values[index];
				if (value > 0)
				{
					if (indices[--value] >= 0)
						return new IndexPair(indices[value], index);
					indices[value] = index;
				}
			}
		}

		// Indicate no conflicting entries
		return null;
	}

	//------------------------------------------------------------------

	public List<Puzzle> solve(
		int				maxNumSolutions,
		boolean			randomiseSearch,
		AtomicLong		solutionsCount,
		ICancellable	taskStatus)
		throws BaseException
	{
		// Solve puzzle
		return solver(maxNumSolutions, randomiseSearch ? new PrngXoshiro256ss() : null, 0, solutionsCount, taskStatus)
				.solve();
	}

	//------------------------------------------------------------------

	public void generateAdditive(
		long			seed,
		int				numEntries,
		boolean			randomiseVerification,
		int				numThreads,
		AtomicLong		attemptsCount,
		ICancellable	taskStatus)
		throws BaseException
	{
		// Get actual number of threads
		int actualNumThreads = actualNumThreads(numThreads);

		// Create procedure to update the values of this puzzle with the generated entries
		IProcedure1<List<Entry>> updateValues = entries ->
		{
			// Update values with entries
			setValues(entries);

			// Add generation-info property to puzzle
			addProperty(Puzzle.Key.GENERATION_INFO,
						GenerationMode.ADDITIVE.versionString() + ", " + seed + ", " + numEntries + ", "
								+ randomiseVerification + ", " + actualNumThreads);
		};

		// Create PRNG
		IPrng prng = new PrngXoshiro256ss(seed);

		// Case: generate entries for the specified number of randomly selected cells
		if (numEntries > 0)
		{
			new AdditiveGenerator1()
					.generate(numEntries, prng, randomiseVerification, actualNumThreads, attemptsCount, taskStatus,
							  updateValues);
		}

		// Case: generate entries using the current entries as a template
		else
		{
			Puzzle puzzle = new Puzzle(puzzleOrder, false);
			puzzle.initAvailableValues();
			puzzle.new AdditiveGenerator2()
					.generate(CollectionUtils.intsToArray(entryIndices()), prng, randomiseVerification,
							  actualNumThreads, attemptsCount, taskStatus, updateValues);
		}
	}

	//------------------------------------------------------------------

	public void generateSubtractive(
		long			seed,
		int				numEntries,
		boolean			randomiseVerification,
		int				numThreads,
		boolean			verifyIncrementally,
		AtomicLong		attemptsCount,
		ICancellable	taskStatus)
		throws BaseException
	{
		// Initialise bit array of cell indices
		BitSet cellIndices = (numEntries > 0) ? null : new BitSet(values.length);
		if (cellIndices != null)
		{
			for (int index : entryIndices())
				cellIndices.set(index);
		}

		// Get actual number of threads
		int actualNumThreads = actualNumThreads(numThreads);

		// Generate entries of puzzle
		new SubtractiveGenerator().generate(numEntries, cellIndices, new PrngXoshiro256ss(seed), randomiseVerification,
											actualNumThreads, verifyIncrementally, attemptsCount, taskStatus, entries ->
		{
			// Update values with generated entries
			setValues(entries);

			// Add generation-info property to puzzle
			addProperty(Puzzle.Key.GENERATION_INFO,
						GenerationMode.SUBTRACTIVE.versionString() + ", " + seed + ", " + numEntries + ", "
								+ randomiseVerification + ", " + actualNumThreads);
		});
	}

	//------------------------------------------------------------------

	public void writeText(
		Path	file)
		throws FileException
	{
		try
		{
			// Write string representation of this puzzle to file
			Files.writeString(file, toText(true), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			throw new FileException(ErrorMsg.ERROR_WRITING_FILE, file, e);
		}
	}

	//------------------------------------------------------------------

	public void writeJson(
		Path		file,
		FileKind	fileKind)
		throws FileException
	{
		// Create root node
		MapNode rootNode = new MapNode();

		// Encode ID
		rootNode.addString(PropertyKey.ID, ID);

		// Encode version
		rootNode.addInt(PropertyKey.VERSION, VERSION);

		// Encode kind
		rootNode.addString(PropertyKey.KIND, fileKind.key());

		// Encode description
		rootNode.addString(PropertyKey.DESCRIPTION, fileKind.description());

		// Create puzzle node
		MapNode puzzleNode = rootNode.addMap(PropertyKey.PUZZLE);

		// Encode order
		puzzleNode.addInt(PropertyKey.ORDER, order);

		// Encode symbols
		puzzleNode.addString(PropertyKey.SYMBOLS, new String(symbols));

		// Encode displayed value
		boolean homogeniseValues = false;
		if (fileKind == FileKind.TEMPLATE)
		{
			int nonZeroValues = 0;
			for (int i = 0; i < values.length; i++)
			{
				if (values[i] > 0)
					nonZeroValues |= 1 << values[i];
			}
			if (Integer.bitCount(nonZeroValues) == 1)
			{
				puzzleNode.addInt(PropertyKey.DISPLAYED_VALUE, Integer.numberOfTrailingZeros(nonZeroValues));
				homogeniseValues = true;
			}
		}

		// Encode rows
		ListNode rowsNode = puzzleNode.addList(PropertyKey.ROWS);
		for (int i = 0; i < numRows; i++)
		{
			// Create array of cell values of row
			int[] rowValues = new int[numColumns];
			boolean notEmpty = false;
			int offset = i * numColumns;
			for (int j = 0; j < numColumns; j++)
			{
				int value = values[offset + j];
				if (value > 0)
				{
					if (homogeniseValues)
						value = 1;
					notEmpty = true;
				}
				rowValues[j] = value;
			}

			// If row contains a non-empty cell, add node for it
			if (notEmpty)
			{
				MapNode rowNode = new MapNode();
				rowNode.addInt(PropertyKey.INDEX, i);
				rowNode.addInts(PropertyKey.VALUES, rowValues);
				rowsNode.add(rowNode);
			}
		}

		// Encode properties
		if (!properties.isEmpty())
		{
			MapNode propertiesNode = puzzleNode.addMap(PropertyKey.PROPERTIES);
			for (Key key : properties.keySet())
				propertiesNode.addString(key.key, properties.get(key));
		}

		// Write file
		try
		{
			JsonUtils.writeFile(file, rootNode, JsonGenerator.builder().maxLineLength(128).build());
		}
		catch (IOException e)
		{
			throw new FileException(ErrorMsg.ERROR_WRITING_FILE, file, e);
		}
	}

	//------------------------------------------------------------------

	private Solver solver(
		int				maxNumSolutions,
		IPrng			prng,
		int				cancellationLevel,
		AtomicLong		solutionsCount,
		ICancellable	taskStatus)
	{
		return new Solver(maxNumSolutions, prng, cancellationLevel, solutionsCount, taskStatus);
	}

	//------------------------------------------------------------------

	private int rowColumnToBlock(
		int	row,
		int	column)
	{
		return row / order * order + column / order;
	}

	//------------------------------------------------------------------

	private int[] cellIndicesOfBlock(
		int	cellIndex)
	{
		int blockIndex = cellIndex / (order * cellsPerBlock) * order + cellIndex % numColumns / order;
		return CELL_INDICES_IN_BLOCK.get(puzzleOrder)[blockIndex];
	}

	//------------------------------------------------------------------

	private void initAvailableValues()
	{
		int[] flags = new int[numRows];
		Arrays.fill(flags, (1 << symbols.length) - 1);
		availableValues = new AvailableValues(flags, flags.clone(), flags.clone());
	}

	//------------------------------------------------------------------

	private boolean hasPotentialSwappablePairs(
		int[]	cellIndices)
	{
		// Initialise bit array of flags for entries
		BitSet entries = new BitSet();
		for (int i = 0; i < cellIndices.length; i++)
			entries.set(cellIndices[i]);

		// Check for potential swappable pairs in block rows
		for (int br = 0; br < order; br++)
		{
			for (int r0 = 0; r0 < order - 1; r0++)
			{
				for (int r1 = r0 + 1; r1 < order; r1++)
				{
					for (int c0 = 0; c0 < numColumns - order; c0++)
					{
						for (int c1 = (c0 / order + 1) * order; c1 < numColumns; c1++)
						{
							int brBase = br * order * numColumns;
							if (entries.get(brBase + r0 * numColumns + c0)
									&& entries.get(brBase + r0 * numColumns + c1)
									&& entries.get(brBase + r1 * numColumns + c0)
									&& entries.get(brBase + r1 * numColumns + c1))
								return true;
						}
					}
				}
			}
		}

		// Check for potential swappable pairs in block columns
		for (int bc = 0; bc < order; bc++)
		{
			for (int c0 = 0; c0 < order - 1; c0++)
			{
				for (int c1 = c0 + 1; c1 < order; c1++)
				{
					for (int r0 = 0; r0 < numRows - order; r0++)
					{
						for (int r1 = (r0 / order + 1) * order; r1 < numRows; r1++)
						{
							int bcBase = bc * order;
							if (entries.get(bcBase + r0 * numColumns + c0)
									&& entries.get(bcBase + r0 * numColumns + c1)
									&& entries.get(bcBase + r1 * numColumns + c0)
									&& entries.get(bcBase + r1 * numColumns + c1))
								return true;
						}
					}
				}
			}
		}

		// Indicate no potential swappable pairs
		return false;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: PUZZLE ORDER


	public enum Order
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		_2
		(
			2,
			"1234"
		),

		_3
		(
			3,
			"123456789"
		),

		_4
		(
			4,
			"ABCDEFGHJKLMNOPQ"
		),

		_5
		(
			5,
			"ABCDEFGHJKLMNOPQRSTUVWXYZ"
		);

		public static final	Order	MIN	=
				Arrays.stream(values()).min(Comparator.comparingInt(Order::value)).orElse(null);
		public static final	Order	MAX	=
				Arrays.stream(values()).max(Comparator.comparingInt(Order::value)).orElse(null);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	int		value;
		private	char[]	defaultSymbols;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Order(
			int		value,
			String	defaultSymbols)
		{
			// Initialise instance variables
			key = Integer.toString(value);
			this.value = value;
			this.defaultSymbols = defaultSymbols.toCharArray();
			if (numSymbols() != value * value)
				throw new UnexpectedRuntimeException();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Order forValue(
			int	value)
		{
			return Arrays.stream(values()).filter(order -> order.value == value).findFirst().orElse(null);
		}

		//--------------------------------------------------------------

		public static EnumMap<Order, char[]> defaultSymbolMap()
		{
			EnumMap<Order, char[]> map = new EnumMap<>(Order.class);
			for (Order order : values())
				map.put(order, order.defaultSymbols.clone());
			return map;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public String key()
		{
			return key;
		}

		//--------------------------------------------------------------

		public int value()
		{
			return value;
		}

		//--------------------------------------------------------------

		public int numSymbols()
		{
			return defaultSymbols.length;
		}

		//--------------------------------------------------------------

		public int pow2()
		{
			return value * value;
		}

		//--------------------------------------------------------------

		public int pow3()
		{
			return value * value * value;
		}

		//--------------------------------------------------------------

		public int pow4()
		{
			return value * value * value * value;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: PROPERTY KEY


	public enum Key
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		GENERATION_INFO
		(
			"Generation information"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Key(
			String	text)
		{
			// Initialise instance variables
			key = StringUtils.toCamelCase(name());
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: PUZZLE ENTRY


	public record Entry(
		int	index,
		int	value)
	{ }

	//==================================================================


	// RECORD: PAIR OF INDICES


	public record IndexPair(
		int	i1,
		int	i2)
	{ }

	//==================================================================


	// RECORD: INFORMATION ABOUT A PUZZLE FILE


	public record FileInfo(
		Path	file,
		Puzzle	puzzle,
		boolean	template)
	{ }

	//==================================================================


	// RECORD: BIT ARRAYS OF FLAGS OF AVAILABLE VALUES FOR ROWS, COLUMNS AND BLOCKS


	private record AvailableValues(
		int[]	rows,
		int[]	columns,
		int[]	blocks)
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public AvailableValues clone()
		{
			return new AvailableValues(rows.clone(), columns.clone(), blocks.clone());
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: ARRAY OF INDICES


	private static class IndexArray
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	ARRAY_LENGTHS_DIFFER_STR	= "Array lengths differ";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int[]	indices;
		private	int		minHammingDistance;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private IndexArray(
			int[]	indices)
		{
			// Initialise instance variables
			this.indices = indices;
			minHammingDistance = -1;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static IndexArray init(
			int	numIndices)
		{
			// Validate argument
			if (numIndices < 0)
				throw new IllegalArgumentException("Number of indices out of bounds");

			// Create array of sequential indices
			int[] indices = new int[numIndices];
			for (int i = 0; i < numIndices; i++)
				indices[i] = i;

			// Return new index-array object
			return new IndexArray(indices);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(
			Object	obj)
		{
			if (this == obj)
				return true;

			return  (obj instanceof IndexArray other) && Arrays.equals(indices, other.indices);
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return Arrays.hashCode(indices);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			StringBuilder buffer = new StringBuilder(128);
			buffer.append('[');
			for (int i = 0; i < indices.length; i++)
			{
				if (i > 0)
					buffer.append(", ");
				buffer.append(indices[i]);
			}
			buffer.append("] : ").append(minHammingDistance);
			return buffer.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private int hammingDistance(
			IndexArray	other)
		{
			// Validate argument
			if (other.indices.length != indices.length)
				throw new IllegalArgumentException(ARRAY_LENGTHS_DIFFER_STR);

			// Count elements that differ
			int hammingDistance = 0;
			for (int i = 0; i < indices.length; i++)
			{
				if (indices[i] != other.indices[i])
					++hammingDistance;
			}
			return hammingDistance;
		}

		//--------------------------------------------------------------

		private void updateMinHammingDistance(
			Iterable<? extends IndexArray>	indexArrays)
		{
			int count = 0;
			minHammingDistance = indices.length;
			for (IndexArray indexArray : indexArrays)
			{
				if (indexArray != this)
				{
					int distance = hammingDistance(indexArray);
					if (minHammingDistance > distance)
						minHammingDistance = distance;
					++count;
				}
			}
			if (count == 0)
				minHammingDistance = -1;
		}

		//--------------------------------------------------------------

		private int[] permute(
			int[]	values)
		{
			// Validate argument
			if (values.length != indices.length)
				throw new IllegalArgumentException(ARRAY_LENGTHS_DIFFER_STR);

			// Permute values by applying array of indices
			int[] permutedValues = new int[indices.length];
			for (int i = 0; i < indices.length; i++)
				permutedValues[i] = values[indices[i]];

			// Return permuted values
			return permutedValues;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PARSE EXCEPTION


	private static class ParseException
		extends BaseException
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	LINE_STR	= "Line ";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ParseException(
			String	message,
			int		lineIndex)
		{
			// Call superclass constructor
			super(LINE_STR + (lineIndex + 1) + ": " + message);
		}

		//--------------------------------------------------------------

		private ParseException(
			BaseException	exception,
			int				lineIndex)
		{
			// Call alternative constructor
			this(exception.getMessage(), lineIndex);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: PUZZLE SOLVER


	private class Solver
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int				maxNumSolutions;
		private	IPrng			prng;
		private	int				cancellationLevel;
		private	AtomicLong		solutionsCount;
		private	ICancellable	taskStatus;
		private	List<Puzzle>	solutions;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Solver(
			int				maxNumSolutions,
			IPrng			prng,
			int				cancellationLevel,
			AtomicLong		solutionsCount,
			ICancellable	taskStatus)
		{
			// Initialise instance variables
			this.maxNumSolutions = maxNumSolutions;
			this.prng = prng;
			this.cancellationLevel = cancellationLevel;
			this.solutionsCount = solutionsCount;
			this.taskStatus = taskStatus;
			solutions = new ArrayList<>();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private List<Puzzle> solve()
			throws TaskCancelledException
		{
			// Test for flags for available values
			boolean hasAvailableValues = (availableValues != null);

			// Initialise flags for available values
			if (!hasAvailableValues)
			{
				// Create the bit arrays of flags that indicate the values that are available in the rows, columns and
				// blocks of the puzzle
				initAvailableValues();

				// Clear the appropriate 'available' flags for rows, columns and blocks that contain non-empty cells
				for (int row = 0; row < numRows; row++)
				{
					for (int column = 0; column < numColumns; column++)
					{
						int value = values[row * numColumns + column];
						if (value > 0)
						{
							--value;
							int mask = ~(1 << value);
							availableValues.rows[row] &= mask;
							availableValues.columns[column] &= mask;
							availableValues.blocks[rowColumnToBlock(row, column)] &= mask;
						}
					}
				}
			}

			// Clear the list of solutions
			solutions.clear();

			// Start the search for solutions
			solve(0);

			// Invalidate flags for available values
			if (!hasAvailableValues)
				availableValues = null;

			// Return a list of the solutions that were found
			return solutions;
		}

		//--------------------------------------------------------------

		private void solve(
			int	level)
			throws TaskCancelledException
		{
			// Test whether task has been cancelled
			if ((level >= cancellationLevel) && taskStatus.isCancelled())
				throw new TaskCancelledException();

			// Initialise variables
			int minCount = Integer.MAX_VALUE;
			int argMinRow = 0;
			int argMinColumn = 0;
			int argMinAvailable = 0;

			// Search for the empty cell that has the fewest available values.
			// Iterate over the rows of the grid
			rowLoop:
			for (int row = 0; row < numRows; row++)
			{
				// Stop if thread is interrupted
				if (Thread.currentThread().isInterrupted())
					break;

				// Get the bit array of flags for the values that are available in the current row
				int availRow = availableValues.rows[row];

				// Skip the row if no values are available
				if (availRow == 0)
					continue;

				// Iterate over the columns of the grid
				for (int column = 0; column < numColumns; column++)
				{
					// Get the bit array of flags for the values that are available in the current column
					int availColumn = availableValues.columns[column];

					// Skip the column if no values are available or if the cell at the current row and column is not
					// empty
					if ((availColumn == 0) || (values[row * numColumns + column] > 0))
						continue;

					// Get the bit array of flags for the values that are available in the current block
					int availBlock = availableValues.blocks[rowColumnToBlock(row, column)];

					// Skip the column if no values are available in the current block
					if (availBlock == 0)
						continue;

					// Combine flags for row, column and block to give a bit array of flags for the values that are
					// available for the current cell
					int available = availRow & availColumn & availBlock;

					// If no values are available for the current cell, the current search path does not lead to a
					// solution
					if (available == 0)
						return;

					// If the current cell has fewer available values than previous cells, update the minimum count of
					// the available values of a cell and the associated arg min variables
					int count = Integer.bitCount(available);
					if (count < minCount)
					{
						minCount = count;
						argMinRow = row;
						argMinColumn = column;
						argMinAvailable = available;
					}

					// If there is only one available value for the current cell, stop searching for better candidates
					if (count == 1)
						break rowLoop;
				}
			}

			// If no empty cell was found, the puzzle has been solved: add the solution to the list ...
			if (argMinAvailable == 0)
			{
				if (solutions.size() < maxNumSolutions)
				{
					// Add solution to list
					solutions.add(Puzzle.this.clone());

					// Increment count
					if (solutionsCount != null)
						solutionsCount.incrementAndGet();
				}
			}

			// ...  otherwise, try each of the available values in the candidate cell
			else
			{
				// Convert the bit array of flags to an array of available values
				int[] availValues = flagsToIndices(argMinAvailable, minCount);

				// If a PRNG has been supplied, permute the array of available values so that the values are tried in a
				// random order
				if (prng != null)
					permuteIntArray(availValues, prng);

				// Try each of the available values in the candidate cell
				int row = argMinRow;
				int column = argMinColumn;
				int block = rowColumnToBlock(row, column);
				int cellIndex = row * numColumns + column;
				for (int value : availValues)
				{
					// Create masks for the bit arrays of flags of available values
					int orMask = 1 << value;
					int andMask = ~orMask;

					// Make the trial value unavailable in further iterations of this method
					availableValues.rows[row] &= andMask;
					availableValues.columns[column] &= andMask;
					availableValues.blocks[block] &= andMask;

					// Set the cell value
					values[cellIndex] = (byte)(value + 1);

					// Continue the search with the next empty cell
					solve(level + 1);

					// Restore the empty cell
					values[cellIndex] = 0;

					// Restore the availability of the trial value
					availableValues.rows[row] |= orMask;
					availableValues.columns[column] |= orMask;
					availableValues.blocks[block] |= orMask;

					// If all the required solutions have been found, stop
					if (solutions.size() >= maxNumSolutions)
						break;
				}
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PUZZLE GENERATOR - ADDITIVE, COMMON


	private class AdditiveGenerator
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int[]			cellIndices;
		private	byte[]			cellValues;
		private	boolean			checkSwappablePairs;
		private	IPrng			prng;
		private	Solver			solver;
		private	AtomicLong		attemptsCount;
		private	ICancellable	taskStatus;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private AdditiveGenerator()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private List<Entry> generate(
			int[]			cellIndices,
			byte[]			cellValues,
			boolean			checkSwappablePairs,
			int				index,
			IPrng			prng,
			Solver			solver,
			AtomicLong		attemptsCount,
			ICancellable	taskStatus)
			throws BaseException
		{
			// Initialise instance variables
			this.cellIndices = cellIndices;
			this.cellValues = cellValues;
			this.checkSwappablePairs = checkSwappablePairs;
			this.prng = prng;
			this.solver = solver;
			this.attemptsCount = attemptsCount;
			this.taskStatus = taskStatus;

			// Generate random values for the remaining target cells
			if (generate(index))
			{
				// Set values on the target cells
				for (int i = index; i < cellIndices.length; i++)
					setValue(cellIndices[i], cellValues[i]);

				// Return entries
				return entries();
			}

			// Indicate failure
			return null;
		}

		//--------------------------------------------------------------

		private boolean generate(
			int	index)
			throws BaseException
		{
			// Stop if thread is interrupted
			if (Thread.currentThread().isInterrupted())
				return false;

			// Test whether task has been cancelled
			if (taskStatus.isCancelled())
				throw new TaskCancelledException();

			// If values have been generated for all the target cells, solve the puzzle
			if (index >= cellIndices.length)
			{
				// Test for maximum number of attempts
				if (attemptsCount.get() >= MAX_NUM_GENERATION_ATTEMPTS)
					throw new BaseException(ErrorMsg.MAX_NUM_GENERATION_ATTEMPTS_REACHED);

				// Increment count of attempts
				attemptsCount.incrementAndGet();

				// Solve the puzzle: generation is successful if there is a unique solution
				return (solver.solve().size() == 1);
			}

			// Find the target cell with the fewest available values
			int cellIndex = 0;
			int available = 0;
			int minCountIndex = 0;
			int minCount = Integer.MAX_VALUE;
			for (int i = index; i < cellIndices.length; i++)
			{
				int cIndex = cellIndices[i];
				int row = cIndex / numColumns;
				int column = cIndex % numColumns;
				int block = rowColumnToBlock(row, column);
				int avail = availableValues.rows[row] & availableValues.columns[column] & availableValues.blocks[block];
				int count = Integer.bitCount(avail);
				if (count < minCount)
				{
					minCount = count;
					minCountIndex = i;
					cellIndex = cIndex;
					available = avail;
				}
			}

			// If the target cell with the fewest available values has no available values, the current search path does
			// not lead to a solution
			if (available == 0)
				return false;

			// If necessary, swap the index of the next cell with the index of the cell with the fewest available values
			if (index != minCountIndex)
			{
				cellIndices[minCountIndex] = cellIndices[index];
				cellIndices[index] = cellIndex;
			}

			// Remove available values that belong to swappable pairs
			if (checkSwappablePairs && (index >= 4))
				available &= ~findSwappablePairs(cellIndex);

			// Convert the flags to an array of the available values for the current cell
			int[] availValues = flagsToIndices(available, Integer.bitCount(available));

			// Permute array of available values
			permuteIntArray(availValues, prng);

			// Initialise row, column and block indices
			int row = cellIndex / numColumns;
			int column = cellIndex % numColumns;
			int block = rowColumnToBlock(row, column);

			// Try the available values for the current cell
			boolean succeeded = false;
			for (int value : availValues)
			{
				// Create masks for the bit arrays of flags of available values
				int orMask = 1 << value;
				int andMask = ~orMask;

				// Make the trial value unavailable in further iterations of this method
				availableValues.rows[row] &= andMask;
				availableValues.columns[column] &= andMask;
				availableValues.blocks[block] &= andMask;

				// Set the cell value
				values[cellIndex] = cellValues[index] = (byte)(value + 1);

				// Continue with the next cell
				succeeded = generate(index + 1);

				// Restore the empty cell
				values[cellIndex] = 0;

				// Restore the availability of the trial value
				availableValues.rows[row] |= orMask;
				availableValues.columns[column] |= orMask;
				availableValues.blocks[block] |= orMask;

				// If values were generated for all the specified cells, stop
				if (succeeded)
					break;
			}

			// Return result
			return succeeded;
		}

		//--------------------------------------------------------------

		/**
		 * Searches the current puzzle for sets of four cells that include the cell at the specified index (the
		 * <i>target cell</i>) and that satisfy the conditions listed below.  Such a set of cells constitutes <i>a pair
		 * of swappable pairs</i> whose existence indicates that a puzzle has multiple solutions.  This method returns a
		 * bit array of flags that correspond to values for the target cell that would result in a pair of swappable
		 * pairs.  Those values can be eliminated as possible values of the target cell when generating a puzzle, since
		 * a puzzle must have a unique solution.
		 * <p>
		 * The following conditions are applied when searching for sets of four cells that constitute a pair of
		 * swappable pairs.  In the conditions, <i>R1</i> and <i>R2</i> denote two distinct rows of cells, <i>C1</i>
		 * and <i>C2</i> denote two distinct columns of cells, and <i>B1</i> and <i>B2</i> denote two distinct blocks of
		 * cells.
		 * <ol>
		 *   <li>
		 *     One of the four cells must be the target cell.  The current value of the target cell is ignored; instead,
		 *     the cell serves as a placeholder for values that are assigned to it in the way described in item 5.
		 *   </li>
		 *   <li>
		 *     Each of the other three cells must contain a value.
		 *   </li>
		 *   <li>
		 *     All four cells must lie within either a single <i>row of blocks</i> or a single column of blocks.  A
		 *     <i>block</i> is one of the principal regions of the grid: a square of <i>order</i> &times; <i>order</i>
		 *     cells.
		 *   </li>
		 *   <li>
		 *     The four cells must be split pairwise between two rows, two columns and two blocks; that is,
		 *     <ul style="margin-bottom: 0.2em;">
		 *       <li>
		 *         two cells must lie in row <i>R1</i> and the other two cells must lie in row <i>R2</i> <b>and</b>
		 *       </li>
		 *       <li>
		 *         two cells must lie in column <i>C1</i> and the other two cells must lie in column <i>C2</i>
		 *         <b>and</b>
		 *       </li>
		 *       <li>
		 *         two cells must lie in block <i>B1</i> and the other two cells must lie in block <i>B2</i>.
		 *       </li>
		 *     </ul>
		 *     These conditions imply that the four cells lie at the corners of a rectangular region of the grid that
		 *     spans multiple blocks in one dimension.
		 *   </li>
		 *   <li>
		 *     If the target cell is at (<i>R1</i>, <i>C1</i>) and the cells at (<i>R1</i>, <i>C2</i>) and (<i>R2</i>,
		 *     <i>C1</i>) have the same value, the value of the cell at (<i>R2</i>, <i>C2</i>) is provisionally assigned
		 *     to the target cell, thereby creating two swappable pairs.
		 *   </li>
		 * </ol>
		 * <p>
		 * If a puzzle is valid when the target cell is assigned a provisional value to create two swappable pairs, it
		 * will remain valid after the elements of <i>both</i> pairs are swapped; therefore, the puzzle has at least two
		 * solutions.
		 * </p>
		 * <b>Example:</b><br>
		 * The figure below is intended to represent part of the grid of an order-3 puzzle in which the target cell is
		 * denoted by <i>X</i>.  Two sets of two swappable pairs are shown:
		 * <ul>
		 *   <li>
		 *     If the target cell is assigned the value 9, there will be two swappable pairs of values 2 and 9 in rows 1
		 *     and 3.  The validity of the puzzle will not be affected if, in those four cells, the value 2 is replaced
		 *     by 9 and the value 9 is replaced by 2.
		 *   </li>
		 *   <li>
		 *     If the target cell is assigned the value 7, there will be two swappable pairs of values 4 and 7 in rows 3
		 *     and 5.  The validity of the puzzle will not be affected if, in those four cells, the value 4 is replaced
		 *     by 7 and the value 7 is replaced by 4.
		 *   </li>
		 * </ul>
		 *
		 * <pre>
		 *     ++===+===+===++===+===+===++===+===+===++
		 * 1 - ||   |   | 9 ||   |   |   ||   | 2 |   ||
		 *     ++---+---+---++---+---+---++---+---+---++
		 * 2 - ||   |   |   ||   |   |   ||   |   |   ||
		 *     ++---+---+---++---+---+---++---+---+---++
		 * 3 - ||   | 4 | X ||   |   |   ||   | 9 |   ||
		 *     ++===+===+===++===+===+===++===+===+===++
		 * 4 - ||   |   |   ||   |
		 *     ++---+---+---++---+-
		 * 5 - ||   | 7 | 4 ||   |
		 *     ++---+---+---++---+-
		 * 6 - ||   |   |   ||   |
		 *     ++===+===+===++===+=</pre>
		 *
		 * @param  cellIndex
		 *           the index of the target cell.
		 * @return a bit array of <i>order</i><sup>2</sup> bits, in which the bit at index <i>n</i> corresponds to the
		 *         cell value (<i>n</i> + 1).  A set bit (ie, a bit whose value is 1) indicates that, if the target cell
		 *         were set to the corresponding cell value, it would be one of four entries that constitute two
		 *         swappable pairs of entries as described above.
		 */

		private int findSwappablePairs(
			int	cellIndex)
		{
			// Initialise bit array of flags for values that are found to belong to a swappable pair
			int valueFlags = 0;

			// Get row and column index from cell index
			int row = cellIndex / numColumns;
			int column = cellIndex % numColumns;

			// Search for swappable pairs in the rows of the block row that contains the target cell
			int blockColumn = column / order;
			int r0 = row / order * order;
			int r1 = r0 + order;
			int iR = row * numColumns;
			for (int r = r0, i = r0 * numColumns + column; r < r1; r++, i += numColumns)
			{
				if (r == row)
					continue;
				int value = values[i];
				if (value == 0)
					continue;
				int i0 = iR;
				int i1 = r * numColumns;
				for (int bc = 0; bc < order; bc++)
				{
					if (bc == blockColumn)
					{
						i0 += order;
						i1 += order;
					}
					else
					{
						for (int k = 0; k < order; k++)
						{
							if (values[i0] == value)
							{
								int v = values[i1];
								if (v > 0)
									valueFlags |= 1 << v - 1;
							}
							++i0;
							++i1;
						}
					}
				}
			}

			// Search for swappable pairs in the columns of the block column that contains the target cell
			int blockRow = row / order;
			int cellsPerBlockRow = order * numColumns;
			int c0 = column / order * order;
			int c1 = c0 + order;
			for (int c = c0, i = row * numColumns + c0; c < c1; c++, i++)
			{
				if (c == column)
					continue;
				int value = values[i];
				if (value == 0)
					continue;
				int i0 = column;
				int i1 = c;
				for (int br = 0; br < order; br++)
				{
					if (br == blockRow)
					{
						i0 += cellsPerBlockRow;
						i1 += cellsPerBlockRow;
					}
					else
					{
						for (int k = 0; k < order; k++)
						{
							if (values[i0] == value)
							{
								int v = values[i1];
								if (v > 0)
									valueFlags |= 1 << v - 1;
							}
							i0 += numColumns;
							i1 += numColumns;
						}
					}
				}
			}

			// Return result
			return valueFlags;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PUZZLE GENERATOR - ADDITIVE #1


	/**
	 * This class implements a generator of a sudoku puzzle that has a specified number of entries.
	 */

	private class AdditiveGenerator1
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int				numEntries;
		private	boolean			randomiseVerification;
		private	ICancellable	taskStatus;
		private	AtomicLong		attemptsCount;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private AdditiveGenerator1()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void generate(
			int							numEntries,
			IPrng						prng,
			boolean						randomiseVerification,
			int							numThreads,
			AtomicLong					attemptsCount,
			ICancellable				taskStatus,
			IProcedure1<List<Entry>>	onSuccess)
			throws BaseException
		{
			// Initialise instance variables
			this.numEntries = numEntries;
			this.randomiseVerification = randomiseVerification;
			this.attemptsCount = attemptsCount;
			this.taskStatus = taskStatus;

			// Create a generation task for each thread
			List<Callable<List<Entry>>> tasks = new ArrayList<>();
			for (int i = 0; i < numThreads; i++)
				tasks.add(() -> generate(prng.child()));

			// Execute tasks
			String threadNamePrefix = getClass().getSimpleName();
			ExecutorService executor = Executors.newFixedThreadPool(numThreads, runnable ->
					DaemonFactory.create(threadNamePrefix + "-" + ++threadIndex, runnable));
			try
			{
				// Wait for a task to complete successfully
				List<Entry> entries = executor.invokeAny(tasks);

				// If no entries were generated, report failure
				if (entries == null)
					throw new BaseException(ErrorMsg.FAILED_TO_GENERATE_PUZZLE);

				// Set entries on puzzle
				onSuccess.invoke(entries);
			}
			catch (ExecutionException e)
			{
				Throwable cause = e.getCause();
				if (cause instanceof BaseException be)
					throw be;
				throw new BaseException(ErrorMsg.ERROR_GENERATING_PUZZLE, cause);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				executor.shutdownNow();
			}
		}

		//--------------------------------------------------------------

		private List<Entry> generate(
			IPrng	parentPrng)
			throws BaseException
		{
			// Repeatedly generate entries for a set of randomly generated cell indices until a set of entries has a
			// unique solution
			while (true)
			{
				// Stop if thread is interrupted
				if (Thread.currentThread().isInterrupted())
					return null;

				// Test whether task has been cancelled
				if (taskStatus.isCancelled())
					throw new TaskCancelledException();

				// Create PRNG
				IPrng prng = parentPrng.child();

				// Create array of randomly generated cell indices
				int[] indices = new int[values.length];
				for (int j = 0; j < indices.length; j++)
					indices[j] = j;
				permuteIntArray(indices, prng);
				int[] cellIndices = Arrays.copyOfRange(indices, 0, numEntries);

				// Create puzzle and solver for it
				Puzzle puzzle = new Puzzle(puzzleOrder, false);
				puzzle.initAvailableValues();
				Solver solver = puzzle.solver(2, randomiseVerification ? prng : null, GENERATE_CANCELLATION_TEST_LEVEL,
											  null, taskStatus);

				// Initialise array of cell values
				byte[] cellValues = new byte[cellIndices.length];

				// Set a random value on the first of the target cells
				int value = prng.nextInt(symbols.length) + 1;
				cellValues[0] = (byte)value;
				puzzle.setValue(cellIndices[0], value);

				// Generate random values for the remaining target cells
				List<Entry> entries = puzzle.new AdditiveGenerator()
						.generate(cellIndices, cellValues, hasPotentialSwappablePairs(cellIndices), 1, prng, solver,
								  attemptsCount, taskStatus);
				if (entries != null)
					return entries;
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PUZZLE GENERATOR - ADDITIVE #2


	/**
	 * This class implements a generator of a sudoku puzzle that has entries at a specified set of cell indices.
	 */

	private class AdditiveGenerator2
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	NUM_INDEX_ARRAYS_FACTOR				= 20;
		private static final	int	NUM_RETAINED_INDEX_ARRAYS_FACTOR	= 10;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private AdditiveGenerator2()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void generate(
			int[]						cellIndices,
			IPrng						parentPrng,
			boolean						randomiseVerification,
			int							numThreads,
			AtomicLong					attemptsCount,
			ICancellable				taskStatus,
			IProcedure1<List<Entry>>	onSuccess)
			throws BaseException
		{
			// Initialise variables
			boolean checkSwappablePairs = hasPotentialSwappablePairs(cellIndices);
			String threadNamePrefix = getClass().getSimpleName();
			int numCells = cellIndices.length;

			// Initialise list of arrays of indices that will be used to permute cell indices
			List<IndexArray> indexArrays = new ArrayList<>();
			indexArrays.add(IndexArray.init(numCells));

			// Generate random permutations of indices
			IPrng indexArrayPrng = parentPrng.child();
			int numIndexArrays = NUM_INDEX_ARRAYS_FACTOR * numThreads;
			while (indexArrays.size() < numIndexArrays)
			{
				IndexArray indexArray = new IndexArray(indexArrayPrng.permutedIndices(numCells));
				if (!indexArrays.contains(indexArray))
					indexArrays.add(indexArray);
			}

			// Update smallest Hamming distance of each array of indices from all other arrays of indices
			for (IndexArray indexArray : indexArrays)
				indexArray.updateMinHammingDistance(indexArrays);

			// Sort arrays of indices in descending order of smallest Hamming distance
			indexArrays.sort(Comparator.<IndexArray>comparingInt(ia -> ia.minHammingDistance).reversed());

			// Remove upper part of list of arrays of indices
			numIndexArrays = NUM_RETAINED_INDEX_ARRAYS_FACTOR * numThreads;
			while (indexArrays.size() < numIndexArrays)
				indexArrays.remove(indexArrays.size() - 1);

			// Get iterator over list of index arrays
			Iterator<IndexArray> indexArrayIt = indexArrays.iterator();

			// Create factory for puzzle-generation tasks
			IFunction0<Callable<List<Entry>>> taskFactory = () ->
			{
				// If there are no more index arrays, stop creating tasks
				if (!indexArrayIt.hasNext())
					return null;

				// Create puzzle
				Puzzle puzzle = new Puzzle(puzzleOrder, false);
				puzzle.initAvailableValues();

				// Create PRNG and solver for puzzle
				IPrng prng = parentPrng.child();
				Solver solver = puzzle.solver(2, randomiseVerification ? prng : null,
											  GENERATE_CANCELLATION_TEST_LEVEL, null, taskStatus);

				// Create permutation of cell indices
				int[] cellIndices0 = indexArrayIt.next().permute(cellIndices);

				// Initialise array of cell values
				byte[] cellValues = new byte[numCells];

				// Set a random value on the first of the target cells
				int value = prng.nextInt(symbols.length) + 1;
				cellValues[0] = (byte)value;
				puzzle.setValue(cellIndices0[0], value);

				// Create generation task and return it
				return () -> puzzle.new AdditiveGenerator()
						.generate(cellIndices0, cellValues, checkSwappablePairs, 1, prng, solver, attemptsCount,
								  taskStatus);
			};

			// Execute tasks
			ExecutorService executor = Executors.newFixedThreadPool(numThreads, runnable ->
					DaemonFactory.create(threadNamePrefix + "-" + ++threadIndex, runnable));
			CompletionService<List<Entry>> completer = new ExecutorCompletionService<>(executor);
			List<Entry> entries = null;
			try
			{
				// Submit a task for each thread
				for (int i = 0; i < numThreads; i++)
					completer.submit(taskFactory.invoke());

				// Wait for tasks to complete
				for (int i = 0; i < numThreads; i++)
				{
					try
					{
						// Wait for task to complete
						List<Entry> result = completer.take().get();

						// If task failed to generate entries, submit a new task ...
						if (result == null)
						{
							Callable<List<Entry>> task = taskFactory.invoke();
							if (task != null)
								completer.submit(task);
						}

						// ... otherwise, set result and stop
						else
						{
							entries = result;
							break;
						}
					}
					catch (ExecutionException e)
					{
						Throwable cause = e.getCause();
						if (cause instanceof BaseException be)
							throw be;
						throw new BaseException(ErrorMsg.ERROR_GENERATING_PUZZLE, cause);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			finally
			{
				executor.shutdownNow();
			}

			// If no entries were generated, report failure ...
			if (entries == null)
				throw new BaseException(ErrorMsg.FAILED_TO_GENERATE_PUZZLE);

			// ... otherwise, set entries on puzzle
			onSuccess.invoke(entries);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PUZZLE GENERATOR - SUBTRACTIVE


	private class SubtractiveGenerator
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int				numEntries;
		private	BitSet			cellIndices;
		private	boolean			randomiseVerification;
		private	boolean			verifyIncrementally;
		private	ICancellable	taskStatus;
		private	AtomicLong		attemptsCount;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SubtractiveGenerator()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void generate(
			int							numEntries,
			BitSet						cellIndices,
			IPrng						prng,
			boolean						randomiseVerification,
			int							numThreads,
			boolean						verifyIncrementally,
			AtomicLong					attemptsCount,
			ICancellable				taskStatus,
			IProcedure1<List<Entry>>	onSuccess)
			throws BaseException
		{
			// Initialise instance variables
			this.numEntries = numEntries;
			this.cellIndices = cellIndices;
			this.randomiseVerification = randomiseVerification;
			this.verifyIncrementally = verifyIncrementally;
			this.attemptsCount = attemptsCount;
			this.taskStatus = taskStatus;

			// Create a generation task for each thread
			List<Callable<List<Entry>>> tasks = new ArrayList<>();
			for (int i = 0; i < numThreads; i++)
				tasks.add(() -> generate(prng.child()));

			// Execute tasks
			String threadNamePrefix = getClass().getSimpleName();
			ExecutorService executor = Executors.newFixedThreadPool(numThreads, runnable ->
					DaemonFactory.create(threadNamePrefix + "-" + ++threadIndex, runnable));
			try
			{
				// Wait for a task to complete successfully
				List<Entry> entries = executor.invokeAny(tasks);

				// If no entries were generated, report failure
				if (entries == null)
					throw new BaseException(ErrorMsg.FAILED_TO_GENERATE_PUZZLE);

				// Execute 'successful generation' procedure
				onSuccess.invoke(entries);
			}
			catch (ExecutionException e)
			{
				Throwable cause = e.getCause();
				if (cause instanceof BaseException be)
					throw be;
				throw new BaseException(ErrorMsg.ERROR_GENERATING_PUZZLE, cause);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				executor.shutdownNow();
			}
		}

		//--------------------------------------------------------------

		private List<Entry> generate(
			IPrng	prng)
			throws BaseException
		{
			// Initialise variables
			Puzzle puzzle = new Puzzle(puzzleOrder, false);
			puzzle.initAvailableValues();
			Solver solver1 = puzzle.solver(1, prng, 0, null, taskStatus);
			Solver solver2 = puzzle.solver(2, randomiseVerification ? prng : null, 0, null, taskStatus);
			List<Puzzle> solutions = null;

			// Initialise result
			List<Entry> result = null;

			// Generate sets of random entries until a set of entries has a unique solution
			while (result == null)
			{
				// Stop if thread is interrupted
				if (Thread.currentThread().isInterrupted())
					break;

				// Test whether task has been cancelled
				if (taskStatus.isCancelled())
					throw new TaskCancelledException();

				// Test for maximum number of attempts
				if (attemptsCount.get() >= MAX_NUM_GENERATION_ATTEMPTS)
					throw new BaseException(ErrorMsg.MAX_NUM_GENERATION_ATTEMPTS_REACHED);

				// Find a random solution for an empty puzzle
				puzzle.clear();
				solutions = solver1.solve();

				// Get entries of solution
				List<Entry> entries = solutions.get(0).entries();

				// Case: verify puzzle after each unwanted entry is removed
				if (verifyIncrementally)
				{
					// Set entries on puzzle
					puzzle.setValues(entries);

					// Invalidate solutions
					solutions = null;

					// Create function to test whether a cell has only a single available value
					IFunction1<Boolean, Integer> isSingleAvailableValue = cellIndex ->
					{
						int row = cellIndex / puzzle.numColumns;
						int column = cellIndex % puzzle.numColumns;
						int block = rowColumnToBlock(row, column);
						int available = puzzle.availableValues.rows[row] & puzzle.availableValues.columns[column]
											& puzzle.availableValues.blocks[block];
						return (Integer.bitCount(available) == 1);
					};

					// If there are no target cells, remove randomly chosen entries until the number of entries is equal
					// to the required maximum ...
					if (cellIndices == null)
					{
						while (entries.size() > numEntries)
						{
							// Remove entry from list and clear cell
							Entry entry = entries.remove(prng.nextInt(entries.size()));
							puzzle.setValue(entry.index, 0);

							// If cell has only a single available value, skip verification on this iteration ...
							if (isSingleAvailableValue.invoke(entry.index))
								solutions = null;

							// ... otherwise, verify the puzzle
							else
							{
								solutions = solver2.solve();
								if (solutions.size() != 1)
									break;
							}
						}
					}

					// ... otherwise, remove entries for cells other than the target cells
					else
					{
						for (int j = entries.size() - 1; j >= 0; j--)
						{
							// If cell is a target cell, leave it
							if (cellIndices.get(j))
								continue;

							// Remove entry from list and clear cell
							Entry entry = entries.remove(j);
							puzzle.setValue(entry.index, 0);

							// If cell has only a single available value, skip verification on this iteration ...
							if (isSingleAvailableValue.invoke(entry.index))
								solutions = null;

							// ... otherwise, verify the puzzle
							else
							{
								solutions = solver2.solve();
								if (solutions.size() != 1)
									break;
							}
						}
					}

					// Verify the puzzle if verification was skipped after the last entry was removed
					if (solutions == null)
						solutions = solver2.solve();
				}

				// Case: verify puzzle once
				else
				{
					// If no cell indices were specified, remove randomly chosen entries until the number of entries is
					// equal to the required maximum ...
					if (cellIndices == null)
					{
						while (entries.size() > numEntries)
							entries.remove(prng.nextInt(entries.size()));
					}

					// ... otherwise, remove entries for cells other than the ones specified
					else
					{
						for (int j = entries.size() - 1; j >= 0; j--)
						{
							if (!cellIndices.get(j))
								entries.remove(j);
						}
					}

					// Set the remaining entries on the puzzle
					puzzle.setValues(entries);

					// Verify the puzzle
					solutions = solver2.solve();
				}

				// Increment count of attempts
				attemptsCount.incrementAndGet();

				// If there is a unique solution, set the result
				if (solutions.size() == 1)
					result = entries;
			}

			// Return result
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
