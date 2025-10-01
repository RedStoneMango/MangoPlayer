package io.github.redstonemango.mangoplayer.logic;

import io.github.redstonemango.mangoplayer.logic.config.PlaylistConfigWrapper;
import io.github.redstonemango.mangoplayer.logic.config.SongConfigWrapper;

import java.security.SecureRandom;
import java.util.*;

public class UniqueIdGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static int songIdLength = -1;
    private static int playlistIdLength = -1;

    private static final int MAX_RANDOM_ATTEMPTS = 1000;

    public static String generateUniqueString(IdUse use) {
        if (songIdLength == -1 || playlistIdLength == -1) initializeIdLengths();

        int length = use == IdUse.SONG_ID ? songIdLength : playlistIdLength;

        Set<String> existingIds = loadExistingIds(use);

        // First try random attempts
        int estimatedCollisions = Math.max(1, (int) Math.min(MAX_RANDOM_ATTEMPTS, 10 * Math.sqrt(existingIds.size()))); // birthday paradox logic
        for (int attempt = 0; attempt < estimatedCollisions; attempt++) {
            String id = generateRandomString(length);
            if (!existingIds.contains(id)) {
                updateLengthIfNeeded(use);
                return id;
            }
        }

        // If random fails, fall back to deterministic approach
        long totalCombinations = (long) Math.pow(CHARSET.length(), length);

        // If there's still space, generate all possible IDs and return an unused one
        if (existingIds.size() < totalCombinations) {
            Optional<String> fallback = findUnusedId(length, existingIds);
            if (fallback.isPresent()) {
                updateLengthIfNeeded(use);
                return fallback.get();
            }
        }

        // All combinations are used, increase length and retry
        if (use == IdUse.SONG_ID) songIdLength++;
        else playlistIdLength++;

        return generateUniqueString(use); // Recurse with increased length
    }

    public static void updateLengthIfNeeded(IdUse use) {
        int currentSize = (use == IdUse.SONG_ID ? SongConfigWrapper.loadConfig().songs.size()
                : PlaylistConfigWrapper.loadConfig().playlists.size()) + 1;

        if (use == IdUse.SONG_ID) {
            while (Math.pow(CHARSET.length(), songIdLength) <= currentSize) {
                songIdLength++;
            }
        } else {
            while (Math.pow(CHARSET.length(), playlistIdLength) <= currentSize) {
                playlistIdLength++;
            }
        }
    }

    public static void initializeIdLengths() {
        songIdLength = computeMinimumLength(SongConfigWrapper.loadConfig().songs.size());
        playlistIdLength = computeMinimumLength(PlaylistConfigWrapper.loadConfig().playlists.size());
    }

    private static int computeMinimumLength(int existingItemCount) {
        int length = 1;
        while (Math.pow(CHARSET.length(), length) <= existingItemCount) {
            length++;
        }
        return Math.max(length, 10);
    }

    private static Set<String> loadExistingIds(IdUse use) {
        Set<String> idSet = new HashSet<>();
        if (use == IdUse.SONG_ID) {
            SongConfigWrapper.loadConfig().songs.forEach((_, song) -> idSet.add(song.getId()));
        } else {
            PlaylistConfigWrapper.loadConfig().playlists.forEach(playlist -> idSet.add(playlist.getId()));
        }
        return idSet;
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    private static Optional<String> findUnusedId(int length, Set<String> existingIds) {
        char[] chars = new char[length];
        Arrays.fill(chars, CHARSET.charAt(0));

        long totalCombinations = (long) Math.pow(CHARSET.length(), length);

        for (long i = 0; i < totalCombinations; i++) {
            String candidate = new String(chars);
            if (!existingIds.contains(candidate)) {
                return Optional.of(candidate);
            }
            increment(chars);
        }
        return Optional.empty(); // All IDs are used
    }

    private static void increment(char[] chars) {
        int base = CHARSET.length();
        int index = chars.length - 1;

        while (index >= 0) {
            int currentCharIndex = CHARSET.indexOf(chars[index]);
            if (currentCharIndex < base - 1) {
                chars[index] = CHARSET.charAt(currentCharIndex + 1);
                return;
            } else {
                chars[index] = CHARSET.charAt(0);
                index--;
            }
        }
    }

    public enum IdUse {
        SONG_ID,
        PLAYLIST_ID
    }
}
