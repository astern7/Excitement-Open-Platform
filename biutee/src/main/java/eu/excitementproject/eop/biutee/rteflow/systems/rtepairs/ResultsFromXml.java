package eu.excitementproject.eop.biutee.rteflow.systems.rtepairs;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.excitementproject.eop.common.utilities.datasets.rtepairs.RTEClassificationType;
import eu.excitementproject.eop.common.utilities.xmldom.XmlDomUtilitiesException;

import static eu.excitementproject.eop.common.utilities.xmldom.XmlDomUtils.*;
import static eu.excitementproject.eop.biutee.rteflow.systems.rtepairs.ResultsToXml.*;

/**
 * 
 * @author Asher Stern
 * @since Apr 3, 2014
 *
 */
public class ResultsFromXml
{
	public ResultsFromXml(File xmlFile)
	{
		super();
		this.xmlFile = xmlFile;
	}


	public void read() throws XmlDomUtilitiesException
	{
		results = new LinkedHashMap<>();
		
		Document document = getDocument(xmlFile);
		Element documentElement = document.getDocumentElement();
		List<Element> pairElements = getChildElements(documentElement,PAIR_ELEMENT_NAME);
		for (Element pairElement : pairElements)
		{
			String id = pairElement.getAttribute(ID_ATTRIBUTE_NAME);
			String entailment = pairElement.getAttribute(ENTAILMENT_ATTIRUBTE_NAME);
			String score = pairElement.getAttribute(SCORE_ATTRIBUTE_NAME);
			
			results.put(id, new ScoreAndRTEClassificationType(Double.valueOf(score),RTEClassificationType.valueOf(entailment)));
		}
	}
	
	
	public Map<String, ScoreAndRTEClassificationType> getResults()
	{
		return results;
	}




	private final File xmlFile;
	private Map<String, ScoreAndRTEClassificationType> results = null;
}
