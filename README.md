# CardioLink

Un système complet de gestion médicale intégrant une application de bureau JavaFX, des services d'IA/ML pour l'analyse de contenu, et une architecture modulaire pour la gestion des dossiers médicaux, rendez-vous, et ordonnances.

## 🎯 Vue d'ensemble

CardioLink est une plateforme médicale qui combine :
- **Frontend** : Application de bureau Java avec JavaFX 21.0.2
- **Backend ML/IA** : Services Python pour l'analyse de texte, la modération et les prédictions
- **Architecture** : Modulaire et extensible avec WebSocket pour la communication en temps réel

### Modules principaux

```
CardioLink/
├── src/                          # Code source Java principal
│   ├── main/java/com/cardiolink/
│   │   ├── Controllers/          # Contrôleurs FXML
│   │   ├── Models/               # Modèles de données
│   │   ├── Services/             # Services métier
│   │   ├── WebSocket/            # Gestion WebSocket
│   │   └── Utils/                # Utilitaires
│   └── test/                     # Tests unitaires
├── ai/                           # Services d'IA
│   ├── moderation/               # Modération de contenu
│   └── requirements.txt
├── ai_matching/                  # Service d'appariement IA
│   ├── app.py
│   └── requirements.txt
├── ml/                           # Machine Learning
│   ├── summarizer.py             # Résumés automatiques
│   └── ml_env/                   # Environnement Python
├── ml_service/                   # Service ML principal
│   ├── app.py
│   ├── train_model.py
│   ├── test_toxicity.py
│   └── requirements.txt
└── pom.xml                       # Configuration Maven
```

## 🚀 Démarrage rapide

### Prérequis

- **Java 17+** (Maven 3.8+)
- **Python 3.9+** (pip)
- **MySQL/MariaDB** (optionnel, pour la base de données)

### Installation

#### 1. Clone le projet
```bash
git clone <repository>
cd CardioLink
```

#### 2. Configuration Java

```bash
# Compiler le projet
mvn clean install

# Ou lancer l'application directement
mvn javafx:run
```

#### 3. Configuration des services Python

##### Service de Modération (FastAPI)
```bash
cd ai/moderation
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r ../../ai/requirements.txt
python app.py
```

##### Service ML (Flask)
```bash
cd ml_service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

#### 4. Service d'Appariement IA
```bash
cd ai_matching
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

## 📋 Fonctionnalités

### Frontend (JavaFX)
- **Authentification** : Login avec face recognition et email verification
- **Gestion des profils** : Patient, Médecin, Administrateur
- **Dossiers médicaux** : Consultation, modification, archivage
- **Gestion des rendez-vous** (RDV) : Création, affichage, prédictions
- **Ordonnances** : Gestion complète (CRUD) et archivage
- **Chat avancé** : Communication en temps réel avec modération IA
- **Suivi médical** : Enregistrement et consultation du suivi
- **Dashboard** : Personnalisé par rôle (patient, médecin, admin)
- **Alertes SOS** : Système d'alerte d'urgence
- **Carte interactive** : Localisation et affichage géographique
- **Interventions** : Suivi des interventions médicales

### Services Python

#### Modération IA (ai/moderation/)
- **Modèle** : XLM-RoBERTa multilingue (`unitary/multilingual-toxic-xlm-roberta`)
- **Détection** : Toxicité, langage offensant, menaces, insultes
- **Endpoint** : `POST /predict`

**Dépendances principales** :
- FastAPI
- Transformers
- Torch
- OpenAI Whisper (pour l'audio)

#### Service ML (ml_service/)
- **Classification** : Analyse et classification des messages
- **Endpoints** :
    - `POST /analyze_message` - Classification avec confiance
    - `POST /analyze_batch` - Analyse en lot
    - `GET /health` - Vérification de santé
    - `GET /classes` - Liste des classes

**Dépendances principales** :
- Flask
- Scikit-learn
- Pandas
- MySQL Connector
- Transformers
- Torch

#### Service d'Appariement (ai_matching/)
- Logique d'appariement patient-médecin basée sur l'IA

### Modèles de Machine Learning
- **Summarizer** : Résumés automatiques des dossiers médicaux
- **Toxicity Detection** : Détection de contenu toxique
- **Message Classification** : Classification des messages médicaux

## 🔧 Configuration

### Variables d'environnement recommandées

```bash
# Base de données
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=password
DB_NAME=cardiolink

# Services Python
ML_SERVICE_URL=http://localhost:5000
AI_MODERATION_URL=http://localhost:8000
AI_MATCHING_URL=http://localhost:8001

# Application
APP_PORT=5000
DEBUG=false
```

### Fichiers de configuration

- `pom.xml` : Configuration Maven et dépendances Java
- `src/main/resources/` : Ressources (FXML, CSS, images)
- `target/classes/credentials.json` : Authentification Google (OAuth)

## 📦 Dépendances principales

### Backend Java
```xml
<dependency>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-controls</artifactId>
  <version>21.0.2</version>
</dependency>
```

- JavaFX 21.0.2
- Java WebSocket 1.5.4
- Google API Client 2.4.0
- OkHttp3 4.12.0
- JBCrypt 0.4 (sécurité)
- SLF4J (logging)

### Services Python
- **FastAPI** 0.129.0 (Modération)
- **Flask** 3.0.0+ (ML Service)
- **Transformers** 4.30.0+ (NLP)
- **Torch** 2.10.0 (Deep Learning)
- **Scikit-learn** 1.3.0+ (ML)
- **Pandas** 2.0.0+ (Data Processing)
- **NumPy** 2.3.5+ (Calcul numérique)

## 🏗️ Architecture

### Couches de l'application

```
┌─────────────────────────────────────┐
│   Interface Utilisateur (JavaFX)    │
│  - FXML Views                       │
│  - Controllers                      │
│  - CSS Styling                      │
└────────────────┬────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
┌───────▼────────┐  ┌────▼──────────┐
│   Services     │  │   WebSocket   │
│   Java         │  │   Chat RT     │
│   (Java)       │  │   (async)     │
└───────┬────────┘  └────┬──────────┘
        │                │
        └────────┬───────┘
                 │
        ┌────────▼────────────────┐
        │   ML/IA Services        │
        │ (Python - HTTP APIs)    │
        ├────────────────────────┤
        │ ├─ Moderation (FastAPI) │
        │ ├─ ML Service (Flask)   │
        │ └─ Matching (FastAPI)   │
        └────────────────────────┘
                 │
        ┌────────▼────────┐
        │  Base de données │
        │   (MySQL)       │
        └─────────────────┘
```

### Flux de communication

1. **Desktop → WebSocket** : Envoi de messages avec chat en temps réel
2. **WebSocket → ML Service** : Analyse de toxicité et classification
3. **Java Services → Python APIs** : Appels HTTP REST
4. **Database** : Persistance des données

## 🗂️ Structure des fichiers FXML

Les interfaces utilisateur sont définies dans `target/classes/` :
- `login.fxml` - Connexion
- `register.fxml` - Inscription
- `face_login.fxml` - Authentification faciale
- `register_face.fxml` - Enregistrement facial
- `dashboard_*.fxml` - Dashboards par rôle (patient, médecin, admin)
- `dossier_medical.fxml` - Dossier médical
- `AfficherRDV.fxml` - Affichage des RDV
- `AjouterRDV.fxml` - Création de RDV
- `ListeOrdonnances.fxml` - Gestion des ordonnances
- `chat_advanced.fxml` - Chat avancé avec modération
- `ConsulterSuivi.fxml` - Consultation du suivi
- Et plusieurs autres...

## 💻 Développement

### Compiler le projet
```bash
mvn clean install
```

### Lancer les tests
```bash
mvn test
```

### Exécuter l'application
```bash
mvn javafx:run
```

### Démarrer en mode développement
```bash
# Terminal 1 : Application Java
mvn javafx:run

# Terminal 2 : Service ML
cd ml_service && python app.py

# Terminal 3 : Service de Modération
cd ai/moderation && python app.py

# Terminal 4 : Service d'Appariement (optionnel)
cd ai_matching && python app.py
```

## 🧪 Tests

### Python (ML Service)
```bash
cd ml_service
python test_toxicity.py
```

### Java
```bash
mvn test
```

## 📝 Logs

Les fichiers de logs et traces peuvent être consultés dans :
- `sent_reminders.txt` - Rappels d'appels/messages envoyés
- Terminal/Console de l'IDE

## 🔐 Sécurité

- **Authentification** : JBCrypt pour le hachage des mots de passe
- **OAuth2** : Intégration Google OAuth
- **Modération** : Détection de contenu toxique via IA
- **WebSocket** : Communication sécurisée en temps réel

## 📊 Performance

- **Environnement ML séparé** : `ml_env/` pour isoler les dépendances
- **Services asynchrones** : FastAPI pour traitement parallèle
- **WebSocket** : Communication bidirectionnelle efficace
- **Cache** : Gestion des modèles pré-chargés

## 🐛 Dépannage

### Les services Python ne répondent pas
```bash
# Vérifier que les services sont en cours d'exécution
curl http://localhost:8000/docs        # Modération
curl http://localhost:5000/health      # ML Service
curl http://localhost:8001/docs        # Matching
```

### Erreur de compilation Maven
```bash
# Nettoyer le cache
mvn clean

# Réinstaller les dépendances
mvn dependency:resolve
```

### Erreur d'importation Python
```bash
# Réinstaller l'environnement virtuel
rm -rf ml_env venv
python -m venv ml_env
source ml_env/bin/activate
pip install -r requirements.txt
```

## 📚 Documentation supplémentaire

- **JavaFX** : https://openjfx.io/
- **FastAPI** : https://fastapi.tiangolo.com/
- **Flask** : https://flask.palletsprojects.com/
- **Transformers** : https://huggingface.co/transformers/
- **Maven** : https://maven.apache.org/

## 👥 Rôles utilisateurs

1. **Patient** : Consultation du dossier, RDV, suivi, chat
2. **Médecin** : Gestion des patients, ordonnances, diagnostic
3. **Administrateur** : Gestion des utilisateurs, système global

## 📈 Évolutions futures

- [ ] Mobile App (iOS/Android)
- [ ] API REST public
- [ ] Dashboard statistiques avancées
- [ ] Intégration HL7/FHIR
- [ ] Signature numérique des ordonnances
- [ ] Système de prescription électronique avancé

## 📄 Licence

Propriétaire - Tous droits réservés

## 👨‍💻 Contribution

Pour contribuer au projet, veuillez :
1. Créer une branche (`git checkout -b feature/AmazingFeature`)
2. Commiter vos changements (`git commit -m 'Add some AmazingFeature'`)
3. Pousser la branche (`git push origin feature/AmazingFeature`)
4. Ouvrir une Pull Request

## 📞 Support

Pour toute question ou problème, veuillez contacter l'équipe de développement.

---

**Dernière mise à jour** : Mai 2026  
**Version** : 1.0-SNAPSHOT
