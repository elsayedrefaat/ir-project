package lucene_for_txt_files.pre_processing;

import lucene_for_txt_files.docs_manager.DocData;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    public static ArrayList<DocData> tokenize(ArrayList<DocData> docs) {
        ArrayList<DocData> results = new ArrayList<>();
        int i;
        for (DocData doc : docs) {
            i = 0;
            doc.abstractText = doc.abstractText.toLowerCase();
            // Replace non-alphanumeric characters with whitespace
            doc.abstractText = doc.abstractText.replaceAll("[^a-zA-Z0-9]", " ");
            // Split by whitespace
            String[] tokensArray = doc.abstractText.split("\\s+");
            // Convert array to list
            List<String> tokensList = new ArrayList<String>();
            for (String token : tokensArray) {
                tokensList.add(token);
            }
            i++;
            results.add(new DocData(String.valueOf(i), "", "", "", String.join(" ",tokensList)));
        }
        return results;
    }

}