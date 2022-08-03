package com.madama.data.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import java.util.Set;

@Entity
public class Project extends AbstractEntity {

    private String name;
    private String version;
    private String phase;
    private String methodo;
    private String client;

    @ManyToMany(fetch= FetchType.EAGER)
    private Set<Technologie> technologies;

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
    public String getPhase() {
        return phase;
    }
    public void setPhase(String phase) {
        this.phase = phase;
    }
    public String getMethodo() {
        return methodo;
    }
    public void setMethodo(String methodo) {
        this.methodo = methodo;
    }
    public String getClient() {
        return client;
    }
    public void setClient(String client) {
        this.client = client;
    }

    public Set<Technologie> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(Set<Technologie> technologies) {
        this.technologies = technologies;
    }
}
