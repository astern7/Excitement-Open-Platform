package eu.excitementproject.eop.biutee.rteflow.macro;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.excitementproject.eop.biutee.classifiers.LinearClassifier;
import eu.excitementproject.eop.biutee.rteflow.macro.search.WithStatisticsTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.macro.search.astar.AStarTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.macro.search.kstaged.KStagedTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.macro.search.local_creative.LocalCreativeTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.macro.search.old_beam_search.BeamSearchTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.systems.TESystemEnvironment;
import eu.excitementproject.eop.biutee.script.OperationsScript;
import eu.excitementproject.eop.common.representation.coreference.TreeCoreferenceInformation;
import eu.excitementproject.eop.common.representation.parse.representation.basic.Info;
import eu.excitementproject.eop.common.representation.parse.tree.dependency.basic.BasicNode;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.lap.biu.lemmatizer.Lemmatizer;
import eu.excitementproject.eop.transformations.representation.ExtendedNode;
import eu.excitementproject.eop.transformations.utilities.TeEngineMlException;

/**
 * A factory of {@link TextTreesProcessor} which creates and returns {@link LocalCreativeTextTreesProcessor}.
 * <P>
 * (Some experiments in BIU might change the behavior of this class. However, for all other users - this class just
 * returns {@link LocalCreativeTextTreesProcessor}).
 *  
 * @author Asher Stern
 * @since Jan 8, 2014
 *
 */
public class TextTreesProcessorFactory
{
	public static final String HIDDEN_PARAMETER_REFLECTION_PROCESSOR = "processor-class";
	public static final String HIDDEN_PARAMETER_SEARCH_ALGORITHM = "search-algorithm";
	
	public TextTreesProcessorFactory(ConfigurationParams params) throws ConfigurationException
	{
		this.params = params;
		if (this.params.containsKey(HIDDEN_PARAMETER_SEARCH_ALGORITHM))
		{
			this.searchAlgorithmName = this.params.getString(HIDDEN_PARAMETER_SEARCH_ALGORITHM);
		}
	}
	
	/**
	 * A method that returns {@link LocalCreativeTextTreesProcessor}.
	 * @return {@link LocalCreativeTextTreesProcessor}
	 */
	public WithStatisticsTextTreesProcessor createProcessor(
			String textText, String hypothesisText,
			List<ExtendedNode> originalTextTrees,
			ExtendedNode hypothesisTree,
			Map<ExtendedNode, String> originalMapTreesToSentences,
			TreeCoreferenceInformation<ExtendedNode> coreferenceInformation,
			LinearClassifier classifier,
			Lemmatizer lemmatizer, OperationsScript<Info, BasicNode> script,
			TESystemEnvironment teSystemEnvironment
			) throws TeEngineMlException
	{
		WithStatisticsTextTreesProcessor ret = null;
		if (this.searchAlgorithmName!=null)
		{
			logger.info("Using search algorithm \""+searchAlgorithmName+"\"");
			ret = loadFromParameter(this.searchAlgorithmName, textText, hypothesisText, originalTextTrees, hypothesisTree, originalMapTreesToSentences, coreferenceInformation, classifier, lemmatizer, script, teSystemEnvironment);
		}
		
		if (null==ret)
		{
			ret = new LocalCreativeTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment);
		}
		return ret;
	}
	
	
	private WithStatisticsTextTreesProcessor loadFromParameter(String searchAlgorithmName,
			String textText, String hypothesisText,
			List<ExtendedNode> originalTextTrees,
			ExtendedNode hypothesisTree,
			Map<ExtendedNode, String> originalMapTreesToSentences,
			TreeCoreferenceInformation<ExtendedNode> coreferenceInformation,
			LinearClassifier classifier,
			Lemmatizer lemmatizer, OperationsScript<Info, BasicNode> script,
			TESystemEnvironment teSystemEnvironment) throws TeEngineMlException
	{
		WithStatisticsTextTreesProcessor ret = null;
		
		if ("weighted-A*-train-and-test".equals(searchAlgorithmName))
		{
			AStarTextTreesProcessor aStarTextTreesProcessor = new AStarTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment);
			aStarTextTreesProcessor.setWeightOfFuture(30.0);
			ret=aStarTextTreesProcessor;
		}
		if ("weighted-A*-test-only".equals(searchAlgorithmName))
		{
			AStarTextTreesProcessor aStarTextTreesProcessor = new AStarTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment);
			aStarTextTreesProcessor.setWeightOfFuture(7.0);
			ret=aStarTextTreesProcessor;
		}
		else if ("dovetaling-WA*".equals(searchAlgorithmName))
		{
			AStarTextTreesProcessor aStarTextTreesProcessor = new AStarTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment);
			aStarTextTreesProcessor.setWeightOfCost(1.0);
			aStarTextTreesProcessor.setWeightOfFuture(30.0);
			aStarTextTreesProcessor.useAnyTimeMode(10000, 0.5);
			aStarTextTreesProcessor.setK_expandInEachIteration(30);
			ret = aStarTextTreesProcessor;
		}
		else if ("greedy".equals(searchAlgorithmName))
		{
			KStagedTextTreesProcessor kStagedTextTreesProcessor = new KStagedTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment,
					1,1,0,
					1.0, 
					6.0 // "w": weight of future, set to 6.
					);
			kStagedTextTreesProcessor.setkStagedDiscardExpandedStates(true);
			kStagedTextTreesProcessor.setSeparatelyProcessTextSentencesMode(true);
			ret = kStagedTextTreesProcessor;
		}
		else if ("pure-heuristic".equals(searchAlgorithmName))
		{
			KStagedTextTreesProcessor kStagedTextTreesProcessor = new KStagedTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment,
					1,1,0,
					0.0, // "w": weight of cost, set to 0 -- pure heuristic.
					1.0);
			kStagedTextTreesProcessor.setkStagedDiscardExpandedStates(true);
			kStagedTextTreesProcessor.setSeparatelyProcessTextSentencesMode(true);
			ret = kStagedTextTreesProcessor;
		}
		else if ("beam-search".equals(searchAlgorithmName))
		{
			KStagedTextTreesProcessor kStagedTextTreesProcessor = new KStagedTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment,
					150,150,
					5, // continue after goal was found, PROBABLY, that what was used in ACL 2012
					1.0, // "w": weight of cost, set to 1, that's PROBABLY what was used in ACL 2012
					5.0 // "w": weight of future, set to 5, that's PROBABLY what was used in ACL 2012
					);
			kStagedTextTreesProcessor.setkStagedDiscardExpandedStates(true);
			kStagedTextTreesProcessor.setSeparatelyProcessTextSentencesMode(true);
			ret = kStagedTextTreesProcessor;
		}
		else if ("dynamic-beam-search".equals(searchAlgorithmName))
		{
			BeamSearchTextTreesProcessor beamSearchTextTreesProcessor = new BeamSearchTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment);
			ret = beamSearchTextTreesProcessor;
		}
		else
		{
			throw new TeEngineMlException("The required search algorithm (by hidden parameters), \""+searchAlgorithmName+"\" could not be recognized.");
		}
		return ret;
	}
	
	

	
	private final ConfigurationParams params;
	private String searchAlgorithmName = null;
	
	private static final Logger logger = Logger.getLogger(TextTreesProcessorFactory.class);
}
