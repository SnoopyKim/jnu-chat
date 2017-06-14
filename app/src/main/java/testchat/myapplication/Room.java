package testchat.myapplication;

import java.util.List;

/**
 * Created by snoopy on 2017-04-21.
 */

//채팅방 정보 데이터
public class Room {

    public List <String> people;
    public String key;
    public String lastTime;

    public Room() {
        //기본 생성자
    }
    public Room(List <String> people, String key) {
        //커스텀 생성자
        this.people = people;
        this.key = key;
        this.lastTime = "";
    }


    public void setPeople(List<String> people) {
        this.people = people;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setlastTime(String alastTime) {this.lastTime = alastTime;}

    public List<String> getPeople() {
        return people;
    }
    public String getKey() {
        return key;
    }
    public String getLastTime() {
        return lastTime;
    }
}
