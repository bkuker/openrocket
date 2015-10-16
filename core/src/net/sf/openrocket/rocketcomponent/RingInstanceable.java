package net.sf.openrocket.rocketcomponent;

public interface RingInstanceable extends Instanceable {

	public double getAngularOffset();

	public double getRadialOffset();
	
	public void setAngularOffset(final double radius);
	
	public void setRadialOffset(final double radius);
	
}