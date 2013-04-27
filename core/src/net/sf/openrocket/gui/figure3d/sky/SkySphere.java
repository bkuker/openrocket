package net.sf.openrocket.gui.figure3d.sky;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import net.sf.openrocket.gui.figure3d.TextureCache;

import com.jogamp.opengl.util.texture.Texture;

public class SkySphere {
	public static void draw(GL2 gl, TextureCache textureCache) {
		gl.glPushMatrix();
		GLU glu = new GLU();
		gl.glRotatef(90, 1, 0, 0);
		Texture sky = textureCache.getTexture(SkySphere.class.getResource("sky.png"));
		sky.enable(gl);
		sky.bind(gl);
		gl.glColor3d(1, 1, 1);
		GLUquadric q = glu.gluNewQuadric();
		glu.gluQuadricTexture(q, true);
		glu.gluQuadricOrientation(q, GLU.GLU_OUTSIDE);
		glu.gluSphere(q, 1f, 20, 20);
		sky.disable(gl);
		gl.glPopMatrix();
	}
}
