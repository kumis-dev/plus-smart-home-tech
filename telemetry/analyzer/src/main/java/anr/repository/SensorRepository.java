package anr.repository;

import anr.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, String> {
    // Существует ли датчик, у которого id входит в список ids, и hubId равен hubId
    // In = id входит в список ids; true, если в хабе есть ХОТЯ БЫ ОДИН датчик из списка (не все!)
    boolean existsByIdInAndHubId(Collection<String> ids, String hubId);
    Optional<Sensor> findByIdAndHubId(String id, String hubId);
    void deleteByIdAndHubId(String id, String hubId);
}
