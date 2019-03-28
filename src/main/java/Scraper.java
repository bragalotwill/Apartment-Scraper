import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class Scraper {

    public static String apartmentURL = "https://tracemidtown.com/floorplans/";

    public static void main(String[] args) throws IOException {
        Document floorplans = Jsoup.connect(apartmentURL).get();
        for (Element e: floorplans.select("a")) {
            String link = e.attr("href");
            if (link.contains(apartmentURL)) {
                Document floorplan = Jsoup.connect(link).get();
                System.out.println(floorplan.title());
            }
        }
    }
}
