/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lucene_for_txt_files.pre_processing;

import docs_manager.DocData;
import docs_manager.DocsSplitter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import pre_processing.Tokenizer;

/**
 *
 * @author Elsayed Refaat
 */
public class Normailzation {

    public static ArrayList<DocData> normalize(ArrayList<DocData> docs) {
        ArrayList<DocData> result = new ArrayList<>();
        for (DocData doc : docs) {
            List<String> data = Tokenizer.tokenize(doc.abstractText);
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
        
        docs = normalize(docs);
        
        for(DocData doc:docs){
            System.out.println(doc.abstractText);
        }
        
    }
}
