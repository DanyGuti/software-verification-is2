package domain;
import java.util.Date;
//1
public class RideObject {
    String from;
    String to;
    Date date;

    public RideObject(String from, String to, Date date) {
        this.from = from;
        this.to = to;
        this.date = date;
    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}
