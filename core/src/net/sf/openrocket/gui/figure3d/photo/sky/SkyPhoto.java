package net.sf.openrocket.gui.figure3d.photo.sky;

import java.net.URL;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import net.sf.openrocket.gui.figure3d.TextureCache;

import com.jogamp.opengl.util.texture.Texture;

public class SkyPhoto extends Sky {
	private final TextureCache cache;
	private final URL imageURL;
	
	public SkyPhoto(final TextureCache cache) {
		this.cache = cache;
		imageURL = SkyPhoto.class.getResource("/datafiles/sky/space.jpg");
	}
	
	public SkyPhoto(final URL imageURL, final TextureCache cache) {
		this.cache = cache;
		this.imageURL = imageURL;
	}
	
	@Override
	public void draw(GL2 gl) {
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glScaled(1, 1, -1);
		
		Texture sky = cache.getTexture(imageURL);
		
		gl.glColor3d(1, 1, 1);
		sky.bind(gl);
		sky.enable(gl);
		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		gl.glNormal3f(0, 0, -1);
		
		gl.glTexCoord2f(1, 1);
		gl.glVertex3f(-1, -1, 1);
		
		gl.glTexCoord2f(0, 1);
		gl.glVertex3f(1, -1, 1);
		
		gl.glTexCoord2f(1, 0);
		gl.glVertex3f(-1, 1, 1);
		
		gl.glTexCoord2f(0, 0);
		gl.glVertex3f(1, 1, 1);
		gl.glEnd();
		sky.disable(gl);
		
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glPopMatrix();
		
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glPopMatrix();
	}
}
