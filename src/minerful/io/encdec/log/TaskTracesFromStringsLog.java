/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.io.encdec.log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class TaskTracesFromStringsLog {
    
    private static Logger logger;
    protected File stringsLogFile;
    private Set<Character> alphabet;

    public TaskTracesFromStringsLog(File stringsLogFile) throws Exception {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getCanonicalName());
        }
        
        if (!stringsLogFile.canRead()) {
        	throw new IllegalArgumentException("Unparsable log file: " + stringsLogFile.getAbsolutePath());
        }
        
        this.stringsLogFile = stringsLogFile;
        alphabet = new TreeSet<Character>();
    }
    
    public String[] extractTraces() throws Exception {
        List<String> traces = new ArrayList<String>();
        FileInputStream fstream;
        fstream = new FileInputStream(this.stringsLogFile);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine = br.readLine();
        
        while (strLine != null) {
        	strLine = strLine.trim();
            traces.add(strLine);
            for (char c : strLine.toCharArray()) {
            	alphabet.add(c);
            }
            strLine = br.readLine();
        }
        in.close();
        
        return traces.toArray(new String[traces.size()]);
    }

	public Character[] getAlphabet() {
		return alphabet.toArray(new Character[alphabet.size()]);
	}
}
