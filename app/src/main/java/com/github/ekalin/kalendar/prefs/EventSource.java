package com.github.ekalin.kalendar.prefs;

public class EventSource {
    private final String id;
    private final String title;
    private final String summary;
    private final int color;

    public EventSource(String id, String title, String summary, int color) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.color = color;
    }

    public String getId() {
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
        return id.equals(that.id) &&
                color == that.color &&
                title.equals(that.title) &&
                summary.equals(that.summary);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
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
