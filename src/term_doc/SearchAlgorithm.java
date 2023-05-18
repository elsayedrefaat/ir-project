package term_doc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class SearchAlgorithm {

    private final int[][] matrix;
    private final List<String> vocabulary;

    public SearchAlgorithm(String csvFilePath) throws IOException {
        CSVParser parser = new CSVParser(new BufferedReader(new FileReader(csvFilePath)), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        int nRows = records.size();
        int nCols = records.get(0).size() - 1;
        matrix = new int[nRows][nCols];
        vocabulary = new ArrayList<>();
        for (int i = 0; i < nRows; i++) {
            for (int j = 1; j <= nCols; j++) {
                matrix[i][j - 1] = Integer.parseInt(records.get(i).get(j));
            }
            vocabulary.add(records.get(i).get(0).trim());
        }
        parser.close();
    }

    public List<Integer> search(String query) {
        query = query.trim();
        String[] queryTerms = query.split(" ");
        List<Integer> result = new ArrayList<>();
        if (query.isEmpty()) return result;
        boolean first = true;
        int[] queryVector = new int[matrix.length];
        for (String term : queryTerms) {
            if (term.equalsIgnoreCase("AND") || term.equalsIgnoreCase("OR")) {
                // Skip boolean operators
                continue;
            }
            if (!vocabulary.contains(term)) {
                // Skip terms that are not in the vocabulary
                continue;
            }
            int i = vocabulary.indexOf(term);
            queryVector[i] = 1;
            if (first) {
                // For the first term, initialize the result vector
                result = Arrays.asList(Arrays.stream(matrix[i]).boxed().toArray(Integer[]::new));
                first = false;
            } else {
                // Apply boolean operator to the result vector and current query vector
                if (query.contains("OR")) {
                    // Union operation for OR queries
                    for (int j = 0; j < matrix[i].length; j++) {
                        if (matrix[i][j] == 1) {
                            result.set(j, 1);
                        }
                    }
                } else if (query.contains("AND")) {
                    // Intersection operation for AND queries
                    for (int j = 0; j < matrix[i].length; j++) {
                        if (matrix[i][j] == 0) {
                            result.set(j, 0);
                        }
                    }
                }
            }
        }
        // Apply final query vector to the result vector
        for (int i = 0; i < queryVector.length; i++) {
            if (queryVector[i] == 1) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] == 0) {
                        result.set(j, 0);
                    }
                }
            }
        }
        // Convert result vector to list of document IDs
        List<Integer> resultList = new ArrayList<>();
        for (int j = 0; j < result.size(); j++) {
            if (result.get(j) == 1) {
                resultList.add(j);
            }
        }
        return resultList;
    }

    public static void main(String[] args) {
        try {
            SearchAlgorithm search = new SearchAlgorithm("matrix.csv");
            List<Integer> result = search.search("approximate AND and");
            System.out.println("Search result: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}