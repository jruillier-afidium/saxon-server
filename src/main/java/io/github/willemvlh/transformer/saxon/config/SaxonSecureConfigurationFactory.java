package io.github.willemvlh.transformer.saxon.config;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;

public class SaxonSecureConfigurationFactory extends SaxonConfigurationFactory {

	@Override
	public Configuration newConfiguration() {
		Configuration config = new Configuration();
		config.setAllowedUriTest(uri -> false);
		config.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
		return config;
	}

	@Override
	public SAXSource newSAXSource(InputStream stream) {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);

		try {
			spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			SAXParser saxParser = spf.newSAXParser();
			
			return new SAXSource(saxParser.getXMLReader(), new InputSource(stream));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
