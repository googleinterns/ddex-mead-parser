public class EnumCandidate extends EntryCandidate {
  public EnumCandidate(String title, String namespace) {
    super(title, namespace);
  }
  @Override
  public boolean isEnum() { return true; }
}
