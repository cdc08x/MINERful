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
import java.util.regex.Pattern;

import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
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

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.io.encdec.DeclareConstraintTransferObject;
import minerful.io.encdec.TransferObjectToConstraintTranslator;
import minerful.utils.ResourceReader;

public class DeclareMapEncoderDecoder {
	private List<DeclareConstraintTransferObject> constraintTOs;
//	private List<DeclareConstraintTransferObject> unmappedConstraintTOs;
	private TaskCharArchive taskCharArchive = null;
	private String processModelName = null;

	public static final String
		INTEREST_FACTOR_LABEL = "IF",
		CONFIDENCE_LABEL = "confidence",
		SUPPORT_LABEL = "support",
		LABEL_VALUE_SEPARATOR = ";";
	public static final String
		INTEREST_FACTOR_EXTRACTION_REG_EXP =
			".*" + INTEREST_FACTOR_LABEL + LABEL_VALUE_SEPARATOR + "([0-9\\.]+).*",
		CONFIDENCE_EXTRACTION_REG_EXP =
			".*" + CONFIDENCE_LABEL + LABEL_VALUE_SEPARATOR + "([0-9\\.]+).*",
		SUPPORT_EXTRACTION_REG_EXP =
			".*" + SUPPORT_LABEL + LABEL_VALUE_SEPARATOR + "([0-9\\.]+).*";
	public static final Pattern
		SUPPORT_PATTERN = Pattern.compile(SUPPORT_EXTRACTION_REG_EXP),
		CONFIDENCE_PATTERN = Pattern.compile(CONFIDENCE_EXTRACTION_REG_EXP),
		INTEREST_FACTOR_PATTERN = Pattern.compile(INTEREST_FACTOR_EXTRACTION_REG_EXP);
	public static final String
		SUPPORT_CONFIDENCE_IF_FORMAT_PATTERN =
				SUPPORT_LABEL + LABEL_VALUE_SEPARATOR + "%f" + LABEL_VALUE_SEPARATOR
			+	CONFIDENCE_LABEL + LABEL_VALUE_SEPARATOR + "%f" + LABEL_VALUE_SEPARATOR
			+	INTEREST_FACTOR_LABEL + LABEL_VALUE_SEPARATOR + "%f";

	public static final String TEMPLATE_TEMP_FILE_EXTENSION = ".xml";
	public static final String TEMPLATE_TMP_FILE_BASENAME = "ConDecTemplate";
	public static final String DECLARE_XML_TEMPLATE_LIBRARY_URL = "minerful/io/encdec/declaremap/";
	public static final String DECLARE_XML_TEMPLATE = TEMPLATE_TMP_FILE_BASENAME + TEMPLATE_TEMP_FILE_EXTENSION;

	public DeclareMapEncoderDecoder(ProcessModel process) {
		this.constraintTOs = new ArrayList<DeclareConstraintTransferObject>(process.bag.howManyConstraints());
//		this.unmappedConstraintTOs = new ArrayList<DeclareConstraintTransferObject>();
		this.taskCharArchive = process.getTaskCharArchive();
		this.processModelName = process.getName();

		Collection<Constraint> auxConstraints = null;
		DeclareConstraintTransferObject auxDeclareConstraintTO = null;
		for (TaskChar tChar: process.bag.getTaskChars()) {
			auxConstraints = process.bag.getConstraintsOf(tChar);
			for (Constraint auxCon : auxConstraints) {
				// Only the mininmal content is saved: redundant, conflicting, or below-the-thresholds constraints are not included in the output.
				if (!auxCon.isMarkedForExclusion()) {
					auxDeclareConstraintTO = new DeclareConstraintTransferObject(auxCon);
					if (auxDeclareConstraintTO.declareMapTemplate != null) {
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
		TaskCharFactory tChFactory = new TaskCharFactory();

		for (ActivityDefinition ad : declareMapModel.getActivityDefinitions()) {
			tasksInDeclareMap.add(tChFactory.makeTaskChar(ad.getName()));
		}

		this.taskCharArchive = new TaskCharArchive(tasksInDeclareMap);
		
		/* Create DTOs for constraints out of the definitions in the Declare Map model */
		this.constraintTOs = new ArrayList<DeclareConstraintTransferObject>(declareMapModel.constraintDefinitionsCount());
		for (ConstraintDefinition cd : declareMapModel.getConstraintDefinitions()) {
			this.constraintTOs.add(new DeclareConstraintTransferObject(cd));
		}		
	}
	
	public ProcessModel createMinerFulProcessModel() {
		Collection<Constraint> minerFulConstraints = new ArrayList<Constraint>(this.constraintTOs.size());
		TransferObjectToConstraintTranslator miFuConMak = new TransferObjectToConstraintTranslator(this.taskCharArchive);
		Constraint tmpCon = null;
		
		for (DeclareConstraintTransferObject conTO: constraintTOs) {
			tmpCon = miFuConMak.createConstraint(conTO);
			if (tmpCon != null) {
				minerFulConstraints.add(tmpCon);
			}
		}

		MetaConstraintUtils.createHierarchicalLinks(new TreeSet<Constraint>(minerFulConstraints));
		ConstraintsBag constraintsBag = new ConstraintsBag(this.taskCharArchive.getTaskChars(), minerFulConstraints);

		return new ProcessModel(taskCharArchive, constraintsBag, this.processModelName);
	}

	public List<DeclareConstraintTransferObject> getConstraintTOs() {
		return constraintTOs;
	}

	public DeclareMap createDeclareMap() {
//		return this.createDeclareMap(true);
//	}
//	public DeclareMap createDeclareMap(boolean addUnmappedDeclareMapConstraints) {
		Map<String, DeclareMapTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareMapTemplate>();
		DeclareMapTemplate[] declareTemplates = DeclareMapTemplate.values();
		for (DeclareMapTemplate d : declareTemplates) {
			String templateNameString = d.getName();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}
		Map<DeclareMapTemplate, ConstraintTemplate> declareTemplateDefinitionsMap = readConstraintTemplates(templateNameStringDeclareTemplateMap);
		InputStream ir = loadConDecXmlTemplate();

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
		ConstraintDefinition constraintDefinition = null;
		int constraintID = 0;

		/* Save activity definitions */
		for (TaskChar tCh : this.taskCharArchive.getTaskChars()) {
			activitydefinition = model.addActivityDefinition(tCh.identifier);
			activitydefinition.setName(tCh.getName());
		}

		for (DeclareConstraintTransferObject constraintTo : constraintTOs) {
			constraintID++;
			constraintDefinition = createConstraintDefinition(
				declareTemplateDefinitionsMap, model, constraintID, constraintTo);
			model.addConstraintDefiniton(constraintDefinition);
		}
//		if (addUnmappedDeclareMapConstraints) {
//			for (DeclareConstraintTransferObject constraintTo : unmappedConstraintTOs) {
//				
//			}
//		}

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

	private ConstraintDefinition createConstraintDefinition(
			Map<DeclareMapTemplate, ConstraintTemplate> declareTemplateDefinitionsMap,
			AssignmentModel model, int constraintID,
			DeclareConstraintTransferObject constraintTo) {
		/* Load constraint definition */
		ConstraintDefinition constraintDefinition = new ConstraintDefinition(constraintID, model, declareTemplateDefinitionsMap.get(constraintTo.declareMapTemplate));
		Collection<Parameter> parameters = (declareTemplateDefinitionsMap.get(constraintTo.declareMapTemplate)).getParameters();
		Iterator<Set<String>> paramsIterator = constraintTo.parameters.iterator();
		/* Fill in parameters */
		for (Parameter parameter : parameters) {
			for (String branchName : paramsIterator.next()) {
				ActivityDefinition activityDefinition = model.activityDefinitionWithName(branchName);
				constraintDefinition.addBranch(parameter, activityDefinition);
			}
		}
		/* Specify the support, confidence and interest factor within the text */
		constraintDefinition.setText(
				constraintDefinition.getText() +
				LABEL_VALUE_SEPARATOR +
				String.format(SUPPORT_CONFIDENCE_IF_FORMAT_PATTERN,
						constraintTo.support,
						constraintTo.confidence,
						constraintTo.interestFactor)
		);
		return constraintDefinition;
	}

	public static Map<DeclareMapTemplate, ConstraintTemplate> readConstraintTemplates(Map<String, DeclareMapTemplate> templateNameStringDeclareTemplateMap) {
		InputStream templateInputStream = //ClassLoader.getSystemClassLoader().getResourceAsStream(DeclareMapEncoderDecoder.DECLARE_XML_TEMPLATE);
				loadConDecXmlTemplate();
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

		// The first language in the list is the ConDec language, which is what we need
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

	private static InputStream loadConDecXmlTemplate() {
		return ResourceReader.loadResource(
				DeclareMapEncoderDecoder.DECLARE_XML_TEMPLATE_LIBRARY_URL +
				DeclareMapEncoderDecoder.DECLARE_XML_TEMPLATE);
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