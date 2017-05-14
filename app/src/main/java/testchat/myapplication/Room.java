package testchat.myapplication;

import java.util.List;

/**
 * Created by snoopy on 2017-04-21.
 */

public class Room {

    public List <String> people;
    public String key;
    public boolean lastText;

    public Room() {
        //default constructor
    }
    public Room(List <String> people, String key,boolean lastText) {
        this.people = people;
        this.key = key;
        this.lastText = lastText;

    }

    public List<String> getPeople() {
        return people;
    }

    public void setPeople(List<String> people) {
        this.people = people;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean getLastText() {
        return lastText;
    }

    public void setLastText(boolean lastText) {
        this.lastText = lastText;
    }
}
