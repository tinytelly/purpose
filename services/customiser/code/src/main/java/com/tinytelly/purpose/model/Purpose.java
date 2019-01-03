package com.tinytelly.purpose.model;

import com.google.common.base.Splitter;

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
            return Splitter.on(FileValue.OVERRIDE_DELIMITER).trimResults().omitEmptyStrings().split(this.name).iterator().next();
        }
        return name;
    }

    public String getValue() {
        if (hasValue()) {
            return this.name.split(FileValue.OVERRIDE_DELIMITER, 2)[1];
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

    public void addFiles(Files files) {
        this.files.addFiles(files.getFiles());
    }

    public boolean hasValue() {
        if (this.name.contains(FileValue.OVERRIDE_DELIMITER)) {
            return true;
        }
        return false;
    }

    public String getOverrideName() {
        String[] parts = this.getValue().split(FileValue.OVERRIDE_DELIMITER);
        return parts[0];
    }

    public String getOverrideValue() {
        String[] parts = this.getValue().split(FileValue.OVERRIDE_DELIMITER);
        return parts[1];
    }

    public String getPair() {
        return getName() + FileValue.OVERRIDE_DELIMITER + getValue();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(" '").append(name).append("\' ");
        if(files != null) {
            sb.append(", files=").append(files);
        }
        return sb.toString();
    }
}