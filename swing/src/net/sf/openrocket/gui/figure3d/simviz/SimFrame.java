package net.sf.openrocket.gui.figure3d.simviz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.main.SwingExceptionHandler;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.logging.LoggingSystemSetup;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.GuiModule;
import net.sf.openrocket.unit.UnitGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class SimFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(SimFrame.class);

	private SimPanel simPanel;

	public SimFrame() {
		setSize(1024, 768);
		this.setMinimumSize(new Dimension(160, 150));
		simPanel = new SimPanel();

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(simPanel, BorderLayout.CENTER);

		DoubleModel maxTime = new DoubleModel(simPanel.settings, "MaxTime", UnitGroup.UNITS_TIME_STEP);
		DoubleModel time = new DoubleModel(simPanel.settings, "Time", UnitGroup.UNITS_TIME_STEP);

		JSlider timeSlider = new BasicSlider(time.getSliderModel(new DoubleModel(0), maxTime));
		JSpinner timeSpinner = new JSpinner(time.getSpinnerModel());
		
		JPanel bottom = new JPanel();
		bottom.add(timeSpinner);
		bottom.add(timeSlider);
		
		p.add(bottom, BorderLayout.SOUTH);

		setContentPane(p);

		GUIUtil.rememberWindowSize(this);
		this.setLocationByPlatform(true);
		GUIUtil.rememberWindowPosition(this);
		GUIUtil.setWindowIcons(this);

	}

	public static void main(String args[]) throws Exception {

		LoggingSystemSetup.setupLoggingAppender();
		LoggingSystemSetup.addConsoleAppender();

		// Setup the uncaught exception handler
		log.info("Registering exception handler");
		SwingExceptionHandler exceptionHandler = new SwingExceptionHandler();
		Application.setExceptionHandler(exceptionHandler);
		exceptionHandler.registerExceptionHandler();

		// Load motors etc.
		log.info("Loading databases");

		GuiModule guiModule = new GuiModule();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		Application.setInjector(injector);

		guiModule.startLoader();

		// Set the best available look-and-feel
		log.info("Setting best LAF");
		GUIUtil.setBestLAF();

		// Load defaults
		((SwingPreferences) Application.getPreferences()).loadDefaultUnits();

		Databases.fakeMethod();

		SimFrame fs = new SimFrame();
		fs.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fs.setTitle("OpenRocket - Sim Viz");
		fs.setVisible(true);

		// Load a model
		GeneralRocketLoader grl = new GeneralRocketLoader(new File(
				"/Users/bkuker/git/openrocket/swing/resources/datafiles/examples/Three-stage rocket.ork"));
		final OpenRocketDocument doc = grl.load();
		Simulation s = doc.getSimulation(0);
		s.simulate();
		fs.simPanel.addSimulation(s);

	}
}
