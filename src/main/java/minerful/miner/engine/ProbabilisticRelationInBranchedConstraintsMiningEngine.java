package minerful.miner.engine;

import java.util.SortedSet;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.MutualRelationConstraint;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.LocalStatsWrapperForCharsets;
import minerful.miner.stats.charsets.TasksSetCounter;

public class ProbabilisticRelationInBranchedConstraintsMiningEngine {
	private GlobalStatsTable globalStats;
	
	public ProbabilisticRelationInBranchedConstraintsMiningEngine(
			GlobalStatsTable globalStats) {
		this.globalStats = globalStats;
	}

	public AlternatePrecedence discoverBranchedAlternatePrecedenceConstraints(
			TaskChar searched,
			LocalStatsWrapper searchedLocalStats,
			long searchedAppearances,
			TaskCharSet comboToAnalyze) {
		AlternatePrecedence nuConstraint = null;
		if (searchedAppearances < 1)
			return nuConstraint;

		double support = 0;
		int negativeOccurrences = 0;

		LocalStatsWrapperForCharsets extSearchedLocalStats = (LocalStatsWrapperForCharsets) searchedLocalStats;
		SortedSet<TasksSetCounter> neverMoreBeforeFirstCharSets = 
				extSearchedLocalStats.getNeverMoreBeforeFirstOccurrenceCharacterSets()
				.selectCharSetCountersSharedAmong(
						comboToAnalyze.getTaskCharsArray()
						);
		SortedSet<TasksSetCounter> alternationCharSets = 
				extSearchedLocalStats.getRepetitionsAfterCharactersAppearingBefore()
				.selectCharSetCountersSharedAmong(
						comboToAnalyze.getTaskCharsArray()
				);
		for (TasksSetCounter neverMoreBeforeFirstCharSet : neverMoreBeforeFirstCharSets) {
			negativeOccurrences += neverMoreBeforeFirstCharSet.getCounter();
		}
		for (TasksSetCounter alternationBeforeCharSet : alternationCharSets) {
			negativeOccurrences += alternationBeforeCharSet.getCounter();
		}

		support = 1.0 - (double)negativeOccurrences / (double)searchedAppearances;

		nuConstraint = new AlternatePrecedence(
				new TaskCharSet(searched),
				comboToAnalyze,
				support);

		return nuConstraint;
	}

	public AlternateResponse discoverBranchedAlternateResponseConstraints(
			TaskChar searched,
			TaskCharSet comboToAnalyze) {
		AlternateResponse nuConstraint = null;
	
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper pivotStatsWrapper = null;
		
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			pivotStatsWrapper = globalStats.statsTable.get(pivot);
			negativeOccurrences += pivotStatsWrapper.interplayStatsTable.get(searched.identifier).howManyTimesItNeverAppearedOnwards();
			negativeOccurrences += pivotStatsWrapper.interplayStatsTable.get(searched.identifier).betweenOnwards;
			denominator += pivotStatsWrapper.getTotalAmountOfOccurrences();
		}
		
		if (denominator > 0) {	// in case no pivot ever appeared, do not add this constraint!
			support = 1.0 - (double) negativeOccurrences / (double) denominator;
			nuConstraint = new AlternateResponse(
					comboToAnalyze,
					new TaskCharSet(searched),
					support);
		}
		
		return nuConstraint;
	}

	public AlternateSuccession discoverBranchedAlternateSuccessionConstraints(
			TaskChar searched,
			LocalStatsWrapper searchedLocalStats,
			long searchedAppearances,
			TaskCharSet comboToAnalyze) {
		AlternateSuccession nuConstraint = null;
		
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper pivotStatsWrapper = null;
		
		LocalStatsWrapperForCharsets extSearchedLocalStats = (LocalStatsWrapperForCharsets) searchedLocalStats;
		SortedSet<TasksSetCounter>
			neverAppearedBeforeCharSets = null,
			repetitionsBeforeAppearingAfterCharSets = null;
		
		negativeOccurrences = 0;
		denominator = 0;
		
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			pivotStatsWrapper = globalStats.statsTable.get(pivot);
			negativeOccurrences += pivotStatsWrapper.interplayStatsTable.get(searched.identifier).howManyTimesItNeverAppearedOnwards();
			negativeOccurrences += pivotStatsWrapper.interplayStatsTable.get(searched.identifier).betweenOnwards;
			denominator += pivotStatsWrapper.getTotalAmountOfOccurrences();
		}
		neverAppearedBeforeCharSets =
				extSearchedLocalStats.getNeverMoreAppearedBeforeCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getTaskCharsArray());
//		repetitionsBeforeAppearingAfterCharSets = 
//				extSearchedLocalStats.repetitionsAfterCharactersAppearingBefore.selectCharSetCountersSharedAmong(comboToAnalyze.getTaskChars());
//		for (CharactersSetCounter repetitionsAfterCharactersAppearingBeforeCharSet : repetitionsBeforeAppearingAfterCharSets) {
//			negativeOccurrences += repetitionsAfterCharactersAppearingBeforeCharSet.getCounter();
//		}
		for (TasksSetCounter neverAppearedCharSet : neverAppearedBeforeCharSets) {
			negativeOccurrences += neverAppearedCharSet.getCounter();
		}
		denominator += searchedAppearances;
	
		
		if (denominator > 0) {	// in case no pivot nor searched ever appeared, do not add this constraint!
			support = 1.0 - (double) negativeOccurrences / (double) denominator;
			nuConstraint = new AlternateSuccession(
					comboToAnalyze,
					new TaskCharSet(searched),
					support);
		}
	
		return nuConstraint;
	}

	public ChainPrecedence discoverBranchedChainPrecedenceConstraints(
			TaskChar searched,
			LocalStatsWrapper searchedLocalStats,
			long searchedAppearances,
			TaskCharSet comboToAnalyze) {
		ChainPrecedence nuConstraint = null;
	
		if (searchedAppearances < 1)
			return nuConstraint;
	
		int	positiveOccurrences = 0;
		double support = 0;
		Integer tmpPositiveOccurrencesAdder = null;
			
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			tmpPositiveOccurrencesAdder = searchedLocalStats.interplayStatsTable.get(pivot).distances.get(-1);
			if (tmpPositiveOccurrencesAdder != null)
				positiveOccurrences += tmpPositiveOccurrencesAdder;
		}				
		support = (double) positiveOccurrences / (double) searchedAppearances;
		nuConstraint = new ChainPrecedence(
				new TaskCharSet(searched),
				comboToAnalyze,
				support);
		return nuConstraint;
	}

	public ChainResponse discoverBranchedChainResponseConstraints(
			TaskChar searched,
			TaskCharSet comboToAnalyze) {
		ChainResponse nuConstraint = null;
		
		int	positiveOccurrences = 0,
			denominator = 0;
		Integer tmpPositiveOccurrencesAdder = null;
		double support = 0;
		LocalStatsWrapper pivotStatsWrapper = null;
		
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			pivotStatsWrapper = globalStats.statsTable.get(pivot);
			tmpPositiveOccurrencesAdder = pivotStatsWrapper.interplayStatsTable.get(searched.identifier).distances.get(1); 
			if (tmpPositiveOccurrencesAdder != null)
				positiveOccurrences += tmpPositiveOccurrencesAdder;
			denominator += pivotStatsWrapper.getTotalAmountOfOccurrences();
		}
		
		if (denominator > 0) {	// in case no pivot ever appeared, do not add this constraint!
			support = (double) positiveOccurrences / (double) denominator;
			nuConstraint = new ChainResponse(
					comboToAnalyze,
					new TaskCharSet(searched),
					support);
		}
		return nuConstraint;
	}

	public ChainSuccession discoverBranchedChainSuccessionConstraint(
			TaskChar searched,
			LocalStatsWrapper searchedLocalStats,
			long searchedAppearances,
			TaskCharSet comboToAnalyze) {
		ChainSuccession nuConstraint = null;
	
		int	positiveOccurrences = 0,
			denominator = 0;
		double support = 0;
		Integer tmpPositiveOccurrencesAdder = null;
		LocalStatsWrapper
			pivotLocalStats = null;
			positiveOccurrences = 0;
			denominator = (int) searchedAppearances;
			
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			pivotLocalStats = globalStats.statsTable.get(pivot);
			tmpPositiveOccurrencesAdder = pivotLocalStats.interplayStatsTable.get(searched.identifier).distances.get(1);
			if (tmpPositiveOccurrencesAdder != null)
				positiveOccurrences += tmpPositiveOccurrencesAdder;
			denominator += pivotLocalStats.getTotalAmountOfOccurrences();
	
			tmpPositiveOccurrencesAdder = searchedLocalStats.interplayStatsTable.get(pivot).distances.get(-1);
			if (tmpPositiveOccurrencesAdder != null)
				positiveOccurrences += tmpPositiveOccurrencesAdder;
		}
		support = (double) positiveOccurrences / (double) denominator;
		
		nuConstraint = new ChainSuccession(
				comboToAnalyze,
				new TaskCharSet(searched),
				support);
	
	
		return nuConstraint;
	}

	public MutualRelationConstraint discoverBranchedCoExistenceConstraints(
			TaskChar searched,
			LocalStatsWrapper searchedLocalStats,
			long searchedAppearances,
			TaskCharSet comboToAnalyze) {
		MutualRelationConstraint nuConstraint = null;
		
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper pivotStatsWrapper = null;
		
		LocalStatsWrapperForCharsets extSearchedLocalStats = (LocalStatsWrapperForCharsets) searchedLocalStats;
		SortedSet<TasksSetCounter> neverAppearedCharSets = null;
		
		negativeOccurrences = 0;
		denominator = 0;
		
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			pivotStatsWrapper = globalStats.statsTable.get(pivot);
			negativeOccurrences += pivotStatsWrapper.interplayStatsTable.get(searched.identifier).howManyTimesItNeverAppearedAtAll();
			denominator += pivotStatsWrapper.getTotalAmountOfOccurrences();
		}
		neverAppearedCharSets =
				extSearchedLocalStats.getNeverAppearedCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getTaskCharsArray());
		for (TasksSetCounter neverAppearedCharSet : neverAppearedCharSets) {
			negativeOccurrences += neverAppearedCharSet.getCounter();
		}
		denominator += searchedAppearances;
	
		
		if (denominator > 0) {	// in case no pivot nor searched ever appeared, do not add this constraint!
			support = 1.0 - (double)negativeOccurrences / (double)denominator;
			nuConstraint = new CoExistence(
					comboToAnalyze,
					new TaskCharSet(searched),
					support);
		}
		
		return nuConstraint;
	}

	public Precedence discoverBranchedPrecedenceConstraints(
			TaskChar searched,
			LocalStatsWrapper searchedLocalStats,
			long searchedAppearances,
			TaskCharSet comboToAnalyze) {
	
		Precedence nuConstraint = null;
		if (searchedAppearances < 1)
			return nuConstraint;
		
		LocalStatsWrapperForCharsets extSearchedLocalStats = (LocalStatsWrapperForCharsets) searchedLocalStats;
		
		SortedSet<TasksSetCounter> neverBeforeAppearedCharSets = null;
		int negativeOccurrences = 0;
		double support = 0;
	
		neverBeforeAppearedCharSets =
				extSearchedLocalStats.getNeverMoreAppearedBeforeCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getTaskCharsArray());
		
		if (neverBeforeAppearedCharSets.size() == 0) {
			nuConstraint = new Precedence(
					new TaskCharSet(searched),
					comboToAnalyze,
					1.0);
		} else {
			for (TasksSetCounter neverAppearedAfterCharSet : neverBeforeAppearedCharSets) {
				negativeOccurrences += neverAppearedAfterCharSet.getCounter();
				support = 1.0 - (double)negativeOccurrences / (double)searchedAppearances;
				nuConstraint = new Precedence(
						new TaskCharSet(searched),
						comboToAnalyze,
						support);
			}
		}
		return nuConstraint;
	}

	public RespondedExistence discoverBranchedRespondedExistenceConstraints(
			TaskChar searched,
			TaskCharSet comboToAnalyze) {
		RespondedExistence nuConstraint = null;
		
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper pivotStatsWrapper = null;
		
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			pivotStatsWrapper = globalStats.statsTable.get(pivot);
			negativeOccurrences += pivotStatsWrapper.interplayStatsTable.get(searched.identifier).howManyTimesItNeverAppearedAtAll();
			denominator += pivotStatsWrapper.getTotalAmountOfOccurrences();
		}
		
		if (denominator > 0) {	// in case no pivot ever appeared, do not add this constraint!
			support = 1.0 - (double) negativeOccurrences / (double) denominator;
			nuConstraint = new RespondedExistence(
					comboToAnalyze,
					new TaskCharSet(searched),
					support);
		}
		
		return nuConstraint;
	}

	public Response discoverBranchedResponseConstraints(
			TaskChar searched,
			TaskCharSet comboToAnalyze) {
		Response nuConstraint = null;
	
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper pivotStatsWrapper = null;
		
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			pivotStatsWrapper = globalStats.statsTable.get(pivot);
			negativeOccurrences += pivotStatsWrapper.interplayStatsTable.get(searched.identifier).howManyTimesItNeverAppearedOnwards();
			denominator += pivotStatsWrapper.getTotalAmountOfOccurrences();
		}
		
		if (denominator > 0) {	// in case no pivot ever appeared, do not add this constraint!
			support = 1.0 - (double) negativeOccurrences / (double) denominator;
			nuConstraint = new Response(
					comboToAnalyze,
					new TaskCharSet(searched),
					support);
		}
		
		return nuConstraint;
	}

	public Succession discoverBranchedSuccessionConstraints(
			TaskChar searched,
			LocalStatsWrapper searchedLocalStats,
			long searchedAppearances,
			TaskCharSet comboToAnalyze) {
		Succession nuConstraint = null;
		
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper pivotStatsWrapper = null;
		
		LocalStatsWrapperForCharsets extSearchedLocalStats = (LocalStatsWrapperForCharsets) searchedLocalStats;
		SortedSet<TasksSetCounter> neverAppearedCharSets = null;
		
		negativeOccurrences = 0;
		denominator = 0;
		
		for (TaskChar pivot : comboToAnalyze.getTaskCharsArray()) {
			pivotStatsWrapper = globalStats.statsTable.get(pivot);
			negativeOccurrences += pivotStatsWrapper.interplayStatsTable.get(searched.identifier).howManyTimesItNeverAppearedOnwards();
			denominator += pivotStatsWrapper.getTotalAmountOfOccurrences();
		}
		neverAppearedCharSets =
				extSearchedLocalStats.getNeverMoreAppearedBeforeCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getTaskCharsArray());
		for (TasksSetCounter neverAppearedCharSet : neverAppearedCharSets) {
			negativeOccurrences += neverAppearedCharSet.getCounter();
		}
		denominator += searchedAppearances;
	
		
		if (denominator > 0) {	// in case no pivot nor searched ever appeared, do not add this constraint!
			support = 1.0 - (double) negativeOccurrences / (double) denominator;
			nuConstraint = new Succession(
					comboToAnalyze,
					new TaskCharSet(searched),
					support);
		}
	
		return nuConstraint;
	}

}
