from fastapi import FastAPI
from sentence_transformers import SentenceTransformer, util
import numpy as np
import json

app = FastAPI()

print("Chargement du modèle de recommandation...")
# Modèle multilingue performant pour comparer le sens des phrases
embedder = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
print("Modèle prêt !")

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

        # 2. Calcul du score pour chaque post
        for post in posts:
            # On utilise le champ exact de ta base : topic_vector
            raw_vector = post.get('topic_vector') or post.get('topicVector')
            
            if raw_vector:
                # Conversion du texte de la base en nombres
                if isinstance(raw_vector, str):
                    post_vector = np.array(json.loads(raw_vector))
                else:
                    post_vector = np.array(raw_vector)
                
                # Calcul de similarité (Cosine Similarity)
                score = float(util.cos_sim(user_vector, post_vector))
                post['score'] = score
            else:
                post['score'] = 0.0

        # 3. Tri des posts du plus pertinent au moins pertinent
        posts.sort(key=lambda x: x.get('score', 0), reverse=True)

        # 4. Nettoyage pour Java (on enlève le score pour éviter l'erreur "Unrecognized field")
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