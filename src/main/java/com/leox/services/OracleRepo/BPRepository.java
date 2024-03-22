package com.leox.services.OracleRepo;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.leox.services.Models.BusinessPartner;

@Repository
public interface BPRepository extends JpaRepository<BusinessPartner, String>{

    @Query("SELECT sg.descrizione " +
           "FROM BusinessPartner bp " +
           "JOIN PartnersStatiGiudizio ps " +
           "ON bp.id = ps.id_business_partner " +
           "JOIN StatiGiudizio sg " +
           "ON ps.id_stato_giudizio = sg.id "+
           "WHERE bp.id = :businessPartnerId")
    List<String> queryStatiGiudizio(@Param("businessPartnerId") String businessPartnerId);
    
}
