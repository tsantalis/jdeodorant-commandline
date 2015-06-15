/*******************************************************************************
 * Copyright (c) 2009 Software Composition and Modeling Laboratory (Softcom Lab)
 *                    Department of Computer and Information Sciences
 *                    University of Alabama at Birmingham
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Tairas - initial version
 *******************************************************************************/

package org.eclipse.contribution.cedar.elements.clones;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.contribution.cedar.elements.clones.CloneGroupGroup;
import org.eclipse.contribution.cedar.elements.clones.IGroup;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author TAIRAS
 * 
 */
public class CeDARPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.contribution.cedar";

	// The shared instance
	private static CeDARPlugin plugin;
	
	public static final int STMT_LINE_TOTAL = 3;
	
	public static final int CLONE_GROUP = 1;
	public static final int CLONE = 2;

	public static final int CCFINDER = 1;
	public static final int CLONEDR = 2;
	public static final int DECKARD = 3;
	public static final int SIMIAN = 4;
	public static final int SIMSCAN = 5;
	
	public static final int NO_FILTER = 0;
	public static final int SETFOCUS = 1;
	public static final int EM_CAND = 2;

	public static IProject project;

	public ArrayList<IGroup> cloneGroups = new ArrayList<IGroup>();
	public ArrayList<CloneGroupGroup> cloneGroupGroups = new ArrayList<CloneGroupGroup>();
	
	public int numClones = 1;

	
	public int filtered = NO_FILTER;
	public String filterFile;

	public static boolean NEW_WAY = true;

	/**
	 * The constructor
	 */
	public CeDARPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CeDARPlugin getDefault() {
		return plugin;
	}
	
	public void resetFiltering() {
		filtered = NO_FILTER;
		filterFile = "";
	}
	
	public static void log(Throwable throwable) {
		getDefault().getLog().log(new Status(IStatus.ERROR, "org.eclipse.contribution.cedar", 0, throwable.getMessage(), throwable));
	}

}