/*====================================================================*\

SymbolsPane.java

Class: symbols pane.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.List;

import javafx.css.PseudoClass;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.ui.jfx.colour.ColourUtils;

import uk.blankaspect.ui.jfx.font.FontUtils;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.FxStyleClass;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;
import uk.blankaspect.ui.jfx.style.StyleUtils;

import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.textfield.FilterFactory;
import uk.blankaspect.ui.jfx.textfield.TextFieldUtils;

//----------------------------------------------------------------------


// CLASS: SYMBOLS PANE


public class SymbolsPane
	extends HBox
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		MAX_NUM_SYMBOLS	= Puzzle.Order.MAX.numSymbols();

	private static final	String	COUNT_PROTOTYPE_TEXT	=
			"0".repeat(NumberUtils.getNumDecDigitsInt(MAX_NUM_SYMBOLS));

	private static final	Insets	COUNT_LABEL_PADDING	= new Insets(3.0, 6.0, 3.0, 6.0);

	/** The pseudo-class that is associated with the <i>invalid</i> state. */
	private static final	PseudoClass	PSEUDO_CLASS_INVALID	=
			PseudoClass.getPseudoClass(PseudoClassKey.INVALID);

	/** The pseudo-class that is associated with the <i>valid</i> state. */
	private static final	PseudoClass	PSEUDO_CLASS_VALID		=
			PseudoClass.getPseudoClass(PseudoClassKey.VALID);

	/** Miscellaneous strings. */
	private static final	String	COUNT_STR	= "Count";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.CONTROL_INNER_BACKGROUND,
			ColourKey.SYMBOLS_FIELD_BACKGROUND_INVALID,
			CssSelector.builder()
					.cls(StyleClass.SYMBOLS_PANE)
					.desc(StyleClass.SYMBOLS_FIELD).pseudo(PseudoClassKey.INVALID)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.CONTROL_INNER_BACKGROUND,
			ColourKey.SYMBOLS_FIELD_BACKGROUND_VALID,
			CssSelector.builder()
					.cls(StyleClass.SYMBOLS_PANE)
					.desc(StyleClass.SYMBOLS_FIELD).pseudo(PseudoClassKey.VALID)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.HIGHLIGHT_FILL,
			ColourKey.SYMBOLS_FIELD_HIGHLIGHT_FOCUSED,
			CssSelector.builder()
					.cls(StyleClass.SYMBOLS_PANE)
					.desc(StyleClass.SYMBOLS_FIELD).pseudo(FxPseudoClass.FOCUSED)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.HIGHLIGHT_TEXT_FILL,
			ColourKey.SYMBOLS_FIELD_HIGHLIGHT_TEXT_FOCUSED,
			CssSelector.builder()
					.cls(StyleClass.SYMBOLS_PANE)
					.desc(StyleClass.SYMBOLS_FIELD).pseudo(FxPseudoClass.FOCUSED)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.SYMBOLS_FIELD_CONTEXT_MENU_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.SYMBOLS_PANE)
					.desc(StyleClass.SYMBOLS_FIELD)
					.desc(FxStyleClass.CONTEXT_MENU)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.SYMBOL_COUNT_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.SYMBOLS_PANE)
					.desc(StyleClass.SYMBOL_COUNT_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.SYMBOL_COUNT_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.SYMBOLS_PANE)
					.desc(StyleClass.SYMBOL_COUNT_LABEL)
					.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	SYMBOL_COUNT_LABEL	= StyleConstants.CLASS_PREFIX + "symbol-count-label";
		String	SYMBOLS_FIELD		= StyleConstants.CLASS_PREFIX + "symbols-field";
		String	SYMBOLS_PANE		= StyleConstants.CLASS_PREFIX + "symbols-pane";
	}

	/** Keys of CSS pseudo-classes. */
	private interface PseudoClassKey
	{
		String	INVALID	= "invalid";
		String	VALID	= "valid";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	SYMBOL_COUNT_LABEL_BACKGROUND			= PREFIX + "symbolCountLabel.background";
		String	SYMBOL_COUNT_LABEL_BORDER				= PREFIX + "symbolCountLabel.border";
		String	SYMBOLS_FIELD_BACKGROUND_INVALID		= PREFIX + "symbolsField.background.invalid";
		String	SYMBOLS_FIELD_BACKGROUND_VALID			= PREFIX + "symbolsField.background.valid";
		String	SYMBOLS_FIELD_CONTEXT_MENU_BACKGROUND	= PREFIX + "symbolsField.contextMenu.background";
		String	SYMBOLS_FIELD_HIGHLIGHT_FOCUSED			= PREFIX + "symbolsField.highlight.focused";
		String	SYMBOLS_FIELD_HIGHLIGHT_TEXT_FOCUSED	= PREFIX + "symbolsField.highlight.text.focused";
	}

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(SymbolsPane.class, COLOUR_PROPERTIES);
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int				expectedNumSymbols;
	private	SymbolsField	symbolsField;
	private	Label			countLabel;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SymbolsPane()
	{
		// Set properties
		setSpacing(6.0);
		setAlignment(Pos.CENTER_LEFT);
		getStyleClass().add(StyleClass.SYMBOLS_PANE);

		// Text field: symbols
		symbolsField = new SymbolsField();
		HBox.setMargin(symbolsField, new Insets(0.0, 6.0, 0.0, 0.0));

		// Label: symbol count
		countLabel = new Label();
		countLabel.setMinWidth(Region.USE_PREF_SIZE);
		countLabel.setAlignment(Pos.CENTER_RIGHT);
		countLabel.setPadding(COUNT_LABEL_PADDING);
		countLabel.setBackground(
				SceneUtils.createColouredBackground(getColour(ColourKey.SYMBOL_COUNT_LABEL_BACKGROUND)));
		countLabel.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.SYMBOL_COUNT_LABEL_BORDER)));
		Insets insets = countLabel.getInsets();
		countLabel.setPrefWidth(TextUtils.textWidthCeil(COUNT_PROTOTYPE_TEXT) + insets.getLeft() + insets.getRight());
		countLabel.getStyleClass().add(StyleClass.SYMBOL_COUNT_LABEL);

		// When content of symbols field changes, update background of symbols field and symbol count
		symbolsField.textProperty().addListener(observable -> update());

		// Add children to this pane
		getChildren().addAll(symbolsField, Labels.hNoShrink(COUNT_STR), countLabel);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Character findDuplicateSymbol(
		char[]	symbols)
	{
		for (int i = 0; i < symbols.length - 1; i++)
		{
			char ch = symbols[i];
			for (int j = i + 1; j < symbols.length; j++)
				if (symbols[j] == ch)
					return ch;
		}
		return null;
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
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public char[] symbols()
	{
		return symbolsField.getText().toCharArray();
	}

	//------------------------------------------------------------------

	public void symbols(
		Puzzle.Order	order,
		char[]			symbols)
	{
		expectedNumSymbols = order.numSymbols();
		symbolsField.setTextFormatter(
				new TextFormatter<>(FilterFactory.createFilter(expectedNumSymbols, (ch, index, text) ->
						Puzzle.isValidSymbol(ch) ? Character.toString(ch) : "")));
		symbolsField.setText(symbols);
	}

	//------------------------------------------------------------------

	public SymbolsField symbolsField()
	{
		return symbolsField;
	}

	//------------------------------------------------------------------

	public boolean symbolsValid()
	{
		return symbolsField.valid();
	}

	//------------------------------------------------------------------

	private void update()
	{
		symbolsField.updateBackground();
		countLabel.setText(Integer.toString(symbolsField.getLength()));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: TEXT FIELD FOR SYMBOLS


	public class SymbolsField
		extends TextField
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	FONT_SIZE_FACTOR	= 1.25;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SymbolsField()
		{
			// Set properties
			setFont(SudokuGeneratorApp.instance().preferences().gridFont());
			getStyleClass().add(StyleClass.SYMBOLS_FIELD);
			TextFieldUtils.setNumColumns(this, 'm', MAX_NUM_SYMBOLS);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setFont(
			FontInfo	fontInfo)
		{
			double size = FontUtils.getSize(FONT_SIZE_FACTOR);
			setFont((fontInfo == null) ? Font.font(size) : fontInfo.font(size));
		}

		//--------------------------------------------------------------

		private void updateBackground()
		{
			// Create procedure to set background colour
			IProcedure1<Color> setBackgroundColour = colour ->
			{
				StyleUtils.setProperty(this, FxProperty.CONTROL_INNER_BACKGROUND.getName(),
									   ColourUtils.colourToHexString(colour));
			};

			// Case: text is valid
			if (valid())
			{
				pseudoClassStateChanged(PSEUDO_CLASS_INVALID, false);
				pseudoClassStateChanged(PSEUDO_CLASS_VALID, true);

				if (StyleManager.INSTANCE.notUsingStyleSheet())
					setBackgroundColour.invoke(getColour(ColourKey.SYMBOLS_FIELD_BACKGROUND_VALID));
			}

			// Case: text is invalid
			else
			{
				pseudoClassStateChanged(PSEUDO_CLASS_INVALID, true);
				pseudoClassStateChanged(PSEUDO_CLASS_VALID, false);

				if (StyleManager.INSTANCE.notUsingStyleSheet())
					setBackgroundColour.invoke(getColour(ColourKey.SYMBOLS_FIELD_BACKGROUND_INVALID));
			}
		}

		//--------------------------------------------------------------

		private boolean valid()
		{
			String text = getText();
			return (text.length() == expectedNumSymbols) && (findDuplicateSymbol(text.toCharArray()) == null);
		}

		//--------------------------------------------------------------

		private void setText(
			char[]	chars)
		{
			// Set text
			setText(new String(chars));

			// Update background
			updateBackground();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
