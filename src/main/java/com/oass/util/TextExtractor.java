package com.oass.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Reads the text out of an uploaded file so the plagiarism engine can compare it.
 * PDF -> PDFBox, DOCX -> Apache POI, plain text / source code -> read as UTF-8.
 * Other types (zip, images ...) have no text: the result is null.
 */
public final class TextExtractor {

    private static final Logger LOG = Logger.getLogger(TextExtractor.class.getName());

    private static final Set<String> PLAIN_TEXT =
            Set.of("txt", "java", "c", "cpp", "py", "md", "csv", "html", "js", "sql", "xml", "json");

    private TextExtractor() {}

    /** Returns the extracted text, or null if the file has no readable text. */
    public static String extract(byte[] data, String extension) {
        try {
            String text = null;
            if (extension.equals("pdf")) {
                text = fromPdf(data);
            } else if (extension.equals("docx")) {
                text = fromDocx(data);
            } else if (PLAIN_TEXT.contains(extension)) {
                text = new String(data, StandardCharsets.UTF_8);
            }
            if (text == null) return null;
            text = text.replace("\u0000", "").trim();
            return text.isEmpty() ? null : text;
        } catch (IOException | RuntimeException e) {
            LOG.warning("Could not extract text from ." + extension + " file: " + e.getMessage());
            return null;
        }
    }

    private static String fromPdf(byte[] data) throws IOException {
        try (PDDocument doc = Loader.loadPDF(data)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private static String fromDocx(byte[] data) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(data))) {
            return new XWPFWordExtractor(doc).getText();
        }
    }
}
