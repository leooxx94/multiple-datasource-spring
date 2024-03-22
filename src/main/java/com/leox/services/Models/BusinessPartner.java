package com.leox.services.Models;

import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Component
@Data
@Entity
@Table(name = "business_partner")
public class BusinessPartner {
    @Id
    private String id;
    private String id_agente_commerciale;
}
