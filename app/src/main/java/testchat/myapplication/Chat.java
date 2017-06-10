package testchat.myapplication;

/**
 * Created by snoopy on 2017-04-01.
 */

//채팅방 내에서의 말풍선 데이터
public class Chat {

    public String uid;
    public String name;
    public String text;
    public String file;
    public String time;

    public Chat() {
        //기본 생성자 (데이터를 Chat.class로 변환할라면 꼭 필요)
    }

    public Chat(String uid, String name, String text, String file, String time) {
        this.uid = uid;
        this.name = name;
        this.text = text;
        this.file = file;
        this.time = time;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFile() {
        return file;
    }
    public void setFile(String file) {
        this.file = file;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}
