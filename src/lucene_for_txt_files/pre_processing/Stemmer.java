/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lucene_for_txt_files.pre_processing;

/**
 *
 * @author Elsayed Refaat
 */
import docs_manager.DocData;
import docs_manager.DocsSplitter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static lucene_for_txt_files.pre_processing.Normailzation.normalize;
import org.tartarus.snowball.ext.PorterStemmer;

public class Stemmer {


    public static ArrayList<DocData> MyStemmer(List<DocData> docs) {
        ArrayList<DocData> result = new ArrayList<>();
        PorterStemmer stemmer = new PorterStemmer();
        for (DocData doc : docs) {
            stemmer.setCurrent(doc.abstractText);
            stemmer.stem();
            result.add(new DocData(doc.id, "", "", "", stemmer.getCurrent()));
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
        
        docs = MyStemmer(docs);
        
        for(DocData doc:docs){
            System.out.println(doc.abstractText);
        }
    }
}
