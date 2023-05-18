package positional_index;

import docs_manager.DocData;
import docs_manager.DocsSplitter;

import java.util.*;
import java.io.*;

public class PositionalIndex {
    private Map<String, Map<Integer, List<Integer>>> index;

    public PositionalIndex(List<DocData> documents) {
        index = new HashMap<>();
        for (DocData doc : documents) {
            int docId = Integer.parseInt(doc.id);
            String[] words = doc.abstractText.split("\\W+");
            for (int i = 0; i < words.length; i++) {
                String word = words[i].toLowerCase();
                if (!index.containsKey(word)) {
                    index.put(word, new HashMap<>());
                }
                if (!index.get(word).containsKey(docId)) {
                    index.get(word).put(docId, new ArrayList<>());
                }
                index.get(word).get(docId).add(i);
            }
        }
    }

    public void writeIndexToFile(String filename) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {
            out.writeInt(index.size());
            for (Map.Entry<String, Map<Integer, List<Integer>>> entry : index.entrySet()) {
                String word = entry.getKey();
                Map<Integer, List<Integer>> postings = entry.getValue();
                out.writeUTF(word);
                out.writeInt(postings.size());
                for (Map.Entry<Integer, List<Integer>> posting : postings.entrySet()) {
                    int docId = posting.getKey();
                    List<Integer> positions = posting.getValue();
                    out.writeInt(docId);
                    out.writeInt(positions.size());
                    for (int position : positions) {
                        out.writeInt(position);
                    }
                }
            }
        }
    }

    public static PositionalIndex readIndexFromFile(String filename) throws IOException {
        PositionalIndex index = new PositionalIndex(Collections.emptyList());
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
            int numWords = in.readInt();
            for (int i = 0; i < numWords; i++) {
                String word = in.readUTF();
                int numPostings = in.readInt();
                for (int j = 0; j < numPostings; j++) {
                    int docId = in.readInt();
                    int numPositions = in.readInt();
                    List<Integer> positions = new ArrayList<>(numPositions);
                    for (int k = 0; k < numPositions; k++) {
                        positions.add(in.readInt());
                    }
                    if (!index.index.containsKey(word)) {
                        index.index.put(word, new HashMap<>());
                    }
                    index.index.get(word).put(docId, positions);
                }
            }
        }
        return index;
    }

    public List<Integer> search(String query) {
        String[] words = query.split("\\W+");
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (String word : words) {
            Map<Integer, List<Integer>> postings = index.get(word.toLowerCase());
            if (postings != null) {
                for (int docId : postings.keySet()) {
                    if (!result.containsKey(docId)) {
                        result.put(docId, new ArrayList<>());
                    }
                    result.get(docId).addAll(postings.get(docId));
                }
            }
        }
        List<Integer> docIds = new ArrayList<>(result.keySet());
        Collections.sort(docIds);
        return docIds;
    }

    public static void main(String[] args) {
        ArrayList<DocData> docs = new ArrayList<>();
        String docsPath = "cisi";

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory does not exist or is not readable, please check the path");
            System.exit(1);
        }

        // Load the documents from a file or some other source
        File[] files = docDir.listFiles();

        for (File f : files) {
            if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead()) {
                try {
                    System.out.println("Indexing " + f.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                docs = DocsSplitter.tokenize(f);
            }
        }

        PositionalIndex index = new PositionalIndex(docs);
        try {
            index.writeIndexToFile("index.txt");
        } catch (IOException e) {
            System.err.println("Error writing index to file: " + e.getMessage());
            return;
        }

        PositionalIndex readIndex;
        try {
            readIndex = PositionalIndex.readIndexFromFile("index.bin");
        } catch (IOException e) {
            System.err.println("Error reading index from file: " + e.getMessage());
            return;
        }

        // Perform a search
        List<Integer> result = readIndex.search("data");
        System.out.println("Search results: " + result);
    }

    }

