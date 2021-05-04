package com.example.polls.repository;

import com.example.polls.model.BarCodeNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface BarCodeNumberRepository extends JpaRepository<BarCodeNumber, Long> {

    Optional<BarCodeNumber> findByCardId(int cardId);

    @Transactional
    @Modifying
    @Query("update BarCodeNumber b set b.barCodeNumber = :barCodeNumber where b.cardId = :cardId")
    void update(@Param("barCodeNumber") Long barCodeNumber,@Param("cardId") int cardId);
}
