import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class TodoFrame extends Frame {
    private final User user;
    private final List<Task> tasks = new ArrayList<>();
    private final List<Task> filteredTasks = new ArrayList<>();
    private final java.awt.List taskList = new java.awt.List();
    private final TextField taskInput = new TextField();
    private final Label deadlineLabel = new Label("Deadline:");
    
    // AWT components for date selection
    private Choice dayChoice = new Choice();
    private Choice monthChoice = new Choice();
    private Choice yearChoice = new Choice();
    
    private final Checkbox doneCheckbox = new Checkbox("Done");
    private final Label totalTasksLabel = new Label("Total Tasks: 0");
    private final Label completedTasksLabel = new Label("Completed: 0");
    private final Label pendingTasksLabel = new Label("Pending: 0");
    private final Choice filterChoice = new Choice();
    private final Choice sortChoice = new Choice();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final String[] monthNames = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public TodoFrame(User user) {
        this.user = user;
        setTitle("To-Do List - " + user.username);
        setSize(700, 550);  // Make window wider to accommodate date components better
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        setLocationRelativeTo(null);
        setLayout(null);

        // Add top margin - shift everything down by 20px
        int topMargin = 20;

        // Filter/sort controls at the top - moved down
        Label filterLabel = new Label("Filter:");
        filterLabel.setBounds(20, topMargin + 20, 50, 25);
        add(filterLabel);
        
        filterChoice.add("All");
        filterChoice.add("Completed");
        filterChoice.add("Pending");
        filterChoice.add("Due Today");
        filterChoice.setBounds(70, topMargin + 20, 170, 25);
        add(filterChoice);
        
        Label sortLabel = new Label("Sort by:");
        sortLabel.setBounds(250, topMargin + 20, 60, 25);
        add(sortLabel);
        
        sortChoice.add("Sort by Deadline");
        sortChoice.add("Sort by Status");
        sortChoice.setBounds(310, topMargin + 20, 170, 25);
        add(sortChoice);
        
        // Stats labels below filter/sort - moved down
        Font boldFont = new Font("Dialog", Font.BOLD, 12);
        totalTasksLabel.setBounds(20, topMargin + 50, 150, 25);
        totalTasksLabel.setFont(boldFont);
        add(totalTasksLabel);

        completedTasksLabel.setBounds(170, topMargin + 50, 150, 25);
        completedTasksLabel.setFont(boldFont);
        add(completedTasksLabel);

        pendingTasksLabel.setBounds(320, topMargin + 50, 150, 25);
        pendingTasksLabel.setFont(boldFont);
        add(pendingTasksLabel);

        // Task list area - moved down
        taskList.setBounds(20, topMargin + 80, 650, 200);
        add(taskList);

        // Task input field - moved down
        taskInput.setBounds(20, topMargin + 290, 180, 25);
        add(taskInput);

        // Date components section - completely redesigned for better visibility
        
        // Deadline label with better positioning
        deadlineLabel.setBounds(210, topMargin + 290, 70, 25);
        deadlineLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        add(deadlineLabel);
        
        // Clear and recreate the choice components
        remove(dayChoice);
        remove(monthChoice);
        remove(yearChoice);
        
        // Create new day choice with more width
        Choice newDayChoice = new Choice();
        for (int i = 1; i <= 31; i++) {
            newDayChoice.add(String.valueOf(i));
        }
        newDayChoice.setBounds(285, topMargin + 290, 60, 25);
        add(newDayChoice);
        dayChoice = newDayChoice;
        
        // Create new month choice with more width
        Choice newMonthChoice = new Choice();
        for (String month : monthNames) {
            newMonthChoice.add(month);
        }
        newMonthChoice.setBounds(350, topMargin + 290, 80, 25);
        add(newMonthChoice);
        monthChoice = newMonthChoice;
        
        // Create new year choice with more width
        Choice newYearChoice = new Choice();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y <= currentYear + 10; y++) {
            newYearChoice.add(String.valueOf(y));
        }
        newYearChoice.setBounds(435, topMargin + 290, 85, 25);
        add(newYearChoice);
        yearChoice = newYearChoice;

        // Remove date labels as requested

        // Done checkbox - move further right
        doneCheckbox.setBounds(530, topMargin + 290, 70, 25);
        add(doneCheckbox);

        // Buttons - all left-aligned and moved down
        Button updateBtn = new Button("Update");
        updateBtn.setBounds(20, topMargin + 330, 100, 25);
        add(updateBtn);

        Button addBtn = new Button("Add");
        addBtn.setBounds(130, topMargin + 330, 80, 25);
        add(addBtn);

        Button deleteBtn = new Button("Delete");
        deleteBtn.setBounds(220, topMargin + 330, 80, 25);
        add(deleteBtn);

        // Load tasks and setup events
        loadTasks();

        // Add task selection listener
        taskList.addItemListener(e -> {
            int index = taskList.getSelectedIndex();
            if (index >= 0 && index < filteredTasks.size()) {
                Task selected = filteredTasks.get(index);
                taskInput.setText(selected.description);
                setDateSelection(selected.deadline);
                doneCheckbox.setState(selected.status == 1);
            }
        });
        
        // Filter and sort listeners
        filterChoice.addItemListener(e -> loadTasks());
        sortChoice.addItemListener(e -> loadTasks());

        // Add button action
        addBtn.addActionListener(e -> {
            String desc = taskInput.getText();
            String deadline = getSelectedDate();
            int status = doneCheckbox.getState() ? 1 : 0;
            if (!desc.isEmpty()) {
                try {
                    if (!deadline.isEmpty()) {
                        dateFormat.parse(deadline); // validate date format
                    }
                    try (Connection conn = DBHelper.getConnection()) {
                        PreparedStatement ps = conn.prepareStatement("INSERT INTO tasks(user_id, description, deadline, status) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, user.id);
                        ps.setString(2, desc);
                        ps.setString(3, deadline);
                        ps.setInt(4, status);
                        ps.executeUpdate();
                        loadTasks();
                        taskInput.setText("");
                        resetDateSelection();
                        doneCheckbox.setState(false);
                    }
                } catch (Exception ex) {
                    showErrorDialog("Invalid date selected.");
                }
            }
        });

        // Delete button action
        deleteBtn.addActionListener(e -> {
            int index = taskList.getSelectedIndex();
            if (index >= 0 && index < filteredTasks.size()) {
                Task selected = filteredTasks.get(index);
                try (Connection conn = DBHelper.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id=?");
                    ps.setInt(1, selected.id);
                    ps.executeUpdate();
                    loadTasks();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Update button action
        updateBtn.addActionListener(e -> {
            int index = taskList.getSelectedIndex();
            if (index >= 0 && index < filteredTasks.size()) {
                Task selected = filteredTasks.get(index);
                String desc = taskInput.getText();
                String deadline = getSelectedDate();
                int status = doneCheckbox.getState() ? 1 : 0;
                if (!desc.isEmpty()) {
                    try {
                        if (!deadline.isEmpty()) {
                            dateFormat.parse(deadline); // validate date format
                        }
                        try (Connection conn = DBHelper.getConnection()) {
                            PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET description=?, deadline=?, status=? WHERE id=?");
                            ps.setString(1, desc);
                            ps.setString(2, deadline);
                            ps.setInt(3, status);
                            ps.setInt(4, selected.id);
                            ps.executeUpdate();
                            loadTasks();
                            taskInput.setText("");
                            resetDateSelection();
                            doneCheckbox.setState(false);
                        }
                    } catch (Exception ex) {
                        showErrorDialog("Invalid date selected.");
                    }
                }
            }
        });

        setVisible(true);
    }

    private void showErrorDialog(String message) {
        Dialog errorDialog = new Dialog(this, "Error", true);
        errorDialog.setLayout(new FlowLayout());
        errorDialog.add(new Label(message));
        Button okButton = new Button("OK");
        okButton.addActionListener(evt -> errorDialog.dispose());
        errorDialog.add(okButton);
        errorDialog.setSize(250, 100);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setVisible(true);
    }

    private String getSelectedDate() {
        try {
            int day = Integer.parseInt(dayChoice.getSelectedItem());
            String monthStr = monthChoice.getSelectedItem();
            int month = Arrays.asList(monthNames).indexOf(monthStr);
            int year = Integer.parseInt(yearChoice.getSelectedItem());
            
            Calendar cal = Calendar.getInstance();
            cal.setLenient(false);
            cal.set(year, month, day);
            
            return dateFormat.format(cal.getTime());
        } catch (Exception e) {
            return "";
        }
    }

    private void setDateSelection(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            resetDateSelection();
            return;
        }
        
        try {
            Date date = dateFormat.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            
            dayChoice.select(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            monthChoice.select(monthNames[cal.get(Calendar.MONTH)]);
            yearChoice.select(String.valueOf(cal.get(Calendar.YEAR)));
        } catch (Exception e) {
            resetDateSelection();
        }
    }

    private void resetDateSelection() {
        Calendar cal = Calendar.getInstance();
        dayChoice.select(0);
        monthChoice.select(0);
        yearChoice.select(0);
    }

    private void loadTasks() {
        tasks.clear();
        filteredTasks.clear();
        taskList.removeAll();
        
        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM tasks WHERE user_id=?");
            ps.setInt(1, user.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Task task = new Task(rs.getInt("id"), rs.getString("description"), rs.getString("deadline"), rs.getInt("status"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Apply filter
        String filter = filterChoice.getSelectedItem();
        Date today = new Date();
        for (Task task : tasks) {
            boolean include = false;
            switch (filter) {
                case "All":
                    include = true;
                    break;
                case "Completed":
                    include = task.status == 1;
                    break;
                case "Pending":
                    include = task.status == 0;
                    break;
                case "Due Today":
                    if (task.deadline != null && !task.deadline.isEmpty()) {
                        try {
                            Date deadlineDate = dateFormat.parse(task.deadline);
                            include = isSameDay(deadlineDate, today);
                        } catch (Exception ex) {
                            include = false;
                        }
                    }
                    break;
            }
            if (include) {
                filteredTasks.add(task);
            }
        }

        // Apply sort
        String sort = sortChoice.getSelectedItem();
        if ("Sort by Deadline".equals(sort)) {
            filteredTasks.sort((a, b) -> {
                if (a.deadline == null || a.deadline.isEmpty()) return 1;
                if (b.deadline == null || b.deadline.isEmpty()) return -1;
                return a.deadline.compareTo(b.deadline);
            });
        } else if ("Sort by Status".equals(sort)) {
            filteredTasks.sort((a, b) -> Integer.compare(a.status, b.status));
        }

        // Update task list
        for (Task task : filteredTasks) {
            String text = task.description;
            if (task.status == 1) {
                text += " (Done)";
            }
            if (task.deadline != null && !task.deadline.isEmpty()) {
                text += " - Due: " + task.deadline;
            }
            taskList.add(text);
        }

        // Update stats
        int total = tasks.size();
        int completed = 0;
        for (Task task : tasks) {
            if (task.status == 1) {
                completed++;
            }
        }
        int pending = total - completed;
        totalTasksLabel.setText("Total Tasks: " + total);
        completedTasksLabel.setText("Completed: " + completed);
        pendingTasksLabel.setText("Pending: " + pending);
    }

    private boolean isSameDay(Date d1, Date d2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(d1).equals(fmt.format(d2));
    }
}
