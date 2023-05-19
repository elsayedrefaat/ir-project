/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lucene_for_txt_files.pre_processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lucene_for_txt_files.JFrame1;
import lucene_for_txt_files.docs_manager.DocData;

/**
 *
 * @author Elsayed Refaat
 */
public class Normailzation {

    public static ArrayList<DocData> normalize(ArrayList<DocData> docs) {
        ArrayList<DocData> result = new ArrayList<>();
        for (DocData doc : docs) {
            List<String> data = Arrays.asList(doc.abstractText.split("\\s+"));
            List<String> docData = new ArrayList<>();
            for (String word : data) {
                String normalizedWord = word.toLowerCase();
                normalizedWord = normalizedWord.replaceAll("[^a-z0-9]", "");
                docData.add(normalizedWord);
            }

            result.add(new DocData(doc.id, "", "", "", String.join(" ", docData)));
        }

        return result;
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
        docs = JFrame1.getDocData(docs, docDir);

        docs = normalize(docs);
        
        for(DocData doc:docs){
            System.out.println(doc.abstractText);
        }
        
    }
}
