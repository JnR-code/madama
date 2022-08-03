package com.madama.data.entity;

import java.time.LocalDate;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

@Entity
public class Mate extends AbstractEntity {

    private String firstName;
    private String lastName;
    @Lob
    private String avatar;
    private boolean isActif;
    private String poste;
    private LocalDate dateDebut;

    @ManyToMany(fetch= FetchType.EAGER)
    private Set<Technologie> technologies;

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public boolean isIsActif() {
        return isActif;
    }
    public void setIsActif(boolean isActif) {
        this.isActif = isActif;
    }
    public String getPoste() {
        return poste;
    }
    public void setPoste(String poste) {
        this.poste = poste;
    }
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

}
