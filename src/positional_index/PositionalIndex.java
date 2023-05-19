package positional_index;

import lucene_for_txt_files.JFrame1;
import lucene_for_txt_files.docs_manager.DocData;
import lucene_for_txt_files.docs_manager.DocsSplitter;

import java.util.*;
import java.io.*;

public class PositionalIndex {
    private Map<String, List<Integer>> index;
    private final Map<Integer, String> documents;

    public PositionalIndex(List<DocData> documents) {
        index = new HashMap<>();
        this.documents = new HashMap<>();
        for (DocData document : documents) {
            this.documents.put(Integer.valueOf(document.id), document.abstractText);
            String[] words = document.abstractText.split(" ");
            for (int i = 0; i < words.length; i++) {
                String word = words[i].toLowerCase();
                if (!index.containsKey(word)) {
                    index.put(word, new ArrayList<>());
                }
                index.get(word).add(Integer.valueOf(document.id) * 1000 + i);
            }
        }
    }

    public void saveToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(filePath)) {
            for (Map.Entry<String, List<Integer>> entry : index.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            index = new HashMap<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                String word = parts[0];
                List<Integer> positions = new ArrayList<>();
                parts[1] = parts[1].replaceAll("\\[|\\]", " ").trim();
                for (String position : parts[1].trim().split(",")) {
                    try{
                        positions.add(Integer.parseInt(position.trim()));
                    }catch (Exception e){

                    }
                }
                index.put(word, positions);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public List<Integer> search(String query) {
        List<String> terms = parseQuery(query);
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }
        boolean isProximity = false;
        int proximityDistance = 0;
        List<Integer> result = searchTerm(terms.get(0));
        for (int i = 1; i < terms.size(); i++) {
            String term = terms.get(i);
            if (term.equalsIgnoreCase("AND")) {
                result = intersect(result, searchTerm(terms.get(i + 1)));
                i++;
            } else if (term.equalsIgnoreCase("OR")) {
                result = union(result, searchTerm(terms.get(i + 1)));
                i++;
            } else if (term.equalsIgnoreCase("NOT")) {
                result = difference(result, searchTerm(terms.get(i + 1)));
                i++;
            } else if (term.equalsIgnoreCase("NEAR")) {
                isProximity = true;
                proximityDistance = Integer.parseInt(terms.get(i + 1));
                i++;
            } else {
                List<Integer> nextResult = searchTerm(term);
                if (isProximity) {
                    result = proximity(result, nextResult, proximityDistance);
                    isProximity = false;
                    proximityDistance = 0;
                } else {
                    result = intersect(result, nextResult);
                }
            }
        }
        return result;
    }

    private List<String> parseQuery(String query) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (char c : query.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '-') {
                sb.append(Character.toLowerCase(c));
            } else if (c == ' ') {
                if (sb.length() > 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                if (sb.length() > 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
                tokens.add(String.valueOf(c));
            }
        }
        if (sb.length() > 0) {
            tokens.add(sb.toString());
        }
        return tokens;
    }
    private List<Integer> difference(List<Integer> a, List<Integer> b) {
        List<Integer> result = new ArrayList<>(a);
        result.removeAll(b);
        return result;
    }
    private List<Integer> proximity(List<Integer> a, List<Integer> b, int distance) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            int diff = a.get(i) - b.get(j);
            if (diff < 0) {
                diff = -diff;
            }
            if (diff <= distance) {
                result.add(a.get(i));
                i++;
                j++;
            } else if (a.get(i) < b.get(j)) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }
    private List<Integer> union(List<Integer> a, List<Integer> b) {
        List<Integer> result = new ArrayList<>(a);
        for (int id : b) {
            if (!result.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }
    private List<Integer> intersect(List<Integer> a, List<Integer> b) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i).equals(b.get(j))) {
                result.add(a.get(i));
                i++;
                j++;
            } else if (a.get(i) < b.get(j)) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }
    private List<Integer> searchTerm(String term) {
        List<Integer> positions = index.get(term);
        if (positions == null) {
            return Collections.emptyList();
        }
        Set<Integer> result = new HashSet<>(positions.size());
        for (int position : positions) {
            result.add(position / 1000);
        }
        return new ArrayList<>(result);
    }
    public static void main(String[] args) {
        ArrayList<DocData> docs = new ArrayList<>();
        String docsPath = "cisi";

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory does not exist or is not readable, please check the path");
            System.exit(1);
        }
        docs = JFrame1.getDocData(docs,docDir);

        PositionalIndex index = new PositionalIndex(docs);

        // Save index to file
        index.saveToFile("positional-index.txt");

        // Load index from file and perform search
        PositionalIndex indexFromFile = new PositionalIndex(Collections.emptyList());
        indexFromFile.loadFromFile("positional-index.txt");

        String phrase = "in large information systems";
        System.out.println("Search query: " + phrase);
        List<Integer> results = indexFromFile.search(phrase);
        for (int id : results) {
            System.out.println("Document " + id);
        }
    }
}

