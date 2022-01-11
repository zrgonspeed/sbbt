package com.run.treadmill.base;

/**
 * @Description
 * @Author YeYueHong
 * @Date 2020/9/7
 */
public class EventMessage {
    private String message;

    public EventMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
