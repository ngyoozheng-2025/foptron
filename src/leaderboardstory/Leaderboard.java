package src.leaderboardstory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Leaderboard system backed by leaderboard.csv
 *
 * - Dates are stored/displayed as dd/MM/yyyy
 * - loadFromFile() is tolerant of several date formats and normalizes to
 * dd/MM/yyyy
 * - addEntry(...) updates only if the new score is higher
 * - addOrReplace(...) always replaces the existing entry for the player
 * - Top 10 entries are kept
 */
public class Leaderboard {

    private static final String FILE_PATH = "leaderboard.csv";
    private static final int MAX_ENTRIES = 10;

    private static final SimpleDateFormat DISPLAY_FMT = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    // Common CSV date formats we might encounter in older files
    private static final List<SimpleDateFormat> KNOWN_DATE_FORMATS = List.of(
            new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH), // e.g. Tue Jan 06 16:45:10 MYT 2026
            new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
            new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH));

    // in-memory list (sorted desc by score)
    private static List<LeaderboardEntry> entries = new ArrayList<>();

    // ---------- Public API ----------

    /**
     * Load leaderboard from CSV file into memory (normalizes dates to dd/MM/yyyy).
     */
    public static synchronized void loadFromFile() {
        entries.clear();
        File f = new File(FILE_PATH);
        if (!f.exists())
            return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty())
                    continue;

                // split into columns - we expect: name,level,score,date (date may contain
                // spaces)
                String[] parts = line.split(",", 4); // limit=4 -> date remains intact even with commas (rare)
                if (parts.length < 3)
                    continue;

                String name = parts[0].trim();
                String levelStr = parts.length > 1 ? parts[1].trim() : "0";
                String scoreStr = parts.length > 2 ? parts[2].trim() : "0";
                String dateStr = parts.length > 3 ? parts[3].trim() : null;

                int level = safeParseInt(levelStr, 0);
                int score = safeParseInt(scoreStr, 0);
                String normDate = normalizeDateString(dateStr);

                entries.add(new LeaderboardEntry(name, level, score, normDate));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sortAndTrim();
    }

    /**
     * Add an entry only if the new score is higher than existing entry for the same
     * player.
     * If player does not exist, this adds a new entry.
     */
    public static synchronized void addEntry(String name, int level, int score) {
        Objects.requireNonNull(name);

        // find existing
        LeaderboardEntry existing = findByName(name);
        if (existing == null) {
            entries.add(new LeaderboardEntry(name, level, score, today()));
        } else {
            // update only if score is higher (preserve better runs)
            if (score > existing.score) {
                existing.level = level;
                existing.score = score;
                existing.date = today();
            }
        }

        sortAndTrim();
        saveToFile();
    }

    /**
     * Add or replace the player's leaderboard entry unconditionally.
     * Use this when you want to overwrite (for example when user explicitly chooses
     * "Save").
     */
    public static synchronized void addOrReplace(String name, int level, int score) {
        Objects.requireNonNull(name);

        entries.removeIf(e -> e.name.equals(name));
        entries.add(new LeaderboardEntry(name, level, score, today()));
        sortAndTrim();
        saveToFile();
    }

    /** Get a copy of current leaderboard entries (sorted, top MAX_ENTRIES). */
    public static synchronized List<LeaderboardEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /** Clear the leaderboard in memory and on disk (use with caution). */
    public static synchronized void clearAll() {
        entries.clear();
        saveToFile();
    }

    // ---------- Internal helpers ----------

    private static LeaderboardEntry findByName(String name) {
        for (LeaderboardEntry e : entries) {
            if (e.name.equals(name))
                return e;
        }
        return null;
    }

    private static void sortAndTrim() {
        entries.sort((a, b) -> Integer.compare(b.score, a.score)); // descending by score
        if (entries.size() > MAX_ENTRIES) {
            entries = new ArrayList<>(entries.subList(0, MAX_ENTRIES));
        }
    }

    private static void saveToFile() {
        // ensure directory exists if user runs inside folder structure
        File out = new File(FILE_PATH);
        try (PrintWriter pw = new PrintWriter(new FileWriter(out, false))) {
            for (LeaderboardEntry e : entries) {
                pw.println(e.toCsv());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String today() {
        return DISPLAY_FMT.format(new Date());
    }

    private static int safeParseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return fallback;
        }
    }

    /**
     * Normalize various date string formats into dd/MM/yyyy.
     * If parsing fails, returns original string (trimmed) or today's date as
     * fallback.
     */
    private static String normalizeDateString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return today();
        }
        String trimmed = raw.trim();

        // try known formats
        for (SimpleDateFormat fmt : KNOWN_DATE_FORMATS) {
            try {
                Date d = fmt.parse(trimmed);
                return DISPLAY_FMT.format(d);
            } catch (ParseException ignored) {
            }
        }

        // try parsing numeric-only e.g. timestamp (milliseconds)
        try {
            long ms = Long.parseLong(trimmed);
            return DISPLAY_FMT.format(new Date(ms));
        } catch (Exception ignored) {
        }

        // fallback: return trimmed (so it won't be lost) but prefer today's date if
        // it's not parseable
        return DISPLAY_FMT.format(new Date());
    }

    // ---------- Data class ----------

    public static class LeaderboardEntry {
        public String name;
        public int level;
        public int score;
        public String date; // dd/MM/yyyy

        public LeaderboardEntry(String name, int level, int score, String date) {
            this.name = name;
            this.level = level;
            this.score = score;
            this.date = (date == null || date.isEmpty()) ? DISPLAY_FMT.format(new Date()) : date;
        }

        public String toCsv() {
            // CSV columns: name,level,score,date
            // names with commas are not supported; if needed we can add quoting.
            return name + "," + level + "," + score + "," + date;
        }

        @Override
        public String toString() {
            return "LeaderboardEntry{" +
                    "name='" + name + '\'' +
                    ", level=" + level +
                    ", score=" + score +
                    ", date='" + date + '\'' +
                    '}';
        }
    }
}
