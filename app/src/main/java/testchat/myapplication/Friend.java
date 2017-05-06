package testchat.myapplication;

/**
 * Created by snoopy on 2017-04-21.
 */

public class Friend {

    public String name;
    public String photo;
    public String facebook_id;

    public Friend() {
        //default constructor
    }

    public String getName() {
        return name;
    }
    public String getPhoto() {
        return photo;
    }
    public String getFacebook_id() {
        return facebook_id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    public void setFacebook_id(String id) {
        this.facebook_id = id;
    }

}
