package com.personalfinance.view.component;

import javax.swing.*;
import java.util.List;

/**
 * A custom combo box component for displaying and selecting categories.
 * 
 * This combo box is editable and allows dynamic updating of the category list.
 */
public class CategoryComboBox extends JComboBox<String> {

    /**
     * Constructs a CategoryComboBox with the provided list of category names.
     *
     * @param categories the initial list of category strings
     */
    public CategoryComboBox(List<String> categories) {
        super(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
        setEditable(true);       // Allow manual text input
        setSelectedIndex(-1);    // No selection by default
    }

    /**
     * Updates the combo box with a new list of category names.
     * 
     * Clears the existing items and repopulates the list.
     *
     * @param categories the new list of category strings
     */
    public void updateCategories(List<String> categories) {
        removeAllItems();
        for (String category : categories) {
            addItem(category);
        }
    }
}
