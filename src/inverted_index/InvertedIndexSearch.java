package inverted_index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class InvertedIndexSearch {
    private final Map<String, List<Integer>> index;

    public InvertedIndexSearch(String indexPath) throws IOException {
        index = new HashMap<>();
        readIndexFromFile(indexPath);
    }

    private void readIndexFromFile(String indexPath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(indexPath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            String term = parts[0];
            List<Integer> postings = new ArrayList<>();
            for (String docIdStr : parts[1].split(",")) {
                postings.add(Integer.parseInt(docIdStr));
            }
            index.put(term, postings);
        }
        reader.close();
    }

    public List<Integer> search(String query) {
        String[] queryTerms = query.split(" ");
        Set<Integer> result = new HashSet<>();
        boolean first = true;
        for (String term : queryTerms) {
            if (term.equalsIgnoreCase("AND") || term.equalsIgnoreCase("OR")) {
                // Skip boolean operators
                continue;
            }
            if (index.containsKey(term)) {
                List<Integer> postings = index.get(term);
                if (first) {
                    // For the first term, initialize the result set
                    result.addAll(postings);
                    first = false;
                } else {
                    // Apply boolean operator to the result set and current postings list
                    if (query.contains("OR")) {
                        // Union operation for OR queries
                        result.addAll(postings);
                    } else if (query.contains("AND")) {
                        // Intersection operation for AND queries
                        result.retainAll(postings);
                    }
                }
            }
        }
        return new ArrayList<>(result);
    }

    public void printSearchResults(List<Integer> docIds) {
        for (int docId : docIds) {
            System.out.println("Document ID: " + docId);
          
        }
    }

    public static void main(String[] args) {
        String indexPath = "index.txt";
        String query = "locations AND information";
        try {
            InvertedIndexSearch invertedIndex = new InvertedIndexSearch(indexPath);
            List<Integer> result = invertedIndex.search(query);
            invertedIndex.printSearchResults(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}