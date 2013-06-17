package uk.ac.ebi.masscascade.knime.curation.bless;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import uk.ac.ebi.masscascade.bless.table.BlessPane;
import uk.ac.ebi.masscascade.bless.table.BlessTable;
import uk.ac.ebi.masscascade.bless.table.BlessTableModel;
import uk.ac.ebi.masscascade.compound.CompoundSpectrum;

public class BlessFrame {

	private final JTable table;

	private final JFrame frame = new JFrame();
	private static Object lock = new Object();

	public BlessFrame(List<CompoundSpectrum> cs, String id) {

		frame.setSize(1280, 720);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		table = new BlessTable(cs);
		JScrollPane pane = new BlessPane(table);
		frame.add(pane);

		JPanel donePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 200, 0));
		JLabel doneLabel = new JLabel(id);
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (lock) {
					frame.setVisible(false);
					lock.notify();
				}
			}
		});
		donePanel.add(doneLabel);
		donePanel.add(doneButton);
		frame.add(donePanel, BorderLayout.SOUTH);
	}

	public void setVisible() throws Exception {

		frame.setVisible(true);

		Thread t = new Thread() {

			public void run() {
				synchronized (lock) {
					while (frame.isVisible())
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
			}
		};
		t.start();

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				synchronized (lock) {
					frame.setVisible(false);
					lock.notify();
				}
			}

		});

		t.join();
	}

	public Map<Integer, Integer> getIdToEntity() {
		return ((BlessTableModel) table.getModel()).getIdToEntityIndex();
	}
}
