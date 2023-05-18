package docs_manager;

public class DocData {
    public String id;
    String title;
    String author;
    String source;
    public String abstractText;

    public DocData(String id, String title, String author, String source, String abstractText) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.source = source;
        this.abstractText = abstractText;
    }
}
