package testchat.myapplication;

/**
 * Created by snoopy on 2017-04-01.
 */

//채팅방 내에서의 말풍선 데이터
public class Chat {

    public String email;
    public String name;
    public String text;

    public Chat() {
        //기본 생성자 (데이터를 Chat.class로 변환할라면 꼭 필요)
    }

    public Chat(String text) {
        this.text = text;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
