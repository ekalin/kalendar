package com.github.ekalin.kalendar.prefs;

public class EventSource {
    private int id;
    private String title;
    private String summary;
    private int color;

    public EventSource(int id, String title, String summary, int color) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSource that = (EventSource) o;
        return id == that.id &&
                color == that.color &&
                title.equals(that.title) &&
                summary.equals(that.summary);
    }

    @Override
    public int hashCode() {
        int result = id;
        result += 31 * title.hashCode();
        result += 31 * summary.hashCode();
        result += 31 * color;
        return result;
    }

    @Override
    public String toString() {
        return "EventSource{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
