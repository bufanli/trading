package com.cbnb.jiaoyishi.util;

/**
 * @Author: hcf
 * @Description:
 * @Date: Create in 11:02 2020/12/24
 */
public class ResultBean {

    private String type;

    private String titles;

    private String data;

    @Override
    public String toString() {
        return "{" +
                "'type':'" + type + "'" +
                ", 'titles':'" + titles + "'" +
                ", 'data':'" + data + "'" +
                "}";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitles() {
        return titles;
    }

    public void setTitles(String titles) {
        this.titles = titles;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
