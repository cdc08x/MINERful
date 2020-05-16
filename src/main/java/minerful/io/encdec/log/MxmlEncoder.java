package minerful.io.encdec.log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.deckfour.xes.out.XMxmlSerializer;

public class MxmlEncoder extends XesEncoder {

	public MxmlEncoder(String[] traces) {
		super(traces);
	}

	@Override
	public File encodeToFile(File outFile) throws IOException {
		OutputStream outStream = new FileOutputStream(outFile);
		new XMxmlSerializer().serialize(this.xLog, outStream);
		return outFile;
	}

	@Override
	public String encodeToString() throws IOException {
		OutputStream outStream = new ByteArrayOutputStream();
		new XMxmlSerializer().serialize(this.xLog, outStream);
		return outStream.toString();
	}
}