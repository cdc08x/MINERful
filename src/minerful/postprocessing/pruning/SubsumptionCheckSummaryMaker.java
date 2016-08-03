package minerful.postprocessing.pruning;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;
import minerful.concept.constraint.relation.MutualRelationConstraint;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.postprocessing.pruning.SubsumptionCheckSummaryMaker.Subsumption.Equalisation;
import minerful.postprocessing.pruning.SubsumptionCheckSummaryMaker.Subsumption.Extension;
import minerful.postprocessing.pruning.SubsumptionCheckSummaryMaker.Subsumption.None;
import minerful.postprocessing.pruning.SubsumptionCheckSummaryMaker.Subsumption.Restriction;
import minerful.postprocessing.pruning.SubsumptionCheckSummaryMaker.Subsumption.SubsumptionKind;
import minerful.postprocessing.pruning.SubsumptionCheckSummaryMaker.Subsumption.SubsumptionKindClassComparator;
import minerful.postprocessing.pruning.SubsumptionCheckSummaryMaker.Subsumption.SubsumptionKindComparator;

public class SubsumptionCheckSummaryMaker {
	public static class Subsumption {
		public static interface SubsumptionKind {
			String getKind();
		}
		public static enum None implements SubsumptionKind {
			NONE;

			@Override
			public String getKind() {
				return this.toString();
			}
		}
		public static enum Equalisation implements SubsumptionKind {
			EQUAL_TO;

			@Override
			public String getKind() {
				return this.toString();
			}
		}
		public static enum Restriction implements SubsumptionKind {
			DIRECT_CHILD_OF,
			DESCENDANT_OF,
			INCLUDES_AS_FORWARD,
			INCLUDES_AS_BACKWARD,
			SAME_TEMPLATE_SAME_ACTIVATION_TARGET_INCLUDED_IN,
			// Both-ways (hierarchy and set-inclusion) relaxation
			TEMPLATE_DESCENDANT_OF_SAME_ACTIVATION_TARGET_INCLUDED_IN;

			@Override
			public String getKind() {
				return this.toString();
			}
		}
		public static enum Extension implements SubsumptionKind {
			DIRECT_PARENT_OF,
			ANCESTOR_OF,
			IS_FORWARD_OF,
			IS_BACKWARD_OF,
			SAME_TEMPLATE_SAME_ACTIVATION_TARGET_INCLUDES,
			// Both-ways (hierarchy and set-inclusion) restriction
			TEMPLATE_ANCESTOR_OF_SAME_ACTIVATION_TARGET_INCLUDES;

			@Override
			public String getKind() {
				return this.toString();
			}
		}

		public final Constraint constraint;
		public final SubsumptionKind kind;
		
		public Subsumption(Constraint constraint,
				SubsumptionKind subsumptionKind) {
			this.constraint = constraint;
			this.kind = subsumptionKind;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(kind);
			builder.append('(');
			builder.append(constraint);
			builder.append(")");
			return builder.toString();
		}
		
		public static class SubsumptionKindClassComparator implements Comparator<Class<? extends SubsumptionKind>> {
			@Override
			public int compare(Class<? extends SubsumptionKind> o1, Class<? extends SubsumptionKind> o2) {
		        return o1.getName().compareTo(o2.getName());
			}
		}
		
		public static class SubsumptionKindComparator implements Comparator<SubsumptionKind> {
			@Override
			public int compare(SubsumptionKind o1, SubsumptionKind o2) {
				return o1.getKind().compareTo(o2.getKind());
			}
		}
	}
	
	private static final SubsumptionKindClassComparator cla_compa = new SubsumptionKindClassComparator();
	private static final SubsumptionKindComparator kin_compa = new SubsumptionKindComparator();
	private NavigableMap<SubsumptionKind, Integer> checks;
	private Map<Class<? extends Subsumption.SubsumptionKind>, Integer> checksSummary;
	private Constraint[] model;
	
	public SubsumptionCheckSummaryMaker(Constraint[] model) {
		this.initCounters();
		this.model = model;
	}
	
	public SubsumptionCheckSummaryMaker(Collection<Constraint> allConstraints) {
		this.initCounters();
		this.model = allConstraints.toArray(new Constraint[allConstraints.size()]);
	}

	private void initCounters() {
		checks = new TreeMap<SubsumptionKind, Integer>(kin_compa);
		checksSummary = new TreeMap<Class<? extends Subsumption.SubsumptionKind>, Integer>(cla_compa);
		// Initialisation
		for (SubsumptionKind kind : Subsumption.Equalisation.values()) {
			checks.put(kind, 0);
		}
		checksSummary.put(Equalisation.class, 0);
		for (SubsumptionKind kind : Subsumption.Restriction.values()) {
			checks.put(kind, 0);
		}
		checksSummary.put(Restriction.class, 0);
		for (SubsumptionKind kind : Subsumption.Extension.values()) {
			checks.put(kind, 0);
		}
		checksSummary.put(Extension.class, 0);
		for (SubsumptionKind kind : Subsumption.None.values()) {
			checks.put(kind, 0);
		}
		checksSummary.put(None.class, 0);
	}
	
	public void resetCounters() {
		this.initCounters();
	}

	public Subsumption[] checkSubsumption(Constraint[] constraintsToBeChecked) {
		Subsumption[] subs = new Subsumption[constraintsToBeChecked.length];
		for (int i = 0; i < constraintsToBeChecked.length; i++) {
			subs[i] = this.checkSubsumption(constraintsToBeChecked[i]);
		}
		
		return subs;
	}

	public Subsumption checkSubsumption(Constraint c) {
		Subsumption subsumption = null;
		Constraint modelCon = null;
		for (int i = 0; i < model.length && subsumption == null; i++) {
			modelCon = model[i];

			if (modelCon.equals(c)) {
				subsumption = new Subsumption(modelCon, Equalisation.EQUAL_TO);
			} else if (modelCon.isChildOf(c)) {
				subsumption = new Subsumption(modelCon, Extension.DIRECT_PARENT_OF);
			} else if (c.isChildOf(modelCon)) {
				subsumption = new Subsumption(modelCon, Restriction.DIRECT_CHILD_OF);
			} else if (modelCon.isDescendantAlongSameBranchOf(c)) {
				subsumption = new Subsumption(modelCon, Extension.ANCESTOR_OF);
			} else if (c.isDescendantAlongSameBranchOf(modelCon)) {
				subsumption = new Subsumption(modelCon, Restriction.DESCENDANT_OF);
			} else if (c.getSubFamily().equals(RelationConstraintSubFamily.COUPLING)) {
				MutualRelationConstraint coCon = ((MutualRelationConstraint)c);
				if (coCon.getPossibleForwardConstraint().equals(modelCon)) {
					subsumption = new Subsumption(modelCon, Restriction.INCLUDES_AS_FORWARD);
				}
				if (coCon.getPossibleBackwardConstraint().equals(modelCon)) {
					subsumption = new Subsumption(modelCon, Restriction.INCLUDES_AS_BACKWARD);
				}
			} else if (modelCon.getSubFamily().equals(RelationConstraintSubFamily.COUPLING)) {
				MutualRelationConstraint coCheckCon = ((MutualRelationConstraint)modelCon);
				if (coCheckCon.getPossibleForwardConstraint().equals(c)) {
					subsumption = new Subsumption(modelCon, Extension.IS_FORWARD_OF);
				}
				if (coCheckCon.getPossibleBackwardConstraint().equals(c)) {
					subsumption = new Subsumption(modelCon, Extension.IS_BACKWARD_OF);
				}
			} else if (modelCon.isBranched() || c.isBranched()) {
				if (modelCon instanceof RelationConstraint && c instanceof RelationConstraint) {
					RelationConstraint reModelCon = ((RelationConstraint)modelCon);
					RelationConstraint reC = ((RelationConstraint)c);
					
					if (reModelCon.getBase().equals(reC.getBase())) {
						if (reModelCon.type.equals(reC.type)) {
							if (reModelCon.hasTargetSetStrictlyIncludingTheOneOf(reC)) {
								subsumption = new Subsumption(modelCon, Restriction.SAME_TEMPLATE_SAME_ACTIVATION_TARGET_INCLUDED_IN);
							} else if (reC.hasTargetSetStrictlyIncludingTheOneOf(reModelCon)) {
								subsumption = new Subsumption(modelCon, Extension.SAME_TEMPLATE_SAME_ACTIVATION_TARGET_INCLUDES);
							}
						} else if (reModelCon.isTemplateDescendantAlongSameBranchOf(reC)) {
							if (reC.hasTargetSetStrictlyIncludingTheOneOf(reModelCon)) {
								subsumption = new Subsumption(modelCon, Extension.TEMPLATE_ANCESTOR_OF_SAME_ACTIVATION_TARGET_INCLUDES);
							}
						} else if (reC.isTemplateDescendantAlongSameBranchOf(reModelCon)) {
							if (reModelCon.hasTargetSetStrictlyIncludingTheOneOf(reC)) {
								subsumption = new Subsumption(modelCon, Restriction.TEMPLATE_DESCENDANT_OF_SAME_ACTIVATION_TARGET_INCLUDED_IN);
							}
						}
					}
				}
			}
		}
		this.categoriseSubsumption(subsumption);
		return subsumption;
	}
	
	public void categoriseSubsumption(Subsumption auXub) {
		if (auXub != null) {
			checks.put(auXub.kind, checks.get(auXub.kind) + 1);
			checksSummary.put(auXub.kind.getClass(), checksSummary.get(auXub.kind.getClass()) + 1);
		} else {
			checks.put(Subsumption.None.NONE, checks.get(Subsumption.None.NONE) + 1);
			checksSummary.put(Subsumption.None.class, checksSummary.get(Subsumption.None.class) + 1);
		}
	}
	
	public String csvLegend() {
		StringBuilder legeSBuil = new StringBuilder();
		legeSBuil.append("Code");
		for (Entry<SubsumptionKind, Integer> checkEntry : checks.entrySet()) {
			legeSBuil.append(";");
			legeSBuil.append(checkEntry.getKey());
		}		
		for (Entry<Class<? extends SubsumptionKind>, Integer> checkSumEntry : checksSummary.entrySet()) {
			legeSBuil.append(";");
			legeSBuil.append(checkSumEntry.getKey().getName().substring(checkSumEntry.getKey().getName().lastIndexOf('$') + 1));
		}
		
		return legeSBuil.toString();
	}
	
	public String csvContent() {
		StringBuilder
			dataSBuil = new StringBuilder();
		dataSBuil.append("SUB");
		
		for (Entry<SubsumptionKind, Integer> checkEntry : checks.entrySet()) {
			dataSBuil.append(";");
			dataSBuil.append(checkEntry.getValue());
		}
		for (Entry<Class<? extends SubsumptionKind>, Integer> checkSumEntry : checksSummary.entrySet()) {
			dataSBuil.append(";");
			dataSBuil.append(checkSumEntry.getValue());
		}
		
		return dataSBuil.toString();
	}
	
	public String csv() {
		return csvLegend() + "\n" + csvContent();
	}

	public NavigableMap<SubsumptionKind, Integer> getChecks() {
		return checks;
	}
	public Map<Class<? extends Subsumption.SubsumptionKind>, Integer> getChecksSummary() {
		return checksSummary;
	}
	public Constraint[] getModel() {
		return model;
	}

	public Subsumption[] checkSubsumption(Collection<Constraint> allConstraints) {
		return this.checkSubsumption(allConstraints.toArray(new Constraint[allConstraints.size()]));
	}
}