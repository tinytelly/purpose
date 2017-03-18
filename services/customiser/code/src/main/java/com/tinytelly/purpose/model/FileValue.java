package com.tinytelly.purpose.model;

public class FileValue {
    public enum STATE {replace, add, set}

    public static final String STAR = "*";//Used as a wildcard
    public static final String EXCLAMATION = "!";//Used to indicate we need to pull the value from the purpose.properties
    public static final String PURPOSE_KEYWORK_START = "!{";//Use to indicate when we want to pull the value for the purpose.properties
    public static final String PURPOSE_KEYWORK_END = "}";
    private String name;
    private String value;
    private STATE state = STATE.set;//Default behaviour

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public boolean containsPurposeKey() {
        if (value.contains(PURPOSE_KEYWORK_START) && value.contains(PURPOSE_KEYWORK_END)) {
            return true;
        }
        return false;
    }

    public String getPurposeKey() {
        if (!containsPurposeKey()) {
            return value;
        }

        return value.substring(value.indexOf(PURPOSE_KEYWORK_START) + PURPOSE_KEYWORK_START.length(), value.indexOf(PURPOSE_KEYWORK_END));
    }

    public void setValue(String value) {
        this.value = value;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public String getPair() {
        return this.getName() + "=" + this.getValue();
    }

    public String getLeftSide() {
        return this.getName() + "=";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileValue{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }
}
