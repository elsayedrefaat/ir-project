package bi_word;

import lucene_for_txt_files.docs_manager.DocData;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiwordIndex {
    private Map<String, Map<String, List<Integer>>> index;

    public BiwordIndex() {
        index = new HashMap<>();
    }

    public void addDocument(int docId, String text) {
        String[] words = text.toLowerCase().split("\\W+");
        for (int i = 0; i < words.length - 1; i++) {
            String biword = words[i] + " " + words[i+1];
            if (!index.containsKey(biword)) {
                index.put(biword, new HashMap<>());
            }
            Map<String, List<Integer>> postings = index.get(biword);
            if (!postings.containsKey(words[i])) {
                postings.put(words[i], new ArrayList<>());
            }
            postings.get(words[i]).add(docId);
        }
    }

    public List<Integer> search(String query) {
        String[] words = query.toLowerCase().split("\\W+");
        if (words.length == 0) {
            return new ArrayList<>();
        }
        List<List<Integer>> docLists = new ArrayList<>();
        for (int i = 0; i < words.length - 1; i++) {
            String biword = words[i] + " " + words[i+1];
            Map<String, List<Integer>> postings = index.get(biword);
            if (postings == null) {
                docLists.add(new ArrayList<>());
            } else {
                List<Integer> docList = postings.get(words[i]);
                if (docList == null) {
                    docList = new ArrayList<>();
                }
                docLists.add(docList);
            }
        }
        List<Integer> result = new ArrayList<>();
        if (docLists.size() > 0) {
            result.addAll(docLists.get(0));
        }
        for (int i = 1; i < docLists.size(); i++) {
            List<Integer> nextList = docLists.get(i);
            if (words[i].equals("and")) {
                result = intersect(result, nextList);
            } else if (words[i].equals("or")) {
                result = union(result, nextList);
            } else if (words[i].equals("not")) {
                result = difference(result, nextList);
            }
        }
        return result;
    }

    private List<Integer> intersect(List<Integer> list1, List<Integer> list2) {
        List<Integer> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < list1.size() && j < list2.size()) {
            int docId1 = list1.get(i);
            int docId2 = list2.get(j);
            if (docId1 == docId2) {
                result.add(docId1);
                i++;
                j++;
            } else if (docId1 < docId2) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }

    private List<Integer> union(List<Integer> list1, List<Integer> list2) {
        List<Integer> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < list1.size() && j < list2.size()) {
            int docId1 = list1.get(i);
            int docId2 = list2.get(j);
            if (docId1 == docId2) {
                result.add(docId1);
                i++;
                j++;
            } else if (docId1 < docId2) {
                result.add(docId1);
                i++;
            } else {
                result.add(docId2);
                j++;
            }
        }
        while (i < list1.size()) {
            result.add(list1.get(i));
            i++;
        }
        while (j < list2.size()) {
            result.add(list2.get(j));
            j++;
        }
        return result;
    }

    private List<Integer> difference(List<Integer> list1, List<Integer> list2) {
        List<Integer> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < list1.size() && j < list2.size()) {
            int docId1 = list1.get(i);
            int docId2 = list2.get(j);
            if (docId1 == docId2) {
                i++;
                j++;
            } else if (docId1 < docId2) {
                result.add(docId1);
                i++;
            } else {
                j++;
            }
        }
        while (i < list1.size()) {
            result.add(list1.get(i));
            i++;
        }
        return result;
    }

    public void indexDocuments(List<DocData> documents, String indexPath) throws IOException {
        // Build the index
        for (DocData doc : documents) {
            addDocument(Integer.parseInt(doc.id), doc.abstractText);
        }

        // Write the index to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(indexPath))) {
            for (String term : index.keySet()) {
                writer.print(term + "\t");
                Map<String, List<Integer>> postings = index.get(term);
                for (String word : postings.keySet()) {
                    writer.print(word + ":");
                    List<Integer> docIds = postings.get(word);
                    for (int docId : docIds) {
                        writer.print(docId + ",");
                    }
                    writer.print(";");
                }
                writer.println();
            }
        }
    }

    public void loadIndex(String indexPath) throws IOException {
        index = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(indexPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String biword = parts[0];
                Map<String, List<Integer>> postings = new HashMap<>();
                String[] docParts = parts[1].split(";");
                for (String docPart : docParts) {
                    String[] wordParts = docPart.split(":");
                    String word = wordParts[0];
                    List<Integer> docIds = new ArrayList<>();
                    String[] docIdStrs = wordParts[1].split(",");
                    for (String docIdStr : docIdStrs) {
                        if (!docIdStr.isEmpty()) {
                            docIds.add(Integer.parseInt(docIdStr));
                        }
                    }
                    postings.put(word, docIds);
                }
                index.put(biword, postings);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Create some example documents
        List<Document> documents = new ArrayList<>();
        documents.add(new Document(0, "the quick brown fox jumps over the lazy dog"));
        documents.add(new Document(1, "the quick brown fox is not as lazy as the dog"));
        documents.add(new Document(2, "don't cry because it's over, smile because it happened"));
        documents.add(new Document(3, "be yourself; everyone else is already taken"));

        // Index the documents and store the index to file
        BiwordIndex index = new BiwordIndex();
//        index.indexDocuments(documents, "bi-index.txt");

        // Load the index from file
        index.loadIndex("bi-index.txt");

        // Search for a query with the AND operator
        List<Integer> docIds = index.search("be yourself");
        System.out.println(docIds); // Output: [0, 1]
    }

    public static class Document {
        private final int id;
        private final String content;

        public Document(int id, String content) {
            this.id = id;
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }
    }
}