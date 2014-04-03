package eu.excitementproject.eop.biutee.rteflow.systems.rtepairs;

import eu.excitementproject.eop.common.utilities.datasets.rtepairs.RTEClassificationType;

public class ScoreAndRTEClassificationType
{
	public ScoreAndRTEClassificationType(double score,RTEClassificationType classification)
	{this.score = score;this.classification = classification;}
	
	public double getScore(){return score;}
	public RTEClassificationType getClassification(){return classification;}

	private final double score;
	private final RTEClassificationType classification;
}