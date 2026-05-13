import sys
from transformers import pipeline

# Chargement du modèle
MODEL_NAME = "unitary/multilingual-toxic-xlm-roberta"
moderation = pipeline("text-classification", model=MODEL_NAME, top_k=None)

# Lecture du texte envoyé par le service Java (Stdin)
texte = sys.stdin.read()

if not texte.strip():
    print("Texte vide")
    sys.exit(0)

results = moderation(texte)[0]
is_toxic = False

for prediction in results:
    label = prediction['label']
    score = prediction['score']

    # Seuil de toxicité de 60% [cite: 1]
    if score > 0.60 and label in ['toxic', 'severe_toxic', 'insult', 'identity_hate', 'threat']:
        is_toxic = True
        print(f"BLOQUÉ : {label}")
        break

if not is_toxic:
    print("OK")