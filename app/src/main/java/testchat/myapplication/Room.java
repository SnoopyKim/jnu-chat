package testchat.myapplication;

import java.util.List;

/**
 * Created by snoopy on 2017-04-21.
 */

/**
 * @Name    Room
 * @Usage   Form of room(Chatting room) info
 * @Comment String photo is photo's url. photo saved in (Facebook user : facebook server  ,  Email user : firebase Storage/users/)
 * TODO     class Room have get/set function. So variable in Friend  change public to private
 * */
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
