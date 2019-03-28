import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scraper {

    public static String apartmentURL = "https://tracemidtown.com/floorplans/";

    public static void main(String[] args) throws IOException {
        Document floorplans = Jsoup.connect(apartmentURL).get();
        List<Apartment> apartments = new ArrayList<>();
        for (Element e: floorplans.select("a")) {
            String link = e.attr("href");
            if (link.contains(apartmentURL)) {
                Document floorplan = Jsoup.connect(link).get();
                System.out.println(floorplan.title());
                String fpType = floorplan.title().split(" \\|")[0];
                Apartment found = null;
                for (Element e2: floorplan.select("td")) {
                    if (e2.select("span").toString().contains("Rent")) {
                        String rent = e2.ownText();
                        found.setRent(rent);
                    }
                    if (e2.select("span").toString().contains("Availability")) {
                        String availability = e2.ownText();
                        if (!availability.contains("Available Now")) {
                            availability = availability.split("on ") [1];
                        }
                        found.setMoveIn(availability);
                        apartments.add(found);
                        found = null;
                    }
                    if (e2.select("span").toString().contains("Apt #")) {
                        found = new Apartment(fpType);
                        String apNum = e2.ownText();
                        found.setApNum(Integer.parseInt(apNum));
                    }
                }
            }
        }
        System.out.println(apartments);
    }
}
class Apartment {

    private String moveIn;
    private String rent;
    private String fpType;
    private int apNum;

    public Apartment(String fpType) {
        this.fpType = fpType;
    }

    public void setMoveIn(String moveIn) {
        this.moveIn = moveIn;
    }

    public void setRent(String rent) {
        this.rent = rent;
    }

    public void setApNum(int apNum) {
        this.apNum = apNum;
    }

    public String toString() {
        return String.format("Floor Plan: %s\nRent: %s\nMove In: %s\n", fpType,
                rent, moveIn);
    }
}
