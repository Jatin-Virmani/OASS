package com.oass.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the plagiarism engine (no database needed). */
class PlagiarismServiceTest {

    private static final String TEXT = "Normalization reduces redundancy in a relational database design";

    @Test
    void identicalTexts_scoreOneHundredPercent() {
        assertEquals(1.0, PlagiarismService.jaccard(TEXT, TEXT), 0.0001);
        assertEquals(1.0, PlagiarismService.cosine(TEXT, TEXT), 0.0001);
        assertEquals(100.0, PlagiarismService.score(1.0, 1.0), 0.0001);
    }

    @Test
    void unrelatedTexts_scoreZero() {
        String other = "Completely different words about football and cricket matches today";
        assertEquals(0.0, PlagiarismService.jaccard(TEXT, other), 0.0001);
        assertEquals(0.0, PlagiarismService.cosine(TEXT, other), 0.0001);
    }

    @Test
    void caseAndPunctuationAreIgnored() {
        assertEquals(1.0, PlagiarismService.jaccard(TEXT, TEXT.toUpperCase() + "!!!"), 0.0001);
    }

    @Test
    void lineBreaksSeparateWords() {
        // the old engine joined "design\nStart" into one word; words must stay separate
        assertEquals(1.0, PlagiarismService.jaccard("one two three\nfour five six", "one two three four five six"), 0.0001);
    }

    @Test
    void emptyText_scoresZeroWithoutError() {
        assertEquals(0.0, PlagiarismService.jaccard("", TEXT), 0.0001);
        assertEquals(0.0, PlagiarismService.cosine("", TEXT), 0.0001);
    }

    @Test
    void riskLevels_followTheThreshold() {
        assertEquals("HIGH", PlagiarismService.riskFor(70, 70));
        assertEquals("MEDIUM", PlagiarismService.riskFor(40, 70));
        assertEquals("LOW", PlagiarismService.riskFor(39.9, 70));
        assertEquals("HIGH", PlagiarismService.riskFor(50, 50));     // stricter teacher
    }

    @Test
    void highlight_marksSharedPhrases() {
        String html = PlagiarismService.highlight("alpha beta gamma delta", "xx alpha beta gamma yy");
        assertEquals("<mark>alpha beta gamma</mark> delta", html);
    }

    @Test
    void highlight_escapesHtml() {
        String html = PlagiarismService.highlight("<script>alert(1)</script> one two three", "one two three");
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("<mark>one two three</mark>"));
    }
}
