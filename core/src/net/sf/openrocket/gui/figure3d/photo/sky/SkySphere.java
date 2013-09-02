package net.sf.openrocket.gui.figure3d.photo.sky;

import java.net.URL;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import net.sf.openrocket.gui.figure3d.TextureCache;

import com.jogamp.opengl.util.texture.Texture;

public class SkySphere extends Sky {
	
	private final TextureCache cache;
	private final URL imageURL;
	
	public SkySphere(final TextureCache cache) {
		this.cache = cache;
		imageURL = SkySphere.class.getResource("sky.png");
	}
	
	public SkySphere(final URL imageURL, final TextureCache cache) {
		this.cache = cache;
		this.imageURL = imageURL;
	}
	
	@Override
	public void draw(GL2 gl) {
		gl.glPushMatrix();
		GLU glu = new GLU();
		gl.glRotatef(90, 1, 0, 0);
		Texture sky = cache.getTexture(imageURL);
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
