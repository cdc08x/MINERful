package minerful.io.encdec.declaremap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
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
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.IItem;
import org.processmining.plugins.declareminer.visualizing.Language;
import org.processmining.plugins.declareminer.visualizing.LanguageGroup;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.TemplateBroker;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.organic.JGraphOrganicLayout;

public class DeclareMapEncoderDecoder {
	public static final String IF_EXTRACTION_REG_EXP = ".*IF;([0-9\\.]+).*";
	public static final String CONFIDENCE_EXTRACTION_REG_EXP = ".*confidence;([0-9\\.]+).*";
	public static final String SUPPORT_EXTRACTION_REG_EXP = ".*support;([0-9\\.]+).*";
	public static final String TEMPLATE_TEMP_FILE_EXTENSION = ".xml";
	public static final String TEMPLATE_TMP_FILE_BASENAME = "template";
	public static final String DECLARE_XML_TEMPLATE = "resources/template.xml";

	private List<DeclareMapConstraintTransferObject> constraintTOs;
	private DeclareMap map;

	public DeclareMapEncoderDecoder(ProcessModel process) {
		this.constraintTOs = new ArrayList<DeclareMapConstraintTransferObject>(process.bag.howManyConstraints());
		Collection<Constraint> auxConstraints = null;
		DeclareMapConstraintTransferObject auxDeclareConstraintTO = null;
		for (TaskChar tChar: process.bag.getTaskChars()) {
			auxConstraints = process.bag.getConstraintsOf(tChar);
			for (Constraint auxCon : auxConstraints) {
				auxDeclareConstraintTO = new DeclareMapConstraintTransferObject(auxCon);
				if (auxDeclareConstraintTO.template != null) {
					this.constraintTOs.add(auxDeclareConstraintTO);
				}
			}
		}
		createDeclareMap();
	}

	public static ProcessModel fromDeclareMapToMinerfulProcessModel(String declareMapFilePath) {
		return fromDeclareMapToMinerfulProcessModel(declareMapFilePath, null);
	}

	public static ProcessModel fromDeclareMapToMinerfulProcessModel(AssignmentModel declareMapModel) {
		return fromDeclareMapToMinerfulProcessModel(declareMapModel, null);
	}
	
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

	private static ProcessModel fromDeclareMapToMinerfulProcessModel(AssignmentModel declareMapModel, TaskCharArchive taskCharArchive) {
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<Constraint> minerFulConstraints = new ArrayList<Constraint>();

		if (taskCharArchive == null) {
			TaskCharEncoderDecoder encdec = new TaskCharEncoderDecoder();

			for(ConstraintDefinition cd : declareMapModel.getConstraintDefinitions()){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						encdec.encode(new StringTaskClass(ad.getName()));
					}
				}
			}
			for(ActivityDefinition ad : declareMapModel.getActivityDefinitions()){
				encdec.encode(new StringTaskClass(ad.getName()));
			}
			taskCharArchive = new TaskCharArchive(encdec.getTranslationMap());
		}

		for(ConstraintDefinition cd : declareMapModel.getConstraintDefinitions()){
			String template = cd.getName().replace("-", "").replace(" ", "").toLowerCase();
			params = new ArrayList<String>();

			Pattern
				supPattern = Pattern.compile(DeclareMapEncoderDecoder.SUPPORT_EXTRACTION_REG_EXP),
				confiPattern = Pattern.compile(DeclareMapEncoderDecoder.CONFIDENCE_EXTRACTION_REG_EXP),
				inteFaPattern = Pattern.compile(DeclareMapEncoderDecoder.IF_EXTRACTION_REG_EXP);
			Matcher
				supMatcher = supPattern.matcher(cd.getText().trim()),
				confiMatcher = confiPattern.matcher(cd.getText().trim()),
				inteFaMatcher = inteFaPattern.matcher(cd.getText().trim());

			Double
				support = (supMatcher.matches() && supMatcher.groupCount() > 0 ? Double.valueOf(supMatcher.group(1)) : Constraint.DEFAULT_SUPPORT),
				confidence = (confiMatcher.matches() && confiMatcher.groupCount() > 0 ? Double.valueOf(confiMatcher.group(1)) : Constraint.DEFAULT_CONFIDENCE),
				interestFact = (inteFaMatcher.matches() && inteFaMatcher.groupCount() > 0 ? Double.valueOf(inteFaMatcher.group(1)): Constraint.DEFAULT_INTEREST_FACTOR);

//			Double support = new Double (cd.getText().split("|")[0].split(";")[1]);
//			Double confidence = new Double (cd.getText().split("|")[1].split(";")[1]);
//			Double interestFact = new Double (cd.getText().split("|")[2].split(";")[1]);
			if(template.equals("alternateprecedence")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				AlternatePrecedence minerConstr = new AlternatePrecedence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("alternateresponse")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				AlternateResponse minerConstr = new AlternateResponse(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);

				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("alternatesuccession")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				AlternateSuccession minerConstr = new AlternateSuccession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("chainprecedence")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				ChainPrecedence minerConstr = new ChainPrecedence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("chainresponse")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				ChainResponse minerConstr = new ChainResponse(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("chainsuccession")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				ChainSuccession minerConstr = new ChainSuccession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("coexistence")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				CoExistence minerConstr = new CoExistence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("notchainsuccession")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				NotChainSuccession minerConstr = new NotChainSuccession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("notcoexistence")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				NotCoExistence minerConstr = new NotCoExistence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("notsuccession")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				NotSuccession minerConstr = new NotSuccession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("precedence")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				Precedence minerConstr = new Precedence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("response")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				Response minerConstr = new Response(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("succession")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				Succession minerConstr = new Succession(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("respondedexistence")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				RespondedExistence minerConstr = new RespondedExistence(taskCharArchive.getTaskChar(params.get(0)),taskCharArchive.getTaskChar(params.get(1)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("init")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				Init minerConstr = new Init(taskCharArchive.getTaskChar(params.get(0)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("existence")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				Participation minerConstr = new Participation(taskCharArchive.getTaskChar(params.get(0)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}else if(template.equals("absence2")){
				for(Parameter p : cd.getParameters()){
					for(ActivityDefinition ad : cd.getBranches(p)){
						params.add(ad.getName());
					}
				}
				AtMostOne minerConstr = new AtMostOne(taskCharArchive.getTaskChar(params.get(0)),support);
				minerConstr.confidence = confidence;
				minerConstr.interestFactor = interestFact;
				minerFulConstraints.add(minerConstr);
			}
		}
		MetaConstraintUtils.createHierarchicalLinks(new TreeSet<Constraint>(minerFulConstraints));
		
		ConstraintsBag constraintsBag = new ConstraintsBag(taskCharArchive.getTaskChars(), minerFulConstraints);
		String processModelName = declareMapModel.getName();
		
		return new ProcessModel(taskCharArchive, constraintsBag, processModelName);
	}

	public List<DeclareMapConstraintTransferObject> getConstraintTOs() {
		return constraintTOs;
	}

	public DeclareMap getMap() {
		return map;
	}

	public void createDeclareMap(){
		Vector<String> activityDefinitions = new Vector<String>();
		Map<String, DeclareMapTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareMapTemplate>();
		DeclareMapTemplate[] declareTemplates = DeclareMapTemplate.values();
		for(DeclareMapTemplate d : declareTemplates){
			String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}
		Map<DeclareMapTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = readConstraintTemplates(templateNameStringDeclareTemplateMap);

		InputStream ir = ClassLoader.getSystemClassLoader().getResourceAsStream(DeclareMapEncoderDecoder.DECLARE_XML_TEMPLATE);
		File language = null;
		try {
			language = File.createTempFile(DeclareMapEncoderDecoder.TEMPLATE_TMP_FILE_BASENAME, DeclareMapEncoderDecoder.TEMPLATE_TEMP_FILE_EXTENSION);
			BufferedReader br = new BufferedReader(new InputStreamReader(ir));
			String line = br.readLine();
			PrintStream out = new PrintStream(language);
			while (line != null) {
				out.println(line);
				line = br.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		TemplateBroker template = XMLBrokerFactory.newTemplateBroker(language.getAbsolutePath());
		List<Language> languages = template.readLanguages();
		Language lang = languages.get(0);
		AssignmentModel model = new AssignmentModel(lang);
		model.setName("new model");
		ActivityDefinition activitydefinition = null;
		int constraintID = 0;
		int activityID = 1;
		for(DeclareMapConstraintTransferObject constraint : constraintTOs){
			for(Set<String> parameterSet : constraint.parameters){
				for(String aName : parameterSet){
					if(!activityDefinitions.contains(aName)){
						activityDefinitions.add(aName);
						activitydefinition = model.addActivityDefinition(activityID); //new ActivityDefinition(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j], activityID, model);
						activitydefinition.setName(aName);
						activityID++;
					}
				}
			}
			constraintID++;
			ConstraintDefinition constraintdefinition = new ConstraintDefinition(constraintID, model, declareTemplateConstraintTemplateMap.get(constraint.template));
			Collection<Parameter> parameters = (declareTemplateConstraintTemplateMap.get(constraint.template)).getParameters();
			Iterator<Set<String>> paramsIterator = constraint.parameters.iterator();
			for(Parameter parameter : parameters) {
				for(String branchName : paramsIterator.next()){
					ActivityDefinition activityDefinition = model.activityDefinitionWithName(branchName);
					constraintdefinition.addBranch(parameter, activityDefinition);
				}
			}
			model.addConstraintDefiniton(constraintdefinition);
		}

		
		AssignmentModelView view = new AssignmentModelView(model);
		map = new DeclareMap(model, null, view, null, null, null);
		final JGraphOrganicLayout oc = new JGraphOrganicLayout();

		oc.setDeterministic(true);
		oc.setOptimizeBorderLine(true);
		oc.setOptimizeEdgeCrossing(true);
		oc.setOptimizeEdgeDistance(true);
		oc.setOptimizeEdgeLength(true);
		oc.setOptimizeNodeDistribution(true);
		oc.setEdgeCrossingCostFactor(999999999);
		oc.setEdgeDistanceCostFactor(999999999);
		oc.setFineTuning(true);

		//	oc.setMinDistanceLimit(0.001);
		oc.setEdgeLengthCostFactor(9999);
		if(map.getModel().getConstraintDefinitions().size()<200){
			oc.setEdgeLengthCostFactor(99);
		}
		oc.setNodeDistributionCostFactor(999999999);
		oc.setBorderLineCostFactor(999);
		oc.setRadiusScaleFactor(0.9);
		final JGraphFacade jgf = new JGraphFacade(view.getGraph());
		oc.run(jgf);
		final Map<?, ?> nestedMap = jgf.createNestedMap(true, true); 
		view.getGraph().getGraphLayoutCache().edit(nestedMap); 
	}

	public void marshal(String outfilePath){
		marshal(outfilePath, this.map);
	}

	public static void marshal(String outfilePath, DeclareMap map) {
		AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(outfilePath);
		broker.addAssignmentAndView(map.getModel(), map.getView());
	}

	public static Map<DeclareMapTemplate, ConstraintTemplate> readConstraintTemplates(Map<String, DeclareMapTemplate> templateNameStringDeclareTemplateMap){
		InputStream templateInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(DeclareMapEncoderDecoder.DECLARE_XML_TEMPLATE);
		File languageFile = null;
		try {
			languageFile = File.createTempFile(DeclareMapEncoderDecoder.TEMPLATE_TMP_FILE_BASENAME, DeclareMapEncoderDecoder.TEMPLATE_TEMP_FILE_EXTENSION);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(templateInputStream));
			String line = bufferedReader.readLine();
			PrintStream out = new PrintStream(languageFile);
			while (line != null) {
				out.println(line);
				line = bufferedReader.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		TemplateBroker templateBroker = XMLBrokerFactory.newTemplateBroker(languageFile.getAbsolutePath());
		List<Language> languagesList = templateBroker.readLanguages();

		//the first language in the list is the condec language, which is what we need
		Language condecLanguage = languagesList.get(0);
		List<IItem> templateList = new ArrayList<IItem>();
		List<IItem> condecLanguageChildrenList = condecLanguage.getChildren();
		for (IItem condecLanguageChild : condecLanguageChildrenList) {
			if (condecLanguageChild instanceof LanguageGroup) {
				templateList.addAll(visit(condecLanguageChild));
			} else {
				templateList.add(condecLanguageChild);
			}
		}

		Map<DeclareMapTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = new HashMap<DeclareMapTemplate, ConstraintTemplate>();

		for(IItem item : templateList){
			if(item instanceof ConstraintTemplate){
				ConstraintTemplate constraintTemplate = (ConstraintTemplate)item;
				//				System.out.println(constraintTemplate.getName()+" @ "+constraintTemplate.getDescription()+" @ "+constraintTemplate.getText());
				if(templateNameStringDeclareTemplateMap.containsKey(constraintTemplate.getName().replaceAll("-", "").toLowerCase())){
					declareTemplateConstraintTemplateMap.put(templateNameStringDeclareTemplateMap.get(constraintTemplate.getName().replaceAll("-", "").toLowerCase()), constraintTemplate);
//					System.out.println(constraintTemplate.getName()+" @ "+templateNameStringDeclareTemplateMap.get(constraintTemplate.getName().replaceAll("-", "").toLowerCase()));
				}
			}
		}

		return declareTemplateConstraintTemplateMap;
	}

	private static List<IItem> visit(IItem item){
		List<IItem> templateList = new ArrayList<IItem>();
		if (item instanceof LanguageGroup) {
			LanguageGroup languageGroup = (LanguageGroup) item;
			List<IItem> childrenList = languageGroup.getChildren();
			for (IItem child : childrenList) {
				if (child instanceof LanguageGroup) {
					templateList.addAll(visit(child));
				}else {
					templateList.add(child);
				}
			}
		}
		return templateList;
	}
}