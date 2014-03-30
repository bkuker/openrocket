package net.sf.openrocket.gui.figure3d.simviz;

import java.awt.geom.PathIterator;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

public class PathRenderer {

	public static interface Path {
		public PathIterator getPathIterator();
	}

	private GLUtessellator tobj = GLU.gluNewTess();

	public synchronized void fill(final GL2 gl , Iterable<Path> paths) {
		for (Path p : paths) {
			fill(gl, p.getPathIterator());
		}
	}

	public synchronized void outline(final GL2 gl , Iterable<Path> paths) {
		for (Path p : paths) {
			outline(gl, p.getPathIterator());
		}
	}

	public synchronized void outline(final GL2 gl , PathIterator pi) {
		gl.glBegin(GL.GL_LINE_LOOP);
		double d[] = new double[] { 0, 0, 0 };

		while (!pi.isDone()) {
			int type = pi.currentSegment(d);
			if (type == PathIterator.SEG_MOVETO) {
				gl.glEnd();
				gl.glBegin(GL.GL_LINE_LOOP);
			}
			gl.glVertex3dv(d, 0);
			pi.next();
		}
		gl.glEnd();
	}

	public synchronized void fill(final GL2 gl , PathIterator pi) {


		GLUtessellatorCallback cb = new GLUtessellatorCallbackAdapter() {
			@Override
			public void vertex(Object vertexData) {
				double d[] = (double[]) vertexData;
				gl.glVertex3dv(d, 0);
			}

			@Override
			public void begin(int type) {
				gl.glBegin(type);
			}

			@Override
			public void end() {
				gl.glEnd();
			}
		};

		GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, cb);
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, cb);
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, cb);

		GLU.gluTessBeginPolygon(tobj, null);
		GLU.gluTessBeginContour(tobj);
		gl.glNormal3f(0, 0, 1);

		while (!pi.isDone()) {
			double d[] = new double[] { 0, 0, 0 };
			pi.currentSegment(d);
			GLU.gluTessVertex(tobj, d, 0, d);
			pi.next();
		}

		GLU.gluTessEndContour(tobj);
		GLU.gluTessEndPolygon(tobj);
	}

}
