package tech.KPIs;

import tech.WareSkladInit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

public class KPIViewer extends JFrame {

    private final JTextArea descriptionArea;
    private final JTextArea resultsArea;
    private WareSkladInit jmeScene;
    private ResourceBundle bundle;

    public void setJmeScene(WareSkladInit jmeScene){
        this.jmeScene = jmeScene;
    }

    public KPIViewer(ResourceBundle bundle) {
        this.bundle = bundle;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int frameWidth = screenSize.width / 2;
        int frameHeight = (int) (screenSize.height * 2 / 3.0);
        setSize(frameWidth, frameHeight);
        setTitle(bundle.getString("kpiViewer"));

        setLayout(new GridLayout(1, 2));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("kpiOptions")));
        JScrollPane scrollPane = new JScrollPane();
        JPanel kpiOptionsPanel = new JPanel();
        kpiOptionsPanel.setLayout(new BoxLayout(kpiOptionsPanel, BoxLayout.Y_AXIS));

        JButton storageUtilisationButton = new JButton(bundle.getString("storageUtilisation"));
        storageUtilisationButton.addActionListener(this::handleStorageSpaceUtilisation);
        kpiOptionsPanel.add(new JLabel(bundle.getString("spaceUtilisationKPI")));
        kpiOptionsPanel.add(storageUtilisationButton);

        scrollPane.setViewportView(kpiOptionsPanel);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("kpiDetails")));

        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createTitledBorder(bundle.getString("description")));

        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("results")));
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsPanel.add(resultsArea, BorderLayout.CENTER);

        rightPanel.add(descriptionScrollPane, BorderLayout.CENTER);
        rightPanel.add(resultsPanel, BorderLayout.SOUTH);

        add(leftPanel);
        add(rightPanel);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void handleStorageSpaceUtilisation(ActionEvent e) {
        StorageSpaceUtilisation storageKPI = new StorageSpaceUtilisation(jmeScene, bundle);
        descriptionArea.setText(storageKPI.getTitle() + "\n\n" + storageKPI.getDescription());
        resultsArea.setText(storageKPI.getResults());
    }
}
