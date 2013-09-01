package net.sf.openrocket.gui.figure3d.photo.sky;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import net.sf.openrocket.gui.figure3d.TextureCache;

import com.jogamp.opengl.util.texture.Texture;

public class SkyBox {
	
	public static void draw(GL2 gl, TextureCache cache) {
		gl.glPushMatrix();
		gl.glColor3d(1, 1, 1);
		square(gl, cache.getTexture(SkyBox.class.getResource("North.jpg")));
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, cache.getTexture(SkyBox.class.getResource("East.jpg")));
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, cache.getTexture(SkyBox.class.getResource("South.jpg")));
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, cache.getTexture(SkyBox.class.getResource("West.jpg")));
		
		gl.glRotatef(-90, 1, 0, 0);
		gl.glRotatef(90, 0, 0, 1);
		square(gl, cache.getTexture(SkyBox.class.getResource("Up.jpg")));
		
		gl.glRotatef(180, 1, 0, 0);
		square(gl, cache.getTexture(SkyBox.class.getResource("Down.jpg")));
		
		
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
