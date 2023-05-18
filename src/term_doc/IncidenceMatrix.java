package term_doc;

import docs_manager.DocData;
import docs_manager.DocsSplitter;

import java.io.*;
import java.util.*;
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

    public static void main(String[] args) throws IOException {
        String indexPath = "matrix";
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
                System.out.println("Indexing " + f.getCanonicalPath());
                docs = DocsSplitter.tokenize(f);
            }
        }

        IncidenceMatrix matrix = new IncidenceMatrix(docs);

        String filename = indexPath + ".csv";
        PrintWriter writer = new PrintWriter(new FileWriter(filename));

        for (int i = 0; i < matrix.getMatrix().length; i++) {
            writer.print(matrix.getAllTokens().get(i));
            writer.print(",");
            for (int j = 0; j < matrix.getMatrix()[0].length; j++) {
                writer.print(matrix.getMatrix()[i][j]);
                if (j < matrix.getMatrix()[0].length - 1) {
                    writer.print(",");
                }
            }
            writer.println();
        }
        writer.close();

        // Do something with the matrix and the list of terms, e.g. use them for information retrieval
    }
}
