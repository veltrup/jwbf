package net.sourceforge.jwbf.mediawiki.actions.meta;

import static net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version.MW1_09;
import static net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version.MW1_10;
import static net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version.MW1_11;
import static net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version.MW1_12;
import static net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version.MW1_13;
import static net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version.MW1_14;
import static net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version.MW1_15;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import net.sourceforge.jwbf.core.JWBF;
import net.sourceforge.jwbf.core.actions.Get;
import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.mediawiki.actions.MediaWiki.Version;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.actions.util.SupportedBy;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

/**
 * Basic action to receive {@link Version}.
 * 
 * @author Thomas Stock
 *
 */

@SupportedBy({ MW1_09, MW1_10, MW1_11, MW1_12, MW1_13, MW1_14, MW1_15 })
public class GetVersion extends MWAction {

	private final Logger log = Logger.getLogger(getClass());
	private final Get msg;
	private String generator = "";
	private String sitename = "";
	private String base = "";
	private String theCase = "";
	private String mainpage = "";
	

	/**
	 * Create and submit the request to the Wiki. 
	 * Do not use {@link MediaWikiBot#performAction(net.sourceforge.jwbf.actions.ContentProcessable)}.
	 * @param bot a
	 * @throws ProcessException a
	 * @throws ActionException a
	 */
	public GetVersion(MediaWikiBot bot) throws ActionException, ProcessException {
		this();
		bot.performAction(this);
	}
	
	/* In this case the superconstructor with no value is allowed, because
	 * the versionrequest is mandatory */
	/**
	 * Create the request.
	 */
	@SuppressWarnings("deprecation")
	public GetVersion()  {
			
			msg = new Get("/api.php?action=query&meta=siteinfo&format=xml");

	}
	private void parse(final String xml) throws ProcessException {
		SAXBuilder builder = new SAXBuilder();
		Element root = null;
		try {
			Reader i = new StringReader(xml);
			Document doc = builder.build(new InputSource(i));

			root = doc.getRootElement();
			findContent(root);
		} catch (JDOMException e) {
			log.error(e.getClass().getName() + e.getLocalizedMessage());
			log.error(xml);
			throw new ProcessException(e.getLocalizedMessage());
		} catch (IOException e) {
			log.error(e.getClass().getName() + e.getLocalizedMessage());
			throw new ProcessException(e.getLocalizedMessage());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final String processAllReturningText(final String s)
			throws ProcessException {
		parse(s);
		return "";
	}


	/**
	 * 
	 * @return the, like "Wikipedia"
	 */
	public String getSitename() {
		return sitename;
	}
	/**
	 * 
	 * @return the, like "http://de.wikipedia.org/wiki/Wikipedia:Hauptseite"
	 */
	public String getBase() {
		return base;
	}
	/**
	 * 
	 * @return the, like "first-letter"
	 */
	public String getCase() {
		return theCase;
	}
	/**
	 * 
	 * @return the
	 * @see Version
	 */
	public Version getVersion() {
		if (getGenerator().contains("alpha")) {
			return Version.DEVELOPMENT;
		}

		Version[] versions = Version.values();
		for (int i = 0; i < versions.length; i++) {
			if (getGenerator().contains(versions[i].getNumber())) {
				return versions[i];
			}

		}

		log.info("\nVersion is UNKNOWN for JWBF ("
						+ JWBF.getVersion(getClass())
						+ ") : \n\t"
						+ getGenerator()
						+ "\n\tUsing settings for actual Wikipedia development version");
		return Version.UNKNOWN;

	}
	/**
	 * 
	 * @return the MediaWiki Generator, like "MediaWiki 1.16alpha"
	 */
	public String getGenerator() {
		return generator;
	}
	/**
	 * 
	 * @return the, like "Main Page"
	 */
	public String getMainpage() {
		return mainpage;
	}
	
	@SuppressWarnings("unchecked")
	protected void findContent(final Element root) {

		Iterator<Element> el = root.getChildren().iterator();
		while (el.hasNext()) {
			Element element = el.next();
			if (element.getQualifiedName().equalsIgnoreCase("general")) {

				mainpage = element
						.getAttributeValue("mainpage");
				base = element.getAttributeValue("base");
				sitename = element
						.getAttributeValue("sitename");
				generator = element
						.getAttributeValue("generator");
				theCase = element.getAttributeValue("case");
			} else {
				findContent(element);
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	public HttpAction getNextMessage() {
		return msg;
	}
}
