package org.processmining.servicelevelagreements.model.xml;

import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.processmining.framework.plugin.PluginContext;

/**
 * Marshaller that converts an {@link org.dom4j.Document} to an XML document
 * written to an outputstreatm.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class XMLXSDUnmarshaller {

	public static void exportToStream(Document document, OutputStream output) {
		exportToStream(null, document, output);
	}

	public static void exportToStream(PluginContext context, Document document, OutputStream output) {
		// Write the document to the OutputStream as XML.
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			//			OutputFormat format = OutputFormat.createCompactFormat();

			XMLWriter writer = new XMLWriter(output, format);
			writer.write(document);
			writer.close();

			// Print the document to System.out
			//			writer = new XMLWriter(System.out, format);
			//			writer.write(document);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
