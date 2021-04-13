package minerful.io.encdec.declaremap;

import minerful.utils.MessagePrinter;

import org.apache.commons.lang3.text.WordUtils;

public enum DeclareMapTemplate {
	Absence, Absence2, Absence3, Alternate_Precedence, Alternate_Response, Alternate_Succession, 
	Chain_Precedence, Chain_Response, Chain_Succession, Choice, CoExistence, 
	Exactly1, Exactly2, Exclusive_Choice, Existence, Existence2, Existence3, 
	Init, 
	Not_Chain_Succession, Not_CoExistence, Not_Succession,
	Precedence, Response, Responded_Existence,
	Succession,
	Not_Chain_Precedence, Not_Chain_Response,
	Not_Precedence, Not_Response,
	Not_Responded_Existence;
	
	public String getName() {
		switch(this) {
		case CoExistence:
		case Not_CoExistence:
			return this.toString().replaceAll("_", " ").toLowerCase().replace("coexi", "co-exi");
		default:
			return this.toString().replaceAll("_", " ").toLowerCase();
		}
	}
	
	public static DeclareMapTemplate fromName(String name) {
		name = WordUtils.capitalizeFully(name).replaceAll(" ", "_");
		if (name.contains("Co-exi")) {
			name = name.replace("Co-exi", "CoExi");
		}
		
		DeclareMapTemplate mapTemplate = null;
		try {
			mapTemplate = DeclareMapTemplate.valueOf(name);
		} catch (IllegalArgumentException e) {
			MessagePrinter.printlnError("The " + name + " template is not yet defined in the MINERful import library.");
		}
		
		return mapTemplate;
	}
}