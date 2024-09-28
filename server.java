package chatroom;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class server extends JFrame implements Runnable,ActionListener
{
	private static final long serialVersionUID = 1L;
	
	
	private ServerSocket ss = null;
	 private JPanel jpl = new JPanel();
	    private JButton jbt1 = new JButton("踢出");
	    private JButton jbt2 = new JButton("群发");
	    private JTextField jtf = new JTextField();
	    private ArrayList<ChatThread> users = new ArrayList<ChatThread>();
	   
	    DefaultListModel<String> dl = new DefaultListModel<String>();
	   
	    private JList<String> userList = new JList<String>(dl);
	    

public server() throws Exception {
	
	this.setTitle("服务器端");
	this.add(jpl,"South");
	this.add(userList, "North");

	
	jpl.setLayout(new BorderLayout());
	jpl.add(jbt1,BorderLayout.EAST);
	jpl.add(jbt2,BorderLayout.WEST);
	jbt1.addActionListener(this);
	jbt2.addActionListener(this);
	
	jtf.setColumns(10);
	jpl.add(jtf,BorderLayout.SOUTH);
	
	ss = new ServerSocket(9999);

	
	this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	this.setSize(500,250);
	this.setLocation(400,100);
	this.setVisible(true);
	
	new Thread(this).start();
}

public void run() {
	while(true){
		try{
			Socket s = ss.accept();
			ChatThread ct = new ChatThread(s);
			users.add(ct);
			ListModel<String> model = userList.getModel();
            for(int i = 0; i < model.getSize(); i++){//更新之前的用户
                ct.ps.println("USERS#" + model.getElementAt(i));
            }

		}catch(Exception ex){}
	}
	
}

public void actionPerformed(ActionEvent e) {
    String label = e.getActionCommand();
    if(label.equals("群发")){
        handleAll();
    }else if(label.equals("踢出")){
    	handleExpel();
    }
}

public void handleAll(){
    if(!jtf.getText().equals("")){
        sendMessage("ALL#" + jtf.getText());
        jtf.setText("");
    }
}//群发消息


public void handleExpel() //throws IOException 
{
    sendMessage("OFFLINE#" + userList.getSelectedValuesList().get(0));
    dl.removeElement(userList.getSelectedValuesList().get(0));//更新defaultModel
    userList.repaint();//更新Jlist
}

class ChatThread extends Thread{
	BufferedReader br = null;
	PrintStream ps = null;
	public String nickName=null;
	ChatThread(Socket s) throws Exception{
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		ps = new PrintStream(s.getOutputStream());
		this.start();
	}
	public void run(){
		 while(true){
             try{
                 String msg = br.readLine();//接收客户端发来的消息
                 String[] strs = msg.split("#");
                 if(strs[0].equals("LOGIN")){//收到来自客户端的上线消息
                    nickName = strs[1];
                     dl.addElement(nickName);
                     userList.repaint();//更新服务器端的用户列表
                     sendMessage(msg);
                 }else if(strs[0].equals("MSG") || strs[0].equals("SMSG") || strs[0].equals("FSMSG")){
                     sendMessage(msg);
                 }else if(strs[0].equals("OFFLINE")){//收到来自客户端的下线消息
                     sendMessage(msg);
                     //System.out.println(msg);
                     dl.removeElement(strs[1]);
                     // 更新List列表
                     userList.repaint();
                 }
                 else if(strs[0].equals("GROUP"))
                 {
                	 String participantList = strs[1];
                	 String sender = strs[2];
                     String message = strs[3];
                     handleGroupMessage(participantList,sender, message);
                 }
                 else if(strs[0].equals("EGROUP"))
                 {
                	 
                	 String participantList = strs[2];
                	 String sender = strs[1];
                	 System.out.println(participantList);
                	 EstablishGroup(sender,participantList);
                 }
                 else if(strs[0].equals("END"))
                 {
                	 String participantList = strs[2];
                	 String sender = strs[1];
                	 endGroup(participantList,sender);
                 }
             }catch (Exception ex){}
         }

}
}

public void sendMessage(String msg){  
    for(ChatThread ct : users){
        ct.ps.println(msg);
    }
}
public void handleGroupMessage(String participantList,String sender,String message) {
    // 根据参与者名单发送消息
    String[] participants = participantList.split(",");
    for (ChatThread ct : users) {
        if (Arrays.asList(participants).contains(ct.nickName)) {
            ct.ps.println("GROUP#" + sender+"#"+message);
        }
    }
}

public void EstablishGroup(String sender,String participantList){
	String[] participants = participantList.split(",");
    for (ChatThread ct : users) {
        if (Arrays.asList(participants).contains(ct.nickName)) {
            ct.ps.println("EGROUP#" + sender+"#"+participantList);
        }
    }
	
}

public void endGroup(String participantList,String sender) {
    // 根据参与者名单发送消息
    String[] participants = participantList.split(",");
    for (ChatThread ct : users) {
        if (Arrays.asList(participants).contains(ct.nickName)&&!ct.nickName.equals(sender)) {
            ct.ps.println("END#end");
        }
    }
}
public static void main(String[] args) throws Exception{
	 new server();
}
}

