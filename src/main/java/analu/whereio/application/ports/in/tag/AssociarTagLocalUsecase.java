package analu.whereio.application.ports.in.tag;
public interface AssociarTagLocalUsecase {
    void execute(String idLocal, String idTag, String userId);
}
