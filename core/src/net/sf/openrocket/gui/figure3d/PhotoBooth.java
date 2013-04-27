package net.sf.openrocket.gui.figure3d;

import java.awt.BorderLayout;
import java.awt.SplashScreen;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLRunnable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;

import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.gui.figure3d.geometry.FlameRenderer;
import net.sf.openrocket.gui.figure3d.sky.SkyBox;
import net.sf.openrocket.gui.main.Splash;
import net.sf.openrocket.gui.util.BlockingMotorDatabaseProvider;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.ApplicationModule;
import net.sf.openrocket.startup.ApplicationModule2;
import net.sf.openrocket.startup.MotorDatabaseLoader;
import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class PhotoBooth extends JPanel implements GLEventListener {
	private static final long serialVersionUID = 1L;
	private static final LogHelper log = Application.getLogger();
	
	static {
		//this allows the GL canvas and things like the motor selection
		//drop down to z-order themselves.
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	
	private final Configuration configuration;
	private GLCanvas canvas;
	private TextureCache textureCache = new TextureCache();
	private double ratio;
	
	public static class Photo extends AbstractChangeSource {
		private double roll = 3.14;
		private double yaw = 0;
		private double pitch = 2.05;
		private double viewAlt = -0.23;
		private double viewAz = 2.08;
		private double viewDistance = .44;
		private double fov = 1.4;
		private double lightAlt = .35;
		private double lightAz = -1;
		
		public double getRoll() {
			return roll;
		}
		
		public void setRoll(double roll) {
			this.roll = roll;
			fireChangeEvent();
		}
		
		public double getYaw() {
			return yaw;
		}
		
		public void setYaw(double yaw) {
			this.yaw = yaw;
			fireChangeEvent();
		}
		
		public double getPitch() {
			return pitch;
		}
		
		public void setPitch(double pitch) {
			this.pitch = pitch;
			fireChangeEvent();
		}
		
		public double getViewAlt() {
			return viewAlt;
		}
		
		public void setViewAlt(double viewAlt) {
			this.viewAlt = viewAlt;
			fireChangeEvent();
		}
		
		public double getViewAz() {
			return viewAz;
		}
		
		public void setViewAz(double viewAz) {
			this.viewAz = viewAz;
			fireChangeEvent();
		}
		
		public double getViewDistance() {
			return viewDistance;
		}
		
		public void setViewDistance(double viewDistance) {
			this.viewDistance = viewDistance;
			fireChangeEvent();
		}
		
		public double getFov() {
			return fov;
		}
		
		public void setFov(double fov) {
			this.fov = fov;
			fireChangeEvent();
		}
		
		public double getLightAlt() {
			return lightAlt;
		}
		
		public void setLightAlt(double lightAlt) {
			this.lightAlt = lightAlt;
			fireChangeEvent();
		}
		
		public double getLightAz() {
			return lightAz;
		}
		
		public void setLightAz(double lightAz) {
			this.lightAz = lightAz;
			fireChangeEvent();
		}
	}
	
	RocketRenderer rr;
	Photo p;
	
	
	public PhotoBooth(final OpenRocketDocument doc, final Configuration config) {
		this.configuration = config;
		this.setLayout(new BorderLayout());
		rr = new RealisticRenderer(doc);
		
		p = new Photo();
		
		//Only initizlize GL if 3d is enabled.
		if (is3dEnabled()) {
			//Fixes a linux / X bug: Splash must be closed before GL Init
			SplashScreen splash = Splash.getSplashScreen();
			if (splash != null && splash.isVisible())
				splash.close();
			
			initGLCanvas();
		}
		
		p.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				PhotoBooth.this.repaint();
			}
		});
		
		this.add(new PhotoConfigPanel(p), BorderLayout.EAST);
		
	}
	
	/**
	 * Return true if 3d view is enabled. This may be toggled by the user at
	 * launch time.
	 * @return
	 */
	public static boolean is3dEnabled() {
		return System.getProperty("openrocket.3d.disable") == null;
	}
	
	private void initGLCanvas() {
		log.debug("Initializing RocketFigure3D OpenGL Canvas");
		try {
			log.debug("Setting up GL capabilities...");
			
			log.verbose("GL - Getting Default Profile");
			final GLProfile glp = GLProfile.get(GLProfile.GL2);
			
			log.verbose("GL - creating GLCapabilities");
			final GLCapabilities caps = new GLCapabilities(glp);
			
			log.verbose("GL - setSampleBuffers");
			caps.setSampleBuffers(true);
			
			log.verbose("GL - setNumSamples");
			caps.setNumSamples(6);
			
			log.verbose("GL - Creating Canvas");
			canvas = new GLCanvas(caps);
			
			log.verbose("GL - Registering as GLEventListener on canvas");
			canvas.addGLEventListener(this);
			
			log.verbose("GL - Adding canvas to this JPanel");
			this.add(canvas, BorderLayout.CENTER);
			
			log.verbose("GL - Setting up mouse listeners");
			setupMouseListeners();
			
			
		} catch (Throwable t) {
			log.error("An error occurred creating 3d View", t);
			canvas = null;
			this.add(new JLabel("Unable to load 3d Libraries: "
					+ t.getMessage()));
		}
	}
	
	private void setupMouseListeners() {
		MouseInputAdapter a = new MouseInputAdapter() {
			
			@Override
			public void mousePressed(final MouseEvent e) {
				
			}
			
			@Override
			public void mouseClicked(final MouseEvent e) {
				
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				
			}
		};
		canvas.addMouseMotionListener(a);
		canvas.addMouseListener(a);
	}
	
	
	@Override
	public void display(final GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = new GLU();
		
		gl.glEnable(GL.GL_MULTISAMPLE);
		
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(p.getFov() * (180.0 / Math.PI), ratio, 0.1f, 50f);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
		//Flip textures for LEFT handed coords
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glScaled(-1, 1, 1);
		gl.glTranslated(-1, 0, 0);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
		gl.glLoadIdentity();
		
		//Draw the sky
		gl.glPushMatrix();
		gl.glDisable(GLLightingFunc.GL_LIGHTING);
		gl.glDepthMask(false);
		gl.glRotated(p.getViewAlt() * (180.0 / Math.PI), 1, 0, 0);
		gl.glRotated(p.getViewAz() * (180.0 / Math.PI), 0, 1, 0);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		SkyBox.draw(gl, textureCache);
		//SkySphere.draw(gl, textureCache);
		gl.glDepthMask(true);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		gl.glPopMatrix();
		
		
		
		glu.gluLookAt(0, 0, p.getViewDistance(), 0, 0, 0, 0, 1, 0);
		gl.glRotated(p.getViewAlt() * (180.0 / Math.PI), 1, 0, 0);
		gl.glRotated(p.getViewAz() * (180.0 / Math.PI), 0, 1, 0);
		
		float[] lightPosition = new float[] {
				(float) Math.cos(p.getLightAlt()) * (float) Math.sin(p.getLightAz()),//
				(float) Math.sin(p.getLightAlt()),//
				(float) Math.cos(p.getLightAlt()) * (float) Math.cos(p.getLightAz()) //
		};
		
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_POSITION,
				lightPosition, 0);
		
		//Change to LEFT Handed coordinates
		gl.glScaled(1, 1, -1);
		gl.glFrontFace(GL.GL_CW);
		setupModel(gl);
		
		rr.render(drawable, configuration, new HashSet<RocketComponent>());
		FlameRenderer.f(gl);
		
	}
	
	@Override
	public void dispose(final GLAutoDrawable drawable) {
		log.verbose("GL - dispose() called");
		rr.dispose(drawable);
		textureCache.dispose(drawable);
	}
	
	@Override
	public void init(final GLAutoDrawable drawable) {
		log.verbose("GL - init()");
		
		final GL2 gl = drawable.getGL().getGL2();
		gl.glClearDepth(1.0f); // clear z-buffer to the farthest
		
		gl.glDepthFunc(GL.GL_LESS); // the type of depth test to do
		
		float amb = 0.5f;
		float dif = 1.0f;
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_AMBIENT,
				new float[] { amb, amb, amb, 1 }, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_DIFFUSE,
				new float[] { dif, dif, dif, 1 }, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_SPECULAR,
				new float[] { dif, dif, dif, 1 }, 0);
		
		gl.glEnable(GLLightingFunc.GL_LIGHT1);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		
		gl.glEnable(GLLightingFunc.GL_NORMALIZE);
		
		rr.init(drawable);
		textureCache.init(drawable);
		
	}
	
	@Override
	public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int w, final int h) {
		log.verbose("GL - reshape()");
		ratio = (double) w / (double) h;
	}
	
	@SuppressWarnings("unused")
	private static class Bounds {
		double xMin, xMax, xSize;
		double yMin, yMax, ySize;
		double zMin, zMax, zSize;
		double rMax;
	}
	
	private Bounds cachedBounds = null;
	
	/**
	 * Calculates the bounds for the current configuration
	 * 
	 * @return
	 */
	private Bounds calculateBounds() {
		if (cachedBounds != null) {
			return cachedBounds;
		} else {
			final Bounds b = new Bounds();
			final Collection<Coordinate> bounds = configuration.getBounds();
			for (Coordinate c : bounds) {
				b.xMax = Math.max(b.xMax, c.x);
				b.xMin = Math.min(b.xMin, c.x);
				
				b.yMax = Math.max(b.yMax, c.y);
				b.yMin = Math.min(b.yMin, c.y);
				
				b.zMax = Math.max(b.zMax, c.z);
				b.zMin = Math.min(b.zMin, c.z);
				
				double r = MathUtil.hypot(c.y, c.z);
				b.rMax = Math.max(b.rMax, r);
			}
			b.xSize = b.xMax - b.xMin;
			b.ySize = b.yMax - b.yMin;
			b.zSize = b.zMax - b.zMin;
			cachedBounds = b;
			return b;
		}
	}
	
	
	
	private void setupModel(final GL2 gl) {
		// Get the bounds
		final Bounds b = calculateBounds();
		gl.glRotated(-p.getPitch() * (180.0 / Math.PI), 0, 0, 1);
		gl.glRotated(p.getYaw() * (180.0 / Math.PI), 0, 1, 0);
		gl.glRotated(p.getRoll() * (180.0 / Math.PI), 1, 0, 0);
		// Center the rocket in the view.
		gl.glTranslated(-b.xMin - b.xSize / 2.0, 0, 0);
	}
	
	/**
	 * Call when the rocket has changed
	 */
	public void updateFigure() {
		log.debug("3D Figure Updated");
		cachedBounds = null;
		canvas.invoke(true, new GLRunnable() {
			@Override
			public boolean run(GLAutoDrawable drawable) {
				rr.updateFigure(drawable);
				return false;
			}
		});
	}
	
	private void internalRepaint() {
		super.repaint();
		if (canvas != null)
			canvas.display();
	}
	
	@Override
	public void repaint() {
		internalRepaint();
	}
	
	public static void main(String args[]) throws Exception {
		GUIUtil.setBestLAF();
		Application.setBaseTranslator(new ResourceBundleTranslator(
				"l10n.messages"));
		Application.setPreferences(new SwingPreferences());
		Module applicationModule = new ApplicationModule();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(applicationModule, pluginModule);
		Application.setInjector(injector);
		MotorDatabaseLoader bg = injector.getInstance(MotorDatabaseLoader.class);
		bg.startLoading();
		BlockingMotorDatabaseProvider db = new BlockingMotorDatabaseProvider(bg);
		ApplicationModule2 module = new ApplicationModule2(db);
		Injector injector2 = injector.createChildInjector(module);
		Application.setInjector(injector2);
		ComponentPresetDatabase componentPresetDao = new ComponentPresetDatabase() {
			@Override
			protected void load() {
			}
		};
		Application.setComponentPresetDao(componentPresetDao);
		
		GeneralRocketLoader grl = new GeneralRocketLoader(new File("C:\\Users\\bkuker\\git\\openrocket\\core\\resources\\datafiles\\examples\\A simple model rocket.ork"));
		OpenRocketDocument doc = grl.load();
		
		JFrame ff = new JFrame();
		ff.setSize(1024, 768);
		ff.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		PhotoBooth pb = new PhotoBooth(doc, doc.getDefaultConfiguration());
		ff.setContentPane(pb);
		ff.setVisible(true);
		
		while (true) {
			Thread.sleep(30);
			//pb.p.setViewAz(pb.p.getViewAz() + 0.01);
		}
	}
}
