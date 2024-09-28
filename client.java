package chatroom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class client extends JFrame implements ActionListener,Runnable{
	
	private static final long serialVersionUID = 1L;

		//菜单面板
	    private JMenuBar bar = new JMenuBar();
	    private JMenu menu = new JMenu("设置");
	    private JMenuItem about = new JMenuItem("功能");
	    private JMenuItem exit = new JMenuItem("退出");
	    JPanel menupanel = new JPanel();
	//好友列表面板
	    DefaultListModel<String> dl = new DefaultListModel<String>();
	    private JList<String> userList = new JList<String>(dl);
	    JScrollPane listPane = new JScrollPane(userList);
	    JPanel listpanel = new JPanel();
	//消息面板  
	    JPanel msgpanel = new JPanel();
	    JTextArea jta = new JTextArea(10,20);
	    JScrollPane js = new JScrollPane(jta);
	    JPanel operPane = new JPanel();//发送消息的操作面板
	    JLabel input = new JLabel("请输入:");
	    JTextField jtf = new JTextField(24);

	    private JButton jbt = new JButton("发送消息");
	    private JButton jbt1 = new JButton("私聊");
	    private JButton sjbt = new JButton("私聊发送");
        private JButton pjbt = new JButton("群聊发送");
        
	    private BufferedReader br = null;
	    private PrintStream ps = null;
	    private String nickname = null;

	    //私聊面板
	    //JFrame jFrame = new JFrame();//新建了一个窗口 
	    JTextArea jTextArea = new JTextArea(11,45);
	    JScrollPane js1 = new JScrollPane(jTextArea);
	    JTextField jTextField = new JTextField(25);
	
	    
	    double MAIN_FRAME_LOC_X;//父窗口x坐标
	    double MAIN_FRAME_LOC_Y;//父窗口y坐标
	    
	    boolean FirstSecret =true;
	 
	    String sender=null;//私聊发送者的名字
	    String receiver=null;//私聊接收者的名字
	    
	    //群聊功能实现
	    DefaultListModel<String> d2 = new DefaultListModel<String>();//群聊名单
	    private JList<String> pubuserList = new JList<String>(d2);
        
	    private JButton addpu = new JButton("添加群聊名单");
	    private JButton crepu = new JButton("创建群聊");
	    JTextArea pjta = new JTextArea(11,45);
	    JScrollPane js2 = new JScrollPane(pjta);//群聊消息区
	    JTextField pjtf = new JTextField(25);//群聊发送消息区
	    
	    private JFrame pubjf = new JFrame();
	    private JButton dispu = new JButton("解散群聊"); 
	    
	    public client() throws Exception{
	    
	    	 //north 菜单栏
	        bar.add(menu);
	        menu.add(about);
	        menu.add(exit);
	        about.addActionListener(this);
	        exit.addActionListener(this);
	       menupanel.setLayout(new BorderLayout());
	       menupanel.add(bar,BorderLayout.NORTH);
	        this.add(menupanel,BorderLayout.NORTH);
	        
	        //east 好友列表
	        Dimension dim = new Dimension(100,150);
	        listpanel.setPreferredSize(dim);//在使用了布局管理器后用setPreferredSize来设置窗口大小
	        //Dimension dim2 = new Dimension(100,150);
	        //listPane.setPreferredSize(dim2);
	        listpanel.setLayout(new BorderLayout());
	        listpanel.add(listPane,BorderLayout.CENTER);//显示好友列表
	        this.add(listpanel,BorderLayout.EAST);
	        userList.setFont(new Font("隶书",Font.BOLD,18));
	        
	        //center 聊天消息框  发送消息操作面板
	        jta.setEditable(false);
	        msgpanel.setLayout(new BorderLayout());
	        operPane.setLayout(new FlowLayout(FlowLayout.LEFT));
	        operPane.add(input);
	        operPane.add(jtf);
	        operPane.add(jbt);
	        operPane.add(jbt1);
	        operPane.add(addpu);
	        operPane.add(crepu);
	        jbt.addActionListener(this);
	        jbt1.addActionListener(this);
	        addpu.addActionListener(this);
	        crepu.addActionListener(this);
	        msgpanel.add(js,BorderLayout.CENTER);//js是消息展示框JScrollPane
	        msgpanel.add(operPane,BorderLayout.SOUTH);
	        this.add( msgpanel,BorderLayout.CENTER);
         
	        nickname = JOptionPane.showInputDialog(this,"输入昵称");
	        this.setTitle(nickname+"的聊天室");
	        d2.addElement(nickname);//DefaultListModel来更改JList的内容
            pubuserList.repaint();
	        
	        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	    	this.setSize(800,500);
	    	this.setLocation(400,100);
	    	MAIN_FRAME_LOC_X=400;
	 	    MAIN_FRAME_LOC_Y=100;
	    	this.setVisible(true);
	    	
	    	Socket s = new Socket("192.168.0.104",9999);	
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			ps = new PrintStream(s.getOutputStream());
			ps.println("LOGIN#"+nickname);
			new Thread(this).start();
			
			this.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                ps.println("OFFLINE#" + nickname);//发送下线信息，消息格式：OFFLINE#nickName
	            }
	        });
	
			
	    }
	    public void actionPerformed(ActionEvent e)
	    {
	    	String label = e.getActionCommand();
	        if(label.equals("发送消息")){//群发
	            handleSend();
	        }
	        else if(label.equals("私聊"))
	        {
	        	receiver = userList.getSelectedValuesList().get(0);//获得被选择的用户
	             handleSec(receiver);//创建私聊窗口
	             sender = nickname;
	
	        }else if(label.equals("私聊发送")){
	            handlepse();//私发消息
	        }
	        else if(label.equals("功能")){
	            JOptionPane.showMessageDialog(this, "1.可以在聊天框中进行群聊\n\n2.可以点击选择用户进行私聊");
	        }else if(label.equals("退出")){
	            JOptionPane.showMessageDialog(this, "您已成功退出！");
	            ps.println("OFFLINE#" + nickname);
	            System.exit(0);
	        }else if(label.equals("添加群聊名单"))
	        {
	        	String temp =userList.getSelectedValuesList().get(0);
	        	 d2.addElement(temp);//DefaultListModel来更改JList的内容
                 pubuserList.repaint();
	        }	  
	        else if(label.equals("创建群聊"))
	        {
	        	handlepub();
	        	DefaultListModel<String> model = (DefaultListModel<String>) pubuserList.getModel();
	    	    StringBuilder participants = new StringBuilder();
	    	    for (int i = 0; i < model.getSize(); i++) {
	    	    	if(i > 0)
	    	    	{
	    	            participants.append(",");
	    	    	}
	    	        participants.append(model.getElementAt(i));
	    	    }
	    	   
	    	    ps.println("EGROUP#"+nickname+"#"+participants);
	        }
	        else if(label.equals("群聊发送"))
	        {
	        	handleSendGroupMessage();
	        }
	        else if(label.equals("解散群聊"))
	        {
	        	DefaultListModel<String> model = (DefaultListModel<String>) pubuserList.getModel();
	    	    // 构建参与者名单字符串
	    	    StringBuilder participants = new StringBuilder();
	    	    for (int i = 0; i < model.getSize(); i++) {
	    	    	if(i > 0)
	    	    	{
	    	            participants.append(",");
	    	    	}
	    	        participants.append(model.getElementAt(i));
	    	    }
	    	    
	    	    ps.println("END#" +nickname+"#"+participants.toString());
	        	d2.clear();
	        	d2.addElement(nickname);
                pubuserList.repaint();
                pubjf.dispose();
	        }
	        else{
	            System.out.println("不识别的事件");
	        }

	
	    }
	    
	    public void run(){
			while(true){
				try{				
					 String msg = br.readLine();//读取服务器是否发送了消息给该客户端
					
		                String[] strs = msg.split("#");
		               //jta.append(nickname+"get"+msg+strs[0]);
		               //System.out.println("First token: " + strs[0]+"First token: " + strs[1]);
		                //判断是否为服务器发来的登陆信息
		                if(strs[0].equals("END"))
		                {
		                	jta.append("群聊被解散");
		                	d2.clear();
		    	        	d2.addElement(nickname);
		                    pubuserList.repaint();
		                    pubjf.dispose();
		                }
				
		                if(strs[0].equals("LOGIN")){
		                    if(!strs[1].equals(nickname)){//不是本人的上线消息就显示，本人的不显示
		                        jta.append(strs[1] + "来啦！\n");
		                        dl.addElement(strs[1]);//DefaultListModel来更改JList的内容
		                        userList.repaint();
		                    }
		                }else if(strs[0].equals("MSG")){//接到服务器发送消息的信息
		                    if(!strs[1].equals(nickname)){//别人说的
		                        jta.append(strs[1] + "说：" + strs[2] + "\n");
		                    }else{
		                        jta.append("我说：" + strs[2] + "\n");
		                    }
		                }else if(strs[0].equals("USERS")){//USER消息，为新建立的客户端更新好友列表
		                    dl.addElement(strs[1]);
		                    userList.repaint();
		                } else if(strs[0].equals("ALL"))
		                {
		                    jta.append("系统消息：" + strs[1] + "\n");
		                }else if(strs[0].equals("OFFLINE"))
		                {
		                	if(strs[1].equals(nickname)) {//如果是自己下线的消息，说明被服务器端踢出聊天室，强制下线
		                        javax.swing.JOptionPane.showMessageDialog(this, "您已被系统给除去！！！");
		                        System.exit(0);
		                	}
		                    jta.append(strs[1] + "除去了！\n");
		                    dl.removeElement(strs[1]);
		                    userList.repaint();
		                }else if((strs[2].equals(nickname) || strs[1].equals(nickname)) && strs[0].equals("SMSG"))
		                {
		                	
		                	if(!strs[1].equals(nickname)){
		                    	jTextArea.append(strs[1] + "说：" + strs[3] + "\n");
		                        jta.append("系统提示：" + strs[1] + "私信了你" + "\n");
		                    }else{
		                        jTextArea.append("我说：" + strs[3] + "\n");
		                    }
		                }else if((strs[2].equals(nickname) || strs[1].equals(nickname)) && strs[0].equals("FSMSG"))
		                {
		                	
		                	//接收方第一次收到私聊消息，自动弹出私聊窗口
		                	if(!strs[1].equals(nickname)) {
		                		sender = strs[2];
			                	receiver = strs[1];
		                		FirstSecret= false;
		                		jTextArea.append(strs[1] + "说：" + strs[3] + "\n");
		                		jta.append("系统提示：" + strs[1] + "与你私信" + "\n");
		                		handleSec(strs[1]);
		                	}
		                	else //如果是发送消息
		                		{
		                		jTextArea.append("我说：" + strs[3] + "\n");
		                	}
		                }
		                
		                else if(strs[0].equals("EGROUP") && !strs[1].equals(nickname)) 
		                {
		                	
		                	// 解析字符串，将其分割成单个用户名
		                	String[] usernames = strs[2].split(",");		                	
		                	// 遍历用户名数组，并将它们添加到 d2 中
		                	for (String username : usernames) {		                	
		                	    // 这里可以添加一些检查，以确保不会重复添加相同的用户名
		                	    if (!d2.contains(username)) {
		                	        d2.addElement(username);
		                	        
		                	    }
		                	}
		                	handlepub();
		                }
		                
		                else if(strs[0].equals("GROUP"))
		                {
		                	if(strs[1].equals(nickname))
		                	{
		                		pjta.append("我说：" + strs[2] + "\n");
		                	}
		                	else 
		                	{
		                		pjta.append(strs[1]+"说：" + strs[2] + "\n");
		                	}
		                }
		                
		               
		               
				}catch(Exception ex){}
			}
		}
	    
	    public void handleSend()
	    {//群发消息
	        //发送信息时标识一下来源
	        ps.println("MSG#" + nickname + "#" +  jtf.getText());
	        //发送完后，是输入框中内容为空
	        jtf.setText("");
	    }
	    public void handleSec(String name){ //建立私聊窗口
	        JFrame jFrame = new JFrame();//新建了一个窗口      
	        JPanel JPL1 = new JPanel();
	        JPanel JPL2 = new JPanel();
	        FlowLayout f2 = new FlowLayout(FlowLayout.LEFT);
	        JPL1.setLayout(f2);
	        JPL1.add(jTextField);
	        JPL1.add(sjbt);
	        JPL2.add(js1,BorderLayout.CENTER);
	        JPL2.add(JPL1,BorderLayout.SOUTH);
	        jFrame.add(JPL2);

	        sjbt.addActionListener(this);
	        jFrame.setSize(450,300);
	        jFrame.setLocation((int)MAIN_FRAME_LOC_X+20,(int)MAIN_FRAME_LOC_Y+20);//将私聊窗口设置总是在父窗口的中间弹出
	        jFrame.setTitle("与"+name + "私聊");
	        jFrame.setVisible(true);

	        
	        jFrame.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	            	if(!FirstSecret) {
	    	    		ps.println("SMSG#" + nickname + "#" + receiver + "#" + "Goodbye,my friend!");
	    	    	} 	
	            	jTextArea.setText("");
	            	FirstSecret = true;
	            }
	        });
	    }
	    public void handlepse(){//在私聊窗口中发消息
	    	
	    	if(FirstSecret) {
	    		ps.println("FSMSG#" + nickname + "#" + receiver + "#" + jTextField.getText());
	        	jTextField.setText(""); 
	        	FirstSecret = false;
	    	}
	    	else {
	    		ps.println("SMSG#" + nickname + "#" + receiver + "#" + jTextField.getText());
	    		jTextField.setText("");
	    	} 	     
	    }

       public void handlepub()
       {
    	    
    	    //JFrame pubjf = new JFrame();//新建了一个窗口      
	        JPanel JPL1 = new JPanel();
	        JPanel JPL2 = new JPanel();
	        JScrollPane plistPane = new JScrollPane(pubuserList);
		    JPanel plistpanel = new JPanel();
		    Dimension dim = new Dimension(80,120);
	        plistpanel.setPreferredSize(dim);
	        plistpanel.setLayout(new BorderLayout());
	        plistpanel.add(plistPane,BorderLayout.CENTER);//显示群聊列表
	        
	        FlowLayout f3 = new FlowLayout(FlowLayout.LEFT);
	        JPL1.setLayout(f3);
	        JPL1.add(pjtf);
	        JPL1.add(pjbt);
	        JPL1.add(dispu);
	        
	        JPL2.add(js2,BorderLayout.CENTER);
	        JPL2.add(JPL1,BorderLayout.SOUTH);
	        pubjf.setLayout(new BorderLayout());
	        pubjf.add(JPL2,BorderLayout.CENTER);
	        pubjf.add(plistpanel,BorderLayout.WEST);
	        pjbt.addActionListener(this);
	        dispu.addActionListener(this);
	        pubjf.setSize(550,300);
	        pubjf.setLocation((int)MAIN_FRAME_LOC_X+20,(int)MAIN_FRAME_LOC_Y+20);//将群聊窗口设置总是在父窗口的中间弹出
	        pubjf.setTitle(nickname+"加入的群聊");
	        pubjf.setVisible(true);//这时pubjf才会出现！！

	        
	        pubjf.addWindowListener(new WindowAdapter() {
	           
	            public void windowClosing(WindowEvent e) {
	            	
	            	pjta.setText("");
	            	
	            }
	        });
       }
       public void handleSendGroupMessage() {
    	    // 获取群聊名单模型
    	    DefaultListModel<String> model = (DefaultListModel<String>) pubuserList.getModel();
    	    // 构建参与者名单字符串
    	    StringBuilder participants = new StringBuilder();
    	    for (int i = 0; i < model.getSize(); i++) {
    	    	if(i > 0)
    	    	{
    	            participants.append(",");
    	    	}
    	        participants.append(model.getElementAt(i));
    	    }
    	    // 发送群聊消息到服务器
    	    String message = pjtf.getText();
    	    ps.println("GROUP#" + participants.toString() + "#" +nickname+"#"+ message);
    	    pjtf.setText(""); // 清空输入框
    	}
	public static void main (String[] args)throws Exception
	{
		new client();
	}
}
