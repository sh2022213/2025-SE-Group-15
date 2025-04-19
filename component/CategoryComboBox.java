package com.personalfinance.view.component;

import javax.swing.*;
import java.util.List;

public class CategoryComboBox extends JComboBox<String> {
    public CategoryComboBox(List<String> categories) {
        super(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
        setEditable(true);
        setSelectedIndex(-1);
    }

    public void updateCategories(List<String> categories) {
        removeAllItems();
        for (String category : categories) {
            addItem(category);
        }
    }
}
