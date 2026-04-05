package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Classement — classe utilitaire avec methodes statiques uniquement
public class Classement {

  // classe non instanciable
  private Classement() {}

  // trie les joueurs par score total decroissant
  public static List<Joueur> trierJoueursParScore(List<Joueur> joueurs) {
    List<Joueur> trie = new ArrayList<>(joueurs);
    trie.sort(Comparator.comparingInt(Joueur::getScoreTotal).reversed());
    return trie;
  }

  // trie les equipes par nombre de victoires decroissant
  public static List<Equipe> trierEquipesParVictoires(List<Equipe> equipes) {
    List<Equipe> trie = new ArrayList<>(equipes);
    trie.sort(Comparator.comparingInt(Equipe::getNbVictoires).reversed()
              .thenComparingDouble(Equipe::getRatioVictoires).reversed());
    return trie;
  }

  // determine le MVP — joueur avec le meilleur score de performance
  public static Joueur determinerMVP(List<Performance> performances) {
    if (performances == null || performances.isEmpty()) return null;
    Performance meilleure = performances.stream()
        .max(Comparator.comparingDouble(Performance::calculerScorePerformance))
        .orElse(null);
    return meilleure != null ? meilleure.getJoueur() : null;
  }

  // affiche le classement des joueurs sous forme de tableau
  public static String afficherClassementJoueurs(List<Joueur> joueurs) {
    List<Joueur> trie = trierJoueursParScore(joueurs);
    StringBuilder sb = new StringBuilder("--- Classement des joueurs ---\n");
    for (int i = 0; i < trie.size(); i++) {
      Joueur j = trie.get(i);
      sb.append(String.format("%2d. %-15s | %-10s | %6d pts%n",
          i + 1, j.getPseudo(), j.getRang(), j.getScoreTotal()));
    }
    return sb.toString();
  }

  // affiche le classement des equipes sous forme de tableau
  public static String afficherClassementEquipes(List<Equipe> equipes) {
    List<Equipe> trie = trierEquipesParVictoires(equipes);
    StringBuilder sb = new StringBuilder("--- Classement des equipes ---\n");
    for (int i = 0; i < trie.size(); i++) {
      Equipe e = trie.get(i);
      sb.append(String.format("%2d. %-15s | V:%-3d D:%-3d | %.0f%%%n",
          i + 1, e.getNom(), e.getNbVictoires(), e.getNbDefaites(),
          e.getRatioVictoires() * 100));
    }
    return sb.toString();
  }
}
