package com.company.domain;

import java.io.Serializable;

/**
 * 管理员类
 */
public class Manager implements Serializable {
    private String id;
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
