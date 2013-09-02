package net.sf.openrocket.gui.figure3d.photo.sky;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;

import net.sf.openrocket.gui.figure3d.TextureCache;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

public class SkyBoxCross {
	
	static Texture north, east, south, west, up, down;
	
	private static BufferedImage fixBug(BufferedImage i) {
		BufferedImage d2 = new BufferedImage(i.getWidth(), i.getHeight(), i.getType());
		d2.getGraphics().drawImage(i, 0, 0, null);
		return d2;
	}
	
	private static void loadTextures(GL2 gl) {
		try {
			BufferedImage i = ImageIO.read(SkyBoxCross.class.getResource("skybox.jpg"));
			int dy = i.getHeight() / 3;
			int dx = i.getWidth() / 4;
			west = AWTTextureIO.newTexture(GLProfile.getDefault(), fixBug(i.getSubimage(0, dy, dx, dy)), false);
			north = AWTTextureIO.newTexture(GLProfile.getDefault(), fixBug(i.getSubimage(dx, dy, dx, dy)), false);
			east = AWTTextureIO.newTexture(GLProfile.getDefault(), fixBug(i.getSubimage(dx * 2, dy, dx, dy)), false);
			south = AWTTextureIO.newTexture(GLProfile.getDefault(), fixBug(i.getSubimage(dx * 3, dy, dx, dy)), false);
			up = AWTTextureIO.newTexture(GLProfile.getDefault(), fixBug(i.getSubimage(dx, 0 + 1, dx, dy - 2)), false);
			down = AWTTextureIO.newTexture(GLProfile.getDefault(), fixBug(i.getSubimage(dx, 2 * dy, dx, dy)), false);
			
			
			
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	public static void draw(GL2 gl, TextureCache cache) {
		if (north == null) {
			loadTextures(gl);
		}
		gl.glPushMatrix();
		gl.glColor3d(1, 1, 1);
		square(gl, north);
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, east);
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, south);
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, west);
		
		gl.glRotatef(-90, 1, 0, 0);
		gl.glRotatef(90, 0, 0, 1);
		square(gl, up);
		
		gl.glRotatef(180, 1, 0, 0);
		square(gl, down);
		
		
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
