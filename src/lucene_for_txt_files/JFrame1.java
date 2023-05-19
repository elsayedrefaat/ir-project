package lucene_for_txt_files;

import inverted_index.InvertedIndex;
import inverted_index.InvertedIndexSearch;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lucene_for_txt_files.docs_manager.DocData;
import lucene_for_txt_files.docs_manager.DocsSplitter;
import lucene_for_txt_files.pre_processing.Stemmer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import positional_index.PositionalIndex;
import pre_processing.Tokenizer;
import term_doc.SearchAlgorithm;

import javax.swing.*;

public class JFrame1 extends javax.swing.JFrame {

    ArrayList<DocData> docs = new ArrayList<>();

    public JFrame1() {
        initComponents();
    }

    ///////////////////////////////////////////////---searcher-----\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    private void searcher() throws IOException, ParseException {
        String indexDir = "index";

        // create an index searcher
        Directory directory = FSDirectory.open(new File(indexDir));
        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            // create a query parser
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
            String[] fieldsToSearch = {"id", "title", "author", "source", "contents"};
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_42, fieldsToSearch, analyzer);
            // get the search query from the user
            String queryString = jTextArea2.getText();
            Query query = queryParser.parse(queryString);
            TopDocs topDocs = searcher.search(query, 55);
            ScoreDoc[] hits = topDocs.scoreDocs;
            // print the results
            System.out.println("Search results:");
            jTextArea1.append("Search results: \n");
            for (int i = 0; i < hits.length; i++) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println((i + 1) + ". " + d.get("id") + " - " + d.get("fullpath"));
                jTextArea1.append((i + 1) + "--------> " + d.get("id") + " - " + d.get("fullpath") + "\n");
            }
            // cleanup
        }
    }
//
    /////////////////////////////////////////////-----incedinceMatrix-------\\\\\\\\\\\\\\\\\\\\\\\\\

    public void incedinceMatrix() throws IOException {
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

        if (jCheckBox3.isSelected()) {
            docs = Stemmer.MyStemmer(docs);
        }
        term_doc.IncidenceMatrix matrix = new term_doc.IncidenceMatrix(docs);

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

    private void indexer() throws IOException {
        ArrayList<DocData> docs = new ArrayList<>();
        Directory indexDirectory = FSDirectory.open(new File("index"));
        SimpleAnalyzer sa = new SimpleAnalyzer(Version.LUCENE_42);

        IndexWriterConfig analyzerConfig
                = new IndexWriterConfig(Version.LUCENE_42, sa);

        try (IndexWriter writer = new IndexWriter(indexDirectory, analyzerConfig)) {
            String dataDir = "cisi";
            File[] files = new File(dataDir).listFiles();

            assert files != null;
            for (File f : files) {
                if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead()) {
                    System.out.println("Indexing " + f.getCanonicalPath());
                    docs = DocsSplitter.tokenize(f);
                }
                for (DocData document : docs) {
                    Document doc = new Document();

                    List<String> text;
                    if (jCheckBox1.isSelected()) {
                        text = Tokenizer.tokenize(document.abstractText);
                    } else {
                        text = Tokenizer.tokenize(document.abstractText);

                    }

                    doc.add(new Field("id", document.id, Field.Store.YES, Field.Index.ANALYZED)); //Index file content

                    doc.add(new Field("contents", String.join(" ", text), Field.Store.YES, Field.Index.ANALYZED)); //Index file content

                    doc.add(new Field("filename", f.getName(), //Index file name
                            Field.Store.YES, Field.Index.ANALYZED));

                    doc.add(new Field("fullpath", f.getCanonicalPath(), // Index file full path
                            Field.Store.YES, Field.Index.NOT_ANALYZED));
                    writer.addDocument(doc); // Add document to Lucene index
                }
            }
            System.out.println("# of Docs indexed = " + writer.numDocs());
            System.out.println("Lucene Index Built Successfully.");
        }

    }

    private void invertedIndexIndexer() {
        List<DocData> documents = new ArrayList<>();
        // populate documents list with DocData objects
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
                documents = DocsSplitter.tokenize(f);
            }
        }

        InvertedIndex index = new InvertedIndex(documents);
        try {
            index.writeIndexToFile("index.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void positionalIndexIndexer() {
        ArrayList<DocData> docs = new ArrayList<>();
        String docsPath = "cisi";

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory does not exist or is not readable, please check the path");
            System.exit(1);
        }
        docs = JFrame1.getDocData(docs,docDir);
        PositionalIndex index = new PositionalIndex(docs);
        index.saveToFile("positional-index.txt");
    }

    public static ArrayList<DocData> getDocData(ArrayList<DocData> docs, File docDir) {
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
        return docs;
    }

    private void matrixSearch() {
        try {
            SearchAlgorithm search = new SearchAlgorithm("matrix.csv");
            List<Integer> result = search.search(jTextArea2.getText());
            System.out.println("Search result: " + result);
            jTextArea1.append("Search result: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void invertedIndexSearch() {
        try {
            String indexPath = "index.txt";
            String query = jTextArea2.getText();
            InvertedIndexSearch invertedIndex = new InvertedIndexSearch(indexPath);
            List<Integer> result = invertedIndex.search(query);
            jTextArea1.setText("Search Results: \n");
            for (int docId : result) {
                System.out.println("Document ID: " + docId);
                jTextArea1.append("Document ID: " + docId + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(JFrame1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void positionalIndexSearch() {
        // Load index from file and perform search
        PositionalIndex indexFromFile = new PositionalIndex(Collections.emptyList());
        indexFromFile.loadFromFile("positional-index.txt");

        String phrase = "in large information systems";
        System.out.println("Search query: " + phrase);
        List<Integer> results = indexFromFile.search(phrase);
        jTextArea1.setText("Search Results : ");
        for (int id : results) {
            System.out.println("Document " + id);
            jTextArea1.append("Document " + id+"\n");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JButton jButton1 = new javax.swing.JButton();
        javax.swing.JButton jButton2 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        // Variables declaration - do not modify//GEN-BEGIN:variables
        javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        javax.swing.JCheckBox jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        javax.swing.JCheckBox jCheckBox4 = new javax.swing.JCheckBox();
        javax.swing.JCheckBox jCheckBox5 = new javax.swing.JCheckBox();
        jComboBox2 = new javax.swing.JComboBox<>();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IR Project");

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("IR Project 2023");

        jButton1.setText("Indexing");
        jButton1.setMaximumSize(new java.awt.Dimension(82, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(82, 23));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed();
            }
        });

        jButton2.setText("Searching");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed();
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Lucene", "Incidence Matrix", "Inverted Index", "Positional Index", "Bi-word Index"}));
        jComboBox1.addItemListener(this::jComboBox1ItemStateChanged);
        jComboBox1.addActionListener(evt -> jComboBox1ActionPerformed());

        jLabel2.setFont(new java.awt.Font("Segoe UI Light", 3, 12)); // NOI18N
        jLabel2.setText("Choose Index");

        jLabel3.setFont(new java.awt.Font("Segoe UI Light", 3, 14)); // NOI18N
        jLabel3.setText("Pre-Processing");

        jCheckBox1.setText("Tokenization");
        jCheckBox1.addActionListener(this::jCheckBox1ActionPerformed);

        jCheckBox2.setText("Normalization");
        jCheckBox2.addActionListener(this::jCheckBox2ActionPerformed);

        jCheckBox3.setText("Stemming");
        jCheckBox3.addActionListener(evt -> jCheckBox3ActionPerformed());

        jCheckBox4.setText("Lemetization");

        jCheckBox5.setText("Stop Words");
        jCheckBox5.addActionListener(evt -> jCheckBox5ActionPerformed());

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Lucene", "Incidence Matrix", "Inverted Index", "Positional Index", "Bi-word Index"}));
        jComboBox2.addItemListener(this::jComboBox2ItemStateChanged);
        jComboBox2.addActionListener(this::jComboBox2ActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI Light", Font.BOLD | Font.ITALIC, 12)); // NOI18N
        jLabel4.setText("Choose Index");

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel5.setFont(new java.awt.Font("Segoe UI Light", Font.BOLD | Font.ITALIC, 14)); // NOI18N
        jLabel5.setText("Type here : ");

        jLabel6.setFont(new java.awt.Font("Segoe UI Light", Font.BOLD | Font.ITALIC, 14)); // NOI18N
        jLabel6.setText("Results : ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(56, 56, 56)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(255, 255, 255))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(145, 145, 145)))
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addGap(68, 68, 68))
                                                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jCheckBox2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jCheckBox3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jCheckBox5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jCheckBox4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(115, 115, 115)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane2)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 416, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                                .addGap(436, 436, 436)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(182, 182, 182)
                                                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(279, 279, 279))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(68, 68, 68)
                                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addGap(54, 54, 54)
                                                                                .addComponent(jLabel5)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(37, 37, 37))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addGap(31, 31, 31)
                                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addGap(18, 18, 18)
                                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(jCheckBox1))
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                .addComponent(jCheckBox2)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(18, 18, 18)))
                                                                .addComponent(jCheckBox4)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(jLabel6)
                                                                        .addComponent(jCheckBox5))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed() {
        try {
            int index = jComboBox1.getSelectedIndex();
            if (index == 0) {
                indexer();
            } else if (index == 1) {
                try {
                    incedinceMatrix();
                } catch (IOException ex) {
                    Logger.getLogger(JFrame1.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (index == 2) {
                invertedIndexIndexer();
            } else if (index == 3) {
                positionalIndexIndexer();
            }

        } catch (IOException ex) {
            Logger.getLogger(JFrame1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void jComboBox1ActionPerformed() {
    }

    private void jCheckBox3ActionPerformed() {
    }

    private void jCheckBox5ActionPerformed() {
    }

    private void jButton2ActionPerformed() {
        try {
            if (jComboBox2.getSelectedIndex() == 0) {
                searcher();

            } else if (jComboBox2.getSelectedIndex() == 1) {
                matrixSearch();
            } else if (jComboBox2.getSelectedIndex() == 2) {
                invertedIndexSearch();
            } else if (jComboBox2.getSelectedIndex() == 3) {
                positionalIndexSearch();
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(JFrame1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void jCheckBox2ActionPerformed(ActionEvent evt) {
    }

    private void jComboBox1ItemStateChanged(ItemEvent evt) {
    }

    private void jComboBox2ItemStateChanged(ItemEvent evt) {
    }

    private void jComboBox2ActionPerformed(ActionEvent evt) {
    }

    private void jCheckBox1ActionPerformed(ActionEvent evt) {
    }

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException |
                 IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JFrame1.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame1().setVisible(true);
            }
        });
    }

    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables


}
