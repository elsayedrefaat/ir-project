package lucene_for_txt_files.term_doc;

import java.util.*;

import lucene_for_txt_files.docs_manager.DocData;
import pre_processing.Tokenizer;

public class IncidenceMatrix {

    private final List<String> allTokens;
    private final int[][] matrix;
    static ArrayList<DocData> docs = new ArrayList<>();

    public IncidenceMatrix(ArrayList<DocData> documents) {
        // Create an ArrayList to store all the dictionary terms
        allTokens = new ArrayList<>();

        // Tokenize each document and add the terms to the ArrayList
        for (DocData document : documents) {
            document.abstractText = String.join(" ", Tokenizer.tokenize(document.abstractText));
            StringTokenizer tokenizer = new StringTokenizer(document.abstractText, " \t\n\r\f.,-;:()[]{}\"'");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if (!allTokens.contains(token)) {
                    allTokens.add(token);
                }
            }
        }

        // Sort the ArrayList of terms in alphabetical order
        Collections.sort(allTokens);

        // Create a matrix of size (# of terms) x (# of documents)
        int nTerms = allTokens.size();
        int nDocs = documents.size();
        matrix = new int[nTerms][nDocs];

        // Populate the matrix by looping over each document and each term
        for (int j = 0; j < nDocs; j++) {
            StringTokenizer tokenizer = new StringTokenizer(documents.get(j).abstractText, " \t\n\r\f.,;:()[]{}\"'");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                int i = allTokens.indexOf(token);
                if (i >= 0) {
                    matrix[i][j] = 1;
                }
            }
        }
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public List<String> getAllTokens() {
        return allTokens;
    }
}
