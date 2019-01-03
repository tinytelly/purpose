package com.tinytelly.purpose.model;

import java.util.*;

/**
 * Created by mattheww on 17/11/2016.
 */
public class Purposes {
    private List<Purpose> purposes;

    public Purposes() {
    }

    public Purposes(List<Purpose> purposes) {
        this.purposes = purposes;
    }

    public List<Purpose> getPurposes() {
        return purposes;
    }

    public void setPurposes(List<Purpose> purposes) {
        this.purposes = purposes;
    }

    public List<String> getPurposeNames() {
        List<String> names = new ArrayList<String>();
        for(Purpose purpose : getPurposes()) {
            names.add(purpose.getName());
        }
        return names;
    }

    public void add(Purposes purposesToAdd){
        for(Purpose purpose : purposesToAdd.getPurposes()) {
            purposes.add(purpose);
        }
    }

    private void removeAllButOne(String name){
        Purpose elvis = null;
        Iterator<Purpose> iter = purposes.iterator();

        while (iter.hasNext()) {
            Purpose purpose = iter.next();

            if(!purpose.hasValue()) {
                if (purpose.getName().equals(name)) {
                    elvis = purpose;
                    iter.remove();
                }
            }
        }
        if(elvis != null){
            purposes.add(elvis);
        }
    }

    public void deduplicatePurposes() {
        Set<String> purposePair = new HashSet<String>();

        Iterator<Purpose> iter = purposes.iterator();

        while (iter.hasNext()) {
            Purpose purpose = iter.next();

            if(purpose.hasValue()) {
                for(String name : purposePair){
                    if (name.equals(purpose.getName())) {
                        iter.remove();
                    }
                }

                purposePair.add(purpose.getName());
            }
        }

        this.removeAllButOne("devops");
    }

    public String getNames() {
        StringBuilder names = new StringBuilder();
        for(Purpose purpose : purposes){
            if(!"devops".equals(purpose.getName())) {
                names.append(purpose.getName());
                if(purpose.hasValue()) {
                    names.append(FileValue.OVERRIDE_DELIMITER).append(purpose.getValue());
                }
                names.append(FileValue.DEVOPS_PURPOSE_DELIMITER);
            }
        }
        return names.toString();
    }

    public boolean contains(String purposeName) {
        for(Purpose purpose : purposes){
            if(purposeName.equals(purpose.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Purposes{ ");
        sb.append(purposes);
        sb.append(" }");
        return sb.toString();
    }
}
