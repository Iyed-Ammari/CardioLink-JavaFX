package com.cardiolink.utils;

public final class StripeConfig {

    private StripeConfig() {}

    // ⚠️ Après tes tests, régénère cette clé car elle a été exposée.
    public static final String SECRET_KEY =
            "sk_test_51TNGS92eXzbzHrlpUqYk46ngY16NS7CvkI5zm6SoVBcxCmqzSMLATljnfqDJdyn4tQIt1xUDZdAazUMvFH9xyU0x00koHZTwCH";

    // Stripe n'affiche pas TND dans la liste actuelle des devises supportées.
    // Pour le test, on utilise EUR côté Stripe.
    public static final String STRIPE_CURRENCY = "eur";

    public static final String LOCAL_HOST = "127.0.0.1";
    public static final String SUCCESS_PATH = "/payment/success";
    public static final String CANCEL_PATH  = "/payment/cancel";

    public static final String SUCCESS_HTML = """
            <html>
            <head><meta charset="UTF-8"><title>Paiement réussi</title></head>
            <body style="font-family: Arial, sans-serif; padding: 40px; background:#f6f7fb;">
              <div style="max-width:700px; margin:auto; background:white; border-radius:16px; padding:32px; box-shadow:0 8px 24px rgba(0,0,0,0.08);">
                <h1 style="color:#065F46; margin-top:0;">✅ Paiement confirmé</h1>
                <p>Le paiement Stripe a été validé. Vous pouvez revenir à l'application CardioLink.</p>
                <p>Vous pouvez maintenant fermer cet onglet.</p>
              </div>
            </body>
            </html>
            """;

    public static final String CANCEL_HTML = """
            <html>
            <head><meta charset="UTF-8"><title>Paiement annulé</title></head>
            <body style="font-family: Arial, sans-serif; padding: 40px; background:#f6f7fb;">
              <div style="max-width:700px; margin:auto; background:white; border-radius:16px; padding:32px; box-shadow:0 8px 24px rgba(0,0,0,0.08);">
                <h1 style="color:#B45309; margin-top:0;">⚠️ Paiement annulé</h1>
                <p>Le paiement n'a pas été terminé. Vous pouvez revenir à l'application CardioLink.</p>
                <p>Vous pouvez maintenant fermer cet onglet.</p>
              </div>
            </body>
            </html>
            """;

    public static final String ERROR_HTML = """
            <html>
            <head><meta charset="UTF-8"><title>Erreur paiement</title></head>
            <body style="font-family: Arial, sans-serif; padding: 40px; background:#f6f7fb;">
              <div style="max-width:700px; margin:auto; background:white; border-radius:16px; padding:32px; box-shadow:0 8px 24px rgba(0,0,0,0.08);">
                <h1 style="color:#DC2626; margin-top:0;">❌ Erreur</h1>
                <p>Une erreur s'est produite pendant le retour de paiement.</p>
                <p>Vous pouvez revenir à l'application CardioLink.</p>
              </div>
            </body>
            </html>
            """;
}