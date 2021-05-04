package com.example.polls.repository;

import com.example.polls.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {

    List<Person> findAllByStatusIn(List<String> list);
    List<Person> findAllByStatus(String status);
    Optional<Person> findById(Long id);
    @Modifying
    @Transactional
    @Query("update Person p set p.status = :status where p.id = :id")
        void update(Long id, String status);

    @Modifying
    @Transactional
    @Query("delete from Person p where p.id = :id")
    void deleteById(Long id);
}
