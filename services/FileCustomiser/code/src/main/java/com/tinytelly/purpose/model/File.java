package com.tinytelly.purpose.model;

import java.util.HashSet;
import java.util.Set;

public class File {
    private String name;
    private Set<FileValue> values = new HashSet<FileValue>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<FileValue> getValues() {
        return values;
    }

    public void setValues(Set<FileValue> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("File{");
        sb.append("name='").append(name).append('\'');
        sb.append(", values=").append(values);
        sb.append('}');
        return sb.toString();
    }
}
