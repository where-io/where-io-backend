package analu.whereio.adapters.out.persistence.mapper;

import analu.whereio.adapters.out.persistence.entity.VisitaEntity;
import analu.whereio.application.model.Visita;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",  unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VisitaPersistanceMapper {

    VisitaEntity toEntity(Visita visita);
    Visita toDomain(VisitaEntity visitaEntity);

//    default ObjectId map(String value) {
//        if (value == null || value.isBlank()) {
//            return null;
//        }
//        return new ObjectId(value);
//    }
//
//    default String map(ObjectId value) {
//        return value == null ? null : value.toHexString();
//    }
}
