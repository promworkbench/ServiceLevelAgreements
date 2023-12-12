package org.processmining.servicelevelagreements.model.xml;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Unmarshaller that converts an input XML stream to a
 * {@link org.dom4j.Document} using XSD schema validation.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class XMLXSDMarshaller {

	/**
	 * Import an XML file as a org.dom4j.Document while validating it against an
	 * XSD Schema
	 * 
	 * @param inputXML
	 *            The input XML as a stream.
	 * @param inputXSD
	 *            The input XSD Schema location.
	 * @return The document.
	 */
	public static Document importFromStream(InputStream inputXML, String inputXSD) {
		return importFromStream(null, inputXML, inputXSD);
	}

	/**
	 * Import an XML file as a org.dom4j.Document while validating it against an
	 * XSD Schema.
	 * 
	 * @param context
	 *            The plugincontext (nullable).
	 * @param inputXML
	 *            The input XML as a stream.
	 * @param inputXSD
	 *            The input XSD Schema location.
	 * @return The document.
	 */
	public static Document importFromStream(PluginContext context, InputStream inputXML, String inputXSD) {

		// Create dom4j parser using the XSD schema
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		try {
			factory.setSchema(schemaFactory.newSchema(new StreamSource(inputXSD)));

			SAXParser parser = factory.newSAXParser();
			SAXReader reader = new SAXReader(parser.getXMLReader());
			// reader validation is for DTD embedded in XML. Since we're using an XSD it needs to be set to false!
			reader.setValidation(false);

			// Show nice output with linenumbers in case something goes wrong.
			if (context != null)
				reader.setErrorHandler(new LineErrorHandler(context));
			else
				reader.setErrorHandler(new LineErrorHandler());

			// Read the file
			Document document = reader.read(inputXML);

			// Return the XML document
			return document;

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Error handler that shows which lines contain an error.
	 * 
	 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
	 *
	 */
	private static class LineErrorHandler implements ErrorHandler {
		PluginContext pluginContext;

		public LineErrorHandler() {
			CLIContext cliContext = new CLIContext();
			this.pluginContext = new CLIPluginContext(cliContext, "LineErrorHandler");
		}

		public LineErrorHandler(PluginContext context) {
			this.pluginContext = context;
		}

		public void warning(SAXParseException exception) throws SAXException {
			String message = "Line: " + exception.getLineNumber() + " - " + exception.getMessage();
			System.out.println(message);
			pluginContext.log(message, MessageLevel.WARNING);
		}

		public void error(SAXParseException exception) throws SAXException {
			String message = "Line: " + exception.getLineNumber() + " - " + exception.getMessage();
			System.out.println(message);
			pluginContext.log(message, MessageLevel.ERROR);
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			String message = "Line: " + exception.getLineNumber() + " - " + exception.getMessage();
			System.out.println(message);
			pluginContext.log(message, MessageLevel.ERROR);
		}
	}
}
