package com.cardiolink.Models;

public class DossierMedical {
    private int    id;
    private String groupeSanguin;
    private String antecedents;
    private String allergies;
    private Double poids;
    private Double taille;
    private Double tensionSystolique;
    private Double tensionDiastolique;
    private Double frequenceCardiaque;
    private int    userId;

    public DossierMedical() {}

    public DossierMedical(int id, String groupeSanguin, String antecedents, String allergies,
                          Double poids, Double taille, Double tensionSystolique,
                          Double tensionDiastolique, Double frequenceCardiaque, int userId) {
        this.id                 = id;
        this.groupeSanguin      = groupeSanguin;
        this.antecedents        = antecedents;
        this.allergies          = allergies;
        this.poids              = poids;
        this.taille             = taille;
        this.tensionSystolique  = tensionSystolique;
        this.tensionDiastolique = tensionDiastolique;
        this.frequenceCardiaque = frequenceCardiaque;
        this.userId             = userId;
    }

    public int    getId()                           { return id; }
    public void   setId(int id)                     { this.id = id; }
    public String getGroupeSanguin()                { return groupeSanguin; }
    public void   setGroupeSanguin(String g)        { this.groupeSanguin = g; }
    public String getAntecedents()                  { return antecedents; }
    public void   setAntecedents(String a)          { this.antecedents = a; }
    public String getAllergies()                     { return allergies; }
    public void   setAllergies(String a)            { this.allergies = a; }
    public Double getPoids()                        { return poids; }
    public void   setPoids(Double p)                { this.poids = p; }
    public Double getTaille()                       { return taille; }
    public void   setTaille(Double t)               { this.taille = t; }
    public Double getTensionSystolique()            { return tensionSystolique; }
    public void   setTensionSystolique(Double t)    { this.tensionSystolique = t; }
    public Double getTensionDiastolique()           { return tensionDiastolique; }
    public void   setTensionDiastolique(Double t)   { this.tensionDiastolique = t; }
    public Double getFrequenceCardiaque()           { return frequenceCardiaque; }
    public void   setFrequenceCardiaque(Double f)   { this.frequenceCardiaque = f; }
    public int    getUserId()                       { return userId; }
    public void   setUserId(int u)                  { this.userId = u; }
}