package com.leox.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "condizioni_commerciali")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CondizioniCommerciali {

    public CondizioniCommerciali() {}

    @Id
    private String id;
    private Double prezzo;

}
