package net.sf.openrocket.gui.main;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.Simulation.Status;
import net.sf.openrocket.document.events.DocumentChangeEvent;
import net.sf.openrocket.document.events.DocumentChangeListener;
import net.sf.openrocket.document.events.SimulationChangeEvent;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.simulation.SimulationEditDialog;
import net.sf.openrocket.gui.simulation.SimulationRunDialog;
import net.sf.openrocket.gui.simulation.SimulationWarningDialog;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.UnitGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationPanel extends JPanel {
	private static final Logger log = LoggerFactory.getLogger(SimulationPanel.class);
	private static final Translator trans = Application.getTranslator();
	
	
	private static final Color WARNING_COLOR = Color.RED;
	private static final String WARNING_TEXT = "\uFF01"; // Fullwidth exclamation mark
	
	private static final Color OK_COLOR = new Color(60, 150, 0);
	private static final String OK_TEXT = "\u2714"; // Heavy check mark
	
	
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	
	private final OpenRocketDocument document;
	
	private final ColumnTableModel simulationTableModel;
	private final JTable simulationTable;
	
	private final JButton editButton;
	private final JButton runButton;
	private final JButton deleteButton;
	private final JButton plotButton;
	
	public SimulationPanel(OpenRocketDocument doc) {
		super(new MigLayout("fill", "[grow][][][][][][grow]"));
		
		this.document = doc;
		
		
		
		////////  The simulation action buttons
		
		//// New simulation button
		{
			JButton button = new JButton(trans.get("simpanel.but.newsimulation"));
			//// Add a new simulation
			button.setToolTipText(trans.get("simpanel.but.ttip.newsimulation"));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Simulation sim = new Simulation(document.getRocket());
					sim.setName(document.getNextSimulationName());
					
					int n = document.getSimulationCount();
					document.addSimulation(sim);
					simulationTableModel.fireTableDataChanged();
					simulationTable.clearSelection();
					simulationTable.addRowSelectionInterval(n, n);
					
					openDialog(false, sim);
				}
			});
			this.add(button, "skip 1, gapright para");
		}
		
		//// Edit simulation button
		editButton = new JButton(trans.get("simpanel.but.editsimulation"));
		//// Edit the selected simulation
		editButton.setToolTipText(trans.get("simpanel.but.ttip.editsim"));
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selection = simulationTable.getSelectedRows();
				if (selection.length == 0)
					return; // TODO: LOW: "None selected" dialog
					
				Simulation[] sims = new Simulation[selection.length];
				for (int i = 0; i < selection.length; i++) {
					selection[i] = simulationTable.convertRowIndexToModel(selection[i]);
					sims[i] = document.getSimulation(selection[i]);
				}
				openDialog(false, sims);
			}
		});
		this.add(editButton, "gapright para");
		
		//// Run simulations
		runButton = new JButton(trans.get("simpanel.but.runsimulations"));
		//// Re-run the selected simulations
		runButton.setToolTipText(trans.get("simpanel.but.ttip.runsimu"));
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selection = simulationTable.getSelectedRows();
				if (selection.length == 0)
					return; // TODO: LOW: "None selected" dialog
					
				Simulation[] sims = new Simulation[selection.length];
				for (int i = 0; i < selection.length; i++) {
					selection[i] = simulationTable.convertRowIndexToModel(selection[i]);
					sims[i] = document.getSimulation(selection[i]);
				}
				
				long t = System.currentTimeMillis();
				new SimulationRunDialog(SwingUtilities.getWindowAncestor(
						SimulationPanel.this), document, sims).setVisible(true);
				log.info("Running simulations took " + (System.currentTimeMillis() - t) + " ms");
				fireMaintainSelection();
			}
		});
		this.add(runButton, "gapright para");
		
		//// Delete simulations button
		deleteButton = new JButton(trans.get("simpanel.but.deletesimulations"));
		//// Delete the selected simulations
		deleteButton.setToolTipText(trans.get("simpanel.but.ttip.deletesim"));
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selection = simulationTable.getSelectedRows();
				if (selection.length == 0)
					return; // TODO: LOW: "None selected" dialog
					
				// Verify deletion
				boolean verify = Application.getPreferences().getBoolean(Preferences.CONFIRM_DELETE_SIMULATION, true);
				if (verify) {
					
					JPanel panel = new JPanel(new MigLayout());
					//// Do not ask me again
					JCheckBox dontAsk = new JCheckBox(trans.get("simpanel.checkbox.donotask"));
					panel.add(dontAsk, "wrap");
					//// You can change the default operation in the preferences.
					panel.add(new StyledLabel(trans.get("simpanel.lbl.defpref"), -2));
					
					int ret = JOptionPane.showConfirmDialog(SimulationPanel.this,
							new Object[] {
									//// Delete the selected simulations?
									trans.get("simpanel.dlg.lbl.DeleteSim1"),
									//// <html><i>This operation cannot be undone.</i>
									trans.get("simpanel.dlg.lbl.DeleteSim2"),
									"",
									panel },
							//// Delete simulations
							trans.get("simpanel.dlg.lbl.DeleteSim3"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (ret != JOptionPane.OK_OPTION)
						return;
					
					if (dontAsk.isSelected()) {
						Application.getPreferences().putBoolean(Preferences.CONFIRM_DELETE_SIMULATION, false);
					}
				}
				
				// Delete simulations
				for (int i = 0; i < selection.length; i++) {
					selection[i] = simulationTable.convertRowIndexToModel(selection[i]);
				}
				Arrays.sort(selection);
				for (int i = selection.length - 1; i >= 0; i--) {
					document.removeSimulation(selection[i]);
				}
				simulationTableModel.fireTableDataChanged();
			}
		});
		this.add(deleteButton, "gapright para");
		
		//// Plot / export button
		plotButton = new JButton(trans.get("simpanel.but.plotexport"));
		//		button = new JButton("Plot flight");
		plotButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selected = simulationTable.getSelectedRow();
				if (selected < 0)
					return; // TODO: MEDIUM: "None selected" dialog
					
				selected = simulationTable.convertRowIndexToModel(selected);
				simulationTable.clearSelection();
				simulationTable.addRowSelectionInterval(selected, selected);
				
				
				Simulation sim = document.getSimulations().get(selected);
				
				if (!sim.hasSimulationData()) {
					new SimulationRunDialog(SwingUtilities.getWindowAncestor(
							SimulationPanel.this), document, sim).setVisible(true);
				}
				
				fireMaintainSelection();
				
				openDialog(true, sim);
				
			}
		});
		this.add(plotButton, "wrap para");
		
		
		
		////////  The simulation table
		
		simulationTableModel = new ColumnTableModel(
				
				////  Status and warning column
				new Column("") {
					private JLabel label = null;
					
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						// Initialize the label
						if (label == null) {
							label = new StyledLabel(2f);
							label.setIconTextGap(1);
							//							label.setFont(label.getFont().deriveFont(Font.BOLD));
						}
						
						// Set simulation status icon
						Simulation.Status status = document.getSimulation(row).getStatus();
						label.setIcon(Icons.SIMULATION_STATUS_ICON_MAP.get(status));
						
						
						// Set warning marker
						if (status == Simulation.Status.NOT_SIMULATED ||
								status == Simulation.Status.EXTERNAL) {
							
							label.setText("");
							
						} else {
							
							WarningSet w = document.getSimulation(row).getSimulatedWarnings();
							if (w == null) {
								label.setText("");
							} else if (w.isEmpty()) {
								label.setForeground(OK_COLOR);
								label.setText(OK_TEXT);
							} else {
								label.setForeground(WARNING_COLOR);
								label.setText(WARNING_TEXT);
							}
						}
						
						return label;
					}
					
					@Override
					public int getExactWidth() {
						return 36;
					}
					
					@Override
					public Class<?> getColumnClass() {
						return JLabel.class;
					}
				},
				
				//// Simulation name
				//// Name
				new Column(trans.get("simpanel.col.Name")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						return document.getSimulation(row).getName();
					}
					
					@Override
					public int getDefaultWidth() {
						return 125;
					}
				},
				
				//// Simulation configuration
				new Column(trans.get("simpanel.col.Configuration")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						Configuration c = document.getSimulation(row).getConfiguration();
						return descriptor.format(c.getRocket(), c.getFlightConfigurationID());
					}
					
					@Override
					public int getDefaultWidth() {
						return 125;
					}
				},
				
				//// Launch rod velocity
				new Column(trans.get("simpanel.col.Velocityoffrod")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_VELOCITY.getDefaultUnit().toStringUnit(
								data.getLaunchRodVelocity());
					}
				},
				
				//// Apogee
				new Column(trans.get("simpanel.col.Apogee")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_DISTANCE.getDefaultUnit().toStringUnit(
								data.getMaxAltitude());
					}
				},
				
				//// Velocity at deployment
				new Column(trans.get("simpanel.col.Velocityatdeploy")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_VELOCITY.getDefaultUnit().toStringUnit(
								data.getDeploymentVelocity());
					}
				},
				
				//// Maximum velocity
				new Column(trans.get("simpanel.col.Maxvelocity")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_VELOCITY.getDefaultUnit().toStringUnit(
								data.getMaxVelocity());
					}
				},
				
				//// Maximum acceleration
				new Column(trans.get("simpanel.col.Maxacceleration")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_ACCELERATION.getDefaultUnit().toStringUnit(
								data.getMaxAcceleration());
					}
				},
				
				//// Time to apogee
				new Column(trans.get("simpanel.col.Timetoapogee")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit().toStringUnit(
								data.getTimeToApogee());
					}
				},
				
				//// Flight time
				new Column(trans.get("simpanel.col.Flighttime")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit().toStringUnit(
								data.getFlightTime());
					}
				},
				
				//// Ground hit velocity
				new Column(trans.get("simpanel.col.Groundhitvelocity")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_VELOCITY.getDefaultUnit().toStringUnit(
								data.getGroundHitVelocity());
					}
				}
				
				) {
					@Override
					public int getRowCount() {
						return document.getSimulationCount();
					}
				};
		
		// Override processKeyBinding so that the JTable does not catch
		// key bindings used in menu accelerators
		simulationTable = new JTable(simulationTableModel) {
			@Override
			protected boolean processKeyBinding(KeyStroke ks,
					KeyEvent e,
					int condition,
					boolean pressed) {
				return false;
			}
		};
		simulationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		simulationTable.setDefaultRenderer(Object.class, new JLabelRenderer());
		simulationTableModel.setColumnWidths(simulationTable.getColumnModel());
		
		
		// Mouse listener to act on double-clicks
		simulationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					int selected = simulationTable.getSelectedRow();
					if (selected < 0)
						return;
					selected = simulationTable.convertRowIndexToModel(selected);
					
					int column = simulationTable.columnAtPoint(e.getPoint());
					if (column == 0) {
						SimulationWarningDialog.showWarningDialog(SimulationPanel.this, document.getSimulations().get(selected));
					} else {
						simulationTable.clearSelection();
						simulationTable.addRowSelectionInterval(selected, selected);
						
						openDialog(document.getSimulations().get(selected));
					}
				} else {
					updateButtonStates();
				}
			}
		});
		
		document.addDocumentChangeListener(new DocumentChangeListener() {
			@Override
			public void documentChanged(DocumentChangeEvent event) {
				if (!(event instanceof SimulationChangeEvent))
					return;
				simulationTableModel.fireTableDataChanged();
			}
		});
		
		
		
		
		// Fire table change event when the rocket changes
		document.getRocket().addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				fireMaintainSelection();
			}
		});
		
		
		JScrollPane scrollpane = new JScrollPane(simulationTable);
		this.add(scrollpane, "spanx, grow, wrap rel");
		
		updateButtonStates();
	}
	
	private void updateButtonStates() {
		int[] selection = simulationTable.getSelectedRows();
		if (selection.length == 0) {
			editButton.setEnabled(false);
			runButton.setEnabled(false);
			deleteButton.setEnabled(false);
			plotButton.setEnabled(false);
		} else {
			if (selection.length > 1) {
				plotButton.setEnabled(false);
			} else {
				plotButton.setEnabled(true);
			}
			editButton.setEnabled(true);
			runButton.setEnabled(true);
			deleteButton.setEnabled(true);
		}
		
	}
	
	public ListSelectionModel getSimulationListSelectionModel() {
		return simulationTable.getSelectionModel();
	}
	
	private void openDialog(boolean plotMode, final Simulation... sims) {
		SimulationEditDialog d = new SimulationEditDialog(SwingUtilities.getWindowAncestor(this), document, sims);
		if (plotMode) {
			d.setPlotMode();
		}
		d.setVisible(true);
		fireMaintainSelection();
	}
	
	private void openDialog(final Simulation sim) {
		boolean plotMode = false;
		if (sim.hasSimulationData() && (sim.getStatus() == Status.UPTODATE || sim.getStatus() == Status.EXTERNAL)) {
			plotMode = true;
		}
		openDialog(plotMode, sim);
	}
	
	private void fireMaintainSelection() {
		int[] selection = simulationTable.getSelectedRows();
		simulationTableModel.fireTableDataChanged();
		for (int row : selection) {
			if (row >= simulationTableModel.getRowCount())
				break;
			simulationTable.addRowSelectionInterval(row, row);
		}
	}
	
	
	private class JLabelRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if (row < 0 || row >= document.getSimulationCount())
				return super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
			
			// A JLabel is self-contained and has set its own tool tip
			if (value instanceof JLabel) {
				JLabel label = (JLabel) value;
				if (isSelected)
					label.setBackground(table.getSelectionBackground());
				else
					label.setBackground(table.getBackground());
				label.setOpaque(true);
				
				label.setToolTipText(getSimulationToolTip(document.getSimulation(row)));
				return label;
			}
			
			Component component = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			
			if (component instanceof JComponent) {
				((JComponent) component).setToolTipText(getSimulationToolTip(
						document.getSimulation(row)));
			}
			return component;
		}
		
		private String getSimulationToolTip(Simulation sim) {
			String tip;
			FlightData data = sim.getSimulatedData();
			
			tip = "<html><b>" + sim.getName() + "</b><br>";
			switch (sim.getStatus()) {
			case UPTODATE:
				tip += trans.get("simpanel.ttip.uptodate") + "<br>";
				break;
			
			case LOADED:
				tip += trans.get("simpanel.ttip.loaded") + "<br>";
				break;
			
			case OUTDATED:
				tip += trans.get("simpanel.ttip.outdated") + "<br>";
				break;
			
			case EXTERNAL:
				tip += trans.get("simpanel.ttip.external") + "<br>";
				return tip;
				
			case NOT_SIMULATED:
				tip += trans.get("simpanel.ttip.notSimulated");
				return tip;
			}
			
			if (data == null) {
				tip += trans.get("simpanel.ttip.noData");
				return tip;
			}
			WarningSet warnings = data.getWarningSet();
			
			if (warnings.isEmpty()) {
				tip += trans.get("simpanel.ttip.noWarnings");
				return tip;
			}
			
			tip += trans.get("simpanel.ttip.warnings");
			for (Warning w : warnings) {
				tip += "<br>" + w.toString();
			}
			
			return tip;
		}
		
	}
}
