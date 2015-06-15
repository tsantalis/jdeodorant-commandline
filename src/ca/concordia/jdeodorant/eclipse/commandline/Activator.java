package ca.concordia.jdeodorant.eclipse.commandline;

import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "ca.concordia.jdeodorant.eclipse.commandline";

    // The shared instance
    private static Activator plugin;

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * The constructor
     */
    public Activator() {
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    
    public static String getPluginPath() {
    	try {
			return FileLocator.getBundleFile(Activator.getDefault().getBundle()).toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
}
