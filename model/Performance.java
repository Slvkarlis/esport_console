package model;

// Performance — statistiques d'un joueur lors d'un match
public class Performance {

  // compteur statique pour auto-incrementer l'id
  private static int compteur = 1;

  // attributs
  private int    id;
  private Joueur joueur;
  private Match  match;
  private int    kills;
  private int    deaths;
  private int    assists;
  private int    dommagesInfliges;
  private int    dommagesSubis;
  private double precisionTirs; // en pourcentage [0..100]

  // constructeur
  public Performance(Joueur joueur, Match match) {
    this.id = compteur++;
    this.joueur = joueur;
    this.match = match;
    this.kills = 0;
    this.deaths = 0;
    this.assists = 0;
    this.dommagesInfliges = 0;
    this.dommagesSubis = 0;
    this.precisionTirs = 0.0;
  }

  // enregistre les stats — valide avec Math.max/min, puis met a jour joueur
  // (Math.clamp n'est disponible qu'a partir de Java 21 — projet compile en Java 17)
  @SuppressWarnings("java:S6885")
  public void enregistrerStats(int kills, int deaths, int assists,
                                int dommagesInfliges, int dommagesSubis,
                                double precisionTirs) {
    this.kills            = Math.max(0, kills);
    this.deaths           = Math.max(0, deaths);
    this.assists          = Math.max(0, assists);
    this.dommagesInfliges = Math.max(0, dommagesInfliges);
    this.dommagesSubis    = Math.max(0, dommagesSubis);
    this.precisionTirs    = Math.max(0.0, Math.min(100.0, precisionTirs));

    // calcule les points : kills*100 + assists*30 - deaths*20
    int points = this.kills * 100 + this.assists * 30 - this.deaths * 20;
    joueur.ajouterPoints(Math.max(0, points)); // ajouterPoints appelle deja mettreAJourRang
  }

  // KDA = (kills + assists) / max(1, deaths) — evite la division par zero
  public double calculerKDA() {
    return (double)(kills + assists) / Math.max(1, deaths);
  }

  // score de performance = KDA*40 + precision*0.3 + dommages/100
  public double calculerScorePerformance() {
    return calculerKDA() * 40.0 + precisionTirs * 0.3 + dommagesInfliges / 100.0;
  }

  // getters
  public int    getId()               { return id; }
  public Joueur getJoueur()           { return joueur; }
  public Match  getMatch()            { return match; }
  public int    getKills()            { return kills; }
  public int    getDeaths()           { return deaths; }
  public int    getAssists()          { return assists; }
  public int    getDommagesInfliges() { return dommagesInfliges; }
  public int    getDommagesSubis()    { return dommagesSubis; }
  public double getPrecisionTirs()    { return precisionTirs; }

  @Override
  public String toString() {
    return "[ID:" + id + "] " + joueur.getPseudo() +
           " | Match#" + match.getId() +
           " | K:" + kills + " D:" + deaths + " A:" + assists +
           " | KDA:" + String.format("%.2f", calculerKDA()) +
           " | Score:" + String.format("%.1f", calculerScorePerformance());
  }
}
