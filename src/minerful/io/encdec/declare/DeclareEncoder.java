package minerful.io.encdec.declare;

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
import java.util.Vector;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

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

public class DeclareEncoder {
	public static final String TEMPLATE_TEMP_FILE_EXTENSION = ".xml";
	public static final String TEMPLATE_TMP_FILE_BASENAME = "template";
	public static final String DECLARE_XML_TEMPLATE = "resources/template.xml";

	private List<DeclareConstraintTransferObject> constraintTOs;
	private DeclareMap map;
	
	public DeclareEncoder(ProcessModel process) {
		this.constraintTOs = new ArrayList<DeclareConstraintTransferObject>(process.bag.howManyConstraints());
		Collection<Constraint> auxConstraints = null;
		DeclareConstraintTransferObject auxDeclareConstraintTO = null;
		for (TaskChar tChar: process.bag.getTaskChars()) {
			auxConstraints = process.bag.getConstraintsOf(tChar);
			for (Constraint auxCon : auxConstraints) {
				auxDeclareConstraintTO = new DeclareConstraintTransferObject(auxCon);
				if (auxDeclareConstraintTO.template != null) {
					this.constraintTOs.add(auxDeclareConstraintTO);
				}
			}
		}
		createModel();
	}

	public List<DeclareConstraintTransferObject> getConstraintTOs() {
		return constraintTOs;
	}



	public void createModel(){
		Vector<String> activityDefinitions = new Vector<String>();
		Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();
		DeclareTemplate[] declareTemplates = DeclareTemplate.values();
		for(DeclareTemplate d : declareTemplates){
			String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}
		Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = readConstraintTemplates(templateNameStringDeclareTemplateMap);

		InputStream ir = ClassLoader.getSystemClassLoader().getResourceAsStream(DeclareEncoder.DECLARE_XML_TEMPLATE);
		File language = null;
		try {
			language = File.createTempFile(DeclareEncoder.TEMPLATE_TMP_FILE_BASENAME, DeclareEncoder.TEMPLATE_TEMP_FILE_EXTENSION);
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
		for(DeclareConstraintTransferObject constraint : constraintTOs){
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
		AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(outfilePath);
		broker.addAssignmentAndView(map.getModel(),map.getView());
	}


	public static Map<DeclareTemplate, ConstraintTemplate> readConstraintTemplates(Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap){
		InputStream templateInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(DeclareEncoder.DECLARE_XML_TEMPLATE);
		File languageFile = null;
		try {
			languageFile = File.createTempFile(DeclareEncoder.TEMPLATE_TMP_FILE_BASENAME, DeclareEncoder.TEMPLATE_TEMP_FILE_EXTENSION);
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

		Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = new HashMap<DeclareTemplate, ConstraintTemplate>();

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