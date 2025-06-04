package kia.example.springbatch.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonAfterProcessRepository extends JpaRepository<PersonAfterProcess , Long> {
}
