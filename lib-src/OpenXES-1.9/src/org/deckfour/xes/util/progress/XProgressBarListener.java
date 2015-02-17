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

import javax.swing.JProgressBar;

/**
 * This class implements a progress listener for controlling
 * an attached Swing progress bar. The progress bar will then reflect
 * the progress as received by this listener.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 *
 */
public class XProgressBarListener implements XProgressListener {

	/**
	 * Controlled progress bar.
	 */
	protected JProgressBar progressBar;
	/**
	 * Start value to use in progress bar.
	 * When the progress listener begins, this will be
	 * the displayed value on the progress bar.
	 */
	protected int startValue;
	/**
	 * Stop value to use in progress bar.
	 * When the progress listener is completed, this will
	 * be the displayed value on the progress bar.
	 */
	protected int stopValue;
	
	/**
	 * Creates a new progress bar listener with the specified progress
	 * bar for display. The progress bar will accurately and completely
	 * reflect the progress, as received by this listener.
	 * 
	 * @param progressBar Progress bar to display progress.
	 */
	public XProgressBarListener(JProgressBar progressBar) {
		this(progressBar, progressBar.getMinimum(), progressBar.getMaximum());
	}
	
	/**
	 * Creates a new progress bar listener with the specified progress
	 * bar for display. Allows the specification of start and stop values
	 * for the progress bar, so that this listener may control only part
	 * of a more higher-level progress (e.g., the loading of one from
	 * a number of files).
	 * 
	 * @param progressBar Progress bar to display progress.
	 * @param startValue When the progress listener begins, this will be
	 * the displayed value on the progress bar.
	 * @param stopValue When the progress listener is completed, this will
	 * be the displayed value on the progress bar.
	 */
	public XProgressBarListener(JProgressBar progressBar, int startValue, int stopValue) {
		this.progressBar = progressBar;
		this.startValue = startValue;
		this.stopValue = stopValue;
	}
	
	/* (non-Javadoc)
	 * @see org.deckfour.xes.util.MonitoredInputStream.ProgressListener#updateProgress(int, int)
	 */
	public void updateProgress(int progress, int maxProgress) {
		int increment = (int)((double)(stopValue - startValue) * ((double)maxProgress / (double)progress));
		progressBar.setValue(startValue + increment);
	}

	/**
	 * Checks whether the monitored process has been canceled.
	 */
	public boolean isAborted() {
		return false;
	}

}
