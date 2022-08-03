package com.madama.data.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.Set;

@Entity
public class Technologie extends AbstractEntity {

    private String name;
    private String version;
    private boolean isLts;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public boolean isIsLts() {
        return isLts;
    }
    public void setIsLts(boolean isLts) {
        this.isLts = isLts;
    }

    @Override
    public String toString() {
        return "Technologie{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", isLts=" + isLts +
                '}';
    }
}
