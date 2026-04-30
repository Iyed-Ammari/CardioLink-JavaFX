"""
app.py — CardioLink ML Service (Flask)
========================================
API REST qui expose le modèle de classification des messages.
Appelée par ChatControllerAdvanced.java via HTTP.

Usage :
    python app.py

Endpoints :
    POST /analyze_message   → classification + confiance
    POST /analyze_batch     → analyse plusieurs messages à la fois
    GET  /health            → vérification de santé du service
    GET  /classes           → liste des classes supportées

Prérequis :
    pip install flask scikit-learn
"""

import os
import pickle
import logging
from datetime import datetime

from flask import Flask, request, jsonify
from flask import abort

# ── Configuration ─────────────────────────────────────────────────────
MODEL_PATH  = "cardio_model.pkl"
PORT        = 5000
DEBUG_MODE  = True  # Mettre False en production

# Classes attendues par CardioLink
VALID_CLASSES    = ["URGENT", "ADMINISTRATIF", "NORMAL"]
CLASS_ICONS      = {
    "URGENT":        "🔴",
    "ADMINISTRATIF": "📋",
    "NORMAL":        "💬",
}
CLASS_PRIORITIES = {
    "URGENT":        1,
    "ADMINISTRATIF": 2,
    "NORMAL":        3,
}

# ── Logging ───────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S"
)
log = logging.getLogger(__name__)

# ── Application Flask ─────────────────────────────────────────────────
app = Flask(__name__)

# ── Chargement du modèle ──────────────────────────────────────────────
model = None

def load_model():
    """Charge le modèle depuis cardio_model.pkl."""
    global model
    if not os.path.exists(MODEL_PATH):
        log.error("❌ Modèle introuvable. Lancez d'abord : python train_model.py")
        return False
    try:
        with open(MODEL_PATH, "rb") as f:
            model = pickle.load(f)
        log.info(f"✅ Modèle chargé depuis '{MODEL_PATH}'")
        return True
    except Exception as e:
        log.error(f"❌ Erreur chargement modèle : {e}")
        return False


# ═══════════════════════════════════════════════════════════════════════
# ENDPOINT 1 : /analyze_message
# Analyse un seul message — appelé par ChatControllerAdvanced.java
# ═══════════════════════════════════════════════════════════════════════
@app.route("/analyze_message", methods=["POST"])
def analyze_message():
    """
    Corps JSON attendu :
        { "content": "texte du message" }

    Réponse JSON :
        {
          "classification": "URGENT",
          "confidence":     97.34,
          "icon":           "🔴",
          "priority":       1,
          "timestamp":      "2026-04-30T04:00:00"
        }
    """
    if model is None:
        return jsonify({"error": "Modèle non chargé. Lancez train_model.py"}), 503

    data = request.get_json(silent=True)
    if not data:
        return jsonify({"error": "Corps JSON requis"}), 400

    content = data.get("content", "").strip()
    if not content:
        return jsonify({"error": "Le champ 'content' est vide"}), 400

    try:
        # Prédiction
        classification = model.predict([content])[0]
        probas         = model.predict_proba([content])[0]
        confidence     = float(probas.max()) * 100

        # Détail des probabilités par classe
        classes_detail = {
            cls: round(float(p) * 100, 2)
            for cls, p in zip(model.classes_, probas)
        }

        log.info(f"📩 '{content[:40]}...' → {classification} ({confidence:.1f}%)")

        return jsonify({
            "classification": classification,
            "confidence":     round(confidence, 2),
            "icon":           CLASS_ICONS.get(classification, "💬"),
            "priority":       CLASS_PRIORITIES.get(classification, 3),
            "probabilities":  classes_detail,
            "timestamp":      datetime.now().isoformat(timespec="seconds"),
        })

    except Exception as e:
        log.error(f"Erreur prédiction : {e}")
        return jsonify({"error": str(e)}), 500


# ═══════════════════════════════════════════════════════════════════════
# ENDPOINT 2 : /analyze_batch
# Analyse plusieurs messages en une seule requête
# ═══════════════════════════════════════════════════════════════════════
@app.route("/analyze_batch", methods=["POST"])
def analyze_batch():
    """
    Corps JSON attendu :
        { "messages": ["texte 1", "texte 2", ...] }

    Réponse JSON :
        { "results": [ { "content":"...", "classification":"URGENT", ... }, ... ] }
    """
    if model is None:
        return jsonify({"error": "Modèle non chargé"}), 503

    data = request.get_json(silent=True)
    if not data or "messages" not in data:
        return jsonify({"error": "Champ 'messages' (array) requis"}), 400

    messages = [str(m).strip() for m in data["messages"] if str(m).strip()]
    if not messages:
        return jsonify({"error": "Liste de messages vide"}), 400

    if len(messages) > 50:
        return jsonify({"error": "Maximum 50 messages par requête"}), 400

    try:
        classifications = model.predict(messages)
        all_probas      = model.predict_proba(messages)

        results = []
        for content, classification, probas in zip(messages, classifications, all_probas):
            results.append({
                "content":        content[:100],
                "classification": classification,
                "confidence":     round(float(probas.max()) * 100, 2),
                "icon":           CLASS_ICONS.get(classification, "💬"),
                "priority":       CLASS_PRIORITIES.get(classification, 3),
            })

        # Trier par priorité (URGENT en premier)
        results.sort(key=lambda r: r["priority"])

        log.info(f"📦 Batch de {len(messages)} messages analysé.")
        return jsonify({"results": results, "count": len(results)})

    except Exception as e:
        log.error(f"Erreur batch : {e}")
        return jsonify({"error": str(e)}), 500


# ═══════════════════════════════════════════════════════════════════════
# ENDPOINT 3 : /suggest_replies
# Suggestions de réponses contextuelles basées sur la classification
# Utilisé par les suggestions intelligentes de ChatControllerAdvanced
# ═══════════════════════════════════════════════════════════════════════
REPLY_SUGGESTIONS = {
    "URGENT": [
        "Je vous contacte immédiatement pour une consultation d'urgence.",
        "Appelez le 15 (SAMU) si les symptômes s'aggravent.",
        "Je prends en charge votre situation en priorité absolue.",
    ],
    "ADMINISTRATIF": [
        "Votre document est bien enregistré dans votre dossier.",
        "Je transmets votre demande au service concerné.",
        "Je vous ferai parvenir les documents dans les 24h.",
    ],
    "NORMAL": [
        "Merci pour votre message, je reviendrai vers vous rapidement.",
        "Votre bilan semble satisfaisant, continuez le traitement.",
        "Je vous propose un rendez-vous de suivi le mois prochain.",
    ],
}

@app.route("/suggest_replies", methods=["POST"])
def suggest_replies():
    """
    Corps JSON attendu :
        { "content": "texte du message" }

    Réponse JSON :
        { "classification": "URGENT", "suggestions": ["...", "...", "..."] }
    """
    if model is None:
        return jsonify({"error": "Modèle non chargé"}), 503

    data = request.get_json(silent=True)
    content = (data or {}).get("content", "").strip()
    if not content:
        return jsonify({"error": "Champ 'content' requis"}), 400

    try:
        classification = model.predict([content])[0]
        suggestions    = REPLY_SUGGESTIONS.get(classification, REPLY_SUGGESTIONS["NORMAL"])
        confidence     = float(model.predict_proba([content])[0].max()) * 100

        return jsonify({
            "classification": classification,
            "confidence":     round(confidence, 2),
            "suggestions":    suggestions,
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ═══════════════════════════════════════════════════════════════════════
# ENDPOINT 4 : /health  (GET)
# Vérification de santé — appelé au démarrage de l'app JavaFX
# ═══════════════════════════════════════════════════════════════════════
@app.route("/health", methods=["GET"])
def health():
    """Retourne l'état du service ML."""
    return jsonify({
        "status":     "ok" if model is not None else "degraded",
        "model":      MODEL_PATH if model is not None else None,
        "classes":    list(model.classes_) if model is not None else [],
        "timestamp":  datetime.now().isoformat(timespec="seconds"),
        "service":    "CardioLink ML Service v1.0",
    })


# ═══════════════════════════════════════════════════════════════════════
# ENDPOINT 5 : /classes  (GET)
# Liste des classes supportées
# ═══════════════════════════════════════════════════════════════════════
@app.route("/classes", methods=["GET"])
def classes():
    return jsonify({
        "classes": [
            {"name": cls, "icon": CLASS_ICONS[cls], "priority": CLASS_PRIORITIES[cls]}
            for cls in VALID_CLASSES
        ]
    })


# ── CORS — Autoriser les requêtes depuis JavaFX (localhost) ───────────
@app.after_request
def add_cors_headers(response):
    response.headers["Access-Control-Allow-Origin"]  = "*"
    response.headers["Access-Control-Allow-Methods"] = "GET, POST, OPTIONS"
    response.headers["Access-Control-Allow-Headers"] = "Content-Type"
    return response

@app.route("/", defaults={"path": ""}, methods=["OPTIONS"])
@app.route("/<path:path>", methods=["OPTIONS"])
def options_handler(path):
    return "", 204


# ── Point d'entrée ────────────────────────────────────────────────────
if __name__ == "__main__":
    print("=" * 55)
    print("  CardioLink ML Service — Flask API")
    print("=" * 55)

    if load_model():
        print(f"\n🚀 Serveur démarré sur http://localhost:{PORT}")
        print("   Endpoints disponibles :")
        print("   POST /analyze_message  → classification d'un message")
        print("   POST /analyze_batch    → classification en lot")
        print("   POST /suggest_replies  → suggestions de réponses")
        print("   GET  /health           → état du service")
        print("   GET  /classes          → classes supportées\n")
        app.run(port=PORT, debug=DEBUG_MODE, host="0.0.0.0")
    else:
        print("\n❌ Impossible de démarrer sans modèle.")
        print("   Lancez d'abord : python train_model.py\n")
