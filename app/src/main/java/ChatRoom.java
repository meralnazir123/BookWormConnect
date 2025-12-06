public class ChatRoom {
    public String chatRoomId;
    public String user1;
    public String user2;
    public String lastMessage;

    public ChatRoom() {}

    public ChatRoom(String chatRoomId, String user1, String user2, String lastMessage) {
        this.chatRoomId = chatRoomId;
        this.user1 = user1;
        this.user2 = user2;
        this.lastMessage = lastMessage;
    }

}
