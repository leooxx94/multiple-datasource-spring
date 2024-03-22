package com.leox.services.OracleRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.leox.services.CondizioniCommerciali;

@Repository
public interface ControllerRepository extends JpaRepository<CondizioniCommerciali, String>{

    @Query("SELECT prezzo FROM CondizioniCommerciali WHERE id = :idCC")
    Double queryPrezzo(@Param("idCC") String idCC);
    
}
