package net.sf.openrocket.gui.figure3d.photo.sky;

import java.net.MalformedURLException;
import java.net.URL;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import net.sf.openrocket.gui.figure3d.TextureCache;

import com.jogamp.opengl.util.texture.Texture;

public class SkyBox extends Sky {
	private final TextureCache cache;
	private final URL baseURL;
	
	public SkyBox(final TextureCache cache) {
		this.cache = cache;
		baseURL = SkyBox.class.getResource("");
	}
	
	public SkyBox(final URL baseURL, final TextureCache cache) {
		this.cache = cache;
		this.baseURL = baseURL;
	}
	
	@Override
	public void draw(GL2 gl) {
		gl.glPushMatrix();
		gl.glColor3d(1, 1, 1);
		try {
			square(gl, cache.getTexture(new URL(baseURL, "North.jpg")));
			
			gl.glRotatef(90, 0, 1, 0);
			square(gl, cache.getTexture(new URL(baseURL, "East.jpg")));
			
			gl.glRotatef(90, 0, 1, 0);
			square(gl, cache.getTexture(new URL(baseURL, "South.jpg")));
			
			gl.glRotatef(90, 0, 1, 0);
			square(gl, cache.getTexture(new URL(baseURL, "West.jpg")));
			
			gl.glRotatef(-90, 1, 0, 0);
			gl.glRotatef(90, 0, 0, 1);
			square(gl, cache.getTexture(new URL(baseURL, "Up.jpg")));
			
			gl.glRotatef(180, 1, 0, 0);
			square(gl, cache.getTexture(new URL(baseURL, "Down.jpg")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		gl.glPopMatrix();
	}
	
	private static final void square(GL2 gl, Texture t) {
		t.bind(gl);
		t.enable(gl);
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
		t.disable(gl);
	}
}
