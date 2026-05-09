/*====================================================================*\

PuzzleIO.java

Class: input/output methods for sudoku puzzles.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

import java.util.List;
import java.util.Map;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;

import uk.blankaspect.common.filesystem.FilenameUtils;
import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.xml2.AttributeList;
import uk.blankaspect.common.xml2.XmlConstants;
import uk.blankaspect.common.xml2.XmlWriter;

//----------------------------------------------------------------------


// CLASS: INPUT/OUTPUT METHODS FOR SUDOKU PUZZLES


class PuzzleIO
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		INDENT_INCREMENT	= 2;

	/** Miscellaneous strings. */
	private static final	String	XML_VERSION_STR		= "1.0";
	private static final	String	XHTML_PUBLIC_ID		= "-//W3C//DTD XHTML 1.1//EN";
	private static final	String	XHTML_SYSTEM_ID		= "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd";
	private static final	String	NAMESPACE_STR		= "http://www.w3.org/1999/xhtml";
	private static final	String	LANG_STR			= "en";
	private static final	String	CONTENT_TYPE_STR	= "content-type";
	private static final	String	MIME_TYPE_STR		= "application/xhtml+xml;charset=UTF-8";
	private static final	String	TEXT_CSS_STR		= "text/css";
	private static final	String	MEDIA_STR			= "print,screen";

	private static final	Map<String, String>	SELECTORS	= Map.of
	(
		Selector.GRID,
		CssSelector.builder()
				.cls(ElementName.DIV, StyleClass.GRID)
				.build(),

		Selector.GRID_ROW,
		CssSelector.builder()
				.cls(ElementName.DIV, StyleClass.GRID)
				.child(ElementName.DIV, null)
				.build(),

		Selector.GRID_CELL,
		CssSelector.builder()
				.cls(ElementName.DIV, StyleClass.GRID)
				.child(ElementName.DIV, null)
				.child(ElementName.DIV, null)
				.build(),

		Selector.GRID_CELL_WIDE_LEFT,
		CssSelector.builder()
				.cls(ElementName.DIV, StyleClass.GRID)
				.child(ElementName.DIV, null)
				.child(ElementName.DIV, StyleClass.WIDE_BORDER_LEFT)
				.build(),

		Selector.GRID_CELL_WIDE_TOP,
		CssSelector.builder()
				.cls(ElementName.DIV, StyleClass.GRID)
				.child(ElementName.DIV, StyleClass.WIDE_BORDER_TOP)
				.child(ElementName.DIV, null)
				.build(),

		Selector.OUTER_GRID,
		CssSelector.builder()
				.cls(ElementName.DIV, StyleClass.OUTER_GRID)
				.build(),

		Selector.OUTER_GRID_ROW,
		CssSelector.builder()
				.cls(ElementName.DIV, StyleClass.OUTER_GRID)
				.child(ElementName.DIV, null)
				.build(),

		Selector.OUTER_GRID_CELL,
		CssSelector.builder()
				.cls(ElementName.DIV, StyleClass.OUTER_GRID)
				.child(ElementName.DIV, null)
				.child(ElementName.DIV, null)
				.build()
	);

	private static final	String	STYLE_SHEET1	= """
		      /*<![CDATA[*/
		      body {
		        margin: 0.5em;
		      }
		      $grid$ {
		        display: table;
		        border-collapse: collapse;
		        border: 3px solid;
		        font: %dpt sans-serif;
		      }
		      $gridRow$ {
		        display: table-row;
		      }
		      $gridCell$ {
		        display: table-cell;
		        width: 1.6em;
		        height: 1.6em;
		        border: 1px solid;
		        vertical-align: middle;
		        text-align: center;
		      }
		      $gridCellWideLeft$ {
		        border-left: 3px solid;
		      }
		      $gridCellWideTop$ {
		        border-top: 3px solid;
		      }
		      $outerGrid$ {
		        display: table;
		        border: none;
		        border-spacing: 1.5em;
		      }
		      $outerGridRow$ {
		        display: table-row;
		      }
		      $outerGridCell$ {
		        display: table-cell;
		      }
		""";

	private static final	String	STYLE_SHEET2	= """
		      /*]]>*/
		""";

	private static final	String	COLOUR_PROPERTIES_AUTOMATIC	= """
		      @media (prefers-color-scheme: light) {
		%s      }
		      @media (prefers-color-scheme: dark) {
		%s      }
		""";

	private static final	String	COLOUR_PROPERTIES_LIGHT	= """
		      :root {
		        --border-color: #A0A0A0;
		      }
		      body {
		        color: #000000;
		        background-color: #F6F6F2;
		      }
		      $grid$ {
		        border-color: var(--border-color);
		      }
		      $gridCell$ {
		        border-color: var(--border-color);
		      }
		      $gridCellWideLeft$ {
		        border-left-color: var(--border-color);
		      }
		      $gridCellWideTop$ {
		        border-top-color: var(--border-color);
		      }
		""";

	private static final	String	COLOUR_PROPERTIES_DARK	= """
		      :root {
		        --border-color: #787878;
		      }
		      body {
		        color: #F4F4F4;
		        background-color: #202020;
		      }
		      $grid$ {
		        border-color: var(--border-color);
		      }
		      $gridCell$ {
		        border-color: var(--border-color);
		      }
		      $gridCellWideLeft$ {
		        border-left-color: var(--border-color);
		      }
		      $gridCellWideTop$ {
		        border-top-color: var(--border-color);
		      }
		""";

	/** Names of HTML elements. */
	private interface ElementName
	{
		String	BODY	= "body";
		String	DIV		= "div";
		String	HEAD	= "head";
		String	HTML	= "html";
		String	META	= "meta";
		String	STYLE	= "style";
		String	TITLE	= "title";
	}

	/** Names of HTML attributes. */
	private interface AttrName
	{
		String	CLASS		= "class";
		String	CONTENT		= "content";
		String	HTTP_EQUIV	= "http-equiv";
		String	ID			= "id";
		String	MEDIA		= "media";
		String	TYPE		= "type";
		String	XMLNS		= "xmlns";
		String	XML_LANG	= "xml:lang";
	}

	/** CSS style classes. */
	private interface StyleClass
	{
		String	GRID				= "grid";
		String	OUTER_GRID			= "outerGrid";
		String	WIDE_BORDER_LEFT	= "wide-border-left";
		String	WIDE_BORDER_TOP		= "wide-border-top";
	}

	/** Identifiers of CSS selectors. */
	private interface Selector
	{
		String	GRID				= "grid";
		String	GRID_ROW			= "gridRow";
		String	GRID_CELL			= "gridCell";
		String	GRID_CELL_WIDE_LEFT	= "gridCellWideLeft";
		String	GRID_CELL_WIDE_TOP	= "gridCellWideTop";
		String	OUTER_GRID			= "outerGrid";
		String	OUTER_GRID_ROW		= "outerGridRow";
		String	OUTER_GRID_CELL		= "outerGridCell";
	}

	/** Prefixes of identifiers of HTML elements. */
	private interface IdPrefix
	{
		String	PUZZLE	= "puzzle";
		String	ROW		= "p";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	FAILED_TO_OPEN_FILE =
				"Failed to open the file.";

		String	FAILED_TO_CLOSE_FILE =
				"Failed to close the file.";

		String	FAILED_TO_LOCK_FILE =
				"Failed to lock the file.";

		String	FAILED_TO_READ_FILE_ATTRIBUTES =
				"Failed to read the attributes of the file.";

		String	FAILED_TO_CREATE_DIRECTORY =
				"Failed to create the directory.";

		String	FAILED_TO_CREATE_TEMPORARY_FILE =
				"Failed to create a temporary file.";

		String	ERROR_WRITING_FILE =
				"An error occurred when writing the file.";

		String	FILE_ACCESS_NOT_PERMITTED =
				"Access to the file was not permitted.";

		String	FAILED_TO_DELETE_FILE =
				"Failed to delete the existing file.";

		String	FAILED_TO_RENAME_FILE =
				"Temporary file: %s\nFailed to rename the temporary file to the specified filename.";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private PuzzleIO()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void writeHtml(
		Path			file,
		List<Puzzle>	puzzles,
		String			title,
		ColourScheme	colourScheme,
		int				fontSize,
		int				numColumns)
		throws FileException
	{
		Path tempFile = null;
		XmlWriter writer = null;
		boolean oldFileDeleted = false;
		try
		{
			// If file already exists, read its attributes
			FileAttribute<?>[] attrs = {};
			if (Files.exists(file, LinkOption.NOFOLLOW_LINKS))
			{
				try
				{
					PosixFileAttributes posixAttrs =
							Files.readAttributes(file, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
					attrs = new FileAttribute<?>[] { PosixFilePermissions.asFileAttribute(posixAttrs.permissions()) };
				}
				catch (UnsupportedOperationException e)
				{
					// ignore
				}
				catch (Exception e)
				{
					throw new FileException(ErrorMsg.FAILED_TO_READ_FILE_ATTRIBUTES, e, file);
				}
			}

			// Create parent directory
			Path directory = PathUtils.absParent(file);
			try
			{
				Files.createDirectories(directory);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CREATE_DIRECTORY, e, directory);
			}

			// Create temporary file
			try
			{
				tempFile = FilenameUtils.tempLocation(file);
				Files.createFile(tempFile, attrs);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CREATE_TEMPORARY_FILE, e, tempFile);
			}

			// Open XML writer on temporary file
			try
			{
				writer = new XmlWriter(tempFile, StandardCharsets.UTF_8);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorMsg.FILE_ACCESS_NOT_PERMITTED, e, tempFile);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_OPEN_FILE, e, tempFile);
			}

			// Lock file
			try
			{
				if (writer.getChannel().tryLock() == null)
					throw new FileException(ErrorMsg.FAILED_TO_LOCK_FILE, tempFile);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_LOCK_FILE, e, tempFile);
			}

			// Write file
			try
			{
				writer.writeXmlDeclaration(XML_VERSION_STR, XmlConstants.ENCODING_NAME_UTF8, XmlWriter.Standalone.NONE);
				writer.writeDocumentType(ElementName.HTML, XHTML_SYSTEM_ID, XHTML_PUBLIC_ID);
				writer.writeEol();
				writeHtml(writer, puzzles, title, colourScheme, fontSize, numColumns);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.ERROR_WRITING_FILE, e, tempFile);
			}

			// Close writer
			try
			{
				writer.close();
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CLOSE_FILE, e, tempFile);
			}
			finally
			{
				writer = null;
			}

			// Delete any existing file
			try
			{
				Files.deleteIfExists(file);
				oldFileDeleted = true;
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_DELETE_FILE, e, file);
			}

			// Rename temporary file
			try
			{
				Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_RENAME_FILE, e, file, PathUtils.abs(tempFile));
			}
		}
		catch (BaseException e)
		{
			// Close writer
			if (writer != null)
			{
				try
				{
					writer.close();
				}
				catch (Exception e0)
				{
					// ignore
				}
			}

			// Delete temporary file
			if (!oldFileDeleted && (tempFile != null))
			{
				try
				{
					Files.deleteIfExists(tempFile);
				}
				catch (Exception e0)
				{
					// ignore
				}
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

	private static void writeHtml(
		XmlWriter		writer,
		List<Puzzle>	puzzles,
		String			title,
		ColourScheme	colourScheme,
		int				fontSize,
		int				numColumns)
		throws IOException
	{
		// Write HTML start tag
		int indent = 0;
		AttributeList attributes = new AttributeList();
		attributes.add(AttrName.XMLNS, NAMESPACE_STR);
		attributes.add(AttrName.XML_LANG, LANG_STR);
		writer.writeElementStart(ElementName.HTML, attributes, indent, true, false);

		// Write head start tag
		indent += INDENT_INCREMENT;
		writer.writeElementStart(ElementName.HEAD, indent, true);

		// Write meta tag
		indent += INDENT_INCREMENT;
		attributes.clear();
		attributes.add(AttrName.HTTP_EQUIV, CONTENT_TYPE_STR);
		attributes.add(AttrName.CONTENT, MIME_TYPE_STR);
		writer.writeEmptyElement(ElementName.META, attributes, indent, true);

		// Write title
		writer.writeElementStart(ElementName.TITLE, indent, false);
		writer.write(title);
		writer.writeElementEnd(ElementName.TITLE, 0);

		// Write style element
		attributes.clear();
		attributes.add(AttrName.TYPE, TEXT_CSS_STR);
		attributes.add(AttrName.MEDIA, MEDIA_STR);
		writer.writeElementStart(ElementName.STYLE, attributes, indent, true, false);
		String styleSheet = String.format(STYLE_SHEET1, fontSize) + switch (colourScheme)
		{
			case AUTOMATIC -> String.format(COLOUR_PROPERTIES_AUTOMATIC,
											indentLines(COLOUR_PROPERTIES_LIGHT, INDENT_INCREMENT),
											indentLines(COLOUR_PROPERTIES_DARK, INDENT_INCREMENT));
			case LIGHT     -> COLOUR_PROPERTIES_LIGHT;
			case DARK      -> COLOUR_PROPERTIES_DARK;
		} + STYLE_SHEET2;
		for (String key : SELECTORS.keySet())
			styleSheet = styleSheet.replace("$" + key + "$", SELECTORS.get(key));
		writer.write(styleSheet);
		writer.writeElementEnd(ElementName.STYLE, indent);

		// Write head end tag
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(ElementName.HEAD, indent);

		// Write body start tag
		writer.writeEol();
		writer.writeElementStart(ElementName.BODY, indent, true);

		// Write start tag, table division
		indent += INDENT_INCREMENT;
		attributes.clear();
		attributes.add(AttrName.CLASS, StyleClass.OUTER_GRID);
		writer.writeElementStart(ElementName.DIV, attributes, indent, true, false);

		// Write puzzles
		indent += INDENT_INCREMENT;
		int index = 0;
		int numPuzzles = puzzles.size();
		int numRows = (numPuzzles + numColumns - 1) / numColumns;
		for (int i = 0; i < numRows; i++)
		{
			// Write start tag, table-row division
			writer.writeElementStart(ElementName.DIV, indent, true);

			// Write row of puzzles
			indent += INDENT_INCREMENT;
			for (int j = 0; j < numColumns; j++)
			{
				if (index < numPuzzles)
				{
					// Write start tag, table-cell division
					writer.writeElementStart(ElementName.DIV, indent, true);

					// Write puzzle
					write(writer, indent + INDENT_INCREMENT, puzzles.get(index++), index);

					// Write end tag, table-cell division
					writer.writeElementEnd(ElementName.DIV, indent);
				}
			}

			// Write end tag, table-row division
			indent -= INDENT_INCREMENT;
			writer.writeElementEnd(ElementName.DIV, indent);
		}

		// Write end tag, table division
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(ElementName.DIV, indent);

		// Write body end tag
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(ElementName.BODY, indent);

		// Write HTML end tag
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(ElementName.HTML, indent);
	}

	//------------------------------------------------------------------

	private static void write(
		XmlWriter	writer,
		int			indent,
		Puzzle		puzzle,
		int			index)
		throws IOException
	{
		// Write start tag, table division
		AttributeList attributes = new AttributeList();
		attributes.add(AttrName.ID, IdPrefix.PUZZLE + index);
		attributes.add(AttrName.CLASS, StyleClass.GRID);
		writer.writeElementStart(ElementName.DIV, attributes, indent, true, false);

		// Write rows of puzzle
		indent += INDENT_INCREMENT;
		for (int i = 0; i < puzzle.numRows(); i++)
		{
			// Write start tag, table-row division
			attributes.clear();
			attributes.add(AttrName.ID, IdPrefix.ROW + index + "-" + (i + 1));
			if ((i > 0) && (i % puzzle.order() == 0))
				attributes.add(AttrName.CLASS, StyleClass.WIDE_BORDER_TOP);
			writer.writeElementStart(ElementName.DIV, attributes, indent, true, false);

			// Write row of puzzle
			indent += INDENT_INCREMENT;
			for (int j = 0; j < puzzle.numColumns(); j++)
			{
				// Write start tag, table-cell division
				attributes.clear();
				if ((j > 0) && (j % puzzle.order() == 0))
					attributes.add(AttrName.CLASS, StyleClass.WIDE_BORDER_LEFT);
				writer.writeElementStart(ElementName.DIV, attributes, indent, false, false);

				// Write symbol
				int value = puzzle.value(i, j);
				if (value > 0)
					writer.write(puzzle.symbols()[value - 1]);

				// Write end tag, table-cell division
				writer.writeElementEnd(ElementName.DIV, 0);
			}

			// Write end tag, table-row division
			indent -= INDENT_INCREMENT;
			writer.writeElementEnd(ElementName.DIV, indent);
		}

		// Write end tag, table division
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(ElementName.DIV, indent);
	}

	//------------------------------------------------------------------

	private static String indentLines(
		String	text,
		int		indent)
	{
		List<String> lines = StringUtils.split(text, '\n', true);
		StringBuilder buffer = new StringBuilder(text.length() + lines.size() * indent);
		String spaces = " ".repeat(indent);
		for (String line : lines)
			buffer.append(spaces).append(line).append('\n');
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COLOUR SCHEME


	public enum ColourScheme
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		AUTOMATIC,
		LIGHT,
		DARK;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourScheme()
		{
			// Initialise instance variables
			key = StringUtils.toCamelCase(name());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return StringUtils.firstCharToUpperCase(name().toLowerCase());
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

	}

	//==================================================================

}

//----------------------------------------------------------------------
