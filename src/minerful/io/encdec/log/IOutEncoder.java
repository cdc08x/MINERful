package minerful.io.encdec.log;

import java.io.File;
import java.io.IOException;

public interface IOutEncoder {

	public abstract void setTraces(String[] traces);

	public abstract File encodeToFile(File outFile) throws IOException;

	public abstract String encodeToString() throws IOException;

}