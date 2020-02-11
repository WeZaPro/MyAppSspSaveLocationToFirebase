package example.com.savelocationtofirebase;

import android.os.Parcel;
import android.os.Parcelable;

public class MyModel{
    String uid;
    String name;
    double Lat;
    double Long;

    public MyModel() {
    }

    public MyModel(String uid, String name, double lat, double aLong) {
        this.uid = uid;
        this.name = name;
        Lat = lat;
        Long = aLong;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double aLong) {
        Long = aLong;
    }
}
