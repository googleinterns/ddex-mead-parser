public class EnumEntry extends AbstractEntry {
  public EnumEntry(String title, String namespace) {
    super(title, namespace);
  }

  @Override
  public boolean isEnum() { return true; }
}
