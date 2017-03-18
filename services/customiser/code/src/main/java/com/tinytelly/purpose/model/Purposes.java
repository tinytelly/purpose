package com.tinytelly.purpose.model;

import java.util.ArrayList;
import java.util.List;

public class Purposes {
    private List<Purpose> purposes;

    public List<Purpose> getPurposes() {
        return purposes;
    }

    public void setPurposes(List<Purpose> purposes) {
        this.purposes = purposes;
    }

    public List<String> getPurposeNames() {
        List<String> names = new ArrayList<String>();
        for (Purpose purpose : getPurposes()) {
            names.add(purpose.getName());
        }
        return names;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Purposes{");
        sb.append("purposes=").append(purposes);
        sb.append('}');
        return sb.toString();
    }
}
