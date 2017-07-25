package minerful.io.encdec.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Map;

import minerful.concept.constraint.Constraint;
import minerful.io.encdec.pojo.ConstraintPojo;
import minerful.io.encdec.pojo.ProcessModelPojo;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class JsonPojoEncoderDecoder {
	private Gson gson;
	
	public JsonPojoEncoderDecoder() {
		this.gson = new Gson();
	}
	
	public ConstraintPojo fromJsonToConstraintPojo(String json) {
		return this.gson.fromJson(json, ConstraintPojo.class);
	}
	public ConstraintPojo fromJsonToConstraintPojo(File jsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		return this.gson.fromJson(new FileReader(jsonFile), ConstraintPojo.class);
	}
	public String fromConstraintPojoToJson(ConstraintPojo pojo) {
		return this.gson.toJson(pojo);
	}
	public void saveConstraintPojo(ConstraintPojo pojo, File jsonFile) throws JsonIOException, FileNotFoundException {
		PrintWriter priWri = new PrintWriter(jsonFile);
		this.gson.toJson(pojo, priWri);
		priWri.flush();
		priWri.close();
	}

	public ProcessModelPojo fromJsonToProcessModelPojo(String json) {
		return this.gson.fromJson(json, ProcessModelPojo.class);
	}
	public ProcessModelPojo fromJsonToProcessModelPojo(File jsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		return this.gson.fromJson(new FileReader(jsonFile), ProcessModelPojo.class);
	}
	public String fromProcessModelPojoToJson(ProcessModelPojo pojo) {
		return this.gson.toJson(pojo);
	}
	public void saveProcessModelPojo(ProcessModelPojo pojo, File jsonFile) throws JsonIOException, FileNotFoundException {
		PrintWriter priWri = new PrintWriter(jsonFile);
		this.gson.toJson(pojo, priWri);
		priWri.flush();
		priWri.close();
	}

}