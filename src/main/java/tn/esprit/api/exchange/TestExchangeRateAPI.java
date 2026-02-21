package tn.esprit.api.exchange;

public class TestExchangeRateAPI {

    public static void main(String[] args) throws Exception {

        ExchangeRateService service =
                new ExchangeRateService("caf8787d8c18e7a731a2cc98b6fba712");

        double rate = service.getEurToTndRate();

        System.out.println("1 EUR = " + rate + " TND");
        System.out.println("100 EUR = " + (100 * rate) + " TND");
    }
}