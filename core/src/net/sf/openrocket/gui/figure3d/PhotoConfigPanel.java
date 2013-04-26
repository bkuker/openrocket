package net.sf.openrocket.gui.figure3d;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
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
		
		add(new JLabel("View Direction"));
		DoubleModel viewDirModel = new DoubleModel(p, "ViewDir", UnitGroup.UNITS_ANGLE);
		add(new JSpinner(viewDirModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(viewDirModel), "wrap");
		
		add(new JLabel("View Distance"));
		DoubleModel viewDistanceXModel = new DoubleModel(p, "ViewDistanceX", UnitGroup.UNITS_LENGTH);
		add(new JSpinner(viewDistanceXModel.getSpinnerModel()), "w 40");
		add(new UnitSelector(viewDistanceXModel), "wrap");
		
	}
}
