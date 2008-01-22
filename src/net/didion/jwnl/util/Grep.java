package net.didion.jwnl.util;

/*
 * @(#)Grep.java	1.3 01/12/13
 * Search a list of files for lines that match a given regular-expression
 * pattern.  Demonstrates NIO mapped byte buffers, charsets, and regular
 * expressions.
 *
 * Copyright 2001-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright  
 * notice, this  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright 
 * notice, this list of conditions and the following disclaimer in 
 * the documentation and/or other materials provided with the 
 * distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR 
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR 
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility. 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This is a utility class to find a pattern within a file, specifically the sense.idx file. 
 * @author brett
 *
 */
public class Grep {

    /**
     * Set the character set for the file. 
     */
    private static Charset charset = Charset.forName("ISO-8859-15");
    
    /**
     * The decoder for the file. 
     */
    private static CharsetDecoder decoder = charset.newDecoder();

    /**
     * Line parsing pattern.
     */
    private static Pattern linePattern
	= Pattern.compile(".*\r?\n");

    /**
     * Input pattern we're looking for. 
     */
    private static Pattern pattern;
    
    /**
     * The character buffer reference. 
     */
    private static CharBuffer indexFile;

    /**
     * Compiles the pattern. 
     * @param pat regex
     */
    private static void compile(String pat) {
	try {
	    pattern = Pattern.compile(pat);
	} catch (PatternSyntaxException x) {
	    System.err.println(x.getMessage());
	}
    }

    /**
     * Use the linePattern to break the given CharBuffer into lines, applying
     * the input pattern to each line to see if we have a match
     */ 
    private static List grep() {
    List matches = new ArrayList();
	Matcher lm = linePattern.matcher(indexFile);	// Line matcher
	Matcher pm = null;			// Pattern matcher
	int lines = 0;
	while (lm.find()) {
	    lines++;
	    CharSequence cs = lm.group(); 	// The current line
	    if (pm == null)
		pm = pattern.matcher(cs);
	    else
		pm.reset(cs);
	    if (pm.find()) {
	    	matches.add(cs.toString());
	    }
	    if (lm.end() == indexFile.limit())
		break;
	}
	return matches;
    }
    
    public static void setFile(File f) throws IOException {
    	FileInputStream fis = new FileInputStream(f);
    	FileChannel fc = fis.getChannel();

    	// Get the file's size and then map it into memory
    	int sz = (int)fc.size();
    	MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
//    	 Decode the file into a char buffer
    	indexFile = decoder.decode(bb);
    	
    	fc.close();
    }

    /**
     * Search for occurrences in the given file of the offset, then find the appropriate lemma.
     * @param f
     * @param synsetOffset
     * @return
     * @throws IOException
     */
    public static List grep(String synsetOffset) throws IOException {

    compile(synsetOffset);

	// Perform the search
	List matches = grep();
	
	
	return matches;
    }

    /**
     * Search for occurrences in the given file of the offset, then find the appropriate lemma.
     * @param f
     * @param synsetOffset
     * @return
     * @throws IOException
     */
    public static String grep(String synsetOffset, String lemma) throws IOException {

    compile(synsetOffset);
    String m = "";
	// Perform the search
	List matches = grep();
	for (int i = 0; i < matches.size(); i++) {
		String match = (String) matches.get(i);
		if (match.indexOf(lemma) != -1) {
			m = match;
		}
	}
	
	return m;
    }

}

