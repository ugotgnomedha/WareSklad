package ui;

import UndoRedo.UndoManager;
import tech.tags.Tag;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

public class TagsUI {

    private final ResourceBundle bundle;
    private UndoManager undoManager;
    private PropertiesPanel propertiesPanel;

    public TagsUI(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public void setPropertiesPanel(PropertiesPanel propertiesPanel) {
        this.propertiesPanel = propertiesPanel;
    }

    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    public JMenu createTagsMenu(JFrame frame) {
        JMenu tagsMenu = new JMenu(bundle.getString("tagsMenu"));

        JMenuItem editTagsItem = new JMenuItem(bundle.getString("editTags"));
        editTagsItem.addActionListener(e -> showEditTagsFrame(frame));
        tagsMenu.add(editTagsItem);

        return tagsMenu;
    }

    private void showEditTagsFrame(JFrame parent) {
        JFrame editTagsFrame = new JFrame(bundle.getString("editTagsTitle"));
        editTagsFrame.setSize(
                Toolkit.getDefaultToolkit().getScreenSize().width / 2,
                Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3
        );
        editTagsFrame.setLocationRelativeTo(null);
        editTagsFrame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        JLabel tagNameLabel = new JLabel(bundle.getString("tagName"));
        JTextField tagNameField = new JTextField(20);
        JButton saveTagButton = new JButton(bundle.getString("addTag"));
        saveTagButton.setEnabled(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(tagNameLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(tagNameField, gbc);

        JLabel colorPreview = new JLabel();
        JComboBox<String> tagsDropdown = new JComboBox<>(getTagNamesArray());

        JButton colorPickerButton = new JButton(bundle.getString("pickColor"));
        colorPickerButton.setEnabled(false);

        colorPreview.setOpaque(true);
        colorPreview.setBackground(Color.WHITE);
        colorPreview.setPreferredSize(new Dimension(20, 20));

        JCheckBox enableCustomColorCheckBox = new JCheckBox(bundle.getString("enableCustomColor"));
        enableCustomColorCheckBox.addActionListener(e -> colorPickerButton.setEnabled(enableCustomColorCheckBox.isSelected()));

        colorPickerButton.addActionListener(e -> {
            if (enableCustomColorCheckBox.isSelected()) {
                Color selectedColor = JColorChooser.showDialog(editTagsFrame, bundle.getString("pickColorTitle"), colorPreview.getBackground());
                if (selectedColor != null) {
                    colorPreview.setBackground(selectedColor);
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(colorPickerButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(colorPreview, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        mainPanel.add(enableCustomColorCheckBox, gbc);

        JLabel currentTagsLabel = new JLabel(bundle.getString("currentTags"));
        JButton deleteTagButton = new JButton(bundle.getString("deleteTag"));
        deleteTagButton.setEnabled(false);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(currentTagsLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(tagsDropdown, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        mainPanel.add(deleteTagButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(saveTagButton, gbc);

        editTagsFrame.add(mainPanel);

        tagNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateTagName(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateTagName(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateTagName(); }

            private void validateTagName() {
                String text = tagNameField.getText().trim();
                boolean tagExists = getTagByName(text) != null;

                if (!text.isEmpty()) {
                    saveTagButton.setEnabled(true);
                    saveTagButton.setText(tagExists ? bundle.getString("updateTag") : bundle.getString("addTag"));
                } else {
                    saveTagButton.setEnabled(false);
                }
            }
        });

        tagsDropdown.addActionListener(e -> {
            String selectedTagName = (String) tagsDropdown.getSelectedItem();
            if (selectedTagName != null && !selectedTagName.equals("---")) {
                Tag selectedTag = getTagByName(selectedTagName);
                if (selectedTag != null) {
                    tagNameField.setText(selectedTag.getName());
                    colorPreview.setBackground(selectedTag.getColor());
                    enableCustomColorCheckBox.setSelected(!selectedTag.getColor().equals(Color.WHITE));
                    colorPickerButton.setEnabled(enableCustomColorCheckBox.isSelected());
                    saveTagButton.setText(bundle.getString("updateTag"));
                    saveTagButton.setEnabled(true);
                }
            } else {
                tagNameField.setText("");
                colorPreview.setBackground(Color.WHITE);
                enableCustomColorCheckBox.setSelected(false);
                colorPickerButton.setEnabled(false);
                saveTagButton.setText(bundle.getString("addTag"));
                saveTagButton.setEnabled(false);
            }
        });

        saveTagButton.addActionListener(e -> {
            String tagName = tagNameField.getText().trim();
            Color tagColor = colorPreview.getBackground();
            Tag existingTag = getTagByName(tagName);

            if (existingTag != null) {
                existingTag.setColor(tagColor);
            } else {
                Tag newTag = new Tag(tagName, true, tagColor);
                undoManager.addTag(newTag);
            }

            tagNameField.setText("");
            refreshTagDropdown(tagsDropdown);
            propertiesPanel.updateTagDropdown(undoManager.getTags());
            saveTagButton.setEnabled(false);
        });

        deleteTagButton.addActionListener(e -> {
            String selectedTag = (String) tagsDropdown.getSelectedItem();
            Tag tagToDelete = getTagByName(selectedTag);
            if (tagToDelete != null) {
                undoManager.deleteTag(tagToDelete);
                tagsDropdown.setSelectedIndex(0);
                refreshTagDropdown(tagsDropdown);
                propertiesPanel.updateTagDropdown(undoManager.getTags());
            }
        });

        editTagsFrame.setVisible(true);
    }

    private String[] getTagNamesArray() {
        List<Tag> tags = undoManager.getTags();
        String[] tagNames = new String[tags.size() + 1];
        tagNames[0] = "---";
        for (int i = 0; i < tags.size(); i++) {
            tagNames[i + 1] = tags.get(i).getName();
        }
        return tagNames;
    }

    private Tag getTagByName(String name) {
        for (Tag tag : undoManager.getTags()) {
            if (tag.getName().equals(name)) {
                return tag;
            }
        }
        return null;
    }

    private void refreshTagDropdown(JComboBox<String> tagsDropdown) {
        tagsDropdown.setModel(new DefaultComboBoxModel<>(getTagNamesArray()));
    }
}
