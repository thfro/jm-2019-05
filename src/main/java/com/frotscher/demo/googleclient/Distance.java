package com.frotscher.demo.googleclient;

public class Distance {

    private String text;
    private Integer value;
    private Integer durationInMinutes;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(Integer durationInMinutes) {

        this.durationInMinutes = durationInMinutes;
    }

    public static Distance from(String text, Integer value, Integer durationInMinutes) {
        Distance d = new Distance();
        d.setText(text);
        d.setValue(value);
        d.setDurationInMinutes(durationInMinutes);
        return d;
    }
}
