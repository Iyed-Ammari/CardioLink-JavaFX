from fastapi import FastAPI
from transformers import pipeline
import uvicorn

app = FastAPI()

# Modèle spécialisé dans la toxicité multilingue
# Il détecte : toxic, severe_toxic, obscene, threat, insult, identity_hate
MODEL_NAME = "unitary/multilingual-toxic-xlm-roberta"

print(f"Chargement du modèle expert en toxicité : {MODEL_NAME}")

try:
    # On utilise 'text-classification' pour ce modèle spécifique
    toxic_classifier = pipeline("text-classification", model=MODEL_NAME, top_k=None)
    print("MODELE DE PROTECTION PRET")
except Exception as e:
    print(f"Erreur au chargement : {str(e)}")
    toxic_classifier = None

@app.post("/predict")
async def predict(data: dict):
    if toxic_classifier is None:
        return {"is_toxic": False, "score": 0.0, "label": "error"}

    text = data.get('text', "")

    # Le modèle renvoie une liste de scores pour chaque catégorie
    results = toxic_classifier(text)[0]

    # On cherche si une des catégories de toxicité dépasse un certain seuil
    # On cible particulièrement : 'toxic', 'insult', 'identity_hate', 'threat'
    is_toxic = False
    highest_score = 0.0
    detected_label = "clean"

    for prediction in results:
        label = prediction['label']
        score = prediction['score']

        # On garde le score le plus haut pour le renvoyer à Java
        if score > highest_score:
            highest_score = score

        # SEUIL DE TOLÉRANCE : 0.6 (60%)
        # Si le score de haine ou d'insulte est > 0.6, on bloque
        if score > 0.60:
            if label in ['toxic', 'severe_toxic', 'insult', 'identity_hate', 'threat']:
                is_toxic = True
                detected_label = label

    print(f"Analyse : '{text}' | Résultat : {detected_label} ({highest_score:.4f})")

    return {
        "is_toxic": is_toxic,
        "score": float(highest_score),
        "label": detected_label
    }

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8001)