/*====================================================================*\

NodeMessage.java

Class: node-related message.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.basictree;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import java.util.stream.Collectors;

import uk.blankaspect.common.tree.TreeUtils;

//----------------------------------------------------------------------


// CLASS: NODE-RELATED MESSAGE


/**
 * This class encapsulates a message that relates to a {@linkplain AbstractNode node}.  A nested class, {@link List},
 * implements a list of such messages.
 */

public class NodeMessage
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The node that is associated with this message. */
	private	AbstractNode	node;

	/** The kind of this message. */
	private	Kind			kind;

	/** The text of this message. */
	private	String			text;

	/** The exception that is associated with this message. */
	private	Throwable		exception;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a message of the specified kind that relates to the specified {@linkplain AbstractNode
	 * node}.
	 *
	 * @param  node
	 *           the node that will be associated with the message.
	 * @param  kind
	 *           the kind of the message.
	 * @param  text
	 *           the text of the message, which may be {@code null}.
	 * @throws IllegalArgumentException
	 *           if {@code node} is {@code null} or {@code kind} is {@code null}.
	 */

	public NodeMessage(
		AbstractNode	node,
		Kind			kind,
		String			text)
	{
		// Call alternative constructor
		this(node, kind, text, null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a message of the specified kind that relates to the specified {@linkplain AbstractNode
	 * node}.
	 *
	 * @param  node
	 *           the node that will be associated with the message.
	 * @param  kind
	 *           the kind of the message.
	 * @param  exception
	 *           the exception that will be associated with the message, which may be {@code null}.
	 * @throws IllegalArgumentException
	 *           if {@code node} is {@code null} or {@code kind} is {@code null}.
	 */

	public NodeMessage(
		AbstractNode	node,
		Kind			kind,
		Throwable		exception)
	{
		// Call alternative constructor
		this(node, kind, null, exception);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a message of the specified kind that relates to the specified {@linkplain AbstractNode
	 * node}.
	 *
	 * @param  node
	 *           the node that will be associated with the message.
	 * @param  kind
	 *           the kind of the message.
	 * @param  text
	 *           the text of the message, which may be {@code null}.
	 * @param  exception
	 *           the exception that will be associated with the message, which may be {@code null}.
	 * @throws IllegalArgumentException
	 *           if {@code node} is {@code null} or {@code kind} is {@code null}.
	 */

	public NodeMessage(
		AbstractNode	node,
		Kind			kind,
		String			text,
		Throwable		exception)
	{
		// Validate arguments
		if (node == null)
			throw new IllegalArgumentException("Null node");
		if (kind == null)
			throw new IllegalArgumentException("Null kind");

		// Initialise instance variables
		this.node = node;
		this.kind = kind;
		this.text = text;
		this.exception = exception;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a string representation of the path from the specified {@linkplain AbstractNode node} from the root of
	 * the tree to which it belongs.  The elements of the path are:
	 * <ul>
	 *   <li>
	 *     an element of a {@linkplain ListNode list node}, which is represented by its index enclosed in square
	 *     brackets (eg, '[2]'), or
	 *   </li>
	 *   <li>
	 *     a key&ndash;value pair of a {@linkplain MapNode map node}, which is represented by its key, with a '/'
	 *     prefixed to it if the map node is not the root.
	 *   </li>
	 * </ul>
	 * <p>
	 * If the specified node is the root of its tree, an empty string is returned.
	 * </p>
	 *
	 * @param  node
	 *           the node for whose path a string representation is required.
	 * @return a string representation of the path from {@code node} to the root of the tree to which it belongs.
	 */

	public static String nodeToPathString(
		AbstractNode	node)
	{
		return TreeUtils.getPath(node).stream()
				.map(node0 ->
				{
					// Get parent node
					AbstractNode parent = node0.getParent();

					// List node
					if (parent instanceof ListNode)
						return "[" + node0.getListIndex() + "]";

					// Map node
					if (parent instanceof MapNode)
						return (parent.isRoot() ? "" : "/") + node0.getMapKey();

					return "";
				})
				.collect(Collectors.joining());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	public String toString()
	{
		return toString(" : ", EnumSet.allOf(Component.class));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the node that is associated with this message.
	 *
	 * @return the node that is associated with this message.
	 */

	public AbstractNode getNode()
	{
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the kind of this message.
	 *
	 * @return the kind of this message.
	 */

	public Kind getKind()
	{
		return kind;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the text of this message.
	 *
	 * @return the text of this message.
	 */

	public String getText()
	{
		return text;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the exception that is associated with this message.
	 *
	 * @return the exception that is associated with this message.
	 */

	public Throwable getException()
	{
		return exception;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the kind of this message is an error or a fatal error.
	 *
	 * @return {@code true} if the kind of this message is an error or a fatal error; {@code false} otherwise.
	 */

	public boolean isError()
	{
		return (kind == Kind.ERROR) || (kind == Kind.FATAL);
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of the specified components of this message.
	 *
	 * @param  separator
	 *           the separator between components in the string representation the message.
	 * @param  components
	 *           the components of this message for which a string representation is required.
	 * @return a string representation of the specified components of this message.
	 */

	public String toString(
		String			separator,
		Component...	components)
	{
		return toString(separator, java.util.List.of(components));
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of the specified components of this message.
	 *
	 * @param  separator
	 *           the separator between components in the string representation the message.
	 * @param  components
	 *           the components of this message for which a string representation is required.
	 * @return a string representation of the specified components of this message.
	 */

	public String toString(
		String				separator,
		Iterable<Component>	components)
	{
		// Create buffer
		StringBuilder buffer = new StringBuilder(128);

		// Append components
		int numComponents = 0;
		for (Component component : components)
		{
			switch (component)
			{
				case NODE:
					String path = nodeToPathString(node);
					if (!path.isEmpty())
					{
						if (numComponents++ > 0)
							buffer.append(separator);
						buffer.append(path);
					}
					break;

				case KIND:
					if (!kind.text.isEmpty())
					{
						if (numComponents++ > 0)
							buffer.append(separator);
						buffer.append(kind.text);
					}
					break;

				case TEXT:
					if ((text != null) && !text.isEmpty())
					{
						if (numComponents++ > 0)
							buffer.append(separator);
						buffer.append(text);
					}
					break;

				case EXCEPTION:
					if (exception != null)
					{
						if (numComponents++ > 0)
							buffer.append(separator);
						buffer.append(exception);
						Throwable cause = exception.getCause();
						while (cause != null)
						{
							buffer.append(" | ");
							buffer.append(cause);
							cause = cause.getCause();
						}
					}
					break;
			}
		}

		// Return string
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: MESSAGE KIND


	/**
	 * This is an enumeration of the kinds of a {@linkplain NodeMessage node-related message}.
	 */

	public enum Kind
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * A debug message.
		 */
		DEBUG
		(
			"Debug"
		),

		/**
		 * An informative message.
		 */
		INFO
		(
			"Info"
		),

		/**
		 * A warning message.
		 */
		WARNING
		(
			"Warning"
		),

		/**
		 * An error message.
		 */
		ERROR
		(
			"Error"
		),

		/**
		 * A fatal error message.
		 */
		FATAL
		(
			"Fatal error"
		),

		/**
		 * The kind of message is not defined.
		 */
		UNDEFINED
		(
			""
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The text that designates this kind of message. */
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new enumeration constant for a kind of message.
		 *
		 * @param text
		 *          the text that designates the kind of message.
		 */

		private Kind(
			String	text)
		{
			// Initialise instance variables
			this.text = text;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: MESSAGE COMPONENTS


	/**
	 * This is an enumeration of the components of a {@linkplain NodeMessage node-related message}.
	 */

	public enum Component
	{
		/**
		 * The node that is associated with the message.
		 */
		NODE,

		/**
		 * The kind of the message.
		 */
		KIND,

		/**
		 * The text of the message.
		 */
		TEXT,

		/**
		 * The exception that is associated with the message.
		 */
		EXCEPTION
	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: LIST OF NODE MESSAGES


	/**
	 * This class implements a list of {@linkplain NodeMessage node-related messages}.
	 */

	public static class List
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The underlying list of the messages of this list. */
		private	java.util.List<NodeMessage>	messages;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a list of {@linkplain NodeMessage node-related messages}.
		 */

		public List()
		{
			// Initialise instance variables
			messages = new ArrayList<>();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns {@code true} if this list contains no messages.
		 *
		 * @return {@code true} if this list contains no messages; {@code false} otherwise.
		 */

		public boolean isEmpty()
		{
			return messages.isEmpty();
		}

		//--------------------------------------------------------------

		/**
		 * Returns the number of messages in this list.
		 *
		 * @return the number of messages in this list.
		 */

		public int getNumMessages()
		{
			return messages.size();
		}

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if this list contains at least one 'fatal error' message.
		 *
		 * @return {@code true} if this list contains at least one 'fatal error' message; {@code false} otherwise.
		 */

		public boolean hasFatalError()
		{
			return messages.stream().anyMatch(element -> element.kind == Kind.FATAL);
		}

		//--------------------------------------------------------------

		/**
		 * Returns an unmodifiable list of the messages in this list.
		 *
		 * @return an unmodifiable list of the messages in this list.
		 */

		public java.util.List<NodeMessage> getMessages()
		{
			return Collections.unmodifiableList(messages);
		}

		//--------------------------------------------------------------

		/**
		 * Creates a new message with the specified attributes and adds it to this list.
		 *
		 * @param  node
		 *           the node that will be associated with the message.
		 * @param  kind
		 *           the kind of the message.
		 * @param  text
		 *           the text of the message, which may be {@code null}.
		 * @param  replacements
		 *           the text that will replace placeholders in {@code text}.
		 * @throws IllegalArgumentException
		 *           if {@code node} is {@code null} or {@code kind} is {@code null}.
		 */

		public void add(
			AbstractNode	node,
			Kind			kind,
			String			text,
			Object...		replacements)
		{
			add(node, kind, text, null, replacements);
		}

		//--------------------------------------------------------------

		/**
		 * Creates a new message with the specified attributes and adds it to this list.
		 *
		 * @param  node
		 *           the node that will be associated with the message.
		 * @param  kind
		 *           the kind of the message.
		 * @param  exception
		 *           the exception that will be associated with the message, which may be {@code null}.
		 * @throws IllegalArgumentException
		 *           if {@code node} is {@code null} or {@code kind} is {@code null}.
		 */

		public void add(
			AbstractNode	node,
			Kind			kind,
			Throwable		exception)
		{
			// Create a message and add it to the list
			messages.add(new NodeMessage(node, kind, null, exception));
		}

		//--------------------------------------------------------------

		/**
		 * Creates a new message with the specified attributes and adds it to this list.
		 *
		 * @param  node
		 *           the node that will be associated with the message.
		 * @param  kind
		 *           the kind of the message.
		 * @param  text
		 *           the text of the message, which may be {@code null}.
		 * @param  exception
		 *           the exception that will be associated with the message, which may be {@code null}.
		 * @param  replacements
		 *           the text that will replace placeholders in {@code text}.
		 * @throws IllegalArgumentException
		 *           if {@code node} is {@code null} or {@code kind} is {@code null}.
		 */

		public void add(
			AbstractNode	node,
			Kind			kind,
			String			text,
			Throwable		exception,
			Object...		replacements)
		{
			// Create a message and add it to the list
			messages.add(new NodeMessage(node, kind, (text == null) ? null : String.format(text, replacements),
										 exception));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
