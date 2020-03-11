package com.finalyear.login.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ConductorLocationBus implements Parcelable {
    private GeoPoint geoPoint;
    private Conductor conductor;
    private String busNumber;
    private String busSource;
    private String busDestination;
    private @ServerTimestamp Timestamp timestamp;
    private String busCount;

    public ConductorLocationBus(GeoPoint geoPoint, Conductor conductor, String busNumber, String busSource, String busDestination, String busCount, Timestamp timestamp) {
        this.geoPoint = geoPoint;
        this.conductor = conductor;
        this.busNumber = busNumber;
        this.busSource = busSource;
        this.busDestination = busDestination;
        this.timestamp = timestamp;
        this.busCount=busCount;
    }

    public ConductorLocationBus() {
    }

    public String getBusCount() {
        return busCount;
    }

    public void setBusCount(String busCount) {
        this.busCount = busCount;
    }

    protected ConductorLocationBus(Parcel in) {
        busNumber = in.readString();
        busSource = in.readString();
        busDestination = in.readString();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<ConductorLocationBus> CREATOR = new Creator<ConductorLocationBus>() {
        @Override
        public ConductorLocationBus createFromParcel(Parcel in) {
            return new ConductorLocationBus(in);
        }

        @Override
        public ConductorLocationBus[] newArray(int size) {
            return new ConductorLocationBus[size];
        }
    };

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusSource() {
        return busSource;
    }

    public void setBusSource(String busSource) {
        this.busSource = busSource;
    }

    public String getBusDestination() {
        return busDestination;
    }

    public void setBusDestination(String busDestination) {
        this.busDestination = busDestination;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ConductorLocationBus{" +
                "geoPoint=" + geoPoint +
                ", conductor=" + conductor +
                ", busNumber='" + busNumber + '\'' +
                ", busSource='" + busSource + '\'' +
                ", busDestination='" + busDestination + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(busNumber);
        dest.writeString(busSource);
        dest.writeString(busDestination);
        dest.writeParcelable(timestamp, flags);
    }
}
