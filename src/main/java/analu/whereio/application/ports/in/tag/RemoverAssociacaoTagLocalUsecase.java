package analu.whereio.application.ports.in.tag;
public interface RemoverAssociacaoTagLocalUsecase {
    void execute(String idLocal, String idTag, String userId);
}
