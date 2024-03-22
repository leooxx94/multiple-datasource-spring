package com.leox.services;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.leox.services.OracleRepo.BPRepository;
import com.leox.services.SupaRepo.SupaRepository;
import com.leox.services.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leox.services.Models.SupaModel;
import com.leox.services.OracleRepo.ControllerRepository;

@RestController()
public class Controller {

    private final ControllerRepository repo;
    private final SupaRepository supaRepo;
    private final BPRepository bpRepo;
  
    @Autowired
    Controller(ControllerRepository repo, SupaRepository supaRepo, BPRepository bpRepo) {
      this.repo = repo;
      this.supaRepo = supaRepo;
      this.bpRepo = bpRepo;
    }

    private static final String AUTH_TOKEN = "Bearer YOUR_TOKEN";

    String json = "";
    String str = "{ \"results\":  ";
    String fin = " }";

    // Pass as a parameter to the URL http://yourip:port/api/getwaste?businessPartnerId=xxxxxx&siteId=xxxxxx

    @RequestMapping(value = "api/getwaste", method = RequestMethod.GET, produces = "application/json")
    public String elabora(@RequestParam("businessPartnerId") String bpId, @RequestParam("siteId") String siteId) throws ParseException{
        String jsonRes = "";
        String url = "https://url_for_business?businessPartnerId="+bpId+"&siteId="+siteId;

        RestTemplate restTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.ofMillis(50000))
        .setReadTimeout(Duration.ofMillis(50000)).build();

        Long dataMinimaRitiro = calcolaDataRitiro(bpId);
        String rifiuto = restTemplate.getForObject(url, String.class);
        ArrayList<Model> listaModel = new ArrayList<>();

        // Parsing the JSON returned from the call

        String rifStr = rifiuto.replace("[", "").replace("]", "").replace(",{", "{");
        String[] lista = rifStr.split("(?<=})");
        if(rifStr==""){
            jsonRes = "{\"numeroContratti\": 0 }";
            return jsonRes;
        }

        // Iterate through the list of contracts returned from the API call for the business partner and site passed in the api/getwaste call

        for(int i=0; i<lista.length; i++){

            JSONObject obj = new JSONObject(lista[i]);
            Model m = new Model();

            String ultimaScadAna = obj.optString("ultimaScadenzaAnalisi");
            String presAnalisi = obj.getString("presenzaAnalisi");
            String inListaAnalisi = obj.getString("inListaControlloAnalisi");
            String statoFisico = obj.getString("statoFisico");
            String idCliente = bpId;
            String fatturareA = obj.getString("idFatturareA");
            String codice = obj.getString("codice");

            // Methods for converting the data received from the call

            String statoFisicoStr = statoFisicoConverter(statoFisico);
            String statoFisicoCode = statoFisicoCodiceConverter(statoFisico);
            boolean presAnalisiBool = booleanConverter(presAnalisi);
            boolean inListaAnalisiBool = booleanConverter(inListaAnalisi);
            boolean dataInScad = calcDataInScadenza(ultimaScadAna);
            boolean statoIntermediario = calcStatoIntermediario(idCliente, fatturareA);
            Long dataAmmissibile = calcDataAmmissibile(ultimaScadAna);
            boolean analisiScaduta = calcAnalisiScaduta(inListaAnalisiBool, presAnalisiBool, ultimaScadAna);
            boolean analisiAssente = calcAnalisiAssente(inListaAnalisiBool, presAnalisiBool);
            boolean operazione = calcCodice(codice);
            
            Long ultimaScad = unixConverter(ultimaScadAna);

            // Setting the attributes of the Model class that represent in this case the features of the contract for the business partner

            m.setPresenzaAnalisi(presAnalisiBool);
            m.setInListaControlloAnalisi(inListaAnalisiBool);
            m.setUltimaScadenzaAnalisiUnix(ultimaScad);
            m.setStatoFisicoStr(statoFisicoStr);
            m.setStatoFisicoCodice(statoFisicoCode);
            m.setAnalisiInScadenza(dataInScad);
            m.setStatoIntermediato(statoIntermediario);
            m.setDataRichiestaAmmissibile(dataAmmissibile);
            m.setBloccatoAnalisiScaduta(analisiScaduta);
            m.setBloccatoAnalisiAssente(analisiAssente);
            m.setIdRifiuto(obj.getString("idRifiuto"));
            m.setIdCondizioneCommerciale(obj.getString("idCondizioneCommerciale"));
            m.setCodice(codice);
            m.setDescrizione(obj.getString("descrizione"));
            m.setDescrizioneCondizioneCommerciale(obj.getString("descrizioneCondizioneCommerciale"));
            m.setCer(obj.getString("cer"));
            m.setStatoFisico(statoFisico);
            m.setClassificazione(obj.getString("classificazione"));
            m.setAttrezzatura(obj.getString("attrezzatura"));
            m.setCodiceAttrezzatura(obj.getString("codiceAttrezzatura"));
            m.setUltimaScadenzaAnalisi(ultimaScadAna);
            m.setIdInsediamento(obj.getString("idInsediamento"));
            m.setIdFatturareA(fatturareA);
            m.setDataMinimaRitiro(dataMinimaRitiro);

            if(operazione){
                m.setOperazione("RECUPERO");
                m.setCodiceOperazione("R");
            }else{
                m.setOperazione("SMALTIMENTO");
                m.setCodiceOperazione("D");
            }

            listaModel.add(m);
        }

        int count = listaModel.size();

        System.out.println("LISTA MODELLI:");
        System.out.println("");
        for(int i=0; i<listaModel.size(); i++){
            System.out.println("MODELLO N°"+i+": " + listaModel.get(i));
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert the list of Model objects into JSON format
            String json = objectMapper.writeValueAsString(listaModel);
            String jsonResult = "";
            if(count==0){
                jsonResult = "{\"numeroContratti\": "+count+"}";
            }else{
                jsonResult = "{\"numeroContratti\": "+count+", \"contratti\": " + json + " }";
            }

            return jsonResult;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Errore durante la conversione in JSON";
        }

    }

    /*----------- Beginning of the methods for various conversions ------------- */

    public Long unixConverter(String dataStr) throws ParseException{
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        if(dataStr==""){
            return null;
        }
        Date date = dateFormat.parse(dataStr);
        Long unixTime = (Long) date.getTime()/1000;
        return unixTime;
    }

    public boolean booleanConverter(String str){
        boolean flag;
        if(str.equalsIgnoreCase("SI")){
            flag = true;
        }else{
            flag = false;
        }
        return flag;
    }

    public String statoFisicoConverter(String num){
        String stato = "";
        switch (num) {
            case "1":
                stato = "SOLIDO POLVERULENTO";
                break;
            case "2":
                stato = "SOLIDO NON POLVERULENTO";
                break;
            case "3":
                stato = "FANGOSO PALABILE";
                break;
            case "4":
                stato = "LIQUIDO";
                break;
            default:
                break;
        }
        return stato;
    }

    public String statoFisicoCodiceConverter(String num){
        String stato = "";
        switch (num) {
            case "1":
                stato = "V";
                break;
            case "2":
                stato = "S";
                break;
            case "3":
                stato = "F";
                break;
            case "4":
                stato = "L";
                break;
            default:
                break;
        }
        return stato;
    }

    public boolean calcDataInScadenza(String dataInScad) throws ParseException{
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(dataInScad==""){
            return false;
        }
        LocalDate dataInScadenza = LocalDate.parse(dataInScad, formatter);
        long differenzaInMesi = ChronoUnit.MONTHS.between(now, dataInScadenza);
        if(differenzaInMesi <= 2 && differenzaInMesi >= 0){
            return true;
        }else{
            return false;
        }
    }

    public Long calcDataAmmissibile(String dataInScad) throws ParseException{
        //LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(dataInScad==""){
            return null;
        }
        LocalDate dataInScadenza = LocalDate.parse(dataInScad, formatter);

        // Calcola 5 giorni lavorativi escludendo sabato e domenica
        int giorniLavorativi = 0;
        while (giorniLavorativi < 5) {
            dataInScadenza = dataInScadenza.minus(1, ChronoUnit.DAYS);
            if (!(dataInScadenza.getDayOfWeek() == DayOfWeek.SATURDAY || dataInScadenza.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                giorniLavorativi++;
            }
        }
        String dataScadMinusCinque = dataInScadenza.format(formatter);
        Long dataScadCinqueUnix = unixConverter(dataScadMinusCinque);
        return dataScadCinqueUnix;
    }

    public boolean calcStatoIntermediario(String idCliente, String idFatturareA){
        if(idCliente.equals(idFatturareA)){
            return false;
        }else{
            return true;
        }
    }

    public boolean calcAnalisiScaduta(boolean listaAnalisi, boolean presenzaAnalisi, String dataScadenza){
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(dataScadenza==""){
            return false;
        }
        LocalDate dataInScadenza = LocalDate.parse(dataScadenza, formatter);

        if(dataInScadenza.isBefore(now) && listaAnalisi && presenzaAnalisi){
            return true;
        }else if(listaAnalisi && !presenzaAnalisi){
            return false;
        }else{
            return false;
        }
    }

    public boolean calcAnalisiAssente(boolean listaAnalisi, boolean presenzaAnalisi){
        if(listaAnalisi && !presenzaAnalisi){
            return true;
        }else{
            return false;
        }
    }

    public boolean calcCodice(String codice){
        if(codice.contains("R")){
            return true;
        }else{
            return false;
        }
    }

    public Long calcolaDataRitiro(String partnerId) {

        String url = "https://api_url/prendi_accordo?businessPartnerId="+partnerId;

        //HttpEntity<String> entity = new HttpEntity<String>("");
        RestTemplate restTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.ofMillis(50000))
        .setReadTimeout(Duration.ofMillis(50000)).build();
        
        String accordo = restTemplate.getForObject(url, String.class);

        System.out.println("ACCORDO: " + accordo);

		LocalDate dataRitiro = calcolaDataMinimaRitiro(accordo);

		// Convert the date into Unix timestamp format
		Long unixTimestamp = dataRitiro.atStartOfDay(ZoneOffset.UTC).toEpochSecond();;

		return unixTimestamp;

    }
	
    private LocalDate calcolaDataMinimaRitiro(String accordo) {
        LocalDate data = LocalDate.now();
        int giorniDaAggiungere;
        switch (accordo != null ? accordo : "st") {
            case "24":
                giorniDaAggiungere = 1;
                break;
            case "48":
                giorniDaAggiungere = 2;
                break;
            default:
                giorniDaAggiungere = 7;
                break;
        }
        for (int i = 0; i < giorniDaAggiungere; ) {
            data = data.plusDays(1);
            // If it's not Saturday or Sunday, increment i
            if (data.getDayOfWeek() != DayOfWeek.SATURDAY && data.getDayOfWeek() != DayOfWeek.SUNDAY) {
                i++;
            }
        }
		System.out.println("GIORNI PASSATI: " + accordo);
		System.out.println("GIORNI AGGIUNTI: " + giorniDaAggiungere);
        return data;
    }

    public HttpHeaders tokenHeader(){
        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", "Bearer YOUR_TOKEN");
        return header;
    }

    @RequestMapping(value = "api/businesspartner", method = RequestMethod.GET, produces = "application/json")
    public String getBp(@RequestParam("idcommerciale") String idCommerciale, @RequestHeader("Authorization") String authToken){
        json = "";
        if (authToken == null || !authToken.equals(AUTH_TOKEN)) {
            String errorMessage = "Token non valido";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return errorMessage;
        }

        //Gestione del cursore

        int cursor = 0;
        String id = idCommerciale;
        int remaining = getAPiBp(cursor, id);
        while(remaining!=0){
            cursor = cursor + 100;
            remaining = getAPiBp(cursor, id);
        }
        return str+json+fin;
    }
    
    public int getAPiBp(int cursor, String idCommerciale){
        String uri ="https://api_url/businesspartners?constraints={constraints}&cursor="+cursor; //Cursore gestito
        String constraints = "[{ \"key\": \"commerciali\", \"constraint_type\": \"contains\", \"value\": \""+idCommerciale+"\" }]"; //1632919895478x194955536160722900
        HttpHeaders header = tokenHeader();
        HttpEntity<String> entity = new HttpEntity<String>(header);
        RestTemplate restTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.ofMillis(50000))
            .setReadTimeout(Duration.ofMillis(50000)).build();
        ResponseEntity<String> rest = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class, constraints);


        JSONObject jsonObject = new JSONObject(rest.getBody());

        JSONObject response = jsonObject.getJSONObject("response");
        JSONArray resultsArray = response.getJSONArray("results");
        String array = resultsArray.toString();
        json = json + array;
        int remaining = response.getInt("remaining");
        return remaining;
    }

    //Scheduling at 5am every day
    @Scheduled(cron = "0 0 5 * * ?", zone = "Europe/Rome")
    public void startGetForSupa() throws SQLException{

        int cursor = 0;
        int remaining = getPartnersForSupa(cursor);
        
        while(remaining!=0){
            cursor = cursor + 100;
            remaining = getPartnersForSupa(cursor);
        }
    }

    public int getPartnersForSupa(int cursor){
        String url = "https://api_url/businesspartners?cursor="+cursor;
        HttpHeaders header = tokenHeader();
        HttpEntity<String> entity = new HttpEntity<String>(header);
        RestTemplate restTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.ofMillis(50000))
            .setReadTimeout(Duration.ofMillis(50000)).build();
        ResponseEntity<String> rest = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        JSONObject jsonObject = new JSONObject(rest.getBody());
        JSONObject response = jsonObject.getJSONObject("response");
        JSONArray resultsArray = response.getJSONArray("results");

        SupaModel sup = new SupaModel();

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject jjsonObj = resultsArray.getJSONObject(i);

            String _id = jjsonObj.optString("_id");
            String id = jjsonObj.optString("id");
            String descrizione = jjsonObj.optString("descrizione");
            String indirizzo = jjsonObj.optString("indirizzo");
            String localita = jjsonObj.optString("localita");
            String numeroCivico = jjsonObj.optString("numeroCivico");
            String cap = jjsonObj.optString("cap");
            String ragioneSociale = jjsonObj.optString("ragioneSociale");
            String intermediarioUser = jjsonObj.optString("intermediarioUser");

            JSONArray arrJson = jjsonObj.optJSONArray("Commerciali");
            List<String> commerciali = new ArrayList<>();
            if(arrJson != null){
                for(int j = 0; j < arrJson.length(); j++){
                    String comm = arrJson.optString(j);
                    commerciali.add(comm);
                }
            }

            sup.setCap(cap);
            sup.setCommerciali(commerciali);
            sup.setDescrizione(descrizione);
            sup.setBusinessPartnerID(id);
            sup.set_id(_id);
            sup.setIndirizzo(indirizzo);
            sup.setLocalita(localita);
            sup.setNumeroCivico(numeroCivico);
            sup.setRagioneSociale(ragioneSociale);
            sup.setBusiness_partner_bubble_id(_id);
            sup.setIntermediarioUser(intermediarioUser);

            if(id != null && id != ""){
                sup.setCommerciale_atlantide_id(bpRepo.findById(id).get().getId_agente_commerciale());
                List<String> statiList = bpRepo.queryStatiGiudizio(id);

                for(int x = 0; x<statiList.size(); x++){
                    if(statiList.get(x) != null && statiList.get(x).equals("OK")){
                        sup.setStato_giudizio(null);
                        break;
                    }else{
                        sup.setStato_giudizio(statiList.get(x));
                    }
                }
            }else{
                sup.setCommerciale_atlantide_id(null);
                sup.setStato_giudizio(null);
            }
            
            try {
                supaRepo.save(sup);

            } catch (Exception e) {
                System.out.println(e);
            }
            
            System.out.println(sup);
            commerciali.clear();
            
        }

        int remaining = response.getInt("remaining");
        return remaining;

    }

}
