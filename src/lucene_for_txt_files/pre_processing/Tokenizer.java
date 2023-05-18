package pre_processing;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    public static List<String> tokenize(String text) {
        // Convert to lowercase
        text = text.toLowerCase();
        // Replace non-alphanumeric characters with whitespace
        text = text.replaceAll("[^a-zA-Z0-9]", " ");
        // Split by whitespace
        String[] tokensArray = text.split("\\s+");
        // Convert array to list
        List<String> tokensList = new ArrayList<String>();
        for (String token : tokensArray) {
            tokensList.add(token);
        }
        return tokensList;
    }

}