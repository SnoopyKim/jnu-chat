package testchat.myapplication;

import java.util.List;

/**
 * Created by snoopy on 2017-04-21.
 */

//채팅방 정보 데이터
public class Room {

    public List <String> people;
    public String key;
    public String lastText;
    public String photo;
    public String lastTime;

    public Room() {
        //기본 생성자
    }
    public Room(List <String> people, String key,String aPhoto) {
        //커스텀 생성자
        this.people = people;
        this.key = key;
        this.lastText = "";
        this.photo = aPhoto;
        this.lastTime = "";
    }


    public void setPeople(List<String> people) {
        this.people = people;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setLastText(String lastText) {
        this.lastText = lastText;
    }
    public void setPhoto(String aPhoto) {this.photo = aPhoto;}
    public void setlastTime(String alastTime) {this.lastTime = alastTime;}

    public List<String> getPeople() {
        return people;
    }
    public String getKey() {
        return key;
    }
    public String getLastText() {
        return lastText;
    }
    public String getPhoto() {
        return photo;
    }
    public String getLastTime() {
        return lastTime;
    }
}
