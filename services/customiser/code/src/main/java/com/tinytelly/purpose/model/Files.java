package com.tinytelly.purpose.model;

import java.util.ArrayList;
import java.util.List;

public class Files {
    List<File> files = new ArrayList<File>();

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void addFiles(List<File> files) {
        this.files.addAll(files);
    }

    public boolean hasFiles(){
        return this.files.size() == 0 ? false : true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Files{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
