package uk.ac.ebi.masscascade.knime.io.reference;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.util.ColumnSelectionComboxBox;

public class OptionalColumnPanel extends JPanel {

	private String description;
	private JCheckBox columnIgnoreBox;
	private Class<? extends DataValue> cellValue;
	private ColumnSelectionComboxBox columnSelectionBox;

	public OptionalColumnPanel(String description, Class<? extends DataValue> cellValue) {

		this.cellValue = cellValue;
		this.description = description;
		columnIgnoreBox = new JCheckBox();
		columnSelectionBox = new ColumnSelectionComboxBox((Border) null, cellValue);

		columnIgnoreBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (columnIgnoreBox.isSelected())
					columnSelectionBox.setEnabled(false);
				else
					columnSelectionBox.setEnabled(true);
			}
		});

		init();
	}

	private void init() {

		this.setBorder(null);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0;
		c.gridy = 0;

		c.insets = new Insets(3, 0, 3, 5);

		this.add(new JLabel(description), c);
		c.gridx++;
		this.add(columnSelectionBox, c);
		c.gridx++;
		this.add(columnIgnoreBox, c);

	}

	public boolean isIgnore() {
		return columnIgnoreBox.isSelected();
	}

	public String getColumnName() {
		return columnSelectionBox.getSelectedColumn();
	}

	public String getDescription() {
		return description;
	}

	public void setIgnore(final DataTableSpec spec, boolean ignore) {
		columnIgnoreBox.setSelected(ignore);
		columnSelectionBox.setEnabled(!ignore);
		if (ignore)
			autoConfig(spec);
	}

	public void setColumnName(final DataTableSpec spec, String columnName) {
		try {
			columnSelectionBox.update(spec, columnName);
		} catch (Exception exception) {
			// do nothing
		}
	}

	private void autoConfig(DataTableSpec inSpec) {

		try {
			int col = -1;
			String name = "";

			if (col == -1) {
				int i = 0;
				for (DataColumnSpec dcs : inSpec) {
					if (dcs.getType().isCompatible(cellValue)) {
						col = i;
						break;
					}
					i++;
				}

				if (col != -1) {
					name = inSpec.getColumnSpec(col).getName();
					columnSelectionBox.update(inSpec, name);
				}
			}
		} catch (Exception exception) {
			// do nothing
		}
	}
}
