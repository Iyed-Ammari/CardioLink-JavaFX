from fastapi import FastAPI
from sentence_transformers import SentenceTransformer, util
import numpy as np
import json

app = FastAPI()

print("Chargement du modèle de recommandation...")
# Modèle performant et compatible avec tes vecteurs actuels
embedder = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
print("Modèle prêt !")

# --- NOUVEAU : CET ENDPOINT MANQUAIT POUR TES NOUVEAUX POSTS ---
@app.post("/vectorize")
async def vectorize(data: dict):
    try:
        text = data.get('text', "")
        if not text:
            return {"vector": []}
        # Encode le texte et le transforme en liste JSON pour Java
        vector = embedder.encode(text).tolist()
        return {"vector": vector}
    except Exception as e:
        print(f"Erreur vectorisation : {e}")
        return {"vector": []}

@app.post("/rank_posts")
async def rank_posts(data: dict):
    try:
        # 1. Récupération du vecteur de l'utilisateur
        user_raw = data.get('user_vector')
        if isinstance(user_raw, str):
            user_vector = np.array(json.loads(user_raw))
        else:
            user_vector = np.array(user_raw)

        posts = data.get('posts', [])

        # 2. Calcul du score AMÉLIORÉ
        for post in posts:
            raw_vector = post.get('topic_vector') or post.get('topicVector')

            if raw_vector:
                if isinstance(raw_vector, str):
                    post_vector = np.array(json.loads(raw_vector))
                else:
                    post_vector = np.array(raw_vector)

                # Calcul de base (Similarité Cosinus)
                semantic_similarity = float(util.cos_sim(user_vector, post_vector))

                # --- AMÉLIORATION DU SCORE ---
                # On applique un seuil : si c'est < 0.3, c'est probablement hors-sujet
                if semantic_similarity < 0.30:
                    final_score = semantic_similarity * 0.5  # On dégrade le score
                else:
                    # On ajoute un petit bonus pour les likes (ex: 100 likes = +0.05)
                    likes = post.get('likes', 0)
                    popularity_bonus = min(likes / 2000, 0.08)
                    final_score = semantic_similarity + popularity_bonus

                post['score'] = final_score
            else:
                post['score'] = -1.0 # Les posts sans vecteurs vont tout en bas

        # 3. Tri rigoureux
        posts.sort(key=lambda x: x.get('score', -1), reverse=True)

        # 4. Nettoyage pour Java
        for post in posts:
            if 'score' in post:
                del post['score']

        return posts

    except Exception as e:
        print(f"Erreur de tri : {e}")
        return data.get('posts', [])

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8007)