package py.una.pol.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import py.una.pol.domain.Simulation;

public interface SimulationRepository extends MongoRepository<Simulation, String> {
}
