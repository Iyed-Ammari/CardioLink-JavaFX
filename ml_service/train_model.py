"""
train_model.py — CardioLink ML
================================
Entraîne un modèle Naive Bayes sur les messages de la table `message`
et le sauvegarde dans `cardio_model.pkl`.

Usage :
    python train_model.py

Prérequis :
    pip install mysql-connector-python pandas scikit-learn
"""

import sys
import mysql.connector
import pandas as pd
import pickle
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import make_pipeline
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report

# ── Configuration BDD (identique à MyDatabase.java) ──────────────────
DB_CONFIG = {
    "host":     "127.0.0.1",
    "user":     "root",
    "password": "",           # Vide sous XAMPP/WAMP
    "database": "cardiolink",
    "charset":  "utf8mb4",
}

MODEL_PATH = "cardio_model.pkl"

# ── Données d'exemple intégrées (fallback si la BDD est vide) ────────
# Ces données reflètent les classifications réelles du projet CardioLink
FALLBACK_DATA = [
    # URGENT
    ("douleur thoracique intense depuis 2 heures", "URGENT"),
    ("essoufflement grave, impossible de respirer", "URGENT"),
    ("palpitations cardiaques très rapides", "URGENT"),
    ("pression cardiaque anormale urgence", "URGENT"),
    ("syncope perte de conscience cardiaque", "URGENT"),
    ("tachycardie sévère besoin aide immédiate", "URGENT"),
    ("douleur poitrine bras gauche urgent", "URGENT"),
    ("mes symptômes s'aggravent rapidement besoin médecin urgent", "URGENT"),
    ("tension artérielle très élevée 180/110", "URGENT"),
    ("fibrillation ventriculaire urgence cardiaque", "URGENT"),

    # ADMINISTRATIF
    ("j'ai besoin d'un certificat médical pour le travail", "ADMINISTRATIF"),
    ("pouvez-vous m'envoyer mon ordonnance renouvelée", "ADMINISTRATIF"),
    ("je souhaite modifier l'heure de mon rendez-vous", "ADMINISTRATIF"),
    ("merci de mettre à jour mon dossier médical", "ADMINISTRATIF"),
    ("demande de remboursement assurance maladie", "ADMINISTRATIF"),
    ("attestation médicale pour assurance", "ADMINISTRATIF"),
    ("renouvellement de prescription médicaments chroniques", "ADMINISTRATIF"),
    ("arrêt maladie et prolongation administrative", "ADMINISTRATIF"),
    ("je veux annuler ma consultation du lundi", "ADMINISTRATIF"),
    ("envoi du compte rendu d'hospitalisation", "ADMINISTRATIF"),

    # NORMAL
    ("comment va mon traitement depuis la dernière visite", "NORMAL"),
    ("j'ai des questions sur ma médication", "NORMAL"),
    ("mes résultats d'analyses de sang sont disponibles", "NORMAL"),
    ("bilan de santé général semaine dernière", "NORMAL"),
    ("suivi habituel de tension artérielle 120/80", "NORMAL"),
    ("je me sens mieux depuis le changement de traitement", "NORMAL"),
    ("questions sur mon régime alimentaire cardiaque", "NORMAL"),
    ("j'ai commencé les exercices recommandés", "NORMAL"),
    ("résultats ECG normaux reçus ce matin", "NORMAL"),
    ("prise de poids légère à surveiller", "NORMAL"),
    ("je prends bien mes médicaments tous les jours", "NORMAL"),
    ("consultation de contrôle rendez-vous prochain mois", "NORMAL"),
]


def load_data_from_db():
    """Charge les messages depuis MySQL cardiolink."""
    print("📡 Connexion à la base de données MySQL...")
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        query = """
            SELECT content, classification
            FROM message
            WHERE classification IS NOT NULL
              AND classification != ''
              AND content IS NOT NULL
              AND content != ''
        """
        df = pd.read_sql(query, conn)
        conn.close()
        return df
    except mysql.connector.Error as e:
        print(f"⚠️  Connexion BDD impossible : {e}")
        return pd.DataFrame()


def prepare_training_data():
    """
    Combinaison BDD + données d'exemple.
    Permet d'entraîner même si la BDD a peu de messages.
    """
    # Données depuis la BDD
    df_db = load_data_from_db()

    if not df_db.empty:
        print(f"✅ {len(df_db)} messages récupérés depuis la BDD.")
    else:
        print("ℹ️  Aucune donnée en BDD — utilisation des données d'exemple.")

    # Données d'exemple (toujours incluses pour améliorer l'équilibre)
    df_fallback = pd.DataFrame(FALLBACK_DATA, columns=["content", "classification"])

    # Fusion (la BDD prime, les exemples complètent)
    df = pd.concat([df_db, df_fallback], ignore_index=True)
    df = df.dropna(subset=["content", "classification"])
    df = df[df["content"].str.strip() != ""]
    df["classification"] = df["classification"].str.upper().str.strip()

    # Ne garder que les classes valides du projet
    valid_classes = {"URGENT", "ADMINISTRATIF", "NORMAL"}
    df = df[df["classification"].isin(valid_classes)]

    print(f"\n📊 Dataset final : {len(df)} exemples")
    print(df["classification"].value_counts().to_string())
    return df


def train_and_save(df):
    """Entraîne le pipeline et sauvegarde le modèle."""

    X = df["content"]
    y = df["classification"]

    # ── Pipeline ──────────────────────────────────────────────────────
    # TfidfVectorizer > CountVectorizer : pondère les mots rares
    # MultinomialNB   : rapide, efficace sur texte court médical
    pipeline = make_pipeline(
        TfidfVectorizer(
            analyzer="word",
            ngram_range=(1, 2),   # unigrammes + bigrammes
            min_df=1,
            max_df=0.95,
            strip_accents="unicode",
            lowercase=True,
        ),
        MultinomialNB(alpha=0.5)  # alpha=0.5 = lissage de Laplace réduit
    )

    # ── Split train/test (si assez de données) ────────────────────────
    if len(df) >= 20:
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y
        )
        pipeline.fit(X_train, y_train)

        print("\n📈 Rapport d'évaluation (20% test) :")
        y_pred = pipeline.predict(X_test)
        print(classification_report(y_test, y_pred, zero_division=0))
    else:
        # Pas assez pour splitter → tout en entraînement
        pipeline.fit(X, y)
        print("✅ Modèle entraîné (dataset trop petit pour split train/test).")

    # ── Sauvegarde ────────────────────────────────────────────────────
    with open(MODEL_PATH, "wb") as f:
        pickle.dump(pipeline, f)
    print(f"\n💾 Modèle sauvegardé dans '{MODEL_PATH}'")

    # ── Test rapide de vérification ───────────────────────────────────
    print("\n🔬 Tests rapides :")
    tests = [
        ("douleur poitrine intense urgence cardiaque", "URGENT"),
        ("rendez-vous annulé besoin certificat médical", "ADMINISTRATIF"),
        ("suivi tension artérielle contrôle habituel", "NORMAL"),
    ]
    for text, expected in tests:
        pred = pipeline.predict([text])[0]
        proba = pipeline.predict_proba([text]).max()
        status = "✅" if pred == expected else "⚠️"
        print(f"  {status} '{text[:45]}...' → {pred} ({proba:.0%})")

    return pipeline


if __name__ == "__main__":
    print("=" * 55)
    print("  CardioLink — Entraînement du modèle de classification")
    print("=" * 55)

    df = prepare_training_data()

    if df.empty:
        print("❌ Aucune donnée disponible. Abandon.")
        sys.exit(1)

    train_and_save(df)
    print("\n🎉 Terminé ! Lancez maintenant : python app.py")
