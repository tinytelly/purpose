package com.tinytelly.purpose.model;

public class Product {
    private String name;

    private Purposes purposes;

    private Files files;

    public Files getFiles() {
        if(files == null){
            files = new Files();
        }
        return files;
    }

    public void setFiles(Files files) {
        this.files = files;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Purposes getPurposes() {
        return purposes;
    }

    public void setPurposes(Purposes purposes) {
        this.purposes = purposes;
    }

    public boolean hasPurposes() {
        if (this.getPurposes() != null) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Product{");
        sb.append("name='").append(name).append('\'');
        sb.append(", purposes=").append(purposes);
        sb.append(", files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
