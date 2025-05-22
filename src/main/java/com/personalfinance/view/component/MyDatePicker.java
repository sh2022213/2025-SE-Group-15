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
 * <p>
 * Combines a spinner for manual date input with a popup calendar view
 * for selecting dates visually.
 */
public class MyDatePicker extends JPanel {
    private JSpinner dateSpinner;
    private JButton popupButton;
    private JPopupMenu popupMenu;
    private MyMonthView monthView;
    private SimpleDateFormat dateFormat;

    /**
     * Constructs a date picker initialized with the current date.
     */
    public MyDatePicker() {
        this(new Date());
    }

    /**
     * Constructs a date picker initialized with a specified date.
     *
     * @param initialDate the initial selected date
     */
    public MyDatePicker(Date initialDate) {
        this(initialDate, "yyyy-MM-dd");
    }

    /**
     * Constructs a date picker with a specified initial date and date format pattern.
     *
     * @param initialDate        the initial selected date
     * @param dateFormatPattern  the date format pattern (e.g. "yyyy-MM-dd")
     */
    public MyDatePicker(Date initialDate, String dateFormatPattern) {
        this.dateFormat = new SimpleDateFormat(dateFormatPattern);
        initComponents(initialDate);
    }

    /**
     * Initializes UI components and layout.
     *
     * @param initialDate the date to initialize the picker with
     */
    private void initComponents(Date initialDate) {
        setLayout(new BorderLayout());

        // Create the date spinner with day precision
        SpinnerDateModel model = new SpinnerDateModel(
                initialDate, null, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(model);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, dateFormat.toPattern()));
        dateSpinner.addChangeListener(e -> firePropertyChange("date", null, getDate()));

        // Create the popup button to show calendar
        popupButton = new JButton("▼");
        popupButton.setMargin(new Insets(0, 2, 0, 2));
        popupButton.addActionListener(e -> showPopup());

        // Create the custom month view calendar
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

        // Create the popup menu and add the calendar to it
        popupMenu = new JPopupMenu();
        popupMenu.add(monthView);
        popupMenu.setPopupSize(new Dimension(300, 200));

        // Add components to this panel
        add(dateSpinner, BorderLayout.CENTER);
        add(popupButton, BorderLayout.EAST);
    }

    /**
     * Shows the date selection popup menu.
     */
    private void showPopup() {
        popupMenu.show(this, 0, getHeight());
        monthView.setSelectionDate(getDate());
    }

    /**
     * Returns the currently selected date.
     *
     * @return the selected date
     */
    public Date getDate() {
        return ((SpinnerDateModel) dateSpinner.getModel()).getDate();
    }

    /**
     * Sets the currently selected date.
     *
     * @param date the date to set
     */
    public void setDate(Date date) {
        Date oldDate = getDate();
        dateSpinner.setValue(date);
        firePropertyChange("date", oldDate, date);
    }

    /**
     * Sets the date format used by the spinner editor.
     *
     * @param dateFormatPatterns the date format patterns (only first used)
     */
    public void setFormats(String... dateFormatPatterns) {
        if (dateFormatPatterns != null && dateFormatPatterns.length > 0) {
            this.dateFormat = new SimpleDateFormat(dateFormatPatterns[0]);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, dateFormatPatterns[0]);
            dateSpinner.setEditor(editor);
        }
    }

    /**
     * Adds a listener to be notified when the date changes.
     *
     * @param listener the ChangeListener to add
     */
    public void addDateChangeListener(ChangeListener listener) {
        dateSpinner.addChangeListener(listener);
    }

    /**
     * Removes a date change listener.
     *
     * @param listener the ChangeListener to remove
     */
    public void removeDateChangeListener(ChangeListener listener) {
        dateSpinner.removeChangeListener(listener);
    }

    /**
     * Inner class for the month view calendar panel.
     */
    private static class MyMonthView extends JPanel {
        private Date selectionDate;
        private Date[][] monthDays;
        private int currentYear;
        private int currentMonth;

        /**
         * Constructs the month view initialized to the current month and year.
         */
        public MyMonthView() {
            setLayout(new GridLayout(0, 7));
            setPreferredSize(new Dimension(300, 200));
            Calendar cal = Calendar.getInstance();
            this.currentYear = cal.get(Calendar.YEAR);
            this.currentMonth = cal.get(Calendar.MONTH);
            updateMonthView();
        }

        /**
         * Updates the month view UI, including day buttons and weekday headers.
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

            // Calculate the first day of the week and number of days in month
            int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Fill blank labels for days before first day of month
            for (int i = 1; i < firstDayOfWeek; i++) {
                add(new JLabel(""));
            }

            // Create day buttons for each day of the month
            monthDays = new Date[6][7];
            int week = 0;
            for (int day = 1; day <= daysInMonth; day++) {
                cal.set(Calendar.DAY_OF_MONTH, day);
                Date date = cal.getTime();
                monthDays[week][(firstDayOfWeek + day - 2) % 7] = date;

                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setMargin(new Insets(1, 1, 1, 1));
                dayButton.addActionListener(e -> setSelectionDate(date));

                // Highlight the currently selected date
                if (selectionDate != null && isSameDay(date, selectionDate)) {
                    dayButton.setBackground(new Color(200, 200, 255));
                }

                // Highlight today's date
                if (isSameDay(date, new Date())) {
                    dayButton.setForeground(Color.RED);
                }

                add(dayButton);

                // Move to next week row if this day is Saturday
                if ((firstDayOfWeek + day - 1) % 7 == 0) {
                    week++;
                }
            }

            revalidate();
            repaint();
        }

        /**
         * Sets the selected date and updates the view.
         *
         * @param date the date to select
         */
        public void setSelectionDate(Date date) {
            Date oldDate = this.selectionDate;
            this.selectionDate = date;
            firePropertyChange("selectionDate", oldDate, date);
            updateMonthView();
        }

        /**
         * Returns the currently selected date.
         *
         * @return the selected date
         */
        public Date getSelectionDate() {
            return selectionDate;
        }

        /**
         * Navigates to the previous month and updates the view.
         */
        public void previousMonth() {
            if (currentMonth == 0) {
                currentMonth = 11;
                currentYear--;
            } else {
                currentMonth--;
            }
            updateMonthView();
        }

        /**
         * Navigates to the next month and updates the view.
         */
        public void nextMonth() {
            if (currentMonth == 11) {
                currentMonth = 0;
                currentYear++;
            } else {
                currentMonth++;
            }
            updateMonthView();
        }

        /**
         * Checks if two dates are on the same calendar day.
         *
         * @param date1 the first date to compare
         * @param date2 the second date to compare
         * @return true if both dates are the same day, false otherwise
         */
        private boolean isSameDay(Date date1, Date date2) {
            if (date1 == null || date2 == null) {
                return false;
            }
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
