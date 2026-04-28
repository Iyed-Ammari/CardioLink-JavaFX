from fastapi import FastAPI
from transformers import pipeline

app = FastAPI()

# Ton modèle actuel
model_name = "lxyuan/distilbert-base-multilingual-cased-sentiments-student"
classifier = pipeline("sentiment-analysis", model=model_name)

# --- LA ZONE DE CONFIGURATION (Tu ne modifieras que cette zone) ---
BLACKLIST = ["insulte1", "insulte2", "fuck", "connard"] # Ajoute tes mots ici
SEUIL_TOXICITE = 0.60 # Augmente pour être plus tolérant, baisse pour être plus strict
# ------------------------------------------------------------------

@app.post("/predict")
async def predict(data: dict):
    text = data.get('text', "").strip()
    text_lower = text.lower()

    # 1. Vérification par la Blacklist (Priorité absolue)
    if any(word in text_lower for word in BLACKLIST):
        return {"is_toxic": True, "score": 1.0, "label": "negative"}

    # 2. Vérification par IA
    result = classifier(text)[0]
    score = result['score']
    label = result['label'].lower()

    # 3. Logique de décision (Encapsulée ici pour ne pas toucher au Java)
    # Si c'est négatif ET que le score dépasse ton seuil, on bloque
    is_toxic = True if (label == "negative" and score > SEUIL_TOXICITE) else False

    print(f"Modération IA: '{text}' | Toxic: {is_toxic} | Score: {score:.4f}")

    return {
        "is_toxic": is_toxic,
        "score": float(score),
        "label": label
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8001)