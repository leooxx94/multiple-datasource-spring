package com.leox.services;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class Model {
    
    private String idRifiuto;
    private String idCondizioneCommerciale;
    private String codice;
    private String descrizione;
    private String descrizioneCondizioneCommerciale;
    private String cer;
    private String statoFisico;
    private String statoFisicoStr;
    private String classificazione;
    private String attrezzatura;
    private String codiceAttrezzatura;
    private boolean presenzaAnalisi;
    private String ultimaScadenzaAnalisi;
    private Long ultimaScadenzaAnalisiUnix;
    private boolean inListaControlloAnalisi;
    private String idInsediamento;
    private String idFatturareA;
    private boolean bloccatoAnalisiScaduta;
    private boolean bloccatoAnalisiAssente;

    private boolean analisiInScadenza;
    private boolean statoIntermediato;
    private Long dataRichiestaAmmissibile;

    private String operazione;
    private String codiceOperazione;
    private String statoFisicoCodice;

    private Long dataMinimaRitiro;

}
