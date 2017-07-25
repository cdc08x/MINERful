package trashbin.minerful.io.encdec.declaremap;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import minerful.concept.ProcessModel;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.StringTaskClass;

import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

public class OldDeclareMapEncoderDecoderMethods {
	public static final String IF_EXTRACTION_REG_EXP = ".*IF;([0-9\\.]+).*";
	public static final String CONFIDENCE_EXTRACTION_REG_EXP = ".*confidence;([0-9\\.]+).*";
	public static final String SUPPORT_EXTRACTION_REG_EXP = ".*support;([0-9\\.]+).*";

	@Deprecated
	public static ProcessModel fromDeclareMapToMinerfulProcessModel(String declareMapFilePath) {
		return fromDeclareMapToMinerfulProcessModel(declareMapFilePath, null);
	}

	@Deprecated
	public static ProcessModel fromDeclareMapToMinerfulProcessModel(AssignmentModel declareMapModel) {
		return fromDeclareMapToMinerfulProcessModel(declareMapModel, null);
	}
	
	@Deprecated
	private static ProcessModel fromDeclareMapToMinerfulProcessModel(String declareMapFilePath, TaskCharArchive taskCharArchive) {
		File inputFile = new File(declareMapFilePath);
		if (!inputFile.canRead() || !inputFile.isFile()) {
			throw new IllegalArgumentException("Unreadable file: " + declareMapFilePath);
		}
		
		AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(declareMapFilePath);
		AssignmentModel model = broker.readAssignment();
		AssignmentModelView view = new AssignmentModelView(model);
		broker.readAssignmentGraphical(model, view);
		return fromDeclareMapToMinerfulProcessModel(model, taskCharArchive);
	}

	@Deprecated
	private static ProcessModel fromDeclareMapToMinerfulProcessModel(AssignmentModel declareMapModel, TaskCharArchive taskCharArchive) {
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<Constraint> minerFulConstraints = new ArrayList<Constraint>();

		if (taskCharArchive == null) {
			TaskCharEncoderDecoder encdec = new TaskCharEncoderDecoder();

			for (ConstraintDefinition cd : declareMapModel.getConstraintDefinitions()) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						encdec.encode(new StringTaskClass(ad.getName()));
					}
				}
			}
			for (ActivityDefinition ad : declareMapModel.getActivityDefinitions()) {
				encdec.encode(new StringTaskClass(ad.getName()));
			}
			taskCharArchive = new TaskCharArchive(encdec.getTranslationMap());
		}

		for (ConstraintDefinition cd : declareMapModel.getConstraintDefinitions()) {
			String template = cd.getName().replace("-", "").replace(" ", "").toLowerCase();
			params = new ArrayList<String>();

			Pattern
				supPattern = Pattern.compile(OldDeclareMapEncoderDecoderMethods.SUPPORT_EXTRACTION_REG_EXP),
				confiPattern = Pattern.compile(OldDeclareMapEncoderDecoderMethods.CONFIDENCE_EXTRACTION_REG_EXP),
				inteFaPattern = Pattern.compile(OldDeclareMapEncoderDecoderMethods.IF_EXTRACTION_REG_EXP);
			Matcher
				supMatcher = supPattern.matcher(cd.getText().trim()),
				confiMatcher = confiPattern.matcher(cd.getText().trim()),
				inteFaMatcher = inteFaPattern.matcher(cd.getText().trim());

			Double
				support = (supMatcher.matches() && supMatcher.groupCount() > 0 ? Double.valueOf(supMatcher.group(1)) : Constraint.DEFAULT_SUPPORT),
				confidence = (confiMatcher.matches() && confiMatcher.groupCount() > 0 ? Double.valueOf(confiMatcher.group(1)) : Constraint.DEFAULT_CONFIDENCE),
				interestFact = (inteFaMatcher.matches() && inteFaMatcher.groupCount() > 0 ? Double.valueOf(inteFaMatcher.group(1)): Constraint.DEFAULT_INTEREST_FACTOR);

			if (template.equals("alternateprecedence")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				AlternatePrecedence minerConstr = new AlternatePrecedence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("alternateresponse")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				AlternateResponse minerConstr = new AlternateResponse(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);

				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("alternatesuccession")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				AlternateSuccession minerConstr = new AlternateSuccession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("chainprecedence")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				ChainPrecedence minerConstr = new ChainPrecedence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("chainresponse")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				ChainResponse minerConstr = new ChainResponse(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("chainsuccession")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				ChainSuccession minerConstr = new ChainSuccession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("coexistence")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				CoExistence minerConstr = new CoExistence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("notchainsuccession")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				NotChainSuccession minerConstr = new NotChainSuccession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("notcoexistence")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				NotCoExistence minerConstr = new NotCoExistence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("notsuccession")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				NotSuccession minerConstr = new NotSuccession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("precedence")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				Precedence minerConstr = new Precedence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("response")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				Response minerConstr = new Response(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("succession")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				Succession minerConstr = new Succession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("respondedexistence")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				RespondedExistence minerConstr = new RespondedExistence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("init")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				Init minerConstr = new Init(taskCharArchive.getTaskChar(params.get(0)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("existence")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				Participation minerConstr = new Participation(taskCharArchive.getTaskChar(params.get(0)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			} else if (template.equals("absence2")) {
				for (Parameter p : cd.getParameters()) {
					for (ActivityDefinition ad : cd.getBranches(p)) {
						params.add(ad.getName());
					}
				}
				AtMostOne minerConstr = new AtMostOne(taskCharArchive.getTaskChar(params.get(0)),support);
				minerConstr.setConfidence(confidence);
				minerConstr.setInterestFactor(interestFact);
				minerFulConstraints.add(minerConstr);
			}
		}
		MetaConstraintUtils.createHierarchicalLinks(new TreeSet<Constraint>(minerFulConstraints));
		
		ConstraintsBag constraintsBag = new ConstraintsBag(taskCharArchive.getTaskChars(), minerFulConstraints);
		String processModelName = declareMapModel.getName();
		
		return new ProcessModel(taskCharArchive, constraintsBag, processModelName);
	}
}