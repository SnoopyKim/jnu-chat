package testchat.myapplication;

import java.util.List;

/**
 * Created by snoopy on 2017-04-21.
 */

//채팅방 정보 데이터
public class Room {

    public List <String> people;
    public String key;
    public boolean lastText;
    public String photo;
    public boolean lastTime;

    public Room() {
        //기본 생성자
    }
    public Room(List <String> people, String key,boolean lastText,String aPhoto, boolean aLasttime) {
        //커스텀 생성자
        this.people = people;
        this.key = key;
        this.lastText = lastText;
        this.photo = aPhoto;
        this.lastTime = aLasttime;
    }


    public void setPeople(List<String> people) {
        this.people = people;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setLastText(boolean lastText) {
        this.lastText = lastText;
    }
    public void setPhoto(String aPhoto) {this.photo = aPhoto;}
    public void setlastTime(boolean alastTime) {this.lastTime = alastTime;}

    public List<String> getPeople() {
        return people;
    }
    public String getKey() {
        return key;
    }
    public boolean getLastText() {
        return lastText;
    }
    public String getPhoto() {
        return photo;
    }
    public boolean getLastTime() {
        return lastTime;
    }
}
