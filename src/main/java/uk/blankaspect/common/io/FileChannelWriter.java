/*====================================================================*\

FileChannelWriter.java

Class: writer to a file channel.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.io;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;
import java.io.Writer;

import java.nio.channels.FileChannel;

import java.nio.charset.Charset;

//----------------------------------------------------------------------


// CLASS: WRITER TO A FILE CHANNEL


public class FileChannelWriter
	extends Writer
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	FileChannel	channel;
	private	Charset		encoding;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FileChannelWriter(FileChannel channel,
							 Charset     encoding)
	{
		if (channel == null)
			throw new IllegalArgumentException("Null channel");

		this.channel = channel;
		this.encoding = (encoding == null) ? Charset.defaultCharset() : encoding;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void write(char[] buffer,
					  int    offset,
					  int    length)
		throws IOException
	{
		channel.write(encoding.encode(new String(buffer, offset, length)));
	}

	//------------------------------------------------------------------

	@Override
	public void flush()
		throws IOException
	{
		channel.force(true);
	}

	//------------------------------------------------------------------

	@Override
	public void close()
		throws IOException
	{
		channel.close();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
