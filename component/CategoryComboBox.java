package com.personalfinance.view.component;

import javax.swing.*;
import java.util.List;

/**
 * A custom combo box component for displaying and selecting financial categories.
 * Supports editable entries and dynamic category updates.
 */
public class CategoryComboBox extends JComboBox<String> {

    /**
     * Constructs a CategoryComboBox with the given list of categories.
     *
     * @param categories the list of category names to display initially
     */
    public CategoryComboBox(List<String> categories) {
        // Initialize with the provided categories
        super(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
        
        // Make the combo box editable and clear any initial selection
        setEditable(true);
        setSelectedIndex(-1);  // No initial selection
    }

    /**
     * Updates the available categories in the combo box.
     *
     * @param categories the new list of category names to display
     */
    public void updateCategories(List<String> categories) {
        // Remove all existing items
        removeAllItems();
        
        // Add all new categories
        for (String category : categories) {
            addItem(category);
        }
    }
}