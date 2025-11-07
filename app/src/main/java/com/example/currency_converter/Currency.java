package com.example.currency_converter;
public class Currency {
    private String code;
    private String name;
    private String flag;

    public Currency(String code, String name, String flag) {
        this.code = code;
        this.name = name;
        this.flag = flag;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getFlag() {
        return flag;
    }

    @Override
    public String toString() {
        return flag + " " + name + " (" + code + ")";
    }
}