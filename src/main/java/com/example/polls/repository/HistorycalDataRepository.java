package com.example.polls.repository;

import com.example.polls.model.HistorycalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorycalDataRepository extends JpaRepository<HistorycalData, Long> {
    List<HistorycalData> findAllByOrderByIdDesc();
}
