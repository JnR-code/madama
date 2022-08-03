package com.madama.data.entity;

import javax.persistence.OneToOne;
import java.util.Date;

public class Experience extends AbstractEntity {

    private Date dateDebut;

    private Date dateFin;

    @OneToOne
    private Mate mate;

    @OneToOne
    private Project project;

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public Mate getMate() {
        return mate;
    }

    public void setMate(Mate mate) {
        this.mate = mate;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
