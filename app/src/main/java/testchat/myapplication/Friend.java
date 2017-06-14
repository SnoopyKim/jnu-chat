package testchat.myapplication;

/**
 * Created by snoopy on 2017-04-21.
 */

/**
 * @Name    Friend
 * @Usage   Form of Friend info
 * @Comment String photo is photo's url. photo saved in (Facebook user : facebook server  ,  Email user : firebase Storage/users/)
 * TODO     class Friend have get/set function. So variable in Friend  change public to private
 * */
public class Friend {

    //이메일, 이름, 사진url으로 이루어짐
    public String uid;
    public String email;
    public String name;
    public String photo;

    public Friend() {
        //기본 생성자
    }
    public Friend(String uid, String email, String name, String photo) {
        //생성자2
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.photo = photo;
    }

    public String getUid() {
        return uid;
    }
    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getPhoto() {
        return photo;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setEmail(String email) {
        this.email = email;
    };
    public void setName(String name) {
        this.name = name;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }

}
