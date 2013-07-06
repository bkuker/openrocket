package net.sf.openrocket.gui.figure3d;

import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import net.sf.openrocket.gui.figure3d.geometry.Geometry;
import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;

public class StereoRenderer extends RocketRenderer {
	private final float[] color = new float[4];
	
	public StereoRenderer() {
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glDisable(GLLightingFunc.GL_LIGHTING);
	}
	
	
	
	@Override
	public boolean isDrawn(RocketComponent c) {
		return true;
	}
	
	@Override
	public boolean isDrawnTransparent(RocketComponent c) {
		return false;
	}
	
	private static final HashMap<Class<?>, Color> defaultColorCache = new HashMap<Class<?>, Color>();
	
	/*@Override
	public void render(GLAutoDrawable drawable, Configuration configuration, Set<RocketComponent> selection) {
		GL2 gl = drawable.getGL().getGL2();
		
		
		gl.glColor3f(1, 0, 0);
		super.render(drawable, configuration, selection);
		gl.glAccum(GL2.GL_LOAD, 0.5f);
		
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		gl.glTranslatef(0.05f, 0, 0);
		
		gl.glColor3d(0, 1, 1);
		super.render(drawable, configuration, selection);
		gl.glAccum(GL2.GL_ACCUM, 0.5f);
		
		gl.glAccum(GL2.GL_RETURN, 1.0f);
	}*/
	
	private void renderOutline(GL2 gl, Geometry g) {
		gl.glLineWidth(5.0f);
		
		//Draw component at zero Z, Z only
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
		gl.glColorMask(false, false, false, false);
		gl.glDepthRange(0, 0);
		g.render(gl);
		
		//Draw with outline at zero z, dpeth and color
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
		gl.glColorMask(true, true, true, true);
		gl.glDepthRange(0, 0);
		gl.glDepthFunc(GL.GL_LESS);
		g.render(gl);
		
		//Draw component at far Z, Z only
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
		gl.glColorMask(false, false, false, false);
		gl.glDepthRange(1, 1);
		gl.glDepthFunc(GL.GL_ALWAYS);
		g.render(gl);
		
		//Back to normal
		gl.glColorMask(true, true, true, true);
		gl.glDepthFunc(GL.GL_LESS);
		gl.glDepthRange(0, 1);
		
	}
	
	@Override
	public void renderComponent(GL2 gl, RocketComponent c, float alpha) {
		renderOutline(gl, cr.getGeometry(c, Surface.ALL));
	}
	
	@Override
	protected void renderMotor(GL2 gl, Motor motor) {
		renderOutline(gl, cr.getGeometry(motor, Surface.ALL));
	}
	
	@Override
	public void flushTextureCache(GLAutoDrawable drawable) {
	}
	
	private static int getShine(RocketComponent c) {
		if (c instanceof ExternalComponent) {
			switch (((ExternalComponent) c).getFinish()) {
			case ROUGH:
				return 10;
			case UNFINISHED:
				return 30;
			case NORMAL:
				return 40;
			case SMOOTH:
				return 80;
			case POLISHED:
				return 128;
			default:
				return 100;
			}
		}
		return 20;
	}
	
	protected static void convertColor(Color color, float[] out) {
		if (color == null) {
			out[0] = 1;
			out[1] = 1;
			out[2] = 0;
		} else {
			out[0] = Math.max(0.2f, (float) color.getRed() / 255f) * 2;
			out[1] = Math.max(0.2f, (float) color.getGreen() / 255f) * 2;
			out[2] = Math.max(0.2f, (float) color.getBlue() / 255f) * 2;
		}
	}
	
	
	
}
