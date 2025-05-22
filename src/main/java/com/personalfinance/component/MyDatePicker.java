package com.personalfinance.view.component;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Custom date picker component.
 */
public class MyDatePicker extends JPanel {
    private JSpinner         dateSpinner;
    private JButton          popupButton;
    private JPopupMenu       popupMenu;
    private MyMonthView      monthView;
    private SimpleDateFormat dateFormat;

    public MyDatePicker() {
        this(new Date());
    }

    public MyDatePicker(Date initialDate) {
        this(initialDate, "yyyy-MM-dd");
    }

    public MyDatePicker(Date initialDate, String dateFormatPattern) {
        this.dateFormat = new SimpleDateFormat(dateFormatPattern);
        initComponents(initialDate);
    }

    /**
     * Initializes components of the date picker.
     */
    private void initComponents(Date initialDate) {
        setLayout(new BorderLayout());

        // Create date spinner
        SpinnerDateModel model = new SpinnerDateModel(
                initialDate, null, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(model);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, dateFormat.toPattern()));
        dateSpinner.addChangeListener(e -> firePropertyChange("date", null, getDate()));

        // Create popup button
        popupButton = new JButton("▼");
        popupButton.setMargin(new Insets(0, 2, 0, 2));
        popupButton.addActionListener(e -> showPopup());

        // Create month view
        monthView = new MyMonthView();
        monthView.setSelectionDate(getDate());
        monthView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Date selected = monthView.getSelectionDate();
                if (selected != null) {
                    setDate(selected);
                    popupMenu.setVisible(false);
                }
            }
        });

        // Create popup menu
        popupMenu = new JPopupMenu();
        popupMenu.add(monthView);
        popupMenu.setPopupSize(new Dimension(300, 200));

        // Add components
        add(dateSpinner, BorderLayout.CENTER);
        add(popupButton, BorderLayout.EAST);
    }

    /**
     * Displays the date selection popup.
     */
    private void showPopup() {
        popupMenu.show(this, 0, getHeight());
        monthView.setSelectionDate(getDate());
    }

    /**
     * Returns the currently selected date.
     */
    public Date getDate() {
        return ((SpinnerDateModel) dateSpinner.getModel()).getDate();
    }

    /**
     * Sets the selected date.
     */
    public void setDate(Date date) {
        Date oldDate = getDate();
        dateSpinner.setValue(date);
        firePropertyChange("date", oldDate, date);
    }

    /**
     * Sets the date format.
     */
    public void setFormats(String... dateFormatPatterns) {
        if (dateFormatPatterns != null && dateFormatPatterns.length > 0) {
            this.dateFormat = new SimpleDateFormat(dateFormatPatterns[0]);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, dateFormatPatterns[0]);
            dateSpinner.setEditor(editor);
        }
    }

    /**
     * Adds a date change listener.
     */
    public void addDateChangeListener(ChangeListener listener) {
        dateSpinner.addChangeListener(listener);
    }

    /**
     * Removes a date change listener.
     */
    public void removeDateChangeListener(ChangeListener listener) {
        dateSpinner.removeChangeListener(listener);
    }

    /**
     * Custom month view component.
     */
    private static class MyMonthView extends JPanel {
        private Date     selectionDate;
        private Date[][] monthDays;
        private int      currentYear;
        private int      currentMonth;

        public MyMonthView() {
            setLayout(new GridLayout(0, 7));
            setPreferredSize(new Dimension(300, 200));
            Calendar cal = Calendar.getInstance();
            this.currentYear = cal.get(Calendar.YEAR);
            this.currentMonth = cal.get(Calendar.MONTH);
            updateMonthView();
        }

        /**
         * Updates the month view display.
         */
        private void updateMonthView() {
            removeAll();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, currentYear);
            cal.set(Calendar.MONTH, currentMonth);
            cal.set(Calendar.DAY_OF_MONTH, 1);

            // Add weekday headers
            String[] weekDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            for (String day : weekDays) {
                JLabel label = new JLabel(day, SwingConstants.CENTER);
                label.setForeground(Color.BLUE);
                add(label);
            }

            // Calculate number of days in the month
            int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Fill initial empty slots
            for (int i = 1; i < firstDayOfWeek; i++) {
                add(new JLabel(""));
            }

            // Add date buttons
            monthDays = new Date[6][7];
            int week = 0;
            for (int day = 1; day <= daysInMonth; day++) {
                cal.set(Calendar.DAY_OF_MONTH, day);
                Date date = cal.getTime();
                monthDays[week][(firstDayOfWeek + day - 2) % 7] = date;

                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setMargin(new Insets(1, 1, 1, 1));
                dayButton.addActionListener(e -> setSelectionDate(date));

                // Highlight selected date
                if (selectionDate != null && isSameDay(date, selectionDate)) {
                    dayButton.setBackground(new Color(200, 200, 255));
                }

                // Highlight today's date
                if (isSameDay(date, new Date())) {
                    dayButton.setForeground(Color.RED);
                }

                add(dayButton);

                if ((firstDayOfWeek + day - 1) % 7 == 0) {
                    week++;
                }
            }

            revalidate();
            repaint();
        }

        /**
         * Sets the selected date.
         */
        public void setSelectionDate(Date date) {
            Date oldDate = this.selectionDate;
            this.selectionDate = date;
            firePropertyChange("selectionDate", oldDate, date);
            updateMonthView();
        }

        /**
         * Returns the selected date.
         */
        public Date getSelectionDate() {
            return selectionDate;
        }

        /**
         * Checks whether two dates fall on the same day.
         */
        private boolean isSameDay(Date date1, Date date2) {
            if (date1 == null || date2 == null) { return false; }
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date1);
            cal2.setTime(date2);
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        }
    }
}
