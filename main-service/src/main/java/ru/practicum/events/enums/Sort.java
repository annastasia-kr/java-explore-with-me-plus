package ru.practicum.events.enums;

public enum Sort {
    EVENT_DATE("eventDate"),
    VIEWS("views");

    String paramName;

    Sort(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }
}
