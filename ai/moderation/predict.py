from transformers import pipeline

# Utilise EXACTEMENT le même modèle que app.py
MODEL_NAME = "unitary/multilingual-toxic-xlm-roberta"
moderation = pipeline("text-classification", model=MODEL_NAME, top_k=None)

texte = "C'est un exemple de commentaire"

results = moderation(texte)[0]

is_toxic = False
for prediction in results:
    label = prediction['label']
    score = prediction['score']

    # Même logique que dans ton app.py
    if score > 0.60 and label in ['toxic', 'severe_toxic', 'insult', 'identity_hate', 'threat']:
        is_toxic = True
        print(f"⚠ BLOQUÉ : {label} ({score:.2%})")
        break

if not is_toxic:
    print("✅ Commentaire OK")