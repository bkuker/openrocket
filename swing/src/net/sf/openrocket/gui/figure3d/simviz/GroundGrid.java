package net.sf.openrocket.gui.figure3d.simviz;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.FlatteningPathIterator;
import java.awt.image.BufferedImage;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class GroundGrid {
	
	final PathRenderer pr = new PathRenderer();
	
	public void ground(final GL2 gl) {

		// Draw a Big green square for the ground plane
		final int MAX = 10000;
		gl.glColor3d(.9, .95, .85);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex3f(-MAX, -MAX, 0);
		gl.glVertex3f(MAX, -MAX, 0);
		gl.glVertex3f(MAX, MAX, 0);
		gl.glVertex3f(-MAX, MAX, 0);
		gl.glEnd();

		// Disable depth test, all of this gets drawn over ground plane
		gl.glDisable(GL.GL_DEPTH_TEST);

		// Draw grid
		final int LEVELS = 2;

		for (int level = LEVELS; level >= 0; level--) {
			int d = (int) (Math.pow(10, level));

			gl.glColor3d(1, 1, 1);
			gl.glBegin(GL.GL_TRIANGLE_FAN);
			int D = 50 * d;
			gl.glVertex3f(-D, -D, 0);
			gl.glVertex3f(D, -D, 0);
			gl.glVertex3f(D, D, 0);
			gl.glVertex3f(-D, D, 0);
			gl.glEnd();

			gl.glColor3d(.7, .75, .7);
			gl.glLineWidth(1);
			gl.glBegin(GL.GL_LINES);
			for (int x = -50 * d; x <= 50 * d; x += d) {
				gl.glVertex3f(x, -d * 50, 0);
				gl.glVertex3f(x, d * 50, 0);
				gl.glVertex3f(-d * 50, x, 0);
				gl.glVertex3f(d * 50, x, 0);
			}
			gl.glEnd();
			gl.glColor3d(0, 0, 0);
			gl.glLineWidth(2);
			gl.glBegin(GL.GL_LINES);
			d = d * 10;
			for (int x = -5 * d; x <= 5 * d; x += d) {
				gl.glVertex3f(x, -d * 5, 0);
				gl.glVertex3f(x, d * 5, 0);
				gl.glVertex3f(-d * 5, x, 0);
				gl.glVertex3f(d * 5, x, 0);
			}
			gl.glEnd();
		}

		gl.glLineWidth(2);

		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();

		FontRenderContext frc = g2.getFontRenderContext();
		Font font = new Font("serif.bolditalic", Font.PLAIN, 12);
		Shape shape;
		FlatteningPathIterator fpi;

		gl.glPushMatrix();
		gl.glScaled(1, -1, 1);
		gl.glTranslated(-50, -51, 0);
		shape = font.createGlyphVector(frc, "100 m").getOutline();
		fpi = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
		pr.outline(gl, fpi);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glScaled(10, -10, 1);
		gl.glTranslated(-50, -51, 0);
		shape = font.createGlyphVector(frc, "1 km").getOutline();
		fpi = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
		pr.outline(gl, fpi);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glScaled(100, -100, 1);
		gl.glTranslated(-50, -51, 0);
		shape = font.createGlyphVector(frc, "10 km").getOutline();
		fpi = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
		pr.outline(gl, fpi);
		gl.glPopMatrix();

		gl.glEnable(GL.GL_DEPTH_TEST);
	}
}
