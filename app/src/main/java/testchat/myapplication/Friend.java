package testchat.myapplication;

/**
 * Created by snoopy on 2017-04-21.
 */

//친구 데이터 클라스
public class Friend {

    //이메일, 이름, 사진url으로 이루어짐
    public String email;
    public String name;
    public String photo;

    public Friend() {
        //기본 생성자
    }
    public Friend(String email, String name, String photo) {
        //생성자2
        this.email = email;
        this.name = name;
        this.photo = photo;
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
