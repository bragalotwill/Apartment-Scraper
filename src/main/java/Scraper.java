import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;



public class Scraper {

    final static String apartmentURL = "https://tracemidtown.com/floorplans/";
    final static long timeInterval = 86400000;
    final static String username = "webscraperapartments@gmail.com";
    final static String password = "webscraper123";
    final static String recipient = "bragalotwill@gmail.com";

    public static void main(String[] args) throws IOException, InterruptedException {
        while(true) {
            Document floorplans = Jsoup.connect(apartmentURL).get();
            List<Apartment> apartments = new ArrayList<>();
            List<Apartment> augustApartments = new ArrayList<>();
            for (Element e : floorplans.select("a")) {
                String link = e.attr("href");
                if (link.contains(apartmentURL)) {
                    Document floorplan = Jsoup.connect(link).get();
                    System.out.println(floorplan.title());
                    String fpType = floorplan.title().split(" \\|")[0];
                    Apartment found = null;
                    for (Element e2 : floorplan.select("td")) {
                        if (e2.select("span").toString().contains("Rent")) {
                            String rent = e2.ownText();
                            found.setRent(rent);
                        }
                        if (e2.select("span").toString().contains("Availability")) {
                            String availability = e2.ownText();
                            if (!availability.contains("Available Now")) {
                                availability = availability.split("on ")[1];
                            }
                            found.setMoveIn(availability);
                            apartments.add(found);
                            if (found.getMoveIn().contains("August")) {
                                augustApartments.add(found);
                            }
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
            if (augustApartments.size() > 0) {
                Properties prop = new Properties();
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.starttls.enable", "true");
                prop.put("mail.smtp.host", "smtp.gmail.com");
                prop.put("mail.smtp.port", "587");
                Session session = Session.getInstance(prop, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(username));
                    message.setRecipients(
                            Message.RecipientType.TO,
                            InternetAddress.parse(recipient));
                    message.setSubject("August Apartment Available!");
                    message.setText(makeMessage(augustApartments));
                    Transport.send(message);
                    System.out.println("Sent!");
                } catch(MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
            Thread.sleep(timeInterval);
        }
    }
    public static String makeMessage(List<Apartment> apartments) {
        String toRet = "";
        for (Apartment a: apartments) {
            toRet += a + "\n";
        }
        return toRet;
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

    public String getMoveIn() {
        return moveIn;
    }

    public String toString() {
        return String.format("Floor Plan: %s\nRent: %s\nMove In: %s\n", fpType,
                rent, moveIn);
    }
}
