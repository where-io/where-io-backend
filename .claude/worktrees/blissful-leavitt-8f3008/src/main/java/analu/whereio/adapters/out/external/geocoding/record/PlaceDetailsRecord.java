package analu.whereio.adapters.out.external.geocoding.record;

public record PlaceDetailsRecord(
        double lat,
        double lng,
        String logradouro,
        String bairro,
        String cidade,
        String estado,
        String cep,
        String pais,
        String formattedAddress
) {}
