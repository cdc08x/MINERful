package minerful.io.encdec;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;

public class StringToLowerCaseAlphanumToTemplateTranslator {
	public static Map<String, Class<? extends Constraint>> LOWERCASE_ALPHANUM_CONSTRAINT_TEMPLATE_NAMES =
			new HashMap<String, Class<? extends Constraint>>(MetaConstraintUtils.ALL_CONSTRAINT_TEMPLATE_NAMES_MAP.keySet().size(), (float)1.0);

	public static Class<? extends Constraint> translateTemplateName(String templateName) {
		populateTemplateNamesMapIfEmpty();
		templateName = makeLowercaseOnlyAlphanum(templateName);
		return LOWERCASE_ALPHANUM_CONSTRAINT_TEMPLATE_NAMES.get(templateName);
	}

	public static boolean containsTemplateName(String templateName) {
		populateTemplateNamesMapIfEmpty();
		templateName = makeLowercaseOnlyAlphanum(templateName);
		return LOWERCASE_ALPHANUM_CONSTRAINT_TEMPLATE_NAMES.containsKey(templateName);
	}
	
	public static String makeLowercaseOnlyAlphanum(String templateName) {
		return templateName.replaceAll("[^\\p{Alpha}\\p{Digit}]", "").toLowerCase();
	}

	private static void populateTemplateNamesMapIfEmpty() {
		if (LOWERCASE_ALPHANUM_CONSTRAINT_TEMPLATE_NAMES.isEmpty()) {
			for (Entry<String, Class<? extends Constraint>> entry : MetaConstraintUtils.ALL_CONSTRAINT_TEMPLATE_NAMES_MAP.entrySet()) {
				LOWERCASE_ALPHANUM_CONSTRAINT_TEMPLATE_NAMES.put(makeLowercaseOnlyAlphanum(entry.getKey()), entry.getValue());
			}
		}
	}
}
