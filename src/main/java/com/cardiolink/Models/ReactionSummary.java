package com.cardiolink.Models;

/**
 * DTO léger : résultat de l'agrégation COUNT(*) GROUP BY emoji
 * sur la table message_reaction pour un message donné.
 *
 * Exemple :  emoji="👍"  count=3
 *            emoji="❤️"  count=1
 */
public class ReactionSummary {

    private String emoji;
    private int    count;

    public ReactionSummary() {}

    public ReactionSummary(String emoji, int count) {
        this.emoji = emoji;
        this.count = count;
    }

    public String getEmoji()          { return emoji; }
    public void   setEmoji(String e)  { this.emoji = e; }

    public int    getCount()          { return count; }
    public void   setCount(int c)     { this.count = c; }

    /** Affichage compact : "👍 3" */
    public String getLabel() {
        return emoji + " " + count;
    }

    @Override
    public String toString() {
        return "ReactionSummary{emoji='" + emoji + "', count=" + count + "}";
    }
}
