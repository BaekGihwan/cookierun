package cookierunrun;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class Room2Client extends JFrame implements ActionListener, Runnable {
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	private Container c;
	private JButton rivalReadyBtn, readyBtn, sendBtn;
	private JPanel panel, panel2, panel2_1, panel3, panel4, panel4_1, panel5;
	private Panel3_12 panel3_1;
	private JLabel user1, user2;
	private JTextArea output;
	private JTextField input;
	private JButton outBtn;
	public static boolean enterClient = false;

	private int sw = 0;
	private Clip clip;
	private LoginDTO loginDTO;
	private boolean gameStart = false;

	private int enterCount = 0;
	private String nickName;
	private ArrayList<String> joinUser = new ArrayList<String>();
	// 레디 여부
	public boolean meReady = false;
	public boolean rivalReady = false;
	private RoomDAO roomDAO;
	private RoomDTO roomDTO;
	private int roomNum;

	public Room2Client(LoginDTO loginDTO, Clip clip, int roomNum) {
		super("Waiting Room");
		roomDAO = new RoomDAO();

		this.clip = clip;
		this.loginDTO = loginDTO;
		this.roomNum = roomNum;
		roomDTO = new RoomDTO();
		nickName = loginDTO.getNickName();

		Image img = new ImageIcon().getImage();
		panel = new JPanel() {
			ImageIcon icon = new ImageIcon("C:\\cookierun\\png\\fruit.png");

			public void paintComponent(Graphics g) {
				g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		panel.setLayout(null);
		getContentPane().add(panel);

		panel2 = new JPanel() {
			ImageIcon icon = new ImageIcon("C:\\cookierun\\png\\id_form.png");

			public void paintComponent(Graphics g) {
				g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		panel2.setLayout(null);
		panel2.setBounds(30, 50, 200, 400);
		panel.add(panel2);
		panel2_1 = new JPanel() {
			public void paintComponent(Graphics g) {
				Image image = Toolkit.getDefaultToolkit().getImage("C:\\cookierun\\png\\cookie_man.gif");
				g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		panel2_1.setLayout(null);
		user1 = new JLabel(loginDTO.getNickName());
		user1.setBounds(85, 15, 65, 65);
		user1.setForeground(Color.WHITE);
		panel2.add(user1);

		rivalReadyBtn = new JButton(new ImageIcon("C:\\cookierun\\png\\ready_2.png"));
		rivalReadyBtn.setBounds(30, 280, 140, 60);
		rivalReadyBtn.setEnabled(false);

		panel2_1.setBounds(20, 85, 170, 170);
		panel2.add(panel2_1);

		panel3 = new JPanel() {
			ImageIcon icon = new ImageIcon("C:\\cookierun\\png\\id_form.png");

			public void paintComponent(Graphics g) {
				g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		panel3.setLayout(null);
		panel3.setBounds(470, 50, 200, 400);
		panel3_1 = new Panel3_12();
		panel3.add(panel3_1);
		user2 = new JLabel("");
		user2.setBounds(85, 15, 65, 65);
		user2.setForeground(Color.WHITE);
		user2.setOpaque(false);
		panel3.add(user2);

		readyBtn = new JButton(new ImageIcon("C:\\cookierun\\png\\ready_2.png"));
		readyBtn.setBounds(30, 280, 140, 60);
		panel.add(panel3);
		panel2.add(readyBtn);
		panel3.add(rivalReadyBtn);
		panel4 = new JPanel(new BorderLayout());
		output = new JTextArea();
		output.setBackground(new Color(255, 188, 121));
		output.setEditable(false);
		JScrollPane scroll = new JScrollPane(output);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel4.add("Center", scroll);

		panel4_1 = new JPanel();
		panel4_1.setLayout(new BorderLayout());

		input = new JTextField();
		sendBtn = new JButton("보내기");

		panel4_1.add("Center", input);
		panel4_1.add("East", sendBtn);

		panel4.add("South", panel4_1);
		panel4.setBounds(265, 155, 180, 240);
		panel.add(panel4);

		panel5 = new JPanel() {
			ImageIcon icon = new ImageIcon("C:\\cookierun\\png\\cookieRun.png");

			public void paintComponent(Graphics g) {
				g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		panel5.setLayout(null);
		panel5.setBounds(255, 15, 200, 100);

		panel.add(panel5);

		outBtn = new JButton(new ImageIcon("C:\\cookierun\\png\\out.png"));
		outBtn.setBounds(550, 8, 130, 45);
		panel.add(outBtn);

		setBounds(200, 100, 700, 500);
		setResizable(false);
		setVisible(true);

		outBtn.addActionListener(this);
		rivalReadyBtn.addActionListener(this);
		readyBtn.addActionListener(this);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				writer.println("exit");
				writer.flush();
			}
		});
	}

	public void service() {
		String serverIP = "192.168.0.159"; // 이렇게 적으면 바로 서버로 들어간다 IP안치고
		//String serverIP = "192.168.147.4";
		if (serverIP == null || serverIP.length() == 0) {
			System.out.println("서버IP가 입력되지 않았습니다.");
			System.exit(0);
		}
		String nickName = loginDTO.getNickName();
		try {
			// 소켓생성
			socket = new Socket(serverIP, 5100); // 아이피는 위에있는거고 port 는 고정? 꼭 9500써야하나
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (UnknownHostException e) {
			System.out.println("서버를 찾을 수 없습니다.");
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("서버와 연결이 안되었습니다.");
			e.printStackTrace();
			System.exit(0);
		}

		// 서버로 닉네임 보내기
		writer.println(nickName); // ln 이라는거 자체가 \n있는거니깐 이렇게 쓰는게 좋다.
		writer.flush(); // 버퍼안에 찌꺼기 없애기
		// 스레드 생성
		Thread t = new Thread(this); // 인터페이스로 햇을때는 꼭 this를 해야됨
		// 스레드 시작
		t.start();
		// 이벤트 처리
		sendBtn.addActionListener(this); // 이거는 샌드버튼 눌렀을때 한다
		input.addActionListener(this); // 타자를 치고 엔터를 쳤을때 나타낸다

	} // service생성자

	@Override
	public void run() {
		String line;
		// 서버로 부터 받기
		while (true) {
			try {
				// reader은 서버로부터 읽어오는 과정이다.
				line = reader.readLine(); // line의 기준은 엔터가 기준이다. 엔터 치기전까지의 모든 Line을 읽어 온다
				// 입장했을 떄 몇명이 접속했는지 확인
				if (line.contains("입장")) {
					String[] name = line.split("님");
					joinUser.add(name[0]);
					if (joinUser.size() == 2) { // 두명 다 입장
						enterClient = true;
						panel3_1.repaint();
						user2.setText(name[0]);
						readyBtn.setEnabled(true);
					} else if (joinUser.size() == 1) { // 혼자 먼저 입장했을 떄
						readyBtn.setEnabled(false);
					}
				}
				// ready관련 버튼 클릭
				if (line.contains("ready")) {
					String[] temp = line.split(":");
					if (temp.length == 2) {
						if (!temp[0].equals(nickName)) { // 상대방의 메세지 -수정(equals)로 변경
							if ((temp[1].trim()).equals("ready")) {
								rivalReady = true;
								rivalReadyBtn.setIcon(new ImageIcon("C:\\cookierun\\png\\ready_1.png"));
								if (meReady) {
									gameStart();
								}
							} else {
								rivalReady = false;
								rivalReadyBtn.setIcon(new ImageIcon("C:\\cookierun\\png\\ready_2.png"));
							}
						} else { // 내가 보낸 ready메세지==ready버튼을 누를 경우와 동일한 결과값을 출력해야만 함
							if ((temp[1].trim()).equals("ready")) {
								sw++;
								readyBtn.setIcon(new ImageIcon("C:\\cookierun\\png\\ready_1.png"));
								meReady = true;
								if (rivalReady) {
									gameStart();
								}
							} else if ((temp[1].trim()).equals("unready")) {
								sw--;
								readyBtn.setIcon(new ImageIcon("C:\\cookierun\\png\\ready_2.png"));
								meReady = false;
							}

						}
					}
				}
				// 상대방이 퇴장했을 경우
				if (line.contains("퇴장")) {
					String[] name = line.split("님");
					if (!name[0].equals(nickName)) {
						for (String data : joinUser) {
							if (data.equals(name[0])) {
								joinUser.remove(data);
								break;
							}
						}
						enterClient = false;
						panel3_1.repaint();
						user2.setText("");
						sw = 0;
						readyBtn.setIcon(new ImageIcon("C:\\cookierun\\png\\ready_2.png"));
						readyBtn.setEnabled(false);
					}
				}

				if (line.contains("게임을 시작하시려면 READY를 눌러주세요!")) {
					readyBtn.setEnabled(true);
					enterClient = true;
					panel3_1.repaint();
				}
				if (line == null || line.equals("exit")) { // 내가 나갈 때
					reader.close(); // 읽기 끊기
					writer.close(); // 쓰기 끊기
					socket.close(); // 소켓 끊기
					clip.close();
					for (String data : joinUser) {
						if (data.equals(nickName)) {
							joinUser.remove(data);
							break;
						}
					}
					if (!gameStart) {
						new LobbyClient(loginDTO).service();
					}
					gameStart = false;
					break;
				}
				output.append(line + "\n");
				int pos = output.getText().length();
				output.setCaretPosition(pos);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		dispose();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == readyBtn) {
			if (sw == 0) {
				writer.println("ready");
				writer.flush();

			} else if (sw == 1) {
				writer.println("unready");
				writer.flush();
			}
		} else if (e.getSource() == outBtn) {
			if (loginDTO.getNickName().equals(roomDAO.selectAllRoom().get(roomNum - 1).getUser1())) {
				roomDAO.outUser1(roomDAO.selectAllRoom().get(roomNum - 1).getUser1(),
						roomDAO.selectAllRoom().get(roomNum - 1).getUser2(),
						roomDAO.selectAllRoom().get(roomNum - 1).getRoomNumber());
			} else {
				roomDAO.outUser2(roomNum);
			}
			if (roomDAO.selectAllRoom().get(roomNum - 1).getUserCount() == 0) {
				roomDAO.updateEmptyRoom(roomNum);
			}
			writer.println("exit");
			writer.flush();
		}
		String data = input.getText();
		if (data == null || data.equals(""))
			return;
		writer.println(data);
		writer.flush();
		input.setText("");
	}

	public void gameStart() {
		gameStart = true;
		joinUser.clear();
		enterClient = false;
		panel3_1.repaint();
		user2.setText("");
		sw = 0;
		meReady = false;
		rivalReady = false;
		readyBtn.setIcon(new ImageIcon("C:\\cookierun\\png\\ready_2.png"));
		clip.close();
		new Loading(loginDTO, 2);
		roomDAO.updateGamingState(2);
		writer.println("exit");
		writer.flush();
	}
	
}


//---------------------------로딩 프레임------------------
class Loading2 extends JFrame implements Runnable {
	
	private JProgressBar jpb = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
	private LoginDTO loginDTO;
	private int roomNum;

	public Loading2(LoginDTO loginDTO, int roomNum) {
		super("Loding....");
		System.out.println();
		this.loginDTO = loginDTO;
		this.roomNum = roomNum;
		JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				Image image = Toolkit.getDefaultToolkit().getImage("C:\\cookierun\\png\\load.gif");
				g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		add("Center", panel);
		jpb.setStringPainted(true);
		jpb.setString("0%");
		add("South", jpb);

		setBounds(400, 200, 300, 350);
		setResizable(false);
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

			}
		});
		Thread th = new Thread(this);
		th.start();
	}

	public void run() {
		for (int i = 0; i <= 100; i++) {
			try {
				Thread.sleep(60);
			} catch (InterruptedException e) {
			}
			jpb.setValue(i);
			jpb.setString(i + "%");
		}
		dispose();
		//new Game2Client(loginDTO).service();
		new Game2Client(loginDTO, 2).service();
	}
}

class Panel3_12 extends JPanel {
	public Panel3_12() {
		setBounds(20, 85, 170, 170);
		setLayout(null);
		setOpaque(false);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (Room2Client.enterClient) {
			Image image = Toolkit.getDefaultToolkit().getImage("C:\\cookierun\\png\\cookie_girl.gif");
			g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		}
		setOpaque(false);
	}
	
}

