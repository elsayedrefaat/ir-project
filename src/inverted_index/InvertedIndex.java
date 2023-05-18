package inverted_index;

import docs_manager.DocData;
import docs_manager.DocsSplitter;

import java.io.*;
import java.util.*;

public class InvertedIndex {
    private final Map<String, List<Integer>> index; // inverted index to store the location of terms in the documents

    public InvertedIndex(List<DocData> documents) {
        index = new HashMap<>();
        indexDocuments(documents);
    }

    private void indexDocuments(List<DocData> documents) {
        for (DocData document : documents) {
            int docId = Integer.parseInt(document.id);
            String content = document.abstractText;
            StringTokenizer tokenizer = new StringTokenizer(content, " \t\n\r\f.,;:()[]{}\"'");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if (!index.containsKey(token)) {
                    index.put(token, new ArrayList<>());
                }
                List<Integer> postings = index.get(token);
                if (postings.isEmpty() || postings.get(postings.size() - 1) != docId) {
                    postings.add(docId);
                }
            }
        }
    }

    public void writeIndexToFile(String indexPath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(indexPath));
        for (String term : index.keySet()) {
            writer.write(term + ":");
            List<Integer> postings = index.get(term);
            for (int i = 0; i < postings.size(); i++) {
                writer.write(postings.get(i) + "");
                if (i < postings.size() - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();
        }
        writer.close();
    }

    public static void main(String[] args) {
        List<DocData> documents = new ArrayList<>();
        // populate documents list with DocData objects
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
                documents = DocsSplitter.tokenize(f);
            }
        }

        InvertedIndex index = new InvertedIndex(documents);
        try {
            index.writeIndexToFile("index.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}