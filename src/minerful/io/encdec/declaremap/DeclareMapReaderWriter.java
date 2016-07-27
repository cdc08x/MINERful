package minerful.io.encdec.declaremap;

import java.io.File;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

public class DeclareMapReaderWriter {
	public static AssignmentModel readFromFile(String declareMapFilePath) {
		File inputFile = new File(declareMapFilePath);
		if (!inputFile.canRead() || !inputFile.isFile()) {
			throw new IllegalArgumentException("Unreadable file: " + declareMapFilePath);
		}
		
		AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(declareMapFilePath);
		AssignmentModel model = broker.readAssignment();
		AssignmentModelView view = new AssignmentModelView(model);
		broker.readAssignmentGraphical(model, view);
		
		return model;
	}


	public static void marshal(String outfilePath, DeclareMap map) {
		AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(outfilePath);
		broker.addAssignmentAndView(map.getModel(), map.getView());
	}
}
