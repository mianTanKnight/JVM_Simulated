package org.weishen.gc_.obj_;

public class Order {
    private final String no;
    private final String userName;

    public Order(String no, String userName) {
        this.no = no;
        this.userName = userName;
    }

    public String getNo() {
        return no;
    }

    public String getUserName() {
        return userName;
    }
}
