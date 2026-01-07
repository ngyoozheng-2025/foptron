package src.leaderboardstory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.awt.Component;
import javax.swing.*;

public class SaveSystem {
    private static final File SAVE_DIR = new File("saves");

    static {
        if (!SAVE_DIR.exists()) {
            SAVE_DIR.mkdirs();
        }
    }

    /**
     * Save progress to a slot file. Overwrites existing slot file.
     * slotName: user-visible save slot id (we use player name by default).
     * Fields saved (one-per-line, CSV style):
     * name,xp,level,lives,discsOwned,arena,difficulty,round,totalScore
     */
    public static boolean saveProgress(String slotName,
            String playerName,
            int xp,
            int level,
            double lives,
            int discsOwned,
            String arena,
            String difficulty,
            int roundNumber,
            int totalScore) {
        File out = new File(SAVE_DIR, slotName + ".save");
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8))) {
            // CSV line - keep simple and stable
            String line = String.join(",",
                    escape(playerName),
                    String.valueOf(xp),
                    String.valueOf(level),
                    String.valueOf(lives),
                    String.valueOf(discsOwned),
                    escape(arena == null ? "" : arena),
                    escape(difficulty == null ? "" : difficulty),
                    String.valueOf(roundNumber),
                    String.valueOf(totalScore));
            w.write(line);
            w.newLine();
            w.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to save progress: " + e.getMessage(), "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static String escape(String s) {
        // very basic escape: replace commas with semicolon (keeps CSV simple)
        return s.replace(",", ";");
    }

    /**
     * Return a list of slot filenames (without extension).
     */
    public static String[] listSaveSlots() {
        String[] files = SAVE_DIR.list((dir, name) -> name.endsWith(".save"));
        if (files == null)
            return new String[0];
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].substring(0, files[i].length() - 5); // remove ".save"
        }
        Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
        return files;
    }

    /**
     * Load a slot by slotName. Returns null on error (or if file missing).
     * Returned array format:
     * [playerName, xp, level, lives, discsOwned, arena, difficulty, roundNumber,
     * totalScore]
     */
    public static String[] loadProgressFromSlot(String slotName) {
        File f = new File(SAVE_DIR, slotName + ".save");
        if (!f.exists())
            return null;
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line = r.readLine();
            if (line == null)
                return null;
            String[] parts = line.split(",", -1);
            // if some fields missing, pad
            if (parts.length < 9) {
                String[] padded = new String[9];
                System.arraycopy(parts, 0, padded, 0, parts.length);
                for (int i = parts.length; i < 9; i++)
                    padded[i] = "";
                parts = padded;
            }
            // unescape (reverse of escape)
            parts[0] = parts[0].replace(";", ",");
            parts[5] = parts[5].replace(";", ",");
            parts[6] = parts[6].replace(";", ",");
            return parts;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convenience: show a selection dialog to pick a slot and then load it.
     * Returns same array format as loadProgressFromSlot, or null if cancelled /
     * none.
     */
    public static String[] chooseAndLoadSlot(Component parent) {
        String[] slots = listSaveSlots();
        if (slots.length == 0) {
            JOptionPane.showMessageDialog(parent, "No save slots found.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        String choice = (String) JOptionPane.showInputDialog(parent, "Choose a save slot:", "Load Game",
                JOptionPane.PLAIN_MESSAGE, null, slots, slots[0]);
        if (choice == null)
            return null;
        return loadProgressFromSlot(choice);
    }
}
