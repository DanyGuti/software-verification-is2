package domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Complaint implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String nor;
	private String nori;
	private Date noiz;
	@ManyToOne 
	private Booking booking;
	private String deskripzioa;
	private Boolean aurkeztua;
	public String egoera;

    private Complaint(Builder builder) {
        this.nor = builder.nor;
        this.nori = builder.nori;
        this.noiz = builder.noiz;
        this.booking = builder.booking;
        this.deskripzioa = builder.deskripzioa;
        this.aurkeztua = builder.aurkeztua;
        this.egoera = "";
    }

    public static class Builder {
        private String nor;
        private String nori;
        private Date noiz;
        private Booking booking;
        private String deskripzioa;
        private boolean aurkeztua;

        public Builder nor(String nor) {
            this.nor = nor;
            return this;
        }

        public Builder nori(String nori) {
            this.nori = nori;
            return this;
        }

        public Builder noiz(Date noiz) {
            this.noiz = noiz; 
            return this;
        }

        public Builder booking(Booking booking) {
            this.booking = booking;
            return this;
        }

        public Builder deskripzioa(String deskripzioa) {
            this.deskripzioa = deskripzioa;
            return this;
        }

        public Builder aurkeztua(boolean aurkeztua) {
            this.aurkeztua = aurkeztua;
            return this;
        }

        public Complaint build() {
            return new Complaint(this);
        }
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNor() {
		return nor;
	}

	public void setNor(String nor) {
		this.nor = nor;
	}

	public String getNori() {
		return nori;
	}

	public void setNori(String nori) {
		this.nori = nori;
	}

	public Date getNoiz() {
		return noiz;
	}

	public void setNoiz(Date noiz) {
		this.noiz = noiz;
	}

	public String getDeskripzioa() {
		return deskripzioa;
	}

	public void setDeskripzioa(String deskripzioa) {
		this.deskripzioa = deskripzioa;
	}

	public Booking getBooking() {
		return booking;
	}

	public void setBooking(Booking booking) {
		this.booking = booking;
	}

	public Boolean getAurkeztua() {
		return aurkeztua;
	}

	public void setAurkeztua(Boolean aurkeztua) {
		this.aurkeztua = aurkeztua;
	}

	public String getEgoera() {
		return egoera;
	}

	public void setEgoera(String egoera) {
		this.egoera = egoera;
	}

}
