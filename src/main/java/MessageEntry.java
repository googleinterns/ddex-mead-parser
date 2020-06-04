public class MessageEntry extends AbstractEntry {
  public MessageEntry(String title, String namespace) {
    super(title, namespace);
  }

  @Override
  public boolean isMessage() { return true; }
}
