public class Task {
    public int id;
    public String description;
    public String deadline;
    public int status; // 0 = pending, 1 = completed

    public Task(int id, String description, String deadline, int status) {
        this.id = id;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
    }

    @Override
    public String toString() {
        return description + (status == 1 ? " (Done)" : "") + (deadline != null && !deadline.isEmpty() ? " - Due: " + deadline : "");
    }
}
