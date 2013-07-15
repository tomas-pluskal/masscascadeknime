package uk.ac.ebi.masscascade.knime.defaults.preview;

import javax.swing.JFrame;

import org.knime.core.node.BufferedDataTable;

import uk.ac.ebi.masscascade.knime.defaults.Settings;


public class PreviewFrame extends JFrame {

	public PreviewFrame(BufferedDataTable[] input, Settings settings) {
		
		setSize(300, 150);
		setName("Parameter Preview");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
//		TraceBuilderNodeModel model = new TraceBuilderNodeFactory().createNodeModel();
	}
}
