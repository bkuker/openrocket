package net.sf.openrocket.gui.figure3d;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.defaults.DefaultAppearance;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class UnfinishedRenderer extends RealisticRenderer {
	
	public UnfinishedRenderer() {
		super();
	}
	
	@Override
	public boolean isDrawnTransparent(RocketComponent c) {
		return c instanceof BodyTube;
	}
	
	@Override
	protected Appearance getAppearance(RocketComponent c) {
		return DefaultAppearance.getDefaultAppearance(c);
	}
}
