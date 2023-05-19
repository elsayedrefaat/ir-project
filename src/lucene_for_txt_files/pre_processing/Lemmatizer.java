package lucene_for_txt_files.pre_processing;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import lucene_for_txt_files.docs_manager.DocData;
import lucene_for_txt_files.docs_manager.DocsSplitter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Lemmatizer {
    public static List<DocData> lemmatize(List<DocData> documents) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        for (DocData document : documents) {
            String content = document.abstractText;
            Annotation annotation = new Annotation(content);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            StringBuilder sb = new StringBuilder();
            for (CoreMap sentence : sentences) {
                for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                    sb.append(lemma).append(" ");
                }
            }
            document.abstractText = sb.toString().trim();
        }


        return documents;
    }
    public static void main(String[] args) {
        List<DocData> documents = new ArrayList<>();
        final File docDir = new File("cisi");
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
                documents = DocsSplitter.tokenize(f);
            }
        }
        lemmatize(documents);
        for (DocData doc : documents) {
            System.out.println(doc.abstractText);
        }

    }
}