package com.tinytelly.purpose.model;

public class FileValue {
    public enum STATE {replace, add, set, overwrite}
    public static final String DEVOPS_PURPOSE = "devops.purpose";
    public static final String DEVOPS_PROPERTIES = "devops.properties";
    public static final String DEPLOY_PROFILE = "deploy.profile";
    public static final String OVERRIDE_DELIMITER = "=";
    public static final String DEVOPS_PURPOSE_DELIMITER = "|";
    public static final String STAR = "*";//Used as a wildcard
    public static final String DEVOPS_KEYWORK_START = "!{";//Use to indicate when we want to pull the value for the devops.properties
    public static final String DEVOPS_KEYWORK_END = "}";
    public static final String ENVIRONMENT_VARIABLE_START = "${";
    public static final String ENVIRONMENT_VARIABLE_END = "}";
    private String name;
    private String value;
    private STATE state = STATE.set;//Default behaviour

    public String getName() {
        return name;
    }

    public String getNameForRegex() {
        return name.replace(".", "[.]").replace("${", "\\Q${\\E");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        if(DEVOPS_PURPOSE.equals(this.name) && !STATE.set.equals(this.state)) {
            throw new RuntimeException("All [" + DEVOPS_PURPOSE + "] properties must be of the state [" + STATE.set + "]");
        } else if (this.name.startsWith("," + STAR) && !FileValue.STATE.set.equals(this.state)) {
            throw new RuntimeException("Any use usage of [,*value] - must have a state of [" + STATE.set + "]");
        } else if (DEPLOY_PROFILE.equals(this.name) && !FileValue.STATE.set.equals(this.state)) {
            throw new RuntimeException("ALL [" + DEPLOY_PROFILE + "] properties must be of the state [" + STATE.set + "]");
        }
        return value;
    }

    public boolean containsDevopsKey() {
        if(value.contains(DEVOPS_KEYWORK_START) && value.contains(DEVOPS_KEYWORK_END) && !name.endsWith("etransfer.password")) {
            return true;
        }
        return false;
    }

    public String getDevopsKey() {
        if(!containsDevopsKey()){
            return value;
        }

        return value.substring(value.indexOf(DEVOPS_KEYWORK_START) + DEVOPS_KEYWORK_START.length(), value.indexOf(DEVOPS_KEYWORK_END));
    }

    public boolean canUseRegex() {
        if(getValue().startsWith(ENVIRONMENT_VARIABLE_START) && getValue().endsWith(ENVIRONMENT_VARIABLE_END)) {
            return false;
        }
        return true;
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