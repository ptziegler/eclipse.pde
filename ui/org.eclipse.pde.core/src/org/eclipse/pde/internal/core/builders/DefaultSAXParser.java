/*******************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.builders;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.util.SAXParserWrapper;
import org.xml.sax.SAXException;

public class DefaultSAXParser {

	public static void parse(IFile file, XMLErrorReporter reporter) {
		SAXParserWrapper parser = null;
		try {
			parser = new SAXParserWrapper();
			try (InputStream stream = new BufferedInputStream(file.getContents())) {
			parser.parse(stream, reporter);
			}
		} catch (CoreException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
		}
	}

}
