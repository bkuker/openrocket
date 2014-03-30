package net.sf.openrocket.gui.figure3d.simviz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.figure3d.RealisticRenderer;
import net.sf.openrocket.gui.figure3d.RocketRenderer;
import net.sf.openrocket.gui.figure3d.TextureCache;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.Mountains;
import net.sf.openrocket.gui.plot.EventGraphics;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimPanel extends JPanel implements GLEventListener {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(SimPanel.class);

	private static Set<FlightEvent.Type> markTypes = new HashSet<FlightEvent.Type>();
	private static Set<FlightEvent.Type> trackColorTypes = new HashSet<FlightEvent.Type>();

	static {
		// These types are not shown
		// markTypes.add(FlightEvent.Type.LAUNCH);
		// markTypes.add(FlightEvent.Type.LIFTOFF);
		// markTypes.add(FlightEvent.Type.LAUNCHROD);
		// markTypes.add(FlightEvent.Type.GROUND_HIT);
		// markTypes.add(FlightEvent.Type.SIMULATION_END);
		// markTypes.add(FlightEvent.Type.ALTITUDE);

		// These types change the color of the line
		trackColorTypes.add(FlightEvent.Type.IGNITION);
		trackColorTypes.add(FlightEvent.Type.BURNOUT);
		trackColorTypes.add(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT);
		trackColorTypes.add(FlightEvent.Type.TUMBLE);

		// These types are shown as little balls
		markTypes.add(FlightEvent.Type.EJECTION_CHARGE);
		markTypes.add(FlightEvent.Type.STAGE_SEPARATION);
		markTypes.add(FlightEvent.Type.APOGEE);
		markTypes.add(FlightEvent.Type.EXCEPTION);
		markTypes.add(FlightEvent.Type.IGNITION);
	}

	static {
		// this allows the GL canvas and things like the motor selection
		// drop down to z-order themselves.
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	static Method color;
	static {
		try {
			color = EventGraphics.class.getDeclaredMethod("getEventColor", FlightEvent.Type.class);
		} catch (Exception e) {
			throw new Error(e);
		}
		color.setAccessible(true);
	}

	static Color getEventColor(FlightEvent.Type t) {
		try {
			return (Color) color.invoke(null, t);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private Component canvas;
	private double ratio;

	private double viewAlt = 45;
	private double viewAz = 0;
	private double viewDist = 100;

	private final Collection<Simulation> simulations = new HashSet<Simulation>();
	Simulation highlighted;

	private final Collection<Coordinate> landing = new HashSet<Coordinate>();
	private final Collection<Coordinate> impact = new HashSet<Coordinate>();

	GL2 gl;
	GLU glu;
	GLUquadric q;

	RocketRenderer rr = new RealisticRenderer();
	TextureCache tc = new TextureCache();

	GroundGrid gg = new GroundGrid();

	public class Settings extends AbstractChangeSource {
		private double time = 0;
		private double maxTime = 10;

		public double getTime() {
			return time;
		}

		public void setTime(double time) {
			this.time = time;
			log.info("Time set to {}", time);
			repaint();
			fireChangeEvent();
		}

		public double getMaxTime() {
			return maxTime;
		}

		public void setMaxTime(double maxTime) {
			this.maxTime = maxTime;
			fireChangeEvent();
		}
	}

	public Settings settings = new Settings();

	public void addSimulation(final Simulation s) {
		simulations.add(s);

		for (int bi = 0; bi < s.getSimulatedData().getBranchCount(); bi++) {
			FlightDataBranch b = s.getSimulatedData().getBranch(bi);

			final Coordinate c = new Coordinate(b.getLast(FlightDataType.TYPE_POSITION_X),
					b.getLast(FlightDataType.TYPE_POSITION_Y), 0);
			final double vz = b.getLast(FlightDataType.TYPE_VELOCITY_Z);

			if (vz < -10)
				impact.add(c);
			else
				landing.add(c);
		}
		settings.setMaxTime(s.getSimulatedData().getFlightTime() );
		repaint();
	}

	public SimPanel() {
		this.setLayout(new BorderLayout());
		initGLCanvas();
		setupMouseListeners();
	}

	private void initGLCanvas() {
		try {
			log.debug("Setting up GL capabilities...");
			final GLProfile glp = GLProfile.get(GLProfile.GL2);

			final GLCapabilities caps = new GLCapabilities(glp);

			// TODO prefs
			caps.setSampleBuffers(true);
			caps.setNumSamples(6);
			canvas = new GLCanvas(caps);

			((GLAutoDrawable) canvas).addGLEventListener(this);
			this.add(canvas, BorderLayout.CENTER);
		} catch (Throwable t) {
			log.error("An error occurred creating 3d View", t);
			canvas = null;
			this.add(new JLabel("Unable to load 3d Libraries: " + t.getMessage()));
		}
	}

	private void setupMouseListeners() {
		MouseInputAdapter a = new MouseInputAdapter() {
			int lastX;
			int lastY;
			MouseEvent pressEvent;

			@Override
			public void mousePressed(final MouseEvent e) {
				lastX = e.getX();
				lastY = e.getY();
				pressEvent = e;
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				final int clicks = Math.abs(e.getWheelRotation());
				final boolean pos = clicks == e.getWheelRotation();
				for (int i = 0; i < clicks; i++) {

					viewDist *= pos ? 1.1 : .90;

				}
				// viewDist += 7 * e.getWheelRotation();
				// viewDist = MathUtil.clamp(viewDist, 7, 500);
				viewDist = Math.max(viewDist, 1);
				SimPanel.this.repaint();
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				// You can get a drag without a press while a modal dialog is
				// shown
				if (pressEvent == null)
					return;

				final double height = canvas.getHeight();
				final double width = canvas.getWidth();
				final double x1 = (width - 2 * lastX) / width;
				final double y1 = (2 * lastY - height) / height;
				final double x2 = (width - 2 * e.getX()) / width;
				final double y2 = (2 * e.getY() - height) / height;

				viewAlt -= (y1 - y2) * 100;
				viewAz += (x1 - x2) * 100;

				viewAlt = MathUtil.clamp(viewAlt, 1, 90);

				lastX = e.getX();
				lastY = e.getY();

				SimPanel.this.repaint();
			}
		};

		canvas.addMouseWheelListener(a);
		canvas.addMouseMotionListener(a);
		canvas.addMouseListener(a);
	}

	@Override
	public void paintImmediately(Rectangle r) {
		super.paintImmediately(r);
		if (canvas != null)
			((GLAutoDrawable) canvas).display();
	}

	@Override
	public void paintImmediately(int x, int y, int w, int h) {
		super.paintImmediately(x, y, w, h);
		if (canvas != null)
			((GLAutoDrawable) canvas).display();
	}

	@Override
	public void display(final GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();

		if (glu == null) {
			glu = new GLU();
			q = glu.gluNewQuadric();
		}

		gl.glEnable(GL.GL_MULTISAMPLE);

		gl.glClearDepth(1.0f); // clear z-buffer to the farthest
		gl.glDepthFunc(GL.GL_LESS);
		gl.glEnable(GL.GL_DEPTH_TEST);

		gl.glClearColor(.80f, .80f, 1, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glDisable(GLLightingFunc.GL_LIGHTING);

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(60, ratio, viewDist / 10, viewDist * 10);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		gl.glLoadIdentity();

		// Draw the sky
		gl.glPushMatrix();

		gl.glDepthMask(false);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glScaled(viewDist, viewDist, viewDist);
		gl.glRotated(viewAlt, 1, 0, 0);
		gl.glRotated(viewAz, 0, 1, 0);
		Mountains.instance.draw(gl, tc);
		gl.glDepthMask(true);
		gl.glPopMatrix();

		glu.gluLookAt(0, 0, viewDist, 0, 0, 0, 0, 1, 0);

		gl.glRotated(-90, 1, 0, 0);

		gl.glRotated(viewAlt, 1, 0, 0);
		gl.glRotated(viewAz, 0, 0, 1);

		gl.glTranslated(0, 0, -viewDist / 3);

		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR);
		gl.glFogfv(GL2.GL_FOG_COLOR, new float[] { .9f, .95f, .85f }, 0);
		gl.glFogf(GL2.GL_FOG_DENSITY, 0.1f);
		gl.glHint(GL2.GL_FOG_HINT, GL2.GL_NICEST);
		gl.glFogf(GL2.GL_FOG_START, (float) (viewDist * 5));
		gl.glFogf(GL2.GL_FOG_END, (float) (viewDist * 9));
		gl.glEnable(GL2.GL_FOG);

		gg.ground(gl);

		gl.glDisable(GL2.GL_FOG);

		// drawUpArrow(drawable);

		drawPoints(drawable);

		if (viewAlt < 89)
			drawSimulations(drawable);

		// new RealisticRenderer(simulations.)

		// gl = null;
	}

	public void drawSimulations(final GLAutoDrawable drawable) {
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		// gl.glDepthMask(false);
		double a = 1 - (.01 * simulations.size());
		for (final Simulation s : simulations) {
			// a = a + .01;
			a = 1;
			drawSimulation(s, s == highlighted, a, drawable);
		}
		// gl.glDepthMask(true);
	}

	public void drawSimulation(final Simulation s, final boolean highlight, final double a,
			final GLAutoDrawable drawable) {

		for (int b = 0; b < s.getSimulatedData().getBranchCount(); b++) {
			Conditions c = drawBranch(s.getSimulatedData().getBranch(b), false, a, drawable, settings.getTime());

			if (c.valid && c.where != null) {
				gl.glPushMatrix();
				gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glLoadIdentity();
				gl.glScaled(-1, 1, 1);
				gl.glTranslated(-1, 0, 0);

				gl.glEnable(GL.GL_CULL_FACE);
				gl.glCullFace(GL.GL_BACK);

				gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
				// gl.glLoadIdentity();
				gl.glTranslated(c.where.x, c.where.y, c.where.z);

				if (c.recovery) {
					//Nothing
				} else if (c.tumbling) {
					gl.glRotated(60 * b * settings.getTime(), 1 + b, 1 - b, 1);
				} else {
					log.debug("Phi {}", c.phi);
					// Az
					gl.glRotated(90 - (c.phi * (180.0 / Math.PI)), 0, 0, 1);
					// Alt (Zenith angle)
					gl.glRotated(90 - (c.theta * (180.0 / Math.PI)), 1, 0, 0);
				}

				// Change to LEFT Handed coordinates
				gl.glScaled(1, 1, -1);
				gl.glFrontFace(GL.GL_CW);

				double sc = 0.3 * viewDist;
				gl.glScaled(sc, sc, sc);

				gl.glRotated(-90, 0, 1, 0);

				Configuration cfg = s.getConfiguration().clone();
				if (b == 0) {
					cfg.setToStage(cfg.getStageCount() - c.stagesSeparated - 1);
				} else {
					cfg.setOnlyStage(b);
				}

				double z = 0;
				for (Coordinate bc : cfg.getBounds()) {
					z = Math.max(z, bc.x);
				}
				gl.glTranslated(-z, 0, 0);

				gl.glEnable(GLLightingFunc.GL_LIGHTING);
				rr.render(drawable, cfg, new HashSet<RocketComponent>());
				gl.glDisable(GLLightingFunc.GL_LIGHTING);

				// Change back to right-hand coordinates
				gl.glScaled(1, 1, -1);
				gl.glFrontFace(GL.GL_CCW);

				gl.glPopMatrix();
			}
		}
	}

	private class Conditions {
		Coordinate where;
		double theta, phi;
		boolean valid = false;
		int stagesSeparated = 0;
		boolean tumbling = false;
		boolean recovery = true;
	};

	public Conditions drawBranch(final FlightDataBranch b, final boolean highlight, final double a,
			final GLAutoDrawable drawable, final double maxTime) {

		Conditions c = new Conditions();

		int n = b.getLength();
		List<Double> t = b.get(FlightDataType.TYPE_TIME);
		List<Double> x = b.get(FlightDataType.TYPE_POSITION_X);
		List<Double> y = b.get(FlightDataType.TYPE_POSITION_Y);
		List<Double> z = b.get(FlightDataType.TYPE_ALTITUDE);

		if (simulations.size() <= 10)
			gl.glLineWidth(3);
		else if (simulations.size() <= 100)
			gl.glLineWidth(2);
		else
			gl.glLineWidth(1);

		// A map of events to draw as markers and where to draw them
		HashMap<FlightEvent, Coordinate> markEvents = new HashMap<FlightEvent, Coordinate>();

		// Iterate through the flight path
		gl.glBegin(GL.GL_LINE_STRIP);
		List<FlightEvent> events = b.getEvents();
		Collections.sort(events);
		gl.glColor4d(0, 0, 0, 1);
		for (int i = 0; i < n; i++) {
			// If there is an event to consider
			if (events.get(0).getTime() < t.get(i)) {
				FlightEvent e = events.remove(0);

				if (e.getType() == FlightEvent.Type.STAGE_SEPARATION)
					c.stagesSeparated++;
				if (e.getType() == FlightEvent.Type.TUMBLE)
					c.tumbling = true;
				if (e.getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT)
					c.recovery = true;

				if (trackColorTypes.contains(e.getType())) {
					// Change the track color based on this event
					Color color = getEventColor(e.getType());
					gl.glColor4d(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, 1);
				}
				if (markTypes.contains(e.getType())) {
					// Note this even to mark later
					markEvents.put(e, new Coordinate(x.get(i), y.get(i), z.get(i)));
				}
			}
			gl.glVertex3d(x.get(i), y.get(i), z.get(i));
			if (t.get(i) > maxTime) {
				c.where = new Coordinate(x.get(i), y.get(i), z.get(i));
				c.theta = b.get(FlightDataType.TYPE_ORIENTATION_THETA).get(i);
				c.phi = b.get(FlightDataType.TYPE_ORIENTATION_PHI).get(i);
				System.err.println(i);
				break;
			}
			c.valid = true;
		}
		gl.glEnd();

		// Draw a marker for every event
		for (Map.Entry<FlightEvent, Coordinate> e : markEvents.entrySet()) {
			Color color = getEventColor(e.getKey().getType());
			gl.glColor4d(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, 1);
			gl.glPushMatrix();
			gl.glTranslated(e.getValue().x, e.getValue().y, e.getValue().z);
			glu.gluSphere(q, .006 * viewDist, 10, 10);
			gl.glPopMatrix();
		}

		// Optionally draw a curtain wall to highlight the ground track
		if (highlight) {
			gl.glLineWidth(1);
			gl.glColor4d(0, 0, 0, .15);
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			for (int i = 0; i < n; i++) {
				gl.glVertex3d(x.get(i), y.get(i), z.get(i));
				gl.glVertex3d(x.get(i), y.get(i), 0);
			}
			gl.glEnd();
			gl.glBegin(GL.GL_LINES);
			for (int i = 0; i < n; i++) {
				if (i % 20 == 0) {
					gl.glVertex3d(x.get(i), y.get(i), z.get(i));
					gl.glVertex3d(x.get(i), y.get(i), 0);
				}
			}
			gl.glEnd();
		}

		return c;
	}

	public void drawPoints(final GLAutoDrawable drawable) {

		gl.glColor3d(1, 0, 0);

		for (final Coordinate c : impact) {
			gl.glPushMatrix();
			gl.glTranslated(c.x, c.y, c.z);
			glu.gluSphere(q, .007 * viewDist, 8, 5);
			gl.glPopMatrix();
		}

		gl.glColor3d(.1, .3, .1);
		for (final Coordinate c : landing) {
			gl.glPushMatrix();
			gl.glTranslated(c.x, c.y, c.z);
			glu.gluSphere(q, .007 * viewDist, 8, 5);
			gl.glPopMatrix();
		}
	}

	/**
	 * Draw a nice little 1m tall arrow at the origin, pointing up
	 * 
	 * @param drawable
	 */
	public void drawUpArrow(final GLAutoDrawable drawable) {

		gl.glColor3d(0.1, 0.5, 0.1);

		glu.gluSphere(q, 0.1f, 10, 10);
		glu.gluCylinder(q, .05, .05, .7, 10, 10);
		gl.glTranslated(0, 0, .7);
		glu.gluCylinder(q, .15, 0, .3, 20, 1);
		gl.glTranslated(0, 0, -.7);
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		log.trace("GL - dispose() called");
		rr.dispose(drawable);
		tc.dispose(drawable);
	}

	@Override
	public void init(final GLAutoDrawable drawable) {
		log.trace("GL - init()");
		drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));

		final GL2 gl = drawable.getGL().getGL2();

		gl.glClearDepth(1.0f); // clear z-buffer to the farthest
		gl.glDepthFunc(GL.GL_LESS); // the type of depth test to do

		rr.init(drawable);
		tc.init(drawable);
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

	public static void main(String args[]) throws Exception {
		JFrame f = new JFrame();
		f.setSize(640, 480);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		SimPanel d = new SimPanel();
		f.setContentPane(d);

		f.setVisible(true);
	}
}
