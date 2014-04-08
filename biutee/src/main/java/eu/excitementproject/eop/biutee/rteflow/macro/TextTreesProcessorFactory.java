package eu.excitementproject.eop.biutee.rteflow.macro;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.excitementproject.eop.biutee.classifiers.LinearClassifier;
import eu.excitementproject.eop.biutee.rteflow.macro.search.WithStatisticsTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.macro.search.kstaged.KStagedTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.macro.search.local_creative.LocalCreativeTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.macro.search.old_beam_search.BeamSearchTextTreesProcessor;
import eu.excitementproject.eop.biutee.rteflow.systems.TESystemEnvironment;
import eu.excitementproject.eop.biutee.script.OperationsScript;
import eu.excitementproject.eop.biutee.utilities.HiddenConfigurationProvider;
import eu.excitementproject.eop.common.representation.coreference.TreeCoreferenceInformation;
import eu.excitementproject.eop.common.representation.parse.representation.basic.Info;
import eu.excitementproject.eop.common.representation.parse.tree.dependency.basic.BasicNode;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.lap.biu.lemmatizer.Lemmatizer;
import eu.excitementproject.eop.transformations.representation.ExtendedNode;
import eu.excitementproject.eop.transformations.utilities.GlobalMessages;
import eu.excitementproject.eop.transformations.utilities.TeEngineMlException;
import eu.excitementproject.eop.biutee.rteflow.macro.search.astar.AStarTextTreesProcessor;

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
	
	/**
	 * A method that returns {@link LocalCreativeTextTreesProcessor}.
	 * @return {@link LocalCreativeTextTreesProcessor}
	 */
	public static WithStatisticsTextTreesProcessor createProcessor(
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
		
		// Ignore the following line. ret will be null.
		WithStatisticsTextTreesProcessor ret = tryLoadFromHiddenParameters(textText,hypothesisText,originalTextTrees,hypothesisTree,originalMapTreesToSentences,coreferenceInformation,classifier,lemmatizer,script,teSystemEnvironment);
		
		// ret must be and will be null! The returned object will be LocalCreativeTextTreesProcessor, as you can see below.
		if (null==ret)
		{
			// This is what really returned.
			ret = new LocalCreativeTextTreesProcessor(textText,hypothesisText,
					originalTextTrees,hypothesisTree,originalMapTreesToSentences,
					coreferenceInformation,classifier,lemmatizer, script,
					teSystemEnvironment);

		}
		return ret;
	}
	
	
	
	/**
	 * A method that returns null and throws no exception. Ignore it.
	 * <P>
	 * (Some experiments in BIU might change the behavior of this method. However, for all other users
	 * this method will return null).
	 */
	private static WithStatisticsTextTreesProcessor tryLoadFromHiddenParameters(
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
		try
		{
			ConfigurationParams hiddenParams = HiddenConfigurationProvider.getHiddenParams();
			if (hiddenParams!=null) // Should be null
			{
				GlobalMessages.globalWarn("Loading TextTreesProcessor from hidden parameters.", logger);

				WithStatisticsTextTreesProcessor ret = null;
				if ( hiddenParams.containsKey(HIDDEN_PARAMETER_SEARCH_ALGORITHM) )
				{
					String searchAlgorithmName = hiddenParams.get(HIDDEN_PARAMETER_SEARCH_ALGORITHM);
					logger.info("Using search algorithm \""+searchAlgorithmName+"\"");
					
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
						aStarTextTreesProcessor.setWeightOfFuture(6.0);
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
				}
//				else if ( hiddenParams.containsKey(HIDDEN_PARAMETER_REFLECTION_PROCESSOR) )
//				{
//					String processorClassName = hiddenParams.get(HIDDEN_PARAMETER_REFLECTION_PROCESSOR);
//					logger.info("TextTreesProcessor class is "+processorClassName);
//					Class<?> clsProcessor = Class.forName(processorClassName);
//					Constructor<?> constructor = clsProcessor.getConstructor(String.class,String.class,List.class,ExtendedNode.class,Map.class,TreeCoreferenceInformation.class,LinearClassifier.class,Lemmatizer.class,OperationsScript.class,TESystemEnvironment.class);
//					ret = (WithStatisticsTextTreesProcessor) constructor.newInstance(textText,hypothesisText,
//						originalTextTrees,hypothesisTree,originalMapTreesToSentences,
//						coreferenceInformation,classifier,lemmatizer, script,
//						teSystemEnvironment);
//				}
				else
				{
					ret = null;
				}
				
				return ret;
			}
			else
			{
				return null;
			}
		}
		catch (ConfigurationException | RuntimeException
//				| ClassNotFoundException| NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException
				e)
		{
			throw new TeEngineMlException("A failure when trying to load from hidden parameters. Please see nested exception.",e);
		}
	}
	
	private static final Logger logger = Logger.getLogger(TextTreesProcessorFactory.class);
}
