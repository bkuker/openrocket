package net.sf.openrocket.gui.figure3d;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.unit.UnitGroup;

public class PhotoConfigPanel extends JPanel {
	
	
	public PhotoConfigPanel(final PhotoBooth.Photo p) {
		super(new MigLayout("fill"));
		
		add(new StyledLabel("Rocket", Style.BOLD), "wrap");
		
		add(new JLabel("Pitch"));
		DoubleModel pitchModel = new DoubleModel(p, "Pitch", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(pitchModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(pitchModel), "wrap");
		
		add(new JLabel("Yaw"));
		DoubleModel yawModel = new DoubleModel(p, "Yaw", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(yawModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(yawModel), "wrap");
		
		add(new JLabel("Roll"));
		DoubleModel rollModel = new DoubleModel(p, "Roll", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(rollModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(rollModel), "wrap");
		
		add(new StyledLabel("Camera", Style.BOLD), "wrap");
		
		add(new JLabel("View Azimuth"));
		DoubleModel viewAzModel = new DoubleModel(p, "ViewAz", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(viewAzModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(viewAzModel), "wrap");
		
		add(new JLabel("View Altitude"));
		DoubleModel viewAltModle = new DoubleModel(p, "ViewAlt", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(viewAltModle.getSpinnerModel()), "w 40");
		add(new UnitSelector(viewAltModle), "wrap");
		
		add(new JLabel("View Distance"));
		DoubleModel viewDistanceModel = new DoubleModel(p, "ViewDistance", UnitGroup.UNITS_LENGTH);
		add(new JSpinner(viewDistanceModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(viewDistanceModel), "wrap");
		
		add(new JLabel("FoV"));
		DoubleModel fovModel = new DoubleModel(p, "Fov", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(fovModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(fovModel), "wrap");
		
		add(new StyledLabel("Light", Style.BOLD), "wrap");
		
		add(new JLabel("Light Azimuth"));
		DoubleModel lightAzModel = new DoubleModel(p, "LightAz", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(lightAzModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(lightAzModel), "wrap");
		
		add(new JLabel("Light Altitude"));
		DoubleModel lightAltModle = new DoubleModel(p, "LightAlt", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(lightAltModle.getSpinnerModel()), "w 40");
		add(new UnitSelector(lightAltModle), "wrap");
		
		add(new StyledLabel("Effects", Style.BOLD), "wrap");
		
		add(new JLabel("Smoke"));
		add(new JCheckBox(new BooleanModel(p, "Smoke")), "wrap");
		
		add(new JLabel("Fire"));
		add(new JCheckBox(new BooleanModel(p, "Flame")), "wrap");
		
		add(new JLabel("Speed"));
		add(new JCheckBox(new BooleanModel(p, "MotionBlurred")), "wrap");
	}
}
