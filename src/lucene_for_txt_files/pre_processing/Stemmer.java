package lucene_for_txt_files.pre_processing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lucene_for_txt_files.docs_manager.DocData;
import org.tartarus.snowball.ext.PorterStemmer;

public class Stemmer {

    public static void main(String[] args) {
        String fileName = "cisi/CISI.ALL";
        String data = loadData(fileName);

        // Split the data into documents
        String[] documents = data.split("\\.I ");

        // Remove the first empty document
        documents = Arrays.copyOfRange(documents, 1, documents.length);

        System.out.println(documents[0]);
    }

    public static String loadData(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return sb.toString();
    }

    public static Set<String> getStopWords() {
        Set<String> stopWords = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("stopwords.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopWords.add(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return stopWords;
    }

    public static ArrayList<DocData> removeStopWords(ArrayList<DocData> documents, Set<String> stopWords) {
        ArrayList<DocData> results = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            String[] words = documents.get(i).abstractText.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (!stopWords.contains(word.toLowerCase())) {
                    sb.append(word).append(" ");
                }
            }
            results.add(new DocData(String.valueOf(i + 1), "", "", "", sb.toString().trim()));
        }
        return results;
    }

    public static ArrayList<DocData> applyStemming(ArrayList<DocData> documents) {
        PorterStemmer stemmer = new PorterStemmer();
        System.out.println(documents.size());
        ArrayList<DocData> results = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            String[] words = documents.get(i).abstractText.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (word.endsWith("sses")) {
                    word = word.substring(0, word.length() - 2);
                } else if (word.endsWith("ies")) {
                    word = word.substring(0, word.length() - 2) + "i";
                } else if (word.endsWith("s")) {
                    word = word.substring(0, word.length() - 1);
                }
                stemmer.setCurrent(word);
                stemmer.stem();
                sb.append(stemmer.getCurrent()).append(" ");
            }
            results.add(new DocData(String.valueOf(i + 1), "", "", "", sb.toString().trim()));
        }
        System.out.println(results.size());
        return results;
    }

}