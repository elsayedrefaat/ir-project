package docs_manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DocsSplitter {
    static ArrayList<DocData> docs = new ArrayList<>();

    static public ArrayList<DocData> tokenize(File f) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line;
            String id = null;
            String title = null;
            String author = null;
            String source = null;
            String abstractText = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(".I")) {
                    id = line.substring(3).trim();
                }
                if (line.startsWith(".T")) {
                    title = extractElement(reader);
                }
                if (line.startsWith(".A")) {
                    author = extractElement(reader);
                }
                if (line.startsWith(".B")) {
                    source = extractElement(reader);
                }
                if (line.startsWith(".W")) {
                    abstractText = extractElement(reader);
                }
                if (id != null && title != null && author != null && source != null && abstractText != null) {
                    docs.add(new DocData(id, title, author, source, abstractText));
                        id = null;
                        title = null;
                        author = null;
                        source = null;
                        abstractText = null;
                } else if (id != null && abstractText != null) {
                    docs.add(new DocData(id, "", "", "", abstractText));
                        id = null;
                        abstractText = null;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return docs;
    }

    private static String extractElement(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.startsWith(".")) {
            sb.append(line).append(" ");
        }
        return sb.toString().trim();

    }

}
