package com.tinytelly.purpose.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class Purpose {
    private String name;
    private Files files;

    public Purpose() {
    }

    public Purpose(String name) {
        this.name = name;
    }

    public String getName() {
        if (hasValue()) {
            return Splitter.on("=").trimResults().omitEmptyStrings().split(this.name).iterator().next();
        }
        return name;
    }

    public String getValue() {
        if (hasValue()) {
            return Lists.newArrayList(Splitter.on("=").trimResults().omitEmptyStrings().split(this.name)).get(1);
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Files getFiles() {
        return files;
    }

    public void setFiles(Files files) {
        this.files = files;
    }

    public boolean hasValue() {
        if (this.name.contains("=")) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Purpose{");
        sb.append("name='").append(name).append('\'');
        if (files != null) {
            sb.append(", files=").append(files);
        }
        sb.append('}');
        return sb.toString();
    }
}
