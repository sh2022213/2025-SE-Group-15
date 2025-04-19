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
 * 自定义日期和选择器组件
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

    private void initComponents(Date initialDate) {
        setLayout(new BorderLayout());

        // 创建日期微调器
        SpinnerDateModel model = new SpinnerDateModel(
                initialDate, null, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(model);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, dateFormat.toPattern()));
        dateSpinner.addChangeListener(e -> firePropertyChange("date", null, getDate()));

        // 创建弹出按钮
        popupButton = new JButton("▼");
        popupButton.setMargin(new Insets(0, 2, 0, 2));
        popupButton.addActionListener(e -> showPopup());

        // 创建月份视图
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

        // 创建弹出菜单
        popupMenu = new JPopupMenu();
        popupMenu.add(monthView);
        popupMenu.setPopupSize(new Dimension(300, 200));

        // 添加组件
        add(dateSpinner, BorderLayout.CENTER);
        add(popupButton, BorderLayout.EAST);
    }

    /**
     * 显示日期选择弹出窗口
     */
    private void showPopup() {
        popupMenu.show(this, 0, getHeight());
        monthView.setSelectionDate(getDate());
    }

    /**
     * 获取当前选择的日期
     */
    public Date getDate() {
        return ((SpinnerDateModel) dateSpinner.getModel()).getDate();
    }

    /**
     * 设置日期
     */
    public void setDate(Date date) {
        Date oldDate = getDate();
        dateSpinner.setValue(date);
        firePropertyChange("date", oldDate, date);
    }

    /**
     * 设置日期格式
     */
    public void setFormats(String... dateFormatPatterns) {
        if (dateFormatPatterns != null && dateFormatPatterns.length > 0) {
            this.dateFormat = new SimpleDateFormat(dateFormatPatterns[0]);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, dateFormatPatterns[0]);
            dateSpinner.setEditor(editor);
        }
    }

    /**
     * 添加日期变化监听器
     */
    public void addDateChangeListener(ChangeListener listener) {
        dateSpinner.addChangeListener(listener);
    }

    /**
     * 移除日期变化监听器
     */
    public void removeDateChangeListener(ChangeListener listener) {
        dateSpinner.removeChangeListener(listener);
    }

    /**
     * 自定义月份视图组件
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
         * 更新月份视图
         */
        private void updateMonthView() {
            removeAll();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, currentYear);
            cal.set(Calendar.MONTH, currentMonth);
            cal.set(Calendar.DAY_OF_MONTH, 1);

            // 添加星期标题
            String[] weekDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            for (String day : weekDays) {
                JLabel label = new JLabel(day, SwingConstants.CENTER);
                label.setForeground(Color.BLUE);
                add(label);
            }

            // 计算当月天数
            int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            // 填充空白
            for (int i = 1; i < firstDayOfWeek; i++) {
                add(new JLabel(""));
            }

            // 添加日期按钮
            monthDays = new Date[6][7];
            int week = 0;
            for (int day = 1; day <= daysInMonth; day++) {
                cal.set(Calendar.DAY_OF_MONTH, day);
                Date date = cal.getTime();
                monthDays[week][(firstDayOfWeek + day - 2) % 7] = date;

                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setMargin(new Insets(1, 1, 1, 1));
                dayButton.addActionListener(e -> setSelectionDate(date));

                // 高亮当前选择日期
                if (selectionDate != null && isSameDay(date, selectionDate)) {
                    dayButton.setBackground(new Color(200, 200, 255));
                }

                // 高亮今天
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
         * 设置选择的日期
         */
        public void setSelectionDate(Date date) {
            Date oldDate = this.selectionDate;
            this.selectionDate = date;
            firePropertyChange("selectionDate", oldDate, date);
            updateMonthView();
        }

        /**
         * 获取选择的日期
         */
        public Date getSelectionDate() {
            return selectionDate;
        }

        /**
         * 导航到上个月
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
         * 导航到下个月
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
         * 检查两个日期是否是同一天
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
