package de.azrael.gif;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DataFormatException;

public class GifOptimizer {
	//take in arguments
	public static String startGifOptimization(String [] args) throws IOException, DataFormatException {
		if (args.length < 2)
			return "Not enough arguments";
		
		// Get file paths
		File inFile  = new File(args[args.length - 2]);
		File outFile = new File(args[args.length - 1]);
		if (!inFile.isFile())
			return "Input file does not exist: " + inFile.getPath();
		if (outFile.getCanonicalFile().equals(inFile.getCanonicalFile()))
			return "Output file is the same as input file";
		
		// Parse options
		int blockSize = -1;
		int dictClear = -2;
		for (int i = 0; i < args.length - 2; i++) {
			String opt = args[i];
			String[] parts = opt.split("=", 2);
			if (parts.length != 2)
				return "Invalid option: " + opt;
			String key = parts[0];
			String value = parts[1];
			
			switch (key) {
				case "blocksize":
					if (blockSize != -1)
						return "Duplicate block size option";
					blockSize = Integer.parseInt(value);
					if (blockSize < 0)
						return "Invalid block size value";
					break;
				case "dictclear":
					if (dictClear != -2)
						return "Duplicate dictionary clear option";
					if (value.equals("dcc"))
						dictClear = -1;
					else {
						dictClear = Integer.parseInt(opt.substring(10));
						if (dictClear < 0)
							return "Invalid dictionary clear value";
					}
					break;
				default:
					return "Invalid option: " + opt;
			}
		}
		
		// Set defaults
		if (blockSize == -1)
			blockSize = 1024;
		if (dictClear == -2)
			dictClear = -1;
		
		// Run optimizer
		optimizeGif(inFile, blockSize, dictClear, outFile);
		return null;
	}
	
	// Reads the given input file, optimizes just the LZW blocks according to the block size, and writes to the given output file.
		// The output file path *must* point to a different file than the input file, otherwise the data will be corrupted.
		private static void optimizeGif(File inFile, int blockSize, int dictClear, File outFile) throws IOException, DataFormatException {
			try (MemoizingInputStream in = new MemoizingInputStream(new FileInputStream(inFile))) {
				Throwable error = null;
				try (OutputStream out = new FileOutputStream(outFile)) {
					optimizeGif(in, blockSize, dictClear, out);
				} catch (DataFormatException|IOException e) {
					error = e;
				}
				if (error != null) {
					error.printStackTrace();
					outFile.delete();
				}
			}
		}
		
		
		private static void optimizeGif(MemoizingInputStream in, int blockSize, int dictClear, OutputStream out) throws IOException, DataFormatException {
			// Header
			int version;
			{
				char[] headCh = new char[6];
				for (int i = 0; i < headCh.length; i++)
					headCh[i] = (char)in.read();
				String headStr = new String(headCh);
				if (!headStr.startsWith("GIF"))
					throw new DataFormatException("Invalid GIF header");
				switch (headStr) {
					case "GIF87a":  version = 87;  break;
					case "GIF89a":  version = 89;  break;
					default:  throw new DataFormatException("Unrecognized GIF version");
				}
			}
			
			// Logical screen descriptor
			{
				byte[] screenDesc = new byte[7];
				in.readFully(screenDesc);
				if ((screenDesc[4] & 0x80) != 0) {
					int gctSize = (screenDesc[4] & 0x7) + 1;
					in.readFully(new byte[(1 << gctSize) * 3]);  // Skip global color table
				}
			}
			
			// Process top-level blocks
			while (true) {
				int b = in.read();
				if (b == -1)
					throw new EOFException();
				else if (b == 0x3B)  // Trailer
					break;
				else if (b == 0x21) {  // Extension introducer
					if (version == 87)
						throw new DataFormatException("Extension block not supported in GIF87a");
					b = in.read();  // Block label
					if (b == -1)
						throw new EOFException();
					try (SubblockInputStream bin = new SubblockInputStream(in)) {
						while (bin.read() != -1);  // Skip all data
						in = (MemoizingInputStream)bin.detach();
					}
					
				} else if (b == 0x2C) {
					// Image descriptor
					byte[] imageDesc = new byte[9];
					in.readFully(imageDesc);
					if ((imageDesc[8] & 0x80) != 0) {
						int lctSize = (imageDesc[8] & 0x7) + 1;
						in.readFully(new byte[(1 << lctSize) * 3]);  // Skip local color table
					}
					int codeSize = in.read();
					if (codeSize == -1)
						throw new EOFException();
					if (codeSize < 2 || codeSize > 8)
						throw new DataFormatException("Invalid number of code bits");
					out.write(in.getBuffer());
					in.clearBuffer();
					recompressData(in, blockSize, dictClear, codeSize, out);
					
				} else
					throw new DataFormatException("Unrecognized data block");
			}
			
			// Copy remainder of data that was read
			out.write(in.getBuffer());
		}
		
		
		// Read and decompress the LZW data fully, perform optimization and compression, and write out the new version.
		private static void recompressData(MemoizingInputStream in, int blockSize, int dictClear, int codeSize, OutputStream out) throws IOException {
			// Read and decompress
			byte[] pixels;
			try (SubblockInputStream blockIn = new SubblockInputStream(in)) {
				pixels = GifLzwDecompressor.decode(new BitInputStream(blockIn), codeSize);
				while (blockIn.read() != -1);  // Discard rest of subblock data after the LZW Stop code
				in = (MemoizingInputStream)blockIn.detach();
			}
			
			// Compress and hold
			ByteArrayOutputStream bufOut = new ByteArrayOutputStream();
			SubblockOutputStream blockOut = new SubblockOutputStream(bufOut);
			ByteBitOutputStream bitOut = new ByteBitOutputStream(blockOut);
			if (blockSize > 0)
				GifLzwCompressor.encodeOptimized(pixels, codeSize, blockSize, dictClear, bitOut, false);
			else if (blockSize == 0)
				GifLzwCompressor.encodeUncompressed(pixels, codeSize, bitOut);
			else
				throw new AssertionError();
			blockOut = (SubblockOutputStream)bitOut.detach();
			blockOut.detach();
			
			// Choose which version to write
			byte[] oldComp = in.getBuffer();
			byte[] newComp = bufOut.toByteArray();
			if (newComp.length < oldComp.length)
				out.write(newComp);
			else
				out.write(oldComp);
			in.clearBuffer();
		}
}
