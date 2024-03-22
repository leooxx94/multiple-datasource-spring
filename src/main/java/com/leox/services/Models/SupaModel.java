package com.leox.services.Models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "clienti_commerciale",schema="public")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupaModel {
    @Id
    @JsonProperty("_id")
    private String _id;
    @JsonProperty("id")
    private String businessPartnerID;
    @JsonProperty("descrizione")
    private String descrizione;
    @JsonProperty("indirizzo")
    private String indirizzo;
    @JsonProperty("localita")
    private String localita;
    @JsonProperty("numeroCivico")
    private String numeroCivico;
    @JsonProperty("cap")
    private String cap;
    @JsonProperty("ragioneSociale")
    private String ragioneSociale;
    @JsonProperty("Commerciali")
    private List<String> commerciali = new ArrayList<>();
    private String business_partner_bubble_id;
    private String stato_giudizio;
    private String commerciale_atlantide_id;
    @JsonProperty("intermediarioUser")
    private String intermediarioUser;
}
