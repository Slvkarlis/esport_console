import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import exception.EsportException;
import model.Administrateur;
import model.Coach;
import model.Equipe;
import model.Joueur;
import model.Match;
import model.Tournoi;
import model.Utilisateur;
import service.GestionEquipe;
import service.GestionMatch;
import service.GestionPerformance;
import service.GestionTournoi;
import service.GestionUtilisateur;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API HTTP locale sans Spring Boot.
 * Fournit les routes utilisées par le frontend React.
 */
public class ApiServer {

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final GestionUtilisateur gestionUtilisateur;
    private final GestionEquipe gestionEquipe;
    private final GestionTournoi gestionTournoi;
    private final GestionMatch gestionMatch;
    private final GestionPerformance gestionPerformance;
    private final BackendDataStore dataStore;

    private HttpServer server;

    public ApiServer(
            GestionUtilisateur gestionUtilisateur,
            GestionEquipe gestionEquipe,
            GestionTournoi gestionTournoi,
            GestionMatch gestionMatch,
            GestionPerformance gestionPerformance,
            BackendDataStore dataStore
    ) {
        this.gestionUtilisateur = gestionUtilisateur;
        this.gestionEquipe = gestionEquipe;
        this.gestionTournoi = gestionTournoi;
        this.gestionMatch = gestionMatch;
        this.gestionPerformance = gestionPerformance;
        this.dataStore = dataStore;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", new ApiHandler());
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
            stop();
        }));

        System.out.println("[API] Backend en ligne sur http://localhost:" + port + "/api");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendNoContent(exchange);
                return;
            }

            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            String route = path.substring("/api".length());
            if (route.isEmpty()) {
                route = "/";
            }

            try {
                if ("/".equals(route) && "GET".equalsIgnoreCase(method)) {
                    sendJson(exchange, 200, "{\"name\":\"esport-console-api\",\"status\":\"ok\"}");
                    return;
                }

                if ("/health".equals(route) && "GET".equalsIgnoreCase(method)) {
                    sendJson(exchange, 200, "{\"status\":\"UP\"}");
                    return;
                }

                if ("/utilisateurs".equals(route)) {
                    if ("GET".equalsIgnoreCase(method)) {
                        handleGetUsers(exchange);
                        return;
                    }
                    if ("POST".equalsIgnoreCase(method)) {
                        handleCreateUser(exchange);
                        return;
                    }
                }

                if (route.startsWith("/utilisateurs/") && "DELETE".equalsIgnoreCase(method)) {
                    handleDeleteUser(exchange, route);
                    return;
                }

                if (route.startsWith("/utilisateurs/") && "PUT".equalsIgnoreCase(method)) {
                    handleUpdateUser(exchange, route);
                    return;
                }

                if ("/equipes".equals(route)) {
                    if ("GET".equalsIgnoreCase(method)) {
                        handleGetTeams(exchange);
                        return;
                    }
                    if ("POST".equalsIgnoreCase(method)) {
                        handleCreateTeam(exchange);
                        return;
                    }
                }

                if (route.startsWith("/equipes/") && "PUT".equalsIgnoreCase(method)) {
                    handleUpdateTeam(exchange, route);
                    return;
                }

                if (route.startsWith("/equipes/") && "DELETE".equalsIgnoreCase(method)) {
                    handleDeleteTeam(exchange, route);
                    return;
                }

                if ("/tournois".equals(route)) {
                    if ("GET".equalsIgnoreCase(method)) {
                        handleGetTournaments(exchange);
                        return;
                    }
                    if ("POST".equalsIgnoreCase(method)) {
                        handleCreateTournament(exchange);
                        return;
                    }
                }

                if (route.startsWith("/tournois/") && "PUT".equalsIgnoreCase(method)) {
                    handleUpdateTournament(exchange, route);
                    return;
                }

                if (route.startsWith("/tournois/") && "DELETE".equalsIgnoreCase(method)) {
                    handleDeleteTournament(exchange, route);
                    return;
                }

                if ("/matchs".equals(route)) {
                    if ("GET".equalsIgnoreCase(method)) {
                        handleGetMatches(exchange);
                        return;
                    }
                    if ("POST".equalsIgnoreCase(method)) {
                        handleCreateMatch(exchange);
                        return;
                    }
                }

                if (route.startsWith("/matchs/") && "PUT".equalsIgnoreCase(method)) {
                    handleUpdateMatch(exchange, route);
                    return;
                }

                if (route.startsWith("/matchs/") && "DELETE".equalsIgnoreCase(method)) {
                    handleDeleteMatch(exchange, route);
                    return;
                }

                if ("/classements/joueurs".equals(route) && "GET".equalsIgnoreCase(method)) {
                    handlePlayerRanking(exchange);
                    return;
                }

                if ("/classements/equipes".equals(route) && "GET".equalsIgnoreCase(method)) {
                    handleTeamRanking(exchange);
                    return;
                }

                sendJson(exchange, 404, "{\"error\":\"Route introuvable\"}");
            } catch (EsportException e) {
                sendJson(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"Erreur serveur\",\"detail\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        List<String> items = new ArrayList<>();
        for (Utilisateur u : gestionUtilisateur.afficherTous()) {
            items.add(toUserJson(u));
        }
        sendJson(exchange, 200, toJsonArray(items));
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException, EsportException {
        String body = readBody(exchange);
        Map<String, String> payload = parseJsonObject(body);

        String pseudo = trimOrEmpty(payload.get("pseudo"));
        String email = trimOrEmpty(payload.get("email"));
        String role = trimOrEmpty(payload.get("role"));
        String niveau = trimOrEmpty(payload.get("niveau"));
        String equipeNom = trimOrEmpty(payload.get("equipe"));

        if (pseudo.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("pseudo et email sont obligatoires");
        }
        validateEmail(email);
        if (niveau.isEmpty()) {
            niveau = "0";
        }
        if (role.isEmpty()) {
            role = "JOUEUR";
        }

        Utilisateur created;
        switch (role.toUpperCase()) {
            case "COACH":
                created = new Coach(pseudo, email, niveau, "Strategie", 1);
                break;
            case "ADMINISTRATEUR":
                created = new Administrateur(pseudo, email, niveau, "PLATFORM_ADMIN");
                break;
            case "JOUEUR":
            default:
                Joueur joueur = new Joueur(pseudo, email, niveau, "VALORANT");
                created = joueur;
                break;
        }

        gestionUtilisateur.ajouter(created);

        if (created instanceof Joueur) {
            Joueur joueur = (Joueur) created;
            if (!equipeNom.isEmpty() && !"Aucune".equalsIgnoreCase(equipeNom)) {
                try {
                    Equipe equipe = gestionEquipe.rechercherParNom(equipeNom);
                    gestionEquipe.ajouterJoueurAEquipe(equipe.getId(), joueur);
                } catch (EsportException ignored) {
                    joueur.setNomEquipe(equipeNom);
                }
            }
        }

        if (created instanceof Coach) {
            Coach coach = (Coach) created;
            if (!equipeNom.isEmpty() && !"Aucune".equalsIgnoreCase(equipeNom)) {
                try {
                    Equipe equipe = gestionEquipe.rechercherParNom(equipeNom);
                    gestionEquipe.affecterCoach(equipe.getId(), coach);
                } catch (EsportException ignored) {
                    coach.setEquipeCoachee(equipeNom);
                }
            }
        }

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendJson(exchange, 201, toUserJson(created));
    }

    private void handleDeleteUser(HttpExchange exchange, String route) throws IOException, EsportException {
        String rawId = route.substring("/utilisateurs/".length());
        int id = Integer.parseInt(rawId);

        Utilisateur user = gestionUtilisateur.rechercherParId(id);

        if (user instanceof Joueur) {
            Joueur joueur = (Joueur) user;
            if (!joueur.getNomEquipe().isEmpty()) {
                try {
                    Equipe equipe = gestionEquipe.rechercherParNom(joueur.getNomEquipe());
                    gestionEquipe.retirerJoueurDeEquipe(equipe.getId(), joueur);
                } catch (Exception ignored) {
                }
            }
        }

        if (user instanceof Coach) {
            Coach coach = (Coach) user;
            if (!coach.estDisponible()) {
                try {
                    Equipe equipe = gestionEquipe.rechercherParNom(coach.getEquipeCoachee());
                    gestionEquipe.affecterCoach(equipe.getId(), null);
                } catch (Exception ignored) {
                }
            }
        }

        gestionUtilisateur.supprimer(id);
        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendNoContent(exchange);
    }

    private void handleUpdateUser(HttpExchange exchange, String route) throws IOException, EsportException {
        int id = Integer.parseInt(route.substring("/utilisateurs/".length()));
        String body = readBody(exchange);
        Map<String, String> payload = parseJsonObject(body);

        String pseudo = trimOrEmpty(payload.get("pseudo"));
        String email = trimOrEmpty(payload.get("email"));
        String niveau = trimOrEmpty(payload.get("niveau"));
        String equipeNom = trimOrEmpty(payload.get("equipe"));

        if (pseudo.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("pseudo et email sont obligatoires");
        }
        validateEmail(email);
        if (niveau.isEmpty()) {
            niveau = "0";
        }

        Utilisateur user = gestionUtilisateur.rechercherParId(id);
        gestionUtilisateur.modifier(id, pseudo, email, niveau);

        if (user instanceof Joueur) {
            Joueur joueur = (Joueur) user;
            joueur.setNomEquipe((equipeNom.isEmpty() || "Aucune".equalsIgnoreCase(equipeNom)) ? "" : equipeNom);
        }

        if (user instanceof Coach) {
            Coach coach = (Coach) user;
            coach.setEquipeCoachee((equipeNom.isEmpty() || "Aucune".equalsIgnoreCase(equipeNom)) ? "" : equipeNom);
        }

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendJson(exchange, 200, toUserJson(gestionUtilisateur.rechercherParId(id)));
    }

    private void handleCreateTeam(HttpExchange exchange) throws IOException, EsportException {
        String body = readBody(exchange);
        Map<String, String> payload = parseJsonObject(body);

        String nom = trimOrEmpty(payload.get("nom"));
        String jeu = trimOrEmpty(payload.get("jeu"));

        if (nom.isEmpty() || jeu.isEmpty()) {
            throw new IllegalArgumentException("nom et jeu sont obligatoires");
        }

        Equipe equipe = new Equipe(nom, jeu);
        gestionEquipe.ajouter(equipe);

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendJson(exchange, 201, toTeamJson(equipe));
    }

    private void handleUpdateTeam(HttpExchange exchange, String route) throws IOException, EsportException {
        int id = Integer.parseInt(route.substring("/equipes/".length()));
        String body = readBody(exchange);
        Map<String, String> payload = parseJsonObject(body);

        String nom = trimOrEmpty(payload.get("nom"));
        String jeu = trimOrEmpty(payload.get("jeu"));
        if (nom.isEmpty() || jeu.isEmpty()) {
            throw new IllegalArgumentException("nom et jeu sont obligatoires");
        }

        gestionEquipe.modifier(id, nom, jeu);
        Equipe equipe = gestionEquipe.rechercherParId(id);

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendJson(exchange, 200, toTeamJson(equipe));
    }

    private void handleDeleteTeam(HttpExchange exchange, String route) throws IOException, EsportException {
        int id = Integer.parseInt(route.substring("/equipes/".length()));
        gestionEquipe.supprimer(id);

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendNoContent(exchange);
    }

    private void handleCreateTournament(HttpExchange exchange) throws IOException, EsportException {
        String body = readBody(exchange);
        Map<String, String> payload = parseJsonObject(body);

        String nom = trimOrEmpty(payload.get("nom"));
        String jeu = trimOrEmpty(payload.get("jeu"));
        String formatValue = trimOrEmpty(payload.get("format"));
        String dateDebutValue = trimOrEmpty(payload.get("dateDebut"));
        String dateFinValue = trimOrEmpty(payload.get("dateFin"));
        int nbEquipesMax = parseIntOrDefault(payload.get("max"), parseIntOrDefault(payload.get("nbEquipesMax"), 8));

        if (nom.isEmpty() || jeu.isEmpty() || formatValue.isEmpty() || dateDebutValue.isEmpty() || dateFinValue.isEmpty()) {
            throw new IllegalArgumentException("nom, jeu, format, dateDebut et dateFin sont obligatoires");
        }

        Tournoi.Format format = parseTournoiFormat(formatValue);
        LocalDate dateDebut = parseLocalDate(dateDebutValue, "dateDebut");
        LocalDate dateFin = parseLocalDate(dateFinValue, "dateFin");
        validateTournamentDateRange(dateDebut, dateFin);

        Tournoi tournoi = new Tournoi(nom, jeu, format, dateDebut, dateFin, Math.max(2, nbEquipesMax));
        gestionTournoi.ajouter(tournoi);

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendJson(exchange, 201, toTournamentJson(tournoi));
    }

    private void handleUpdateTournament(HttpExchange exchange, String route) throws IOException, EsportException {
        int id = Integer.parseInt(route.substring("/tournois/".length()));
        String body = readBody(exchange);
        Map<String, String> payload = parseJsonObject(body);

        Tournoi tournoi = gestionTournoi.rechercherParId(id);

        String nom = trimOrEmpty(payload.get("nom"));
        String jeu = trimOrEmpty(payload.get("jeu"));
        String formatValue = trimOrEmpty(payload.get("format"));
        String dateDebutValue = trimOrEmpty(payload.get("dateDebut"));
        String dateFinValue = trimOrEmpty(payload.get("dateFin"));
        String statutValue = trimOrEmpty(payload.get("statut"));
        int nbEquipesMax = parseIntOrDefault(payload.get("max"), parseIntOrDefault(payload.get("nbEquipesMax"), tournoi.getNbEquipesMax()));

        LocalDate effectiveDateDebut = tournoi.getDateDebut();
        LocalDate effectiveDateFin = tournoi.getDateFin();
        if (!dateDebutValue.isEmpty()) {
            effectiveDateDebut = parseLocalDate(dateDebutValue, "dateDebut");
        }
        if (!dateFinValue.isEmpty()) {
            effectiveDateFin = parseLocalDate(dateFinValue, "dateFin");
        }
        validateTournamentDateRange(effectiveDateDebut, effectiveDateFin);

        if (!nom.isEmpty()) tournoi.setNom(nom);
        if (!jeu.isEmpty()) tournoi.setJeu(jeu);
        if (!formatValue.isEmpty()) tournoi.setFormat(parseTournoiFormat(formatValue));
        if (!dateDebutValue.isEmpty()) tournoi.setDateDebut(effectiveDateDebut);
        if (!dateFinValue.isEmpty()) tournoi.setDateFin(effectiveDateFin);
        tournoi.setNbEquipesMax(Math.max(2, nbEquipesMax));
        if (!statutValue.isEmpty()) tournoi.setStatut(parseTournoiStatut(statutValue));

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendJson(exchange, 200, toTournamentJson(tournoi));
    }

    private void handleDeleteTournament(HttpExchange exchange, String route) throws IOException, EsportException {
        int id = Integer.parseInt(route.substring("/tournois/".length()));
        gestionTournoi.supprimer(id);

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendNoContent(exchange);
    }

    private void handleCreateMatch(HttpExchange exchange) throws IOException, EsportException {
        String body = readBody(exchange);
        Map<String, String> payload = parseJsonObject(body);

        int equipe1Id = parseRequiredInt(payload.get("equipe1Id"), "equipe1Id");
        int equipe2Id = parseRequiredInt(payload.get("equipe2Id"), "equipe2Id");
        String dateHeureValue = trimOrEmpty(payload.get("dateHeure"));
        String tournoiNom = trimOrEmpty(payload.get("tournoi"));
        if (tournoiNom.isEmpty()) tournoiNom = trimOrEmpty(payload.get("tournoiNom"));

        LocalDateTime dateHeure = dateHeureValue.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(dateHeureValue);
        Equipe e1 = gestionEquipe.rechercherParId(equipe1Id);
        Equipe e2 = gestionEquipe.rechercherParId(equipe2Id);

        Match match = new Match(e1, e2, dateHeure, tournoiNom);
        applyMatchStateFromPayload(match, payload);
        gestionMatch.ajouter(match);

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendJson(exchange, 201, toMatchJson(match));
    }

    private void handleUpdateMatch(HttpExchange exchange, String route) throws IOException, EsportException {
        int id = Integer.parseInt(route.substring("/matchs/".length()));
        String body = readBody(exchange);
        Map<String, String> payload = parseJsonObject(body);

        Match match = gestionMatch.rechercherParId(id);

        int equipe1Id = parseIntOrDefault(payload.get("equipe1Id"), -1);
        int equipe2Id = parseIntOrDefault(payload.get("equipe2Id"), -1);
        if (equipe1Id > 0) {
            match.setEquipe1(gestionEquipe.rechercherParId(equipe1Id));
        }
        if (equipe2Id > 0) {
            match.setEquipe2(gestionEquipe.rechercherParId(equipe2Id));
        }

        String dateHeureValue = trimOrEmpty(payload.get("dateHeure"));
        if (!dateHeureValue.isEmpty()) {
            match.setDateHeure(LocalDateTime.parse(dateHeureValue));
        }

        String tournoiNom = trimOrEmpty(payload.get("tournoi"));
        if (tournoiNom.isEmpty()) tournoiNom = trimOrEmpty(payload.get("tournoiNom"));
        if (!tournoiNom.isEmpty()) {
            match.setTournoiNom(tournoiNom);
        }

        applyMatchStateFromPayload(match, payload);

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendJson(exchange, 200, toMatchJson(match));
    }

    private void handleDeleteMatch(HttpExchange exchange, String route) throws IOException, EsportException {
        int id = Integer.parseInt(route.substring("/matchs/".length()));
        gestionMatch.supprimer(id);

        dataStore.saveAll(gestionUtilisateur, gestionEquipe, gestionTournoi, gestionMatch);
        sendNoContent(exchange);
    }

    private void handleGetTeams(HttpExchange exchange) throws IOException {
        List<String> items = new ArrayList<>();
        for (Equipe e : gestionEquipe.afficherToutes()) {
            items.add(toTeamJson(e));
        }
        sendJson(exchange, 200, toJsonArray(items));
    }

    private void handleGetTournaments(HttpExchange exchange) throws IOException {
        List<String> items = new ArrayList<>();
        for (Tournoi t : gestionTournoi.afficherTous()) {
            items.add(toTournamentJson(t));
        }
        sendJson(exchange, 200, toJsonArray(items));
    }

    private void handleGetMatches(HttpExchange exchange) throws IOException {
        List<String> items = new ArrayList<>();
        for (Match m : gestionMatch.afficherTous()) {
            items.add(toMatchJson(m));
        }
        sendJson(exchange, 200, toJsonArray(items));
    }

    private void handlePlayerRanking(HttpExchange exchange) throws IOException {
        List<Joueur> joueurs = new ArrayList<>(gestionUtilisateur.getJoueurs());
        joueurs.sort(Comparator.comparingInt(Joueur::getScoreTotal).reversed());

        List<String> items = new ArrayList<>();
        for (Joueur j : joueurs) {
            items.add("{"
                    + "\"id\":" + j.getId() + ","
                    + "\"pseudo\":\"" + escapeJson(j.getPseudo()) + "\","
                    + "\"equipe\":\"" + escapeJson(j.getNomEquipe().isEmpty() ? "Aucune" : j.getNomEquipe()) + "\","
                    + "\"pts\":" + j.getScoreTotal() + ","
                    + "\"rang\":\"" + escapeJson(j.getRang()) + "\","
                    + "\"wins\":0,"
                    + "\"losses\":0"
                    + "}");
        }

        sendJson(exchange, 200, toJsonArray(items));
    }

    private void handleTeamRanking(HttpExchange exchange) throws IOException {
        List<Equipe> equipes = new ArrayList<>(gestionEquipe.afficherToutes());
        equipes.sort(Comparator.comparingInt(Equipe::getNbVictoires).reversed());

        List<String> items = new ArrayList<>();
        for (Equipe e : equipes) {
            items.add("{"
                    + "\"id\":" + e.getId() + ","
                    + "\"nom\":\"" + escapeJson(e.getNom()) + "\","
                    + "\"wins\":" + e.getNbVictoires() + ","
                    + "\"losses\":" + e.getNbDefaites() + ","
                    + "\"ratio\":" + String.format("%.3f", e.getRatioVictoires())
                    + "}");
        }
        sendJson(exchange, 200, toJsonArray(items));
    }

    private static String toTeamJson(Equipe e) {
        String coach = (e.getCoach() != null) ? e.getCoach().getPseudo() : "Aucun";
        String capitaine = (e.getCapitaine() != null) ? e.getCapitaine().getPseudo() : "Aucun";
        int total = e.getNbVictoires() + e.getNbDefaites();
        int winRate = (total == 0) ? 0 : (int) Math.round((e.getNbVictoires() * 100.0) / total);

        return "{"
                + "\"id\":" + e.getId() + ","
                + "\"nom\":\"" + escapeJson(e.getNom()) + "\","
                + "\"jeu\":\"" + escapeJson(e.getJeu()) + "\","
                + "\"coach\":\"" + escapeJson(coach) + "\","
                + "\"capitaine\":\"" + escapeJson(capitaine) + "\","
                + "\"joueurs\":" + e.getJoueurs().size() + ","
                + "\"wins\":" + e.getNbVictoires() + ","
                + "\"losses\":" + e.getNbDefaites() + ","
                + "\"winRate\":" + winRate
                + "}";
    }

    private static String toTournamentJson(Tournoi t) {
        String statut;
        switch (t.getStatut()) {
            case EN_COURS:
                statut = "LIVE";
                break;
            case TERMINE:
                statut = "TERMINE";
                break;
            case EN_ATTENTE:
            default:
                statut = "PROCHAIN";
                break;
        }

        return "{"
                + "\"id\":" + t.getId() + ","
                + "\"nom\":\"" + escapeJson(t.getNom()) + "\","
                + "\"jeu\":\"" + escapeJson(t.getJeu()) + "\","
                + "\"format\":\"" + escapeJson(t.getFormat().name()) + "\","
                + "\"statut\":\"" + statut + "\","
                + "\"equipes\":" + t.getEquipes().size() + ","
                + "\"max\":" + t.getNbEquipesMax() + ","
                + "\"dateDebut\":\"" + t.getDateDebut() + "\","
                + "\"dateFin\":\"" + t.getDateFin() + "\""
                + "}";
    }

    private static String toMatchJson(Match m) {
        String resultat = m.estTermine()
                ? (m.getGagnant() == null ? "NUL" : "WIN " + m.getGagnant().getNom())
                : "EN ATTENTE";

        return "{"
                + "\"id\":" + m.getId() + ","
                + "\"equipe1\":\"" + escapeJson(m.getEquipe1().getNom()) + "\","
                + "\"equipe2\":\"" + escapeJson(m.getEquipe2().getNom()) + "\","
                + "\"equipe1Id\":" + m.getEquipe1().getId() + ","
                + "\"equipe2Id\":" + m.getEquipe2().getId() + ","
                + "\"score\":\"" + m.getScoreEquipe1() + "-" + m.getScoreEquipe2() + "\","
                + "\"score1\":" + m.getScoreEquipe1() + ","
                + "\"score2\":" + m.getScoreEquipe2() + ","
                + "\"jeu\":\"" + escapeJson(m.getEquipe1().getJeu()) + "\","
                + "\"tournoi\":\"" + escapeJson(m.getTournoiNom()) + "\","
                + "\"tournoiNom\":\"" + escapeJson(m.getTournoiNom()) + "\","
                + "\"dateHeure\":\"" + m.getDateHeure() + "\","
                + "\"statut\":\"" + escapeJson(m.getStatut().name()) + "\","
                + "\"resultat\":\"" + escapeJson(resultat) + "\""
                + "}";
    }

    private static int parseRequiredInt(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " est obligatoire");
        }
        return Integer.parseInt(value.trim());
    }

    private static LocalDate parseLocalDate(String value, String field) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(field + " invalide (format attendu YYYY-MM-DD)");
        }
    }

    private static void validateTournamentDateRange(LocalDate dateDebut, LocalDate dateFin) {
        if (!dateFin.isAfter(dateDebut)) {
            throw new IllegalArgumentException("dateFin doit etre strictement apres dateDebut");
        }
    }

    private static void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email invalide");
        }
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Tournoi.Format parseTournoiFormat(String value) {
        String normalized = trimOrEmpty(value).toUpperCase();
        if ("ROUND ROBIN".equals(normalized) || "ROUND-ROBIN".equals(normalized)) {
            normalized = "ROUND_ROBIN";
        }
        return Tournoi.Format.valueOf(normalized);
    }

    private static Tournoi.Statut parseTournoiStatut(String value) {
        String normalized = trimOrEmpty(value).toUpperCase();
        if ("LIVE".equals(normalized) || "EN_COURS".equals(normalized)) {
            return Tournoi.Statut.EN_COURS;
        }
        if ("TERMINE".equals(normalized)) {
            return Tournoi.Statut.TERMINE;
        }
        return Tournoi.Statut.EN_ATTENTE;
    }

    private static Match.Statut parseMatchStatut(String value) {
        String normalized = trimOrEmpty(value).toUpperCase();
        if ("LIVE".equals(normalized)) {
            return Match.Statut.EN_COURS;
        }
        if ("TERMINE".equals(normalized)) {
            return Match.Statut.TERMINE;
        }
        if ("EN_COURS".equals(normalized)) {
            return Match.Statut.EN_COURS;
        }
        return Match.Statut.PLANIFIE;
    }

    private void applyMatchStateFromPayload(Match match, Map<String, String> payload) {
        int score1 = parseIntOrDefault(payload.get("score1"), match.getScoreEquipe1());
        int score2 = parseIntOrDefault(payload.get("score2"), match.getScoreEquipe2());
        String statutValue = trimOrEmpty(payload.get("statut"));
        String gagnantIdValue = trimOrEmpty(payload.get("gagnantId"));

        match.setScoreEquipe1(score1);
        match.setScoreEquipe2(score2);

        if (!statutValue.isEmpty()) {
            Match.Statut statut = parseMatchStatut(statutValue);
            match.setStatut(statut);
            if (statut != Match.Statut.TERMINE) {
                match.setGagnant(null);
            }
        }

        if (match.getStatut() == Match.Statut.TERMINE) {
            if (!gagnantIdValue.isEmpty()) {
                int gagnantId = parseIntOrDefault(gagnantIdValue, -1);
                if (gagnantId == match.getEquipe1().getId()) {
                    match.setGagnant(match.getEquipe1());
                } else if (gagnantId == match.getEquipe2().getId()) {
                    match.setGagnant(match.getEquipe2());
                } else {
                    match.setGagnant(null);
                }
            } else if (score1 > score2) {
                match.setGagnant(match.getEquipe1());
            } else if (score2 > score1) {
                match.setGagnant(match.getEquipe2());
            } else {
                match.setGagnant(null);
            }
        }
    }

    private static String toUserJson(Utilisateur u) {
        String role;
        String equipe = "Aucune";

        if (u instanceof Joueur) {
            role = "JOUEUR";
            Joueur j = (Joueur) u;
            if (!j.getNomEquipe().isEmpty()) {
                equipe = j.getNomEquipe();
            }
        } else if (u instanceof Coach) {
            role = "COACH";
            Coach c = (Coach) u;
            if (!c.getEquipeCoachee().isEmpty()) {
                equipe = c.getEquipeCoachee();
            }
        } else if (u instanceof Administrateur) {
            role = "ADMINISTRATEUR";
        } else {
            role = u.getRole().toUpperCase();
        }

        return "{"
                + "\"id\":" + u.getId() + ","
                + "\"pseudo\":\"" + escapeJson(u.getPseudo()) + "\","
                + "\"email\":\"" + escapeJson(u.getEmail()) + "\","
                + "\"role\":\"" + role + "\","
                + "\"niveau\":\"" + escapeJson(u.getNiveau()) + "\","
                + "\"equipe\":\"" + escapeJson(equipe) + "\","
                + "\"dateInscription\":\"" + u.getDateInscription().format(DATE_FR) + "\""
                + "}";
    }

    private static String toJsonArray(List<String> items) {
        return "[" + String.join(",", items) + "]";
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseJsonObject(String json) {
        Map<String, String> values = new HashMap<>();
        if (json == null) {
            return values;
        }

        Matcher stringMatcher = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"").matcher(json);
        while (stringMatcher.find()) {
            values.put(stringMatcher.group(1), stringMatcher.group(2));
        }

        Matcher numberMatcher = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*([0-9]+(?:\\\\.[0-9]+)?)").matcher(json);
        while (numberMatcher.find()) {
            values.put(numberMatcher.group(1), numberMatcher.group(2));
        }

        return values;
    }

    private static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if ("http://localhost:5173".equals(origin) || "http://127.0.0.1:5173".equals(origin)) {
            headers.set("Access-Control-Allow-Origin", origin);
            headers.set("Vary", "Origin");
        } else {
            headers.set("Access-Control-Allow-Origin", "http://localhost:5173");
        }
        headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
