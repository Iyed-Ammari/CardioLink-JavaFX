package com.cardiolink.Test;

import com.cardiolink.Models.Message;
import com.cardiolink.Services.ServiceMessage;
import com.cardiolink.utils.MyDatabase;

import java.sql.SQLDataException;

public class Main {
    public static void main(String[] args) {
        // kol wehed ch yaaml instance m service mteeo lehne (betbiaa baaed ma tasnaa service mtaa l model mteeo fl package Services ahwka)
        // par exemple : ServicePersonne servicePersonne = new ServicePersonne();
        ServiceMessage serviceMessage = new ServiceMessage();
        try {
            // mbaaed lehne testi les services mteek yekhdmo comme il faut wle, normalement aamlt liaison bl base de donnée deja w ntouma aandkom menha ml projet mtaa symfony donc mch mochkl
            // EXEMPLE :
//            servicePersonne.ajouter(new Personne("Yassine","Dhaya",90));
//            servicePersonne.ajouter(new Personne("Rihem","MATTOUSI",190));
//            servicePersonne.ajouter(new Personne("Falten","Foulen",70));
//            servicePersonne.modifier(new Personne("TEsting","Hmed",33,1));
//            System.out.println(servicePersonne.recuperer());

            serviceMessage.add(new Message());
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }
}

