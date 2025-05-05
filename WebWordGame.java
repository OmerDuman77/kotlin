import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class WebWordGame {
    private static final int PORT = 5000;
    private static final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    // Words and their hints
    private static final Map<String, String> WORDS_WITH_HINTS = Map.of(
            "cat", "A small domesticated carnivorous mammal",
            "book", "A written or printed work consisting of pages",
            "house", "A building for human habitation",
            "table", "A piece of furniture with a flat top",
            "music", "Vocal or instrumental sounds combined",
            "garden", "A plot of ground where plants are cultivated",
            "computer", "An electronic device for storing and processing data",
            "mountain", "A large natural elevation of the earth's surface",
            "telephone", "A system for transmitting voices over a distance",
            "chocolate", "A food made from roasted and ground cacao seeds"
    );

    static class GameSession {
        String id;
        List<String> words;
        int currentWordIndex = 0;
        int timeRemaining = 120;
        int score = 0;
        Set<Integer> revealedLetters = new HashSet<>();
        long lastActionTime = System.currentTimeMillis();

        GameSession(String id) {
            this.id = id;
            this.words = new ArrayList<>(WORDS_WITH_HINTS.keySet());
            // Sort by length
            Collections.sort(this.words, Comparator.comparing(String::length));
        }

        boolean isExpired() {
            return System.currentTimeMillis() - lastActionTime > 300000; // 5 minutes
        }

        void updateLastActionTime() {
            lastActionTime = System.currentTimeMillis();
        }

        String getCurrentWord() {
            if (currentWordIndex < words.size()) {
                return words.get(currentWordIndex);
            }
            return "";
        }

        String getCurrentHint() {
            String currentWord = getCurrentWord();
            return WORDS_WITH_HINTS.getOrDefault(currentWord, "");
        }

        String getCurrentWordDisplay() {
            String currentWord = getCurrentWord();
            if (currentWord.isEmpty()) return "";

            StringBuilder display = new StringBuilder();
            for (int i = 0; i < currentWord.length(); i++) {
                if (revealedLetters.contains(i)) {
                    display.append(currentWord.charAt(i));
                } else {
                    display.append("_");
                }
                display.append(" ");
            }
            return display.toString();
        }

        boolean isGameOver() {
            return timeRemaining <= 0 || currentWordIndex >= words.size();
        }

        void revealRandomLetter() {
            String currentWord = getCurrentWord();
            if (currentWord.isEmpty() || score < 50) return;

            List<Integer> unrevealed = new ArrayList<>();
            for (int i = 0; i < currentWord.length(); i++) {
                if (!revealedLetters.contains(i)) {
                    unrevealed.add(i);
                }
            }

            if (!unrevealed.isEmpty()) {
                int randomIndex = unrevealed.get(new Random().nextInt(unrevealed.size()));
                revealedLetters.add(randomIndex);
                score -= 50; // Cost of hint
            }
        }

        void checkAnswer(String answer) {
            String currentWord = getCurrentWord();
            timeRemaining -= 2; // Time passes for each guess

            if (currentWord.equalsIgnoreCase(answer)) {
                score += currentWord.length() * 100;
                currentWordIndex++;
                revealedLetters.clear();
                timeRemaining -= 5; // Each word costs time
            } else {
                timeRemaining -= 3; // Wrong guess penalty
            }
        }

        String getGameState() {
            if (isGameOver()) {
                int timeBonus = Math.max(0, timeRemaining * 50);
                int finalScore = score + timeBonus;
                
                return String.format(
                    "GAME OVER\nWord Score: %d\nTime Bonus: %d\nFinal Score: %d\n\n" +
                    "You completed %d out of %d words.",
                    score, timeBonus, finalScore, currentWordIndex, words.size()
                );
            }

            return String.format(
                "Word %d/%d (%d letters)\nHint: %s\n\n%s\n\nTime remaining: %d seconds\nScore: %d",
                currentWordIndex + 1, words.size(), getCurrentWord().length(),
                getCurrentHint(), getCurrentWordDisplay(),
                Math.max(0, timeRemaining), score
            );
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/", new GameHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Web Word Game running on port " + PORT);
    }

    static class GameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String sessionId = getSessionId(exchange);
            
            // Create session if new
            if (!activeSessions.containsKey(sessionId)) {
                activeSessions.put(sessionId, new GameSession(sessionId));
            }
            
            GameSession session = activeSessions.get(sessionId);
            session.updateLastActionTime();
            
            // Clean up expired sessions
            cleanupExpiredSessions();
            
            String response;
            
            if (path.startsWith("/hint")) {
                session.revealRandomLetter();
                response = generateHtmlResponse(session);
            } else if (path.startsWith("/guess") && exchange.getRequestMethod().equals("POST")) {
                // Handle guess
                int contentLength = Integer.parseInt(exchange.getRequestHeaders().getFirst("Content-Length"));
                byte[] requestBody = new byte[contentLength];
                exchange.getRequestBody().read(requestBody);
                
                String formData = new String(requestBody);
                String guess = parseGuessFromForm(formData);
                
                if (!guess.isEmpty()) {
                    session.checkAnswer(guess);
                }
                
                response = generateHtmlResponse(session);
            } else if (path.startsWith("/newgame")) {
                // Start a new game
                activeSessions.put(sessionId, new GameSession(sessionId));
                response = generateHtmlResponse(activeSessions.get(sessionId));
            } else {
                // Main page
                response = generateHtmlResponse(session);
            }
            
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length());
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
        
        private String parseGuessFromForm(String formData) {
            for (String param : formData.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("guess")) {
                    return pair[1].toLowerCase().replace("+", " ");
                }
            }
            return "";
        }
        
        private String getSessionId(HttpExchange exchange) {
            // Get session ID from cookie, or create new one
            String sessionId = null;
            
            List<String> cookies = exchange.getRequestHeaders().get("Cookie");
            if (cookies != null) {
                for (String cookie : cookies) {
                    if (cookie.startsWith("SESSIONID=")) {
                        sessionId = cookie.substring("SESSIONID=".length());
                        break;
                    }
                }
            }
            
            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();
                exchange.getResponseHeaders().add("Set-Cookie", "SESSIONID=" + sessionId + "; Path=/");
            }
            
            return sessionId;
        }
        
        private void cleanupExpiredSessions() {
            List<String> expiredSessions = new ArrayList<>();
            
            for (Map.Entry<String, GameSession> entry : activeSessions.entrySet()) {
                if (entry.getValue().isExpired()) {
                    expiredSessions.add(entry.getKey());
                }
            }
            
            for (String sessionId : expiredSessions) {
                activeSessions.remove(sessionId);
            }
        }
        
        private String generateHtmlResponse(GameSession session) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>");
            html.append("<html><head><title>Word Game</title>");
            html.append("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }");
            html.append("h1 { color: #2c3e50; }");
            html.append(".game-container { background-color: #f7f9fc; border-radius: 10px; padding: 20px; margin-top: 20px; }");
            html.append(".game-info { white-space: pre-wrap; font-size: 18px; line-height: 1.6; }");
            html.append(".controls { margin-top: 20px; }");
            html.append("button, input[type='submit'] { background-color: #3498db; color: white; border: none; padding: 10px 15px; border-radius: 5px; cursor: pointer; margin-right: 10px; }");
            html.append("button:hover, input[type='submit']:hover { background-color: #2980b9; }");
            html.append("input[type='text'] { padding: 10px; width: 200px; border: 1px solid #ddd; border-radius: 5px; }");
            html.append("</style>");
            html.append("</head><body>");
            html.append("<h1>Word Game Challenge</h1>");
            
            html.append("<div class='game-container'>");
            html.append("<div class='game-info'>");
            html.append(session.getGameState());
            html.append("</div>");
            
            html.append("<div class='controls'>");
            if (!session.isGameOver()) {
                html.append("<form action='/guess' method='post'>");
                html.append("<input type='text' name='guess' placeholder='Type your answer' autocomplete='off' autofocus>");
                html.append("<input type='submit' value='Submit'>");
                html.append("</form>");
                html.append("<div style='margin-top: 10px;'>");
                html.append("<a href='/hint'><button>Get Hint (-50 points)</button></a>");
                html.append("</div>");
            } else {
                html.append("<a href='/newgame'><button>Play Again</button></a>");
            }
            html.append("</div>"); // controls
            html.append("</div>"); // game-container
            
            html.append("</body></html>");
            return html.toString();
        }
    }
}