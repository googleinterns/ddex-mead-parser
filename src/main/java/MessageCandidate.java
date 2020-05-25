public class MessageCandidate extends EntryCandidate {
  public MessageCandidate(String title, String namespace) {
    super(title, namespace);
  }
  @Override
  public boolean isMessage() { return false; }
}
