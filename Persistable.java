/** Contract: can serialize itself to a CSV line and return a display summary. */
public interface Persistable {
    String toCSVString();
    String getSummary();
}
