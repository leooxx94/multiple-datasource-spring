package com.leox.services.Models;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Component
@Data
@Entity
@Table(name = "partners_statigiudizio")
@IdClass(PartnersStatiGiudizioID.class)
public class PartnersStatiGiudizio {
    
    @Id @Column
    private String id_business_partner;
    @Id @Column
    private String id_stato_giudizio;
}
