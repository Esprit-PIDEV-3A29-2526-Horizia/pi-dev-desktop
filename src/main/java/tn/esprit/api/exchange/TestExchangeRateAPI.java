package tn.esprit.api.exchange;

public class TestExchangeRateAPI {
    public static void main(String[] args) throws Exception {
        ExchangeRateService service = new ExchangeRateService("caf8787d8c18e7a731a2cc98b6fba712");

        var eurToTnd = service.getEurToTndRate();
        System.out.println("1 EUR = " + eurToTnd + " TND");

        var tndToEur = service.getTndTo("EUR");
        var tndToUsd = service.getTndTo("USD");
        var tndToGbp = service.getTndTo("GBP");

        System.out.println("1 TND = " + tndToEur.rate + " EUR");
        System.out.println("1 TND = " + tndToUsd.rate + " USD");
        System.out.println("1 TND = " + tndToGbp.rate + " GBP");
        System.out.println("Maj: " + tndToEur.lastUpdated + (tndToEur.fromCache ? " (cache)" : ""));
    }
}