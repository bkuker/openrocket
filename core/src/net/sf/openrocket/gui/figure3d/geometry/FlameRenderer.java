/*
 ** License Applicability. Except to the extent portions of this file are
 ** made subject to an alternative license as permitted in the SGI Free
 ** Software License B, Version 2.0 (the "License"), the contents of this
 ** file are subject only to the provisions of the License. You may not use
 ** this file except in compliance with the License. You may obtain a copy
 ** of the License at Silicon Graphics, Inc., attn: Legal Services, 1600
 ** Amphitheatre Parkway, Mountain View, CA 94043-1351, or at:
 ** 
 ** http://oss.sgi.com/projects/FreeB
 ** 
 ** Note that, as provided in the License, the Software is distributed on an
 ** "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
 ** DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
 ** CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
 ** PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
 ** 
 ** NOTE:  The Original Code (as defined below) has been licensed to Sun
 ** Microsystems, Inc. ("Sun") under the SGI Free Software License B
 ** (Version 1.1), shown above ("SGI License").   Pursuant to Section
 ** 3.2(3) of the SGI License, Sun is distributing the Covered Code to
 ** you under an alternative license ("Alternative License").  This
 ** Alternative License includes all of the provisions of the SGI License
 ** except that Section 2.2 and 11 are omitted.  Any differences between
 ** the Alternative License and the SGI License are offered solely by Sun
 ** and not by SGI.
 **
 ** Original Code. The Original Code is: OpenGL Sample Implementation,
 ** Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
 ** Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
 ** Copyright in any portions created by third parties is as indicated
 ** elsewhere herein. All Rights Reserved.
 ** 
 ** Additional Notice Provisions: The application programming interfaces
 ** established by SGI in conjunction with the Original Code are The
 ** OpenGL(R) Graphics System: A Specification (Version 1.2.1), released
 ** April 1, 1999; The OpenGL(R) Graphics System Utility Library (Version
 ** 1.3), released November 4, 1998; and OpenGL(R) Graphics with the X
 ** Window System(R) (Version 1.3), released October 19, 1998. This software
 ** was created using the OpenGL(R) version 1.2.1 Sample Implementation
 ** published by SGI, but has not been independently verified as being
 ** compliant with the OpenGL(R) version 1.2.1 Specification.
 **
 ** $Date: 2009-03-04 17:23:34 -0800 (Wed, 04 Mar 2009) $ $Revision: 1856 $
 ** $Header$
 */

/* 
 * Copyright (c) 2002-2004 LWJGL Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * * Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of 
 *   its contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */
package net.sf.openrocket.gui.figure3d.geometry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLProfile;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import net.sf.openrocket.util.Color;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

public final class FlameRenderer {
	
	private FlameRenderer() {
	}
	
	private static interface Radius {
		double getRadius(double z);
		
		public void getColor(double z, float[] color);
	}
	
	static Texture noise;
	
	protected static void convertColor(Color color, float[] out) {
		if (color == null) {
			out[0] = 1;
			out[1] = 1;
			out[2] = 0;
		} else {
			out[0] = (float) color.getRed() / 255f;
			out[1] = (float) color.getGreen() / 255f;
			out[2] = (float) color.getBlue() / 255f;
		}
	}
	
	static Texture smokeT;
	
	public static void f(GL2 gl, boolean flame, boolean smoke, Color smokeColor, Color flameColor) {
		if (smokeT == null) {
			try {
				TextureData data = TextureIO.newTextureData(GLProfile.getDefault(), FlameRenderer.class.getResourceAsStream("smoke.png"), GL.GL_RGBA, GL.GL_RGBA, true, null);
				smokeT = TextureIO.newTexture(data);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		gl.glRotated(90, 0, 1, 0);
		gl.glTranslated(0, 0, 0);
		gl.glDisable(GLLightingFunc.GL_LIGHTING);
		gl.glEnable(GL.GL_BLEND);
		
		
		if (flame) {
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			
			
			gl.glPushMatrix();
			gl.glScaled(.03, .03, .07);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
			drawFlame(gl, 1, new Radius() {
				@Override
				public double getRadius(double z) {
					z = 1 - z;
					return (z * z - z * z * z);
				}
				
				@Override
				public void getColor(double z, float[] color) {
					color[0] = color[1] = color[2] = 1;
					color[3] = 1;// - (float) (z * z);
				}
			}, 80, 80);
			
			gl.glScaled(1.4, 1.4, 1.2);
			final float[] fc = new float[3];
			convertColor(flameColor, fc);
			drawFlame(gl, 1, new Radius() {
				@Override
				public double getRadius(double z) {
					z = 1 - z;
					return z * z - z * z * z;
				}
				
				@Override
				public void getColor(double z, float[] color) {
					color[0] = fc[0];
					color[1] = fc[1];
					color[2] = fc[2];
					color[3] = (1 - (float) (z));
				}
			}, 80, 80);
			gl.glPopMatrix();
		}
		
		if (smoke) {
			
			gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
			
			
			smokeT.enable(gl);
			smokeT.bind(gl);
			
			double z = .00;
			double v = 0;
			double r = 0;
			
			
			gl.glEnable(GL2ES1.GL_POINT_SPRITE);
			float quadratic[] = { 0.0f, 0.0f, 1.0f };
			gl.glPointParameterfv(GL2ES1.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0);
			gl.glPointParameterf(GL.GL_POINT_FADE_THRESHOLD_SIZE, 60.0f);
			
			gl.glPointParameterf(GL2ES1.GL_POINT_SIZE_MIN, 1.0f);
			gl.glPointParameterf(GL2ES1.GL_POINT_SIZE_MAX, 64.0f);
			gl.glTexEnvf(GL2ES1.GL_POINT_SPRITE, GL2ES1.GL_COORD_REPLACE, GL.GL_TRUE);
			gl.glPointSize(32.0f);
			
			gl.glDepthMask(false);
			
			gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
			
			
			
			float[] color = new float[4];
			float[] fc = new float[4];
			convertColor(flameColor, fc);
			
			for (int i = 0; i < 100; i++) {
				
				v = .01 + z * .15;
				r = .1 * z + .0001;
				
				float fa = flame ? (float) (.06 / (z)) : 0;
				fa = fa * .9f;
				
				convertColor(smokeColor, color);
				color[0] = Math.min(color[0] + fc[0] * fa, 1.0f);
				color[1] = Math.min(color[1] + fc[1] * fa, 1.0f);
				color[2] = Math.min(color[2] + fc[2] * fa, 1.0f);
				color[3] = 1 - fa;
				gl.glColor4fv(color, 0);
				
				
				gl.glPointSize((float) r * 5000);
				gl.glBegin(GL.GL_POINTS);
				for (int j = 0; j < 40; j++) {
					gl.glVertex3d(Math.random() * v - v / 2, Math.random() * v - v / 2, z + Math.random() * v - v / 2);
				}
				gl.glEnd();
				z = z + r;
			}
			
			
			gl.glDepthMask(true);
			
			smokeT.disable(gl);
			
		}
		
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		
	}
	
	public static void oldf(GL2 gl, boolean flame, boolean smoke, Color smokeColor, Color flameColor) {
		
		if (noise == null) {
			try {
				TextureData data = TextureIO.newTextureData(GLProfile.getDefault(), FlameRenderer.class.getResourceAsStream("snoise.png"), GL.GL_RGBA, GL.GL_RGBA, true, null);
				noise = TextureIO.newTexture(data);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_BLEND);
		gl.glDisable(GLLightingFunc.GL_LIGHTING);
		gl.glRotated(90, 0, 1, 0);
		gl.glTranslated(0, 0, 0);
		
		
		if (flame) {
			gl.glPushMatrix();
			gl.glScaled(.03, .03, .07);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
			drawFlame(gl, 1, new Radius() {
				@Override
				public double getRadius(double z) {
					z = 1 - z;
					return (z * z - z * z * z);
				}
				
				@Override
				public void getColor(double z, float[] color) {
					color[0] = color[1] = color[2] = 1;
					color[3] = 1;// - (float) (z * z);
				}
			}, 80, 80);
			
			gl.glScaled(1.4, 1.4, 1.2);
			final float[] fc = new float[3];
			convertColor(flameColor, fc);
			drawFlame(gl, 1, new Radius() {
				@Override
				public double getRadius(double z) {
					z = 1 - z;
					return z * z - z * z * z;
				}
				
				@Override
				public void getColor(double z, float[] color) {
					color[0] = fc[0];
					color[1] = fc[1];
					color[2] = fc[2];
					color[3] = (1 - (float) (z));
				}
			}, 80, 80);
			gl.glPopMatrix();
		}
		
		if (smoke) {
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			GLU glu = new GLU();
			GLUquadric q = glu.gluNewQuadric();
			glu.gluQuadricTexture(q, true);
			
			float[] color = new float[4];
			convertColor(smokeColor, color);
			color[3] = 0.4f;
			
			gl.glColor4fv(color, 0);
			noise.enable(gl);
			noise.bind(gl);
			
			double z = .02;
			double v = 0;
			double r = 0;
			for (int i = 0; i < 50; i++) {
				if (z < 2) {
					v = z * .1;
					r = .1 * z + .005;
				} else {
					v += .01;
				}
				if (i < 40)
					color[3] = Math.min(1, color[3] + .01f);
				else
					color[3] = color[3] * .9f;
				gl.glColor4fv(color, 0);
				for (int j = 0; j < 3; j++) {
					gl.glPushMatrix();
					gl.glTranslated(Math.random() * v - v / 2, Math.random() * v - v / 2, z + Math.random() * v - v / 2);
					gl.glRotated(Math.random() * 360, 1, 0, 0);
					gl.glRotated(Math.random() * 360, 0, 1, 0);
					
					glu.gluSphere(q, r, 5, 5);
					gl.glPopMatrix();
				}
				z = z + r;
			}
			
			noise.disable(gl);
		}
		
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
	}
	
	private static void drawFlame(final GL2 gl, final double length, final Radius rad,
			final int slices, final int stacks) {
		
		double da = 2.0f * Math.PI / slices, r = 0, dzBase = (double) length / stacks;
		double x, y, z = 0, nz, lnz = 0;
		double ds = 1.0f / slices;
		
		float color[] = new float[4];
		float colorNext[] = new float[4];
		
		while (z < length) {
			double t = z / length;
			
			double dz = t < 0.025 ? dzBase / 8.0 : dzBase;
			double zNext = Math.min(z + dz, length);
			
			r = Math.max(0, rad.getRadius(z));
			double rNext = Math.max(0, rad.getRadius(zNext));
			
			nz = (r - rNext) / dz;
			
			double s = 0.0f;
			
			rad.getColor(zNext, colorNext);
			rad.getColor(z, color);
			
			gl.glBegin(GL2.GL_QUAD_STRIP);
			for (int i = 0; i <= slices; i++) {
				if (i == slices) {
					x = Math.sin(0.0f);
					y = Math.cos(0.0f);
				} else {
					x = Math.sin((i * da));
					y = Math.cos((i * da));
				}
				if (r == 0) {
					normal3d(gl, x, y, nz);
				} else {
					normal3d(gl, x, y, lnz);
				}
				gl.glTexCoord2d(s, z / length);
				gl.glColor4fv(color, 0);
				gl.glVertex3d((x * r), (y * r), z);
				
				
				normal3d(gl, x, y, nz);
				gl.glTexCoord2d(s, zNext / length);
				gl.glColor4fv(colorNext, 0);
				gl.glVertex3d((x * rNext), (y * rNext), zNext);
				
				s += ds;
			} // for slices
			gl.glEnd();
			lnz = nz;
			z = Math.min(z + dz, length);
		} // for stacks
		
	}
	
	static final void normal3d(GL2 gl, double x, double y, double z) {
		double mag;
		mag = (double) Math.sqrt(x * x + y * y + z * z);
		if (mag > 0.00001F) {
			x /= mag;
			y /= mag;
			z /= mag;
		}
		gl.glNormal3d(x, y, z);
	}
	
}
