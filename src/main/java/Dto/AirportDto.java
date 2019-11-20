package Dto;

public class AirportDto {
    public String city;
    public String country;
    public String airportName;
    public String IATA;
    public String ICAO;
    public String FAA;

    @Override
    public String toString() {
        return "AirportDto{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", airportName='" + airportName + '\'' +
                ", IATA='" + IATA + '\'' +
                ", ICAO='" + ICAO + '\'' +
                ", FAA='" + FAA + '\'' +
                '}';
    }
}
