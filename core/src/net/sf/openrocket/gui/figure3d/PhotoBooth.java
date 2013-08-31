package net.sf.openrocket.gui.figure3d;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;

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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.gui.figure3d.geometry.FlameRenderer;
import net.sf.openrocket.gui.figure3d.sky.SkyBox;
import net.sf.openrocket.gui.main.Splash;
import net.sf.openrocket.gui.main.SwingExceptionHandler;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.GuiModule;
import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

public class PhotoBooth extends JPanel implements GLEventListener {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(PhotoBooth.class);
	
	static {
		//this allows the GL canvas and things like the motor selection
		//drop down to z-order themselves.
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	private final int SHORTCUT_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	private final Translator trans = Application.getTranslator();
	private Configuration configuration;
	private GLCanvas canvas;
	private TextureCache textureCache = new TextureCache();
	private double ratio;
	private boolean doCopy = false;
	
	public static class Photo extends AbstractChangeSource {
		private double roll = 3.14;
		private double yaw = 0;
		private double pitch = 2.05;
		private double advance = 0;
		
		private double viewAlt = -0.23;
		private double viewAz = 2.08;
		private double viewDistance = .44;
		private double fov = 1.4;
		
		private double lightAlt = .35;
		private double lightAz = -1;
		private Color sunlight = new Color(255, 255, 255);
		private double ambiance = .3f;
		
		private boolean skyEnabled = true;
		private Color skyColor = new Color(55, 95, 155);
		
		
		private boolean motionBlurred = false;
		private boolean flame = false;
		private Color flameColor = new Color(255, 100, 50);
		private boolean smoke = true;
		private Color smokeColor = new Color(230, 230, 230, 204);
		private boolean sparks = false;
		private double exhaustScale = 1.0;
		
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
		
		public double getAdvance() {
			return advance;
		}
		
		public void setAdvance(double advance) {
			this.advance = advance;
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
		
		public boolean isMotionBlurred() {
			return motionBlurred;
		}
		
		public void setMotionBlurred(boolean motionBlurred) {
			this.motionBlurred = motionBlurred;
			fireChangeEvent();
		}
		
		public boolean isFlame() {
			return flame;
		}
		
		public void setFlame(boolean flame) {
			this.flame = flame;
			fireChangeEvent();
		}
		
		public boolean isSmoke() {
			return smoke;
		}
		
		public void setSmoke(boolean smoke) {
			this.smoke = smoke;
			fireChangeEvent();
		}
		
		public Color getSunlight() {
			return sunlight;
		}
		
		public void setSunlight(Color sunlight) {
			this.sunlight = sunlight;
			fireChangeEvent();
		}
		
		public double getAmbiance() {
			return ambiance;
		}
		
		public void setAmbiance(double ambiance) {
			this.ambiance = ambiance;
			fireChangeEvent();
		}
		
		public boolean isSkyEnabled() {
			return skyEnabled;
		}
		
		public void setSkyEnabled(boolean skyEnabled) {
			this.skyEnabled = skyEnabled;
			fireChangeEvent();
		}
		
		public Color getSkyColor() {
			return skyColor;
		}
		
		public void setSkyColor(Color skyColor) {
			this.skyColor = skyColor;
			fireChangeEvent();
		}
		
		public Color getFlameColor() {
			return flameColor;
		}
		
		public void setFlameColor(Color flameColor) {
			this.flameColor = flameColor;
			fireChangeEvent();
		}
		
		public Color getSmokeColor() {
			return smokeColor;
		}
		
		public void setSmokeColor(Color smokeColor) {
			smokeColor.setAlpha(this.smokeColor.getAlpha());
			this.smokeColor = smokeColor;
			fireChangeEvent();
		}
		
		public double getSmokeAlpha() {
			return smokeColor.getAlpha() / 255f;
		}
		
		public void setSmokeAlpha(double alpha) {
			smokeColor.setAlpha((int) (alpha * 255));
			fireChangeEvent();
		}
		
		public boolean isSparks() {
			return sparks;
		}
		
		public void setSparks(boolean sparks) {
			this.sparks = sparks;
			fireChangeEvent();
		}
		
		public double getExhaustScale() {
			return exhaustScale;
		}
		
		public void setExhaustScale(double exhaustScale) {
			this.exhaustScale = exhaustScale;
			fireChangeEvent();
		}
	}
	
	RocketRenderer rr;
	Photo p;
	
	public void setDoc(final OpenRocketDocument doc) {
		canvas.invoke(true, new GLRunnable() {
			@Override
			public boolean run(GLAutoDrawable drawable) {
				PhotoBooth.this.configuration = doc.getDefaultConfiguration();
				cachedBounds = null;
				rr = new RealisticRenderer(doc);
				rr.init(drawable);
				return false;
			}
		});
	}
	
	public PhotoBooth() {
		this.setLayout(new BorderLayout());
		
		p = new Photo();
		
		//Fixes a linux / X bug: Splash must be closed before GL Init
		SplashScreen splash = Splash.getSplashScreen();
		if (splash != null && splash.isVisible())
			splash.close();
		
		initGLCanvas();
		
		p.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				PhotoBooth.this.repaint();
			}
		});
		
		this.add(new PhotoConfigPanel(p), BorderLayout.EAST);
	}
	
	private void initGLCanvas() {
		try {
			log.debug("Setting up GL capabilities...");
			
			log.trace("GL - Getting Default Profile");
			final GLProfile glp = GLProfile.get(GLProfile.GL2);
			
			log.trace("GL - creating GLCapabilities");
			final GLCapabilities caps = new GLCapabilities(glp);
			
			log.trace("GL - setSampleBuffers");
			caps.setSampleBuffers(true);
			
			log.trace("GL - setNumSamples");
			caps.setNumSamples(6);
			
			log.trace("GL - Creating Canvas");
			canvas = new GLCanvas(caps);
			
			log.trace("GL - Registering as GLEventListener on canvas");
			canvas.addGLEventListener(this);
			
			log.trace("GL - Adding canvas to this JPanel");
			this.add(canvas, BorderLayout.CENTER);
			
			log.trace("GL - Setting up mouse listeners");
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
		
		d(drawable, 0);
		
		if (p.isMotionBlurred()) {
			Bounds b = calculateBounds();
			
			float m = .6f;
			int c = 10;
			float d = (float) b.xSize / 25.0f;
			
			gl.glAccum(GL2.GL_LOAD, m);
			
			for (int i = 1; i <= c; i++) {
				d(drawable, d / c * i);
				gl.glAccum(GL2.GL_ACCUM, (1.0f - m) / c);
			}
			
			gl.glAccum(GL2.GL_RETURN, 1.0f);
		}
		
		
		if (doCopy) {
			copy(drawable);
			doCopy = false;
		}
		
	}
	
	protected static void convertColor(Color color, float[] out) {
		if (color == null) {
			out[0] = 1;
			out[1] = 1;
			out[2] = 0;
		} else {
			out[0] = (float) color.getRed() / 255f;
			out[1] = (float) color.getGreen() / 255f;
			out[2] = (float) color.getBlue() / 255f;
		}
	}
	
	public void d(final GLAutoDrawable drawable, float dx) {
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = new GLU();
		
		float[] color = new float[3];
		
		gl.glEnable(GL.GL_MULTISAMPLE);
		
		
		
		convertColor(p.getSunlight(), color);
		float amb = (float) p.getAmbiance();
		float dif = 1.0f - amb;
		float spc = 1.0f;
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_AMBIENT, new float[] { amb * color[0], amb * color[1], amb * color[2], 1 }, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_DIFFUSE, new float[] { dif * color[0], dif * color[1], dif * color[2], 1 }, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_SPECULAR, new float[] { spc * color[0], spc * color[1], spc * color[2], 1 }, 0);
		
		
		convertColor(p.getSkyColor(), color);
		gl.glClearColor(color[0], color[1], color[2], 1);
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
		if (p.isSkyEnabled()) {
			SkyBox.draw(gl, textureCache);
		}
		//SkySphere.draw(gl, textureCache);
		gl.glDepthMask(true);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		gl.glPopMatrix();
		
		if (rr == null)
			return;
		
		
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
		
		gl.glTranslated(dx - p.getAdvance(), 0, 0);
		
		
		if (p.isFlame()) {
			convertColor(p.getFlameColor(), color);
			
			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_AMBIENT, new float[] { 0, 0, 0, 1 }, 0);
			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_DIFFUSE, new float[] { color[0], color[1], color[2], 1 }, 0);
			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_SPECULAR, new float[] { color[0], color[1], color[2], 1 }, 0);
			
			Bounds b = calculateBounds();
			gl.glLightf(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_QUADRATIC_ATTENUATION, 20f);
			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_POSITION, new float[] { (float) (b.xMax + .1f), 0, 0, 1 }, 0);
			gl.glEnable(GLLightingFunc.GL_LIGHT2);
		} else {
			gl.glDisable(GLLightingFunc.GL_LIGHT2);
		}
		
		
		rr.render(drawable, configuration, new HashSet<RocketComponent>());
		//glu.gluSphere(new GLUquadricImpl(gl, false), .2, 10, 10);
		
		String motorID = configuration.getFlightConfigurationID();
		Iterator<MotorMount> iterator = configuration.motorIterator();
		while (iterator.hasNext()) {
			MotorMount mount = iterator.next();
			Motor motor = mount.getMotorConfiguration().get(motorID).getMotor();
			double length = motor.getLength();
			
			Coordinate[] position = ((RocketComponent) mount).toAbsolute(new Coordinate(((RocketComponent) mount)
					.getLength() + mount.getMotorOverhang() - length));
			
			for (int i = 0; i < position.length; i++) {
				gl.glPushMatrix();
				gl.glTranslated(position[i].x + motor.getLength(), position[i].y, position[i].z);
				FlameRenderer.f(gl, p.isFlame(), p.isSmoke(), p.isSparks(), p.getSmokeColor(), p.getFlameColor(), motor, (float) p.getExhaustScale());
				gl.glPopMatrix();
			}
		}
		
	}
	
	@Override
	public void dispose(final GLAutoDrawable drawable) {
		log.trace("GL - dispose() called");
		if (rr != null)
			rr.dispose(drawable);
		textureCache.dispose(drawable);
	}
	
	@Override
	public void init(final GLAutoDrawable drawable) {
		log.trace("GL - init()");
		//drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
		
		final GL2 gl = drawable.getGL().getGL2();
		
		gl.glClearDepth(1.0f); // clear z-buffer to the farthest
		gl.glDepthFunc(GL.GL_LESS); // the type of depth test to do
		
		
		textureCache.init(drawable);
		
		//gl.glDisable(GLLightingFunc.GL_LIGHT1);
		
		FlameRenderer.init(gl);
		
	}
	
	@Override
	public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int w, final int h) {
		log.trace("GL - reshape()");
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
	
	private void copy(final GLAutoDrawable drawable) {
		
		final BufferedImage image = (new AWTGLReadBufferUtil(GLProfile.get(GLProfile.GL2), false))
				.readPixelsToBufferedImage(drawable.getGL(), 0, 0, drawable.getWidth(), drawable.getHeight(), true);
		
		
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
			@Override
			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				if (flavor.equals(DataFlavor.imageFlavor) && image != null) {
					return image;
				}
				else {
					throw new UnsupportedFlavorException(flavor);
				}
			}
			
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				DataFlavor[] flavors = new DataFlavor[1];
				flavors[0] = DataFlavor.imageFlavor;
				return flavors;
			}
			
			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				DataFlavor[] flavors = getTransferDataFlavors();
				for (int i = 0; i < flavors.length; i++) {
					if (flavor.equals(flavors[i])) {
						return true;
					}
				}
				
				return false;
			}
		}, null);
	}
	
	JMenuBar getMenu() {
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		JMenuItem item;
		
		////  File
		menu = new JMenu(trans.get("main.menu.file"));
		menu.setMnemonic(KeyEvent.VK_F);
		//// File-handling related tasks
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.desc"));
		menubar.add(menu);
		
		item = new JMenuItem(trans.get("main.menu.file.open"), KeyEvent.VK_O);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_KEY));
		//// Open a rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.Openrocketdesign"));
		item.setIcon(Icons.FILE_OPEN);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Open... selected");
				
				final FileDialog fd = new FileDialog((JFrame) SwingUtilities.getWindowAncestor(PhotoBooth.this), "Open...", FileDialog.LOAD);
				fd.setVisible(true);
				if (fd.getFile() != null) {
					File file = new File(fd.getDirectory() + fd.getFile());
					log.debug("Opening File " + file.getAbsolutePath());
					GeneralRocketLoader grl = new GeneralRocketLoader(file);
					try {
						OpenRocketDocument doc = grl.load();
						setDoc(doc);
					} catch (RocketLoadException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		menu.add(item);
		
		////  Edit
		menu = new JMenu(trans.get("main.menu.edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		//// Rocket editing
		menu.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.menu.Rocketedt"));
		menubar.add(menu);
		
		
		Action action = new AbstractAction("Copy") {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCopy = true;
				repaint();
			}
		};
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, SHORTCUT_KEY));
		item.setMnemonic(KeyEvent.VK_C);
		item.getAccessibleContext().setAccessibleDescription("Copy image to clipboard");
		menu.add(item);
		
		return menubar;
	}
	
	public static void main(String args[]) throws Exception {
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
		
		JFrame ff = new JFrame();
		ff.setSize(1024, 768);
		ff.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		PhotoBooth pb = new PhotoBooth();
		ff.setJMenuBar(pb.getMenu());
		ff.setContentPane(pb);
		ff.setVisible(true);
		
		
		if (true) {
			Thread.sleep(1);
			//String f = "C:\\Users\\bkuker\\git\\openrocket\\core\\resources\\datafiles\\examples\\Simulation Listeners.ork";
			//String f = "C:\\Users\\bkuker\\git\\openrocket\\core\\resources\\datafiles\\examples\\High Power Airstart.ork";
			String f = "C:\\Users\\bkuker\\git\\openrocket\\core\\resources\\datafiles\\examples\\A simple model rocket.ork";
			//String f = "C:\\Users\\bkuker\\git\\openrocket\\core\\resources\\datafiles\\examples\\Clustered rocket design.ork";
			//String f = "C:\\Users\\bkuker\\git\\openrocket\\core\\resources\\datafiles\\examples\\Boosted Dart.ork";
			GeneralRocketLoader grl = new GeneralRocketLoader(new File(f));
			OpenRocketDocument doc = grl.load();
			pb.setDoc(doc);
		}
	}
}
