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
import minerful.concept.TaskCharFactory;
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
	public static final String TEMPLATE_TEMP_FILE_EXTENSION = ".xml";
	public static final String TEMPLATE_TMP_FILE_BASENAME = "template";
	public static final String DECLARE_XML_TEMPLATE = "resources/" + TEMPLATE_TMP_FILE_BASENAME + TEMPLATE_TEMP_FILE_EXTENSION;

	private List<DeclareMapConstraintTransferObject> constraintTOs;
	private TaskCharArchive taskCharArchive = null;
	private String processModelName = null;
	
	public DeclareMapEncoderDecoder(ProcessModel process) {
		this.constraintTOs = new ArrayList<DeclareMapConstraintTransferObject>(process.bag.howManyConstraints());
		this.taskCharArchive = process.getTaskCharArchive();
		this.processModelName = process.getName();

		Collection<Constraint> auxConstraints = null;
		DeclareMapConstraintTransferObject auxDeclareConstraintTO = null;
		for (TaskChar tChar: process.bag.getTaskChars()) {
			auxConstraints = process.bag.getConstraintsOf(tChar);
			for (Constraint auxCon : auxConstraints) {
				if (!auxCon.isMarkedForExclusion()) {
					auxDeclareConstraintTO = new DeclareMapConstraintTransferObject(auxCon);
					if (auxDeclareConstraintTO.template != null) {
						this.constraintTOs.add(auxDeclareConstraintTO);
					}
				}
			}
		}
	}
	
	public DeclareMapEncoderDecoder(String declareMapFilePath) {
		AssignmentModel declareMapModel = DeclareMapReaderWriter.readFromFile(declareMapFilePath);
		this.buildFromDeclareMapModel(declareMapModel);
	}

	public DeclareMapEncoderDecoder(AssignmentModel declareMapModel) {
		this.buildFromDeclareMapModel(declareMapModel);
	}
	
	private void buildFromDeclareMapModel(AssignmentModel declareMapModel) {
		/* Record the name of the process */
		this.processModelName = declareMapModel.getName();

		/* Create an archive of TaskChars out of the activity definitions in the Declare Map model */
		Collection<TaskChar> tasksInDeclareMap = new ArrayList<TaskChar>(declareMapModel.activityDefinitionsCount());
		TaskCharEncoderDecoder tChEncDec = new TaskCharEncoderDecoder();
		TaskCharFactory tChFactory = new TaskCharFactory(tChEncDec);

		for (ActivityDefinition ad : declareMapModel.getActivityDefinitions()) {
			tasksInDeclareMap.add(tChFactory.makeTaskChar(new StringTaskClass(ad.getName())));
		}

		this.taskCharArchive = new TaskCharArchive(tasksInDeclareMap);
		
		/* Create DTOs for constraints out of the definitions in the Declare Map model */
		this.constraintTOs = new ArrayList<DeclareMapConstraintTransferObject>(declareMapModel.constraintDefinitionsCount());
		for (ConstraintDefinition cd : declareMapModel.getConstraintDefinitions()) {
			this.constraintTOs.add(new DeclareMapConstraintTransferObject(cd));
		}		
	}
	
	public ProcessModel createMinerFulProcessModel() {
		Collection<Constraint> minerFulConstraints = new ArrayList<Constraint>(this.constraintTOs.size());
		MinerFulConstraintMaker miFuConMak = new MinerFulConstraintMaker(this.taskCharArchive);
		
		for (DeclareMapConstraintTransferObject conTO: constraintTOs) {
			minerFulConstraints.add(miFuConMak.createConstraintOutOfTransferObject(conTO));
		}

		MetaConstraintUtils.createHierarchicalLinks(new TreeSet<Constraint>(minerFulConstraints));
		
		ConstraintsBag constraintsBag = new ConstraintsBag(this.taskCharArchive.getTaskChars(), minerFulConstraints);

		return new ProcessModel(taskCharArchive, constraintsBag, this.processModelName);
	}

	public List<DeclareMapConstraintTransferObject> getConstraintTOs() {
		return constraintTOs;
	}

	public DeclareMap createDeclareMap() {
		Vector<String> activityDefinitions = new Vector<String>();
		Map<String, DeclareMapTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareMapTemplate>();
		DeclareMapTemplate[] declareTemplates = DeclareMapTemplate.values();
		for (DeclareMapTemplate d : declareTemplates) {
			String templateNameString = d.getName();
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
		model.setName(this.processModelName);
		ActivityDefinition activitydefinition = null;
		int constraintID = 0;
		int activityID = 1;
		for (DeclareMapConstraintTransferObject constraint : constraintTOs) {
			for (Set<String> parameterSet : constraint.parameters) {
				for (String aName : parameterSet) {
					if(!activityDefinitions.contains(aName)) {
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
			for (Parameter parameter : parameters) {
				for (String branchName : paramsIterator.next()) {
					ActivityDefinition activityDefinition = model.activityDefinitionWithName(branchName);
					constraintdefinition.addBranch(parameter, activityDefinition);
				}
			}
			model.addConstraintDefiniton(constraintdefinition);
		}

		AssignmentModelView view = new AssignmentModelView(model);
		DeclareMap map = new DeclareMap(model, null, view, null, null, null);
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
		if(map.getModel().getConstraintDefinitions().size()<200) {
			oc.setEdgeLengthCostFactor(99);
		}
		oc.setNodeDistributionCostFactor(999999999);
		oc.setBorderLineCostFactor(999);
		oc.setRadiusScaleFactor(0.9);
		final JGraphFacade jgf = new JGraphFacade(view.getGraph());
		oc.run(jgf);
		final Map<?, ?> nestedMap = jgf.createNestedMap(true, true); 
		view.getGraph().getGraphLayoutCache().edit(nestedMap); 
		
		return map;
	}

	public static Map<DeclareMapTemplate, ConstraintTemplate> readConstraintTemplates(Map<String, DeclareMapTemplate> templateNameStringDeclareTemplateMap) {
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
		for (IItem item : templateList) {
			if(item instanceof ConstraintTemplate) {
				ConstraintTemplate constraintTemplate = (ConstraintTemplate)item;
				if(templateNameStringDeclareTemplateMap.containsKey(constraintTemplate.getName())) {
					declareTemplateConstraintTemplateMap.put(templateNameStringDeclareTemplateMap.get(constraintTemplate.getName()), constraintTemplate);
				} else {
				}
			}
		}

		return declareTemplateConstraintTemplateMap;
	}

	private static List<IItem> visit(IItem item) {
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