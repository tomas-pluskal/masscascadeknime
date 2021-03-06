/*
 * Copyright (C) 2013 EMBL - European Bioinformatics Institute
 * 
 * All rights reserved. This file is part of the MassCascade feature for KNIME.
 * 
 * The feature is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version.
 * 
 * The feature is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * the feature. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *    Stephan Beisken - initial API and implementation
 */
package uk.ac.ebi.masscascade.knime.io.read;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import uk.ac.ebi.masscascade.parameters.Constants;

/**
 * <code>NodeDialog</code> for the "MzFileReader" Node. File reader node for Thermo RAW and PSI mzML mass spectrometry
 * files.
 * 
 * @author Stephan Beisken
 */
public class MsFileReaderNodeDialog extends NodeDialogPane {

	private final JFileChooser fileChooser = new JFileChooser();
	private final JTextField filePath = new JTextField(30);
	private final MsFileReaderSettings settings = new MsFileReaderSettings();
//	private final JCheckBox retainData = new JCheckBox();
	private final JPanel dialogPanel = new JPanel(new GridLayout(2, 1));
	static final FilenameFilter FILTER = new FilenameFilter() {

		@Override
		public boolean accept(final File file, final String name) {

			String s = name.toUpperCase();
			if (s.endsWith(Constants.FILE_FORMATS.MZML.name()) || s.endsWith(Constants.FILE_FORMATS.RAW.name()) ||
					s.endsWith(Constants.FILE_FORMATS.CDF.name())) {
				return true;
			}
			return false;
		}
	};

	/**
	 * Creates a new new dialog.
	 */
	public MsFileReaderNodeDialog() {

		JPanel i = new JPanel(new GridBagLayout());
		i.setBorder(BorderFactory.createTitledBorder("Source"));
		JPanel r = new JPanel(new GridBagLayout());
		r.setBorder(BorderFactory.createTitledBorder("Parameter"));

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		i.add(new JLabel("Select directory or (multiple) files:"), c);
		c.gridy++;
		i.add(filePath, c);
		filePath.setText(settings.filePath());
		filePath.setEditable(false);

		c.gridx = 0;
		c.gridy++;

		JButton browse = new JButton("Browse");
		browse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				int res = fileChooser.showOpenDialog(MsFileReaderNodeDialog.this.getPanel());
				if (res == JFileChooser.APPROVE_OPTION) {
					File[] files = fileChooser.getSelectedFiles();

					if (files.length == 1 && files[0].isDirectory())
						files = files[0].listFiles(FILTER);

					String selFile = fileChooser.getSelectedFile().getAbsolutePath();
					if (selFile.lastIndexOf(".") < 0) {
						filePath.setText(selFile);
					} else {
						filePath.setText(selFile.substring(0, selFile.lastIndexOf(".")));
					}
				}
			}
		});

		i.add(browse, c);

//		c.gridy = 0;
//		c.gridx = 0;
//		c.insets = new Insets(10, 0, 0, 0);
//		c.gridx = 0;
//		r.add(new JLabel("Retain Data (Loop)   "), c);
//		c.gridx++;
//		r.add(retainData, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		c.gridx = 0;
		dialogPanel.add(i);
		c.gridy++;
		dialogPanel.add(r);

		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File file) {

				if (file.isDirectory())
					return true;
				String fileName = file.getName();
				int i = fileName.lastIndexOf('.');

				if (i > 0 && i < fileName.length() - 1)
					if (fileName.substring(i + 1).toUpperCase().equals(Constants.FILE_FORMATS.RAW.name())
							|| fileName.substring(i + 1).toUpperCase().equals(Constants.FILE_FORMATS.MZML.name())
							|| fileName.substring(i + 1).toUpperCase().equals(Constants.FILE_FORMATS.CDF.name()))
						return true;

				return false;
			}

			public String getDescription() {

				String wildcard = "*.";
				return wildcard + Constants.FILE_FORMATS.RAW.name().toLowerCase() + ", " + wildcard
						+ Constants.FILE_FORMATS.MZML.name().toLowerCase() + ", " + wildcard
						+ Constants.FILE_FORMATS.CDF.name().toLowerCase();
			}
		});

		addTab("Settings", dialogPanel);
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

		filePath.setText(this.settings.filePath());
		fileChooser.setSelectedFiles(this.settings.files());
		fileChooser.setCurrentDirectory(new File(this.settings.filePath()));
//		retainData.setSelected(this.settings.retainData());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

		this.settings.files(fileChooser.getSelectedFiles());
		this.settings.filePath(filePath.getText());
//		this.settings.retainData(retainData.isSelected());

		this.settings.saveSettings(settings);
	}
}
