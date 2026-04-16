package com.cardiolink.Models;

public class User {
    private int     id;
    private String  email;
    private String  password;
    private String  nom;
    private String  prenom;
    private String  roles;
    private String  adresse;
    private String  tel;        // ← "tel" pas "phone"
    private String  cabinet;
    private boolean isActive;
    private boolean isVerified;
    private String  imageUrl;

    public User() {}

    public User(int id, String email, String password,
                String nom, String prenom, String roles, String adresse,
                String tel, String cabinet, boolean isActive, boolean isVerified, String imageUrl) {
        this.id         = id;
        this.email      = email;
        this.password   = password;
        this.nom        = nom;
        this.prenom     = prenom;
        this.roles      = roles;
        this.adresse    = adresse;
        this.tel        = tel;
        this.cabinet    = cabinet;
        this.isActive   = isActive;
        this.isVerified = isVerified;
        this.imageUrl   = imageUrl;
    }

    public int     getId()                    { return id; }
    public void    setId(int id)              { this.id = id; }
    public String  getEmail()                 { return email; }
    public void    setEmail(String e)         { this.email = e; }
    public String  getPassword()              { return password; }
    public void    setPassword(String p)      { this.password = p; }
    public String  getNom()                   { return nom; }
    public void    setNom(String n)           { this.nom = n; }
    public String  getPrenom()                { return prenom; }
    public void    setPrenom(String p)        { this.prenom = p; }
    public String  getRoles()                 { return roles; }
    public void    setRoles(String r)         { this.roles = r; }
    public String  getAdresse()               { return adresse; }
    public void    setAdresse(String a)       { this.adresse = a; }
    public String  getTel()                   { return tel; }
    public void    setTel(String t)           { this.tel = t; }
    public String  getCabinet()               { return cabinet; }
    public void    setCabinet(String c)       { this.cabinet = c; }
    public boolean isActive()                 { return isActive; }
    public void    setActive(boolean a)       { this.isActive = a; }
    public boolean isVerified()               { return isVerified; }
    public void    setVerified(boolean v)     { this.isVerified = v; }
    public String  getImageUrl()              { return imageUrl; }
    public void    setImageUrl(String i)      { this.imageUrl = i; }

    // Retourne le rôle propre sans les crochets JSON Symfony
    // ["ROLE_PATIENT"] → ROLE_PATIENT
    public String getRoleClean() {
        if (roles == null) return "";
        return roles.replace("[\"", "").replace("\"]", "").trim();
    }
}