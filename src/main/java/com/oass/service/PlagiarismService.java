package com.oass.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The plagiarism engine. Pure methods (no database, no servlet) so it is easy to test.
 *
 * Idea: split the text into words, then compare
 *   1. Jaccard on 3-word shingles  = |A ∩ B| / |A ∪ B|   (copied phrases)
 *   2. Cosine on word counts       = (a·b) / (|a| |b|)    (same vocabulary)
 * The final score is the average of both, in percent.
 */
public class PlagiarismService {

    /** Number of words in one shingle. */
    public static final int SHINGLE_SIZE = 3;

    private static final Pattern WORD = Pattern.compile("[A-Za-z0-9]+");

    // ---------------------------------------------------------------- similarity

    /** Jaccard similarity of the two texts' shingles, 0.0 to 1.0. */
    public static double jaccard(String textA, String textB) {
        Set<String> a = shingles(textA);
        Set<String> b = shingles(textB);
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        Set<String> common = new HashSet<>(a);
        common.retainAll(b);
        Set<String> all = new HashSet<>(a);
        all.addAll(b);
        return (double) common.size() / all.size();
    }

    /** Cosine similarity of the two texts' word counts, 0.0 to 1.0. */
    public static double cosine(String textA, String textB) {
        Map<String, Integer> fa = wordCounts(textA);
        Map<String, Integer> fb = wordCounts(textB);

        double dot = 0, magA = 0, magB = 0;
        Set<String> vocabulary = new HashSet<>(fa.keySet());
        vocabulary.addAll(fb.keySet());
        for (String w : vocabulary) {
            int ca = fa.getOrDefault(w, 0);
            int cb = fb.getOrDefault(w, 0);
            dot  += ca * cb;
            magA += ca * ca;
            magB += cb * cb;
        }
        if (magA == 0 || magB == 0) return 0.0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    /** Final score in percent: the average of Jaccard and cosine. */
    public static double score(double jaccard, double cosine) {
        return (jaccard + cosine) / 2.0 * 100.0;
    }

    /**
     * HIGH at or above the assignment's threshold, MEDIUM from (threshold - 30), otherwise LOW.
     * With the default threshold 70 this gives the familiar 70 / 40 split.
     */
    public static String riskFor(double scorePercent, double thresholdPercent) {
        if (scorePercent >= thresholdPercent) return "HIGH";
        if (scorePercent >= Math.max(thresholdPercent - 30, 0)) return "MEDIUM";
        return "LOW";
    }

    // ---------------------------------------------------------------- highlighting

    /**
     * Returns the text as safe HTML where every word that is part of a 3-word phrase
     * also found in the other text is wrapped in &lt;mark&gt;. The result is already
     * HTML-escaped, so a JSP may print it without c:out.
     */
    public static String highlight(String text, String otherText) {
        Set<String> otherShingles = shingles(otherText);

        List<int[]> spans = new ArrayList<>();      // start/end of each word in the original text
        List<String> words = new ArrayList<>();
        Matcher m = WORD.matcher(text);
        while (m.find()) {
            spans.add(new int[]{m.start(), m.end()});
            words.add(m.group().toLowerCase());
        }

        boolean[] matched = new boolean[words.size()];
        for (int i = 0; i + SHINGLE_SIZE <= words.size(); i++) {
            if (otherShingles.contains(String.join(" ", words.subList(i, i + SHINGLE_SIZE)))) {
                for (int k = i; k < i + SHINGLE_SIZE; k++) matched[k] = true;
            }
        }

        StringBuilder html = new StringBuilder();
        int pos = 0;
        int i = 0;
        while (i < matched.length) {
            if (!matched[i]) { i++; continue; }
            int last = i;                                            // extend over a run of matched words
            while (last + 1 < matched.length && matched[last + 1]) last++;
            int start = spans.get(i)[0];
            int end   = spans.get(last)[1];
            html.append(escape(text.substring(pos, start)));
            html.append("<mark>").append(escape(text.substring(start, end))).append("</mark>");
            pos = end;
            i = last + 1;
        }
        html.append(escape(text.substring(pos)));
        return html.toString();
    }

    // ---------------------------------------------------------------- helpers

    /** All lowercase words (letters and digits only) of a text, in order. */
    static List<String> words(String text) {
        List<String> list = new ArrayList<>();
        Matcher m = WORD.matcher(text);
        while (m.find()) list.add(m.group().toLowerCase());
        return list;
    }

    /** The set of all 3-word phrases of a text. */
    static Set<String> shingles(String text) {
        List<String> words = words(text);
        Set<String> set = new HashSet<>();
        for (int i = 0; i + SHINGLE_SIZE <= words.size(); i++) {
            set.add(String.join(" ", words.subList(i, i + SHINGLE_SIZE)));
        }
        return set;
    }

    private static Map<String, Integer> wordCounts(String text) {
        Map<String, Integer> counts = new HashMap<>();
        for (String w : words(text)) counts.merge(w, 1, Integer::sum);
        return counts;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
