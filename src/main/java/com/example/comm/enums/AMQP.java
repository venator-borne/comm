package com.example.comm.enums;

public enum AMQP {
    SUB("_subscribe_queue_device_id"),
    PUB("_publish_queue_device_id");

    private String str;
    private AMQP(String s) {
        this.str = s;
    }

}
