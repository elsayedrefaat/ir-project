/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lucene_for_txt_files.pre_processing;

/**
 *
 * @author Elsayed Refaat
 */
import java.util.*;

public class StopWordRemover {

    private Set<String> stopWords;

    public StopWordRemover() {
        stopWords = new HashSet<>(Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with"));
    }

    public List<String> removeStopWords(List<String> words) {
        List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                filteredWords.add(word);
            }
        }
        return filteredWords;
    }

    public static void main(String[] args) {
        StopWordRemover remover = new StopWordRemover();
        List<String> words = Arrays.asList("the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog");
        List<String> filteredWords = remover.removeStopWords(words);
        System.out.println(filteredWords);
    }
}
