package com.cardiolink.Services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.cardiolink.Models.Rendezvous;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "CardioLink Calendar Integration";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Calendar service;
    private String initError = null;

    public GoogleCalendarService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (FileNotFoundException e) {
            initError = "Fichier credentials.json introuvable dans src/main/resources/";
            System.err.println(initError);
        } catch (Exception e) {
            initError = "Google Calendar init failed: " + e.getMessage();
            System.err.println(initError);
        }
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        
        com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp.Browser browser = new com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp.Browser() {
            @Override
            public void browse(String url) {
                try {
                    if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } else {
                        String os = System.getProperty("os.name").toLowerCase();
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                        } else if (os.contains("mac")) {
                            Runtime.getRuntime().exec(new String[]{"open", url});
                        } else if (os.contains("nix") || os.contains("nux")) {
                            Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Impossible d'ouvrir le navigateur automatiquement. Veuillez ouvrir ce lien manuellement : " + url);
                    e.printStackTrace();
                }
            }
        };

        return new AuthorizationCodeInstalledApp(flow, receiver, browser).authorize("user");
    }

    public boolean isInitialized() {
        return service != null;
    }

    public String getInitError() {
        return initError;
    }

    public void pushRendezvousToCalendar(Rendezvous rv, String patientName) throws Exception {
        if (service == null) {
            throw new Exception(initError != null ? initError : "Google Calendar Service non initialisé.");
        }
        
        Event event = new Event()
            .setSummary("RDV CardioLink: " + patientName)
            .setDescription("Motif: " + (rv.getRemarques() != null ? rv.getRemarques() : "N/A") + "\nType: " + rv.getType());

        LocalDateTime ldt = rv.getDateHeure();
        Date startDate = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        DateTime startDateTime = new DateTime(startDate);
        EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(ZoneId.systemDefault().getId());
        event.setStart(start);

        // Durée estimée à 1 heure
        Date endDate = Date.from(ldt.plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        DateTime endDateTime = new DateTime(endDate);
        EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(ZoneId.systemDefault().getId());
        event.setEnd(end);

        Event createdEvent = service.events().insert("primary", event).execute();
        System.out.printf("Event created: %s\n", createdEvent.getHtmlLink());
    }
}
