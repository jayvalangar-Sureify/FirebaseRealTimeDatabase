package com.example.firebaserealtimedatabase;

public class pdfClass {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public pdfClass(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String name, url;
}
