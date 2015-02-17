/*
 * OpenXES
 * 
 * The reference implementation of the XES meta-model for event 
 * log data management.
 * 
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
 * 
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * The use of this software can also be conditionally licensed for
 * other programs, which do not satisfy the specified conditions. This
 * requires an exemption from the general license, which may be
 * granted on a per-case basis.
 * 
 * If you want to license the use of this software with a program
 * incompatible with the LGPL, please contact the author for an
 * exemption at the following email address: 
 * christian@deckfour.org
 * 
 */
package org.deckfour.xes.util.progress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JProgressBar;

/**
 * This class implements an input stream which can provide
 * a progress listener with feedback about how much of the
 * data in the stream has already been read.
 * 
 * This is a useful utility for reading XML-based data,
 * while still providing feedback about expected progress.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 *
 */
public class XMonitoredInputStream extends InputStream {
	
	/**
	 * The number of steps to be used for progress expression.
	 */
	protected int stepNumber = 1000;
	/**
	 * Number of bytes per step.
	 */
	protected long stepSize;
	/**
	 * The last step which has been notified about.
	 */
	protected int lastStep;
	/**
	 * Number of bytes read so far.
	 */
	protected long bytesRead = 0;
	/**
	 * Wrapped, monitored input stream.
	 */
	protected InputStream stream;
	/**
	 * Progress listener which is being notified.
	 */
	protected XProgressListener progressListener;
	
	/**
	 * Creates a new monitored input stream.
	 * 
	 * @param file The file to be read from.
	 * @param progressListener Progress listener to be notified.
	 */
	public XMonitoredInputStream(File file, XProgressListener progressListener) throws FileNotFoundException {
		this(new BufferedInputStream(new FileInputStream(file)), file.length(), progressListener);
	}
	
	/**
	 * Creates a new monitored input stream.
	 * 
	 * @param stream Monitored, wrapped lower-level input stream.
	 * @param size Number of bytes to be read from the stream.
	 * @param progressBar Progress bar to be updated.
	 */
	public XMonitoredInputStream(InputStream stream, long size, JProgressBar progressBar) {
		this(stream, size, new XProgressBarListener(progressBar), 1000);
	}
	
	/**
	 * Creates a new monitored input stream.
	 * 
	 * @param stream Monitored, wrapped lower-level input stream.
	 * @param size Number of bytes to be read from the stream.
	 * @param progressListener Progress listener to be notified.
	 */
	public XMonitoredInputStream(InputStream stream, long size, XProgressListener progressListener) {
		this(stream, size, progressListener, 1000);
	}
	
	/**
	 * Creates a new monitored input stream.
	 * 
	 * @param stream Monitored, wrapped lower-level input stream.
	 * @param size Number of bytes to be read from the stream.
	 * @param progressListener Progress listener to be notified.
	 * @param stepNumber Number of steps used to express progress.
	 */
	public XMonitoredInputStream(InputStream stream, long size, XProgressListener progressListener, int stepNumber) {
		this.progressListener = progressListener;
		this.stream = stream;
		this.stepNumber = stepNumber;
		// avoid potential divide by 0 when stepsize == 0
		this.stepSize = Math.max(size / stepNumber, 1);
		this.lastStep = 0;
		this.bytesRead = 0;
	}
	
	/**
	 * This method is called by the actual input stream method
	 * to provide feedback about the number of read bytes.
	 * 
	 * Notifies the attached progress listener if appropriate.
	 * 
	 * @param readBytes The number of read bytes in this call.
	 */
	protected void update(long readBytes) throws IOException {
		if (progressListener.isAborted()) {
			throw new IOException("Reading Cancelled by ProgressListener");
		}
		this.bytesRead += readBytes;
		int step = (int)(bytesRead / stepSize);
		if(step > lastStep) {
			lastStep = step;
			progressListener.updateProgress(step, stepNumber);
		}
	}
	
	/**
	 * Returns the number of steps so far.
	 * 
	 * @return Number of steps.
	 */
	public int getStepNumber() {
		return stepNumber;
	}
	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		int result = stream.read();
		update(1);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = stream.read(b, off, len);
		update(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		int result = stream.read(b);
		update(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		long result = stream.skip(n);
		update(result);
		return result;
	}
	
	

}
