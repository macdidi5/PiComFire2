package net.macdidi5.picomfire;

public class MonitorItem {

    private String id;
    private String title;
    private int value;
    private int min = 0;
    private int max = 100;

    public MonitorItem(String id, String title, int value, int min, int max) {
        this(id, title, value);
        this.min = min;
        this.max = max;
    }

    public MonitorItem(String id, String title, int value) {
        this.id = id;
        this.title = title;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "MonitorItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", value=" + value +
                ", min=" + min +
                ", max=" + max +
                '}';
    }

}
