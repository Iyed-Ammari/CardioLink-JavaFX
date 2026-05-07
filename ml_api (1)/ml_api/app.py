from fastapi import FastAPI
import joblib
import pandas as pd
import mysql.connector

app = FastAPI()

# Charger modèle une seule fois
model = joblib.load("model_regression.pkl")

def get_daily_data():
    conn = mysql.connector.connect(
        host="127.0.0.1",
        user="root",
        password="",              # XAMPP: vide
        database="cardiolink"     # ✅ ton .env Symfony
    )

    query = """
    SELECT DATE(date_commande) as jour,
           COUNT(*) as nb_commandes
    FROM commande
    WHERE statut = 'PAYEE'
    GROUP BY DATE(date_commande)
    ORDER BY jour
    """

    df = pd.read_sql(query, conn)
    conn.close()

    df["jour"] = pd.to_datetime(df["jour"])
    return df

@app.get("/predict")
def predict(month: str):
    daily = get_daily_data()

    # Features
    daily["mois"] = daily["jour"].dt.month
    daily["jour_semaine"] = daily["jour"].dt.dayofweek
    daily["est_weekend"] = (daily["jour_semaine"] >= 5).astype(int)
    # min_periods=1 : produit une valeur dès la 1ère ligne au lieu d'attendre 7/30 lignes
    daily["ma7"]  = daily["nb_commandes"].rolling(7,  min_periods=1).mean()
    daily["ma30"] = daily["nb_commandes"].rolling(30, min_periods=1).mean()
    daily = daily.dropna()

    # Valeurs par défaut si pas assez de données historiques
    if daily.empty:
        ma7_last  = 0.0
        ma30_last = 0.0
    else:
        ma7_last  = float(daily["ma7"].iloc[-1])
        ma30_last = float(daily["ma30"].iloc[-1])

    # Générer jours du mois demandé
    start = pd.to_datetime(month + "-01")
    end = start + pd.offsets.MonthEnd(0)
    days = pd.date_range(start, end, freq="D")

    predictions = []
    for d in days:
        X = [[
            d.month,
            d.dayofweek,
            int(d.dayofweek >= 5),
            ma7_last,
            ma30_last,
        ]]
        pred = model.predict(X)[0]
        predictions.append({"date": str(d.date()), "prediction": float(pred)})

    top5 = sorted(predictions, key=lambda x: x["prediction"], reverse=True)[:10]
    return {"mois": month, "top_jours_pic": top5}
