package de.azrael.gif;

/* 
 * Library classes for GIF optimizer (Java)
 * 
 * Copyright (c) 2018 Project Nayuki
 * All rights reserved. Contact Nayuki for licensing.
 * https://www.nayuki.io/page/gif-optimizer-java
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/* 
 * An input stream that converts GIF's subblock format to a plain byte stream.
 * This converter returns EOF after a zero-length subblock is encountered.
 */
final class SubblockInputStream extends InputStream {
	
	/*---- Fields ----*/
	
	private InputStream input;  // Underlying stream
	
	// -2 = stream ended correctly
	// -1 = unexpected EOF
	// 0 = need to read new block
	// 1 to 255 = bytes remaining
	private int bytesRemain;
	
	
	
	/*---- Constructors ----*/
	
	public SubblockInputStream(InputStream in) {
		input = Objects.requireNonNull(in);
		bytesRemain = 0;
	}
	
	
	
	/*---- Methods ----*/
	
	// Returns the next unsigned byte value in the stream, or -1 if the sequence of subblocks has ended.
	public int read() throws IOException {
		while (bytesRemain <= 0) {
			switch (bytesRemain) {
				case 0:  // Previous subblock ended, so read new one
					bytesRemain = input.read();
					if (bytesRemain == 0)
						bytesRemain = -2;
					break;
				case -1:
					throw new EOFException();
				case -2:
					return -1;  // Normal EOF
			}
		}
		
		int result = input.read();
		if (result == -1) {
			bytesRemain = -1;
			throw new EOFException();
		}
		bytesRemain--;
		return result;
	}
	
	
	public InputStream detach() {
		if (bytesRemain != -2 || input == null)
			throw new IllegalStateException();
		InputStream result = input;
		input = null;
		return result;
	}
	
}
