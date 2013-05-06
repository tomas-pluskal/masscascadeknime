/*
 * Copyright (C) 2013 EMBL - European Bioinformatics Institute
 * 
 * All rights reserved. This file is part of the MassCascade feature for KNIME.
 * 
 * The feature is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * The feature is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with the feature. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * Contributors: Stephan Beisken - initial API and implementation
 */
package uk.ac.ebi.masscascade.knime.defaults;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.FilesHistoryPanel;

import uk.ac.ebi.masscascade.exception.MassCascadeException;
import uk.ac.ebi.masscascade.knime.defaults.elements.BooleanTableModel;
import uk.ac.ebi.masscascade.knime.io.reference.OptionalColumnPanel;
import uk.ac.ebi.masscascade.parameters.Parameter;

/**
 * <code>DefaultDialog</code> for MassCascade nodes.
 * 
 * @author Stephan Beisken
 */
public class DefaultDialog extends NodeDialogPane {

	/*
	 * Padding for text labels.
	 */
	public final static String PADDING = "   ";
	/*
	 * Constant to control file deletion in loops.
	 */
	public final static String TERMINUS = "loopTerminus";

	private final GridBagConstraints c;
	private final JCheckBox loopTerminus;

	private final Settings settings;
	private final Map<String, JTextField> textField;
	private final Map<String, JComponent> customField;
	private final Map<String, JScrollPane> scrollPane;
	private final Map<String, ColumnSelectionComboxBox> comboBox;
	private final List<OptionalColumnPanel> columnPanels;
	// if the column selection combo box does not refer to the first input port
	private final Map<String, Integer> comboBoxSpec;

	/**
	 * Constructs a template dialog pane.
	 */
	public DefaultDialog() {

		this.settings = new DefaultSettings();

		this.textField = new LinkedHashMap<String, JTextField>();
		this.customField = new LinkedHashMap<String, JComponent>();
		this.comboBox = new LinkedHashMap<String, ColumnSelectionComboxBox>();
		this.comboBoxSpec = new LinkedHashMap<String, Integer>();
		this.scrollPane = new LinkedHashMap<String, JScrollPane>();
		this.columnPanels = new ArrayList<>();
		
		this.loopTerminus = new JCheckBox();

		this.c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
	}

	/**
	 * Adds a column selection box to the source panel.
	 * 
	 * @param label a label for the selection box
	 * @param cellValue a <code>CellValue</code> defining the selection box
	 */
	public void addColumnSelection(final String label, final Class<? extends DataValue>... cellValue) {

		comboBox.put(label, new ColumnSelectionComboxBox((Border) null, cellValue));
		comboBoxSpec.put(label, 0);
	}

	/**
	 * Adds a column selection box to the source panel.
	 * 
	 * @param parameter a parameter enum for the selection box
	 * @param cellValue a <code>CellValue</code> defining the selection box
	 */
	public void addColumnSelection(final Parameter parameter, final Class<? extends DataValue>... cellValue) {
		addColumnSelection(parameter.getDescription(), cellValue);
	}

	/**
	 * Adds a column selection box to the source panel referring to a specific data table specification.
	 * 
	 * @param label a label for the selection box
	 * @param tableSpec a data table specification index
	 * @param cellValue a <code>CellValue</code> defining the selection box
	 */
	public void addColumnSelection(final String label, final int tableSpec,
			final Class<? extends DataValue>... cellValue) {

		comboBox.put(label, new ColumnSelectionComboxBox((Border) null, cellValue));
		comboBoxSpec.put(label, tableSpec);
	}

	/**
	 * Adds a column selection box to the source panel referring to a specific data table specification.
	 * 
	 * @param parameter a parameter for the selection box
	 * @param tableSpec a data table specification index
	 * @param cellValue a <code>CellValue</code> defining the selection box
	 */
	public void addColumnSelection(final Parameter parameter, final int tableSpec,
			final Class<? extends DataValue>... cellValue) {
		addColumnSelection(parameter.getDescription(), tableSpec, cellValue);
	}

	/**
	 * Adds a <code>JTextField</code> to the parameter panel.
	 * 
	 * @param parameter a parameter enum for the text field
	 * @param width a width for the text field
	 */
	public void addTextOption(final Parameter parameter, final int width) {
		addTextOption(parameter.getDescription(), width);
	}

	/**
	 * Adds a <code>JTextField</code> to the parameter panel.
	 * 
	 * @param label a label for the text field
	 * @param width a width for the text field
	 */
	public void addTextOption(final String label, final int width) {
		textField.put(label, new JTextField(width));
	}

	/**
	 * Adds a component to the parameter panel.
	 * 
	 * @param parameter a parameter enum for the component
	 * @param component a component
	 */
	public void addCustomOption(final Parameter parameter, final JComponent component) {
		addCustomOption(parameter.getDescription(), component);
	}

	/**
	 * Adds a component to the parameter panel. Only supports FilesHistoryPanel, JRadioButton, and JCheckBox components.
	 * 
	 * @param label a label for the component
	 * @param component a component
	 */
	public void addCustomOption(final String label, final JComponent component) {

		if (component instanceof FilesHistoryPanel || component instanceof JRadioButton
				|| component instanceof JCheckBox)
			customField.put(label, component);
		else
			throw new MassCascadeException("Dialog component not supported: " + component.getClass());
	}

	/**
	 * Adds a boolean table to the parameter panel with the selected objects.
	 * 
	 * @param parameter the parameter for the table
	 * @param objects the objects and their state
	 */
	public void addBooleanTable(final Parameter parameter, Map<String, Boolean> objects) {
		addBooleanTable(parameter.getDescription(), objects);
	}

	/**
	 * Adds a boolean table to the parameter panel with the selected objects.
	 * 
	 * @param label the label for the table
	 * @param objects the objects and their state
	 */
	public void addBooleanTable(final String label, Map<String, Boolean> objects) {

		BooleanTableModel tableModel = new BooleanTableModel(objects);
		JTable table = new JTable(tableModel);
		JScrollPane tableScrollPane = new JScrollPane(table);
		tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tableScrollPane.setPreferredSize(new Dimension(250, 250));
		table.setFillsViewportHeight(true);

		scrollPane.put(label, tableScrollPane);
	}

	public void addOptionalColumnPanel(final String label, final Class<? extends DataValue> cellValue) {
		columnPanels.add(new OptionalColumnPanel(label, cellValue));
	}

	/**
	 * Builds a panel containing all node settings.
	 * 
	 * @return the panel
	 */
	public DefaultDialog build() {

		JPanel panel = new JPanel(new GridLayout(2, 1));

		JPanel columnPanel = buildColumnSelection();
		JPanel optionPanel = buildTextandCustomOption();

		JScrollPane columnSPane = new JScrollPane(columnPanel);
		columnSPane.setBorder(null);
		JScrollPane optionSPane = new JScrollPane(optionPanel);
		optionSPane.setBorder(null);

		panel.add(columnSPane);
		panel.add(optionSPane);

		panel.setPreferredSize(new Dimension(400, 300));
		this.addTab("Settings", panel);

		return this;
	}

	/**
	 * Builds a panel with column selection options.
	 * 
	 * @return the panel
	 */
	private JPanel buildColumnSelection() {

		JPanel columnPanel = new JPanel(new GridBagLayout());
		columnPanel.setBorder(BorderFactory.createTitledBorder("Source"));

		c.gridx = 0;
		c.gridy = 0;

		c.insets = new Insets(0, 0, 5, 0);

		for (String label : comboBox.keySet()) {

			columnPanel.add(new JLabel(label + PADDING), c);
			c.gridx++;
			columnPanel.add(comboBox.get(label), c);
			c.gridx = 0;
			c.gridy++;
		}
		
		c.insets = new Insets(0, 0, 2, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		for (OptionalColumnPanel singleColPanel : columnPanels) {
			
			columnPanel.add(singleColPanel, c);
			c.gridy++;
		}
		
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 0, 0);

		return columnPanel;
	}

	/**
	 * Builds a panel with parameter options.
	 * 
	 * @return the panel
	 */
	private JPanel buildTextandCustomOption() {

		JPanel optionPanel = new JPanel(new GridBagLayout());
		optionPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));

		c.gridx = 0;
		c.gridy = 0;

		for (String label : textField.keySet()) {

			optionPanel.add(new JLabel(label + PADDING), c);
			c.gridx++;
			optionPanel.add(textField.get(label), c);
			c.gridx = 0;
			c.gridy++;
		}

		for (String label : customField.keySet()) {

			optionPanel.add(new JLabel(label + PADDING), c);
			c.gridx++;
			optionPanel.add(customField.get(label), c);
			c.gridx = 0;
			c.gridy++;
		}

		c.insets = new Insets(0, 0, 0, 0);
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;

		c.gridwidth = 2;
		for (String label : scrollPane.keySet()) {

			optionPanel.add(scrollPane.get(label), c);
			c.gridy++;
		}
		c.gridwidth = 1;

		c.insets = new Insets(10, 0, 0, 0);
		optionPanel.add(new JLabel("Retain Data (Loop)" + PADDING), c);
		c.gridx++;
		optionPanel.add(loopTerminus, c);
		c.gridx = 0;
		c.gridy++;

		return optionPanel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
			throws NotConfigurableException {

		try {
			this.settings.loadSettings(settings);
		} catch (InvalidSettingsException exception) {
			// do nothing
		}

		for (String label : comboBox.keySet())
			comboBox.get(label).update(specs[comboBoxSpec.get(label)], this.settings.getColumnName(label));

		for (String label : textField.keySet())
			textField.get(label).setText(this.settings.getTextOption(label));

		for (String label : customField.keySet()) {
			if (customField.get(label) instanceof JRadioButton) {
				((JRadioButton) customField.get(label)).setSelected(this.settings.getBooleanOption(label));
			} else if (customField.get(label) instanceof FilesHistoryPanel) {
				((FilesHistoryPanel) customField.get(label)).setSelectedFile(this.settings.getTextOption(label));
			} else if (customField.get(label) instanceof JCheckBox) {
				((JCheckBox) customField.get(label)).setSelected(this.settings.getBooleanOption(label));
			}
		}

		for (String label : scrollPane.keySet()) {
			List<String> selected = new ArrayList<>();
			for (String object : this.settings.getStringArrayOption(label))
				selected.add(object);

			JTable table = (JTable) scrollPane.get(label).getViewport().getView();
			((BooleanTableModel) table.getModel()).update(selected);
		}
		
		for (OptionalColumnPanel singleColPanel : columnPanels) {
			String colName = this.settings.getTextOption(singleColPanel.getDescription());
			if (colName != null && colName.isEmpty()) singleColPanel.setIgnore(specs[0], true);
			else {
				singleColPanel.setIgnore(specs[0], false);
				singleColPanel.setColumnName(specs[0], colName);
			}
		}

		loopTerminus.setSelected(this.settings.getBooleanOption(TERMINUS));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

		for (String label : comboBox.keySet())
			this.settings.setColumnName(label, comboBox.get(label).getSelectedColumn());

		for (String label : textField.keySet())
			this.settings.setTextOption(label, textField.get(label).getText());

		for (String label : customField.keySet()) {
			if (customField.get(label) instanceof JRadioButton) {
				this.settings.setTextOption(label, ((JRadioButton) customField.get(label)).isSelected() + "");
			} else if (customField.get(label) instanceof FilesHistoryPanel) {
				this.settings.setTextOption(label, ((FilesHistoryPanel) customField.get(label)).getSelectedFile());
			} else if (customField.get(label) instanceof JCheckBox) {
				this.settings.setTextOption(label, ((JCheckBox) customField.get(label)).isSelected() + "");
			}
		}

		for (String label : scrollPane.keySet()) {
			JTable table = (JTable) scrollPane.get(label).getViewport().getView();
			String[] selectedObjects = ((BooleanTableModel) table.getModel()).getSelected().toArray(new String[] {});
			this.settings.setStringArrayOption(label, selectedObjects);
		}
		
		for (OptionalColumnPanel singleColPan : columnPanels) {
			if (singleColPan.isIgnore()) this.settings.setTextOption(singleColPan.getDescription(), "");
			else this.settings.setTextOption(singleColPan.getDescription(), singleColPan.getColumnName());
		}

		this.settings.setTextOption(TERMINUS, loopTerminus.isSelected() + "");
		this.settings.saveSettings(settings);
	}
}
