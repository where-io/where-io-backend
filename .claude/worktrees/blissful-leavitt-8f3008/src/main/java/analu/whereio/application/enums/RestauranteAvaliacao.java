package analu.whereio.application.enums;

public enum RestauranteAvaliacao {
    OTIMO(1),
    BOM(2),
    REGULAR(3),
    RUIM(4),
    PESSIMO(5);

    private final int avaliacaoNumero;

    RestauranteAvaliacao(int avaliacaoNumero) {
        this.avaliacaoNumero = avaliacaoNumero;
    }
}
