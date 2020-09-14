package cookierunrun;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javax.swing.ImageIcon;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class Login extends JFrame implements ActionListener {
	private JButton loginBtn, registerBtn, idSearchBtn, pwSearchBtn;
	private JLabel idL, pwL;
	private JTextField idT;
	private ImageIcon icon;
	private JPasswordField pwF;
	private LoginDTO dto = new LoginDTO();
	private String getPw;
	private Clip clip;

	public Login() {
		super("LogIn");

		icon = new ImageIcon("C:\\cookierun\\png\\Login.png");
		// 배경화면을 입혀주는 패널 생성
		JPanel backP = new JPanel(new GridLayout(2, 1, 10, 10)) {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(icon.getImage(), 0, 0, d.width, d.height, null);
				setOpaque(true); // 투명화 시키는것
			}
		};

		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		Font fL = new Font("맑은 고딕", Font.BOLD, 18);

		// 아이디와 비밀번호 입력하는 라벨과 텍스트필드 생성
		JPanel idP = new JPanel();
		JPanel idTemp = new JPanel();
		idL = new JLabel("ID");
		idT = new JTextField(10);
		idL.setFont(fL);
		idTemp.add(idL);
		idTemp.add(idT);
		idTemp.setOpaque(false);

		// 비밀번호 필드 설정
		JPanel ppw = new JPanel();
		pwL = new JLabel("PW");
		pwL.setFont(fL);
		pwF = new JPasswordField(10);
		pwF.setEchoChar('*');
		ppw.add(pwL);
		ppw.add(pwF);
		ppw.setOpaque(false);

		idP.add(idTemp);
		idP.add(ppw);
		idP.setOpaque(false);

		// 버튼
		JPanel btnP = new JPanel(new GridLayout(2, 2, 10, 10));
		loginBtn = new JButton("로그인");
		registerBtn = new JButton("회원가입");
		idSearchBtn = new JButton("ID 찾기");
		pwSearchBtn = new JButton("PW 찾기");
		btnP.add(loginBtn);
		btnP.add(registerBtn);
		btnP.add(idSearchBtn);
		btnP.add(pwSearchBtn);
		btnP.setOpaque(false);
		p2.add("Center", idP);
		p2.add("South", btnP);
		p1.setOpaque(false);
		p2.setOpaque(false);

		Container con = getContentPane();
		// add
		backP.setOpaque(false);

		con.add("Center", backP);
		backP.add("Center", p1);
		backP.add("South", p2);

		setResizable(false);
		setBounds(700, 200, 400, 300);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// 배경음악
		File file = new File("C:\\cookierun\\music\\loginMusic.wav");

		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(file);
			try {
				clip = AudioSystem.getClip();
				clip.open(stream);
				clip.start();
				clip.loop(-1);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 이벤트
		loginBtn.addActionListener(this);
		registerBtn.addActionListener(this);
		idSearchBtn.addActionListener(this);
		pwSearchBtn.addActionListener(this);

		pwF.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					keyEnter();
				}
			}
		});
	}// Login생성자

	@Override
	public void actionPerformed(ActionEvent e) {
		getPw = new String(pwF.getPassword());
		if (e.getSource() == loginBtn) {
			int sw = 0;
			RegisterDAO dao = new RegisterDAO();
			ArrayList<LoginDTO> ar = dao.selectAll();
			if (idT.getText().equals("") || getPw.equals("")) {
				JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요!!");
			} else {
				for (int i = 0; i < ar.size(); i++) {
					if (ar.get(i).getId().equals(idT.getText()) && ar.get(i).getPw().equals(getPw)) {
						dao.insertJoin(ar.get(i));
						new LobbyClient(ar.get(i)).service();
						sw = 1;
						dispose();
						clip.close();
						break;
					}
				}
				if (sw == 0) {
					JOptionPane.showMessageDialog(this, "일치하는 정보가 없습니다.");
					idT.setText("");
					pwF.setText("");
				}
			}
		} else if (e.getSource() == registerBtn) {
			// 회원가입
			new Register();
		} else if (e.getSource() == idSearchBtn) {
			// 아이디 찾기
			new IdSearch();
		} else if (e.getSource() == pwSearchBtn) {
			// 비밀번호 찾기
			new PwSearch();
		}
	}

	// key 액션
	public void keyEnter() {
		getPw = new String(pwF.getPassword());
		int sw = 0;
		RegisterDAO dao = new RegisterDAO();
		ArrayList<LoginDTO> ar = dao.selectAll();
		if (idT.getText().equals("") || getPw.equals("")) {
			JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요!!");
		} else {
			for (int i = 0; i < ar.size(); i++) {
				if (ar.get(i).getId().equals(idT.getText()) && ar.get(i).getPw().equals(getPw)) {
					dao.insertJoin(ar.get(i));
					new LobbyClient(ar.get(i)).service();
					sw = 1;
					dispose();
					clip.close();
					break;
				}
			}
			if (sw == 0) {
				JOptionPane.showMessageDialog(this, "일치하는 정보가 없습니다.");
				idT.setText("");
				pwF.setText("");
			}
		}
	}
	public static void main(String[] args) {
		new Login();
	}
}

//---------------------------회원가입 창 클래스--------------------------------------
class Register extends JFrame implements ActionListener {

	private JLabel idL, nickNameL, pwL, pwconfirmL, nameL, phoneNL, emailL, titleL, idconfirmL, nickconfirmL,
			emailconfirmL, birthL, emailNumCheckL;
	private JLabel lengthId, lengthNick, lengthPw;
	private JTextField idT, nickNameT, nameT, phoneNT, emailT, emailconfirmT, birthT;
	private JButton registerBtn, cancelBtn, idconfirmBtn, nickconfirmBtn, pwconfirmBtn, sendEmailBtn, emailconfirmBtn;
	private ImageIcon icon;
	private JPasswordField pwF, pwconfirmF;
	private JPasswordField pwT, pwconfirmT;

	private JList<LoginDTO> list;
	private DefaultListModel<LoginDTO> model;

	private Email email;

	public JTextField getEmailT() {
		return emailT;
	}

	public Register() {
		super("Cookirun SignUp");
		list = new JList<LoginDTO>();
		icon = new ImageIcon("C:\\cookierun\\png\\RegisterBackgroundImg.jpg");
		JPanel background = new JPanel() {
			public void paintComponent(Graphics g) {
				Dimension d = getSize();
				g.drawImage(icon.getImage(), 0, 0, d.width, d.height, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		background.setLayout(null);

		JPanel northP = new JPanel();
		titleL = new JLabel("회원가입");
		titleL.setOpaque(false);
		northP.add(titleL);
		northP.setBounds(165, 15, 50, 50);
		northP.setOpaque(false);

/////////////////////////////////////////////////
		// 라벨 마들기
		idL = new JLabel("아이디 : ");
		lengthId = new JLabel("(6~15자)");
		nickNameL = new JLabel("닉네임 : ");
		lengthNick = new JLabel("(3~15자)");
		pwL = new JLabel("비밀번호 : ");
		lengthPw = new JLabel("(6~15자)");
		pwconfirmL = new JLabel("비밀번호 확인 : ");
		nameL = new JLabel("이름 : ");
		birthL = new JLabel("생년월일: ");
		phoneNL = new JLabel("전화번호: ");
		emailL = new JLabel("이메일 : ");
		emailconfirmL = new JLabel("인증번호: ");
		// 서쪽 라벨 설정하기
		JPanel westP = new JPanel();
		westP.setBounds(20, 20, 105, 400);
		westP.setLayout(null);
		westP.setOpaque(false);
		westP.add(idL);
		westP.add(lengthId);
		westP.add(nickNameL);
		westP.add(lengthNick);
		westP.add(pwL);
		westP.add(lengthPw);
		westP.add(pwconfirmL);
		westP.add(nameL);
		westP.add(birthL);
		westP.add(phoneNL);
		westP.add(emailL);
		westP.add(emailconfirmL);

		westP.setOpaque(false);

		idL.setBounds(10, 30, 65, 20);
		lengthId.setBounds(10, 45, 65, 20);
		nickNameL.setBounds(10, 65, 65, 20);
		lengthNick.setBounds(10, 80, 65, 20);
		pwL.setBounds(10, 100, 80, 20);
		lengthPw.setBounds(10, 115, 65, 20);
		pwconfirmL.setBounds(10, 135, 150, 20);
		nameL.setBounds(10, 175, 65, 20);
		birthL.setBounds(10, 205, 65, 20);
		phoneNL.setBounds(10, 235, 65, 20);
		emailL.setBounds(10, 265, 65, 20);
		emailconfirmL.setBounds(10, 295, 65, 20);

//////////////////////////////////////////////////////
		// 텍스트 필드와 라벨 새롭게 생성하기
		idT = new JTextField();
		pwT = new JPasswordField("");
		pwconfirmT = new JPasswordField("");
		nameT = new JTextField();
		nickNameT = new JTextField();
		birthT = new JTextField();
		phoneNT = new JTextField();
		emailT = new JTextField();
		emailconfirmT = new JTextField();
		sendEmailBtn = new JButton("인증");
		emailconfirmBtn = new JButton("확인");
		idconfirmBtn = new JButton("확인");
		nickconfirmBtn = new JButton("확인");
		pwconfirmBtn = new JButton("확인");
		idconfirmL = new JLabel("");
		nickconfirmL = new JLabel("");
		pwconfirmL = new JLabel("");
		emailNumCheckL = new JLabel("");

		// 텍스트 필드 중앙에 넣기(패널)
		JPanel centerP = new JPanel();
		centerP.setBounds(15, 30, 400, 350);
		centerP.setLayout(null);
		centerP.add(idT);
		centerP.add(pwT);
		centerP.add(pwconfirmT);
		centerP.add(nameT);
		centerP.add(nickNameT);
		centerP.add(phoneNT);
		centerP.add(emailT);
		centerP.add(emailconfirmT);
		centerP.add(sendEmailBtn);
		centerP.add(emailconfirmBtn);
		centerP.add(idconfirmBtn);
		centerP.add(idconfirmL);
		centerP.add(nickconfirmBtn);
		centerP.add(nickconfirmL);
		centerP.add(pwconfirmBtn);
		centerP.add(pwconfirmL);
		centerP.add(birthT);
		centerP.setOpaque(false);

		idT.setBounds(110, 20, 100, 20);
		nickNameT.setBounds(110, 55, 100, 20);
		pwT.setBounds(110, 90, 100, 20);
		pwconfirmT.setBounds(110, 125, 100, 20);
		nameT.setBounds(110, 165, 100, 20);
		birthT.setBounds(110, 195, 100, 20);
		phoneNT.setBounds(110, 225, 150, 20);
		emailT.setBounds(110, 255, 150, 20);
		emailconfirmT.setBounds(110, 285, 150, 20);
		idconfirmBtn.setBounds(225, 20, 60, 20);
		nickconfirmBtn.setBounds(225, 55, 60, 20);
		pwconfirmBtn.setBounds(225, 125, 60, 20);
		sendEmailBtn.setBounds(275, 255, 60, 20);
		emailconfirmBtn.setBounds(275, 285, 60, 20);
		idconfirmL.setBounds(110, 35, 170, 20);
		nickconfirmL.setBounds(110, 70, 170, 20);
		pwconfirmL.setBounds(110, 140, 200, 20);
		emailNumCheckL.setBounds(125, 330, 170, 20);

////////////////////////////////////////////////////////
		// 남쪽 패널 설정(버튼 넣기)
		JPanel southP = new JPanel();

		southP.setBounds(100, 350, 220, 30);
		southP.setLayout(null);

		registerBtn = new JButton("회원가입");
		registerBtn.setPreferredSize(new Dimension(100, 28));

		cancelBtn = new JButton(" 취    소 ");
		cancelBtn.setPreferredSize(new Dimension(100, 28));
		southP.add(registerBtn);
		southP.add(cancelBtn);
		southP.setOpaque(false);

		registerBtn.setBounds(0, 0, 100, 28);
		cancelBtn.setBounds(110, 0, 100, 28);

		/////////////////////
		background.add(northP);
		background.add(westP);
		background.add(centerP);
		background.add(southP);
		background.add(emailNumCheckL);
		background.setOpaque(false);

		Container c = this.getContentPane();

		c.add(background);

		setBounds(500, 300, 400, 450);
		setVisible(true);
		setResizable(false);

		registerBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		idconfirmBtn.addActionListener(this);
		nickconfirmBtn.addActionListener(this);
		pwconfirmBtn.addActionListener(this);
		sendEmailBtn.addActionListener(this);
		emailconfirmBtn.addActionListener(this);
	}

	// 데이터 입력 메서드
	public LoginDTO getData() {
		String id = idT.getText();
		String nickName = nickNameT.getText();
		String Pw = new String(pwT.getPassword());
		String Pwc = new String(pwconfirmT.getPassword());
		String name = nameT.getText();
		String birth = birthT.getText();
		String phoneN = phoneNT.getText();
		String email = emailT.getText();

		LoginDTO dto = new LoginDTO();
		dto.setId(id);
		dto.setNickName(nickName);
		dto.setPw(Pw);
		dto.setPwConfirm(Pwc);
		dto.setName(name);
		dto.setBirth(birth);
		dto.setPhoneN(phoneN);
		dto.setEmail(email);

		return dto;
	}

	public void getLoginList() {
		RegisterDAO dao = new RegisterDAO();
		ArrayList<LoginDTO> list = (ArrayList<LoginDTO>) dao.selectAll();
		for (LoginDTO dto : list) {
			model.addElement(dto);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		RegisterDAO dao = new RegisterDAO();
		ArrayList<LoginDTO> ar = dao.selectAll();
		String strPw = new String(pwT.getPassword());
		String strPwc = new String(pwconfirmT.getPassword());
		if (e.getSource() == idconfirmBtn) {
			for (int i = 0; i < ar.size(); i++) {
				System.out.println(ar.get(i).getId());
				if (idT.getText().equals(ar.get(i).getId())) {
					idconfirmL.setText("중복된 아이디입니다.");
					idconfirmL.setForeground(Color.RED);
					break;
				} else if (idT.getText().equals("") || idT.getText().length() < 6) {
					idconfirmL.setText("사용불가능한 아이디입니다.");
					idconfirmL.setForeground(Color.RED);
					break;
				} else {
					idconfirmL.setText("사용가능한 아이디입니다.");
					idconfirmL.setForeground(Color.BLACK);
				}
			}
			for (int i = 0; i < idT.getText().length(); i++) {
				if (idT.getText().charAt(i) == ' ') {
					idconfirmL.setText("공백을 없애주세요.");
					idconfirmL.setForeground(Color.RED);
				}
			}
		}
		// NickName 중복확인
		else if (e.getSource() == nickconfirmBtn) {
			for (int i = 0; i < ar.size(); i++) {
				if (nickNameT.getText().equals("")) {
					nickconfirmL.setText("닉네임을 입력하세요.");
					nickconfirmL.setForeground(Color.RED);
					break;
				} else if (nickNameT.getText().equals(ar.get(i).getNickName())) {
					nickconfirmL.setText("중복된 닉네임입니다.");
					nickconfirmL.setForeground(Color.RED);
					break;
				} else if (nickNameT.getText().equals("") || nickNameT.getText().length() < 3) {
					nickconfirmL.setText("사용불가한 닉네임입니다.");
					nickconfirmL.setForeground(Color.RED);
					break;
				} else {
					nickconfirmL.setText("사용가능한 닉네임입니다.");
					nickconfirmL.setForeground(Color.BLACK);
				}
			}
			for (int i = 0; i < nickNameT.getText().length(); i++) {
				if (nickNameT.getText().charAt(i) == ' ') {
					nickconfirmL.setText("공백을 없애주세요.");
					nickconfirmL.setForeground(Color.RED);
				}
			}
			// pw 중복확인
		} else if (e.getSource() == pwconfirmBtn) {
			if (strPw.equals("") || strPwc.equals("")) {
				pwconfirmL.setText("비밀번호를 입력해주세요.");
				pwconfirmL.setForeground(Color.RED);
			} else if (strPw.equals(strPwc) && strPw.length() < 6) {
				pwconfirmL.setText("사용불가능한 비밀번호입니다.");
				pwconfirmL.setForeground(Color.RED);
			} else if (strPw.equals(strPwc) && strPw.length() > 5) {
				pwconfirmL.setText("사용가능한 비밀번호입니다.");
				pwconfirmL.setForeground(Color.BLACK);
			} else if (strPw != strPwc) {
				pwconfirmL.setText("비밀번호가 일치하지 않습니다.");
				pwconfirmL.setForeground(Color.RED);
			}
			// 패스워드에 공백
			for (int i = 0; i < strPw.length(); i++) {
				if (strPw.charAt(i) == ' ') {
					pwconfirmL.setText("공백을 없애주세요.");
					pwconfirmL.setForeground(Color.RED);
				}
			}
			// 패스워드확인 공백
			for (int i = 0; i < strPwc.length(); i++) {
				if (strPwc.charAt(i) == ' ') {
					pwconfirmL.setText("공백을 없애주세요.");
					pwconfirmL.setForeground(Color.RED);
				}
			}
		} else if (e.getSource() == sendEmailBtn) {
			email = new Email(this);
		}
		if (e.getSource() == emailconfirmBtn) {
			if (email.getCode().equals(emailconfirmT.getText())) {
				emailNumCheckL.setText("인증되었습니다.");
			} else if (!email.getCode().equals(emailconfirmT.getText())) {
				emailNumCheckL.setText("인증번호가 잘못 되었습니다.");
				emailNumCheckL.setForeground(Color.RED);
			}
		} else if (e.getSource() == registerBtn) {
			if (!idT.getText().equals("") && idT.getText().length() > 5 && !nickNameT.getText().equals("")
					&& nickNameT.getText().length() > 2 && !strPw.equals("") && strPw.length() > 5 && !strPwc.equals("")
					&& strPwc.length() > 5 && !phoneNT.getText().equals("") && phoneNT.getText().length() > 5
					&& !emailT.getText().equals("") && idconfirmL.getText().equals("사용가능한 아이디입니다.")
					&& nickconfirmL.getText().equals("사용가능한 닉네임입니다.") && pwconfirmL.getText().equals("사용가능한 비밀번호입니다.")
					&& email.getCode().equals(emailconfirmT.getText())) {
				LoginDTO dto = getData();
				dao.memberInsert(dto);
				ar.add(dto);
				JOptionPane.showMessageDialog(this, "회원가입에 성공하셨습니다.");
				setVisible(false);
			} else {
				JOptionPane.showMessageDialog(this, "회원정보를 다시 확인하세요.");
			}
		} else if (e.getSource() == cancelBtn) {
			dispose();
		}
	}

	public static void main(String[] args) {
		new Register();
	}
}

class RegisterDAO {
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@localhost:1521:xe";
	private String username = "c##java";
	private String password = "bit";
	private Connection conn;
	private PreparedStatement pstmt;
	private ResultSet rs;

	public RegisterDAO() {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void getConnection() {
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<LoginDTO> selectAll() {
		ArrayList<LoginDTO> ar = new ArrayList<LoginDTO>();
		getConnection();
		String sql = "select * from gameuser";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				LoginDTO loginDTO = new LoginDTO();
				loginDTO.setId(rs.getString("id"));
				loginDTO.setNickName(rs.getString("nickname"));
				loginDTO.setPw(rs.getString("pw"));
				loginDTO.setPwConfirm(rs.getString("pwconfirm"));
				loginDTO.setName(rs.getString("name"));
				loginDTO.setBirth(rs.getString("birth"));
				loginDTO.setPhoneN(rs.getString("phoneN"));
				loginDTO.setEmail(rs.getString("email"));
				loginDTO.setScore(rs.getInt("score"));
				loginDTO.setCoin(rs.getInt("coin"));
				ar.add(loginDTO);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ar = null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ar;
	}

	public void memberInsert(LoginDTO loginDTO) {
		getConnection();
		String sql = "insert into gameuser values(?,?,?,?,?,?,?,?,?,?)";
		try {
			// 테이블에 데이터 넣기
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginDTO.getId());
			pstmt.setString(2, loginDTO.getNickName());
			pstmt.setString(3, loginDTO.getPw());
			pstmt.setString(4, loginDTO.getPwConfirm());
			pstmt.setString(5, loginDTO.getName());
			pstmt.setString(6, loginDTO.getBirth());
			pstmt.setString(7, loginDTO.getPhoneN());
			pstmt.setString(8, loginDTO.getEmail());
			pstmt.setInt(9, 0);
			pstmt.setInt(10, 0);

			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void updatePw(LoginDTO loginDTO) {
		getConnection();
		String sql = "update gameuser set pw=?,pwConfirm=? where id=?";
		try {
			pstmt = conn.prepareStatement(sql);
			// 데이터를 ?에 주입
			pstmt.setString(1, loginDTO.getPw());
			pstmt.setString(2, loginDTO.getPwConfirm());
			pstmt.setString(3, loginDTO.getId());

			pstmt.executeUpdate();// 실행

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();// 나중에 만든 것 먼저 끊기
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void insertJoin(LoginDTO loginDTO) {
		getConnection();
		String sql = "insert into joinTable values(?,?,?,?,?,?,?,?,?,?)";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginDTO.getId());
			pstmt.setString(2, loginDTO.getNickName());
			pstmt.setString(3, loginDTO.getPw());
			pstmt.setString(4, loginDTO.getPwConfirm());
			pstmt.setString(5, loginDTO.getName());
			pstmt.setString(6, loginDTO.getBirth());
			pstmt.setString(7, loginDTO.getPhoneN());
			pstmt.setString(8, loginDTO.getEmail());
			pstmt.setInt(9, 0);
			pstmt.setInt(10, 0);

			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public List<LoginDTO> getJoinList() {
		List<LoginDTO> list = new ArrayList<LoginDTO>();
		getConnection();
		String sql = "select * from joinTable";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				LoginDTO loginDTO = new LoginDTO();
				loginDTO.setId(rs.getString("id"));
				loginDTO.setNickName(rs.getString("nickname"));
				loginDTO.setPw(rs.getString("pw"));
				loginDTO.setPwConfirm(rs.getString("pwconfirm"));
				loginDTO.setName(rs.getString("name"));
				loginDTO.setBirth(rs.getString("birth"));
				loginDTO.setPhoneN(rs.getString("phoneN"));
				loginDTO.setEmail(rs.getString("email"));
				loginDTO.setScore(rs.getInt("score"));
				loginDTO.setCoin(rs.getInt("coin"));
				list.add(loginDTO);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public List<LoginDTO> deleteJoinList(String id) {
		List<LoginDTO> list = new ArrayList<LoginDTO>();
		getConnection();
		String sql = "delete from joinTable where id =? ";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public ArrayList<LoginDTO> selectJoinTable() {
		ArrayList<LoginDTO> ar = new ArrayList<LoginDTO>();
		getConnection();
		String sql = "select * from gameuser";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				LoginDTO loginDTO = new LoginDTO();
				loginDTO.setId(rs.getString("id"));
				loginDTO.setNickName(rs.getString("nickname"));
				loginDTO.setPw(rs.getString("pw"));
				loginDTO.setPwConfirm(rs.getString("pwconfirm"));
				loginDTO.setName(rs.getString("name"));
				loginDTO.setBirth(rs.getString("birth"));
				loginDTO.setPhoneN(rs.getString("phoneN"));
				loginDTO.setEmail(rs.getString("email"));
				loginDTO.setScore(rs.getInt("score"));
				loginDTO.setCoin(rs.getInt("coin"));
				ar.add(loginDTO);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ar = null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ar;
	}

}

//-----------------비밀번호 찾기 관련 클래스-----------------------
class PwSearch extends JFrame implements ActionListener {
	private JButton checkBtn, cancelBtn;
	private JLabel idL, emailL;
	private JTextField idT, emailT;
	private LoginDTO dto = new LoginDTO();

	public PwSearch() {
		super("비밀번호 찾기");
		setLayout(null);
		idL = new JLabel("ID: ");
		emailL = new JLabel("Email: ");
		idT = new JTextField();
		emailT = new JTextField();

		idL.setBounds(60, 30, 80, 20);
		idT.setBounds(110, 30, 170, 20);

		emailL.setBounds(60, 60, 80, 20);
		emailT.setBounds(110, 60, 170, 20);

		checkBtn = new JButton("확인");
		checkBtn.setBounds(75, 110, 90, 30);
		cancelBtn = new JButton("취소");
		cancelBtn.setBounds(170, 110, 90, 30);

		Container con = getContentPane();

		con.add(idL);
		con.add(idT);
		con.add(emailL);
		con.add(emailT);
		con.add(checkBtn);
		con.add(cancelBtn);

		checkBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		emailT.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					keyEnter();
				}
			}
		});
		setResizable(false);
		setBounds(750, 250, 350, 175);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == checkBtn) {
			int sw = 0;
			RegisterDAO dao = new RegisterDAO();
			ArrayList<LoginDTO> ar = dao.selectAll();
			if (idT.getText().equals("") || emailT.getText().equals("")) {
				JOptionPane.showMessageDialog(this, "아이디 또는 이메일을 입력해 주세요!!");
			} else {
				for (int i = 0; i < ar.size(); i++) {
					if (ar.get(i).getId().equals(idT.getText()) && ar.get(i).getEmail().equals(emailT.getText())) {
						new PwChange(ar.get(i));
						setVisible(false);
						sw = 1;
						break;
					}
				}
				if (sw == 0) {
					JOptionPane.showMessageDialog(this, "일치하는 정보가 없습니다");
					idT.setText("");
					emailT.setText("");
				}
			}
		} else if (e.getSource() == cancelBtn) {
			setVisible(false);
		}
	}
	public void keyEnter() {
		int sw = 0;
		RegisterDAO dao = new RegisterDAO();
		ArrayList<LoginDTO> ar = dao.selectAll();
		for (int i = 0; i < ar.size(); i++) {
			if (ar.get(i).getId().equals(idT.getText()) && ar.get(i).getEmail().equals(emailT.getText())) {
				new PwChange(ar.get(i));
				setVisible(false);
				sw = 1;
				break;
			}
		}
		if (sw == 0) {
			JOptionPane.showMessageDialog(this, "일치하는 정보가 없습니다");
			idT.setText("");
			emailT.setText("");
		}
	}
}

class PwChange extends JFrame implements ActionListener {
	private JButton checkBtn, cancelBtn;
	private JLabel newPwL, checkPwL;
	private JPasswordField newPwT, checkPwT;
	private String getNewPw, getCheckPw;
	private LoginDTO loginDTO;

	public PwChange(LoginDTO loginDTO) {
		super("비밀번호 변경");
		this.loginDTO = loginDTO;
		setLayout(null);

		newPwL = new JLabel("새 비밀번호");
		newPwL.setBounds(60, 0, 100, 60);
		newPwT = new JPasswordField(10);
		newPwT.setBounds(190, 20, 80, 20);

		checkPwL = new JLabel("새 비밀번호 확인");
		checkPwL.setBounds(60, 30, 100, 60);
		checkPwT = new JPasswordField(10);
		checkPwT.setBounds(190, 50, 80, 20);

		checkBtn = new JButton("확인");
		checkBtn.setBounds(100, 80, 60, 30);
		cancelBtn = new JButton("취소");
		cancelBtn.setBounds(170, 80, 60, 30);

		Container con = getContentPane();

		con.add(newPwL);
		con.add(newPwT);
		con.add(checkPwL);
		con.add(checkPwT);
		con.add(checkBtn);
		con.add(cancelBtn);
		 
		setResizable(false);
		setBounds(750, 250, 350, 150);
		setVisible(true);

		// 이벤트
		checkBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		checkPwT.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					keyEnter();
				}
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getNewPw = new String(newPwT.getPassword());
		getCheckPw = new String(checkPwT.getPassword());
		if (e.getSource() == checkBtn) {
			if (getNewPw.equals("") || getCheckPw.equals("")) {
				JOptionPane.showMessageDialog(this, "항목을 모두 입력해주세요");
			} else if(!getNewPw.equals(getCheckPw)) {
				JOptionPane.showMessageDialog(this, "비밀번호가 일치하지않습니다.");
			} else {
				loginDTO.setPw(getNewPw);
				loginDTO.setPwConfirm(getCheckPw);
				new RegisterDAO().updatePw(loginDTO);
				JOptionPane.showMessageDialog(this, "비밀번호가 수정되었습니다");
				setVisible(false);
			}
		} else if (e.getSource() == cancelBtn) {
			setVisible(false);
		}
	}
	public void keyEnter() {
		getNewPw = new String(newPwT.getPassword());
		getCheckPw = new String(checkPwT.getPassword());
		if (getNewPw.equals("") || getCheckPw.equals("")) {
			JOptionPane.showMessageDialog(this, "항목을 모두 입력해주세요");
		} else if(!getNewPw.equals(getCheckPw)) {
			JOptionPane.showMessageDialog(this, "비밀번호가 일치하지않습니다.");
		} else {
			loginDTO.setPw(getNewPw);
			loginDTO.setPwConfirm(getCheckPw);
			new RegisterDAO().updatePw(loginDTO);
			JOptionPane.showMessageDialog(this, "비밀번호가 수정되었습니다");
			setVisible(false);
		}
	}
}

//-------------------아이디 찾기-----------------------
class IdSearch extends JFrame implements ActionListener {
	private JButton cancelBtn, confirmNBtn;
	private JLabel nameL, emailL;
	private JTextField nameT, emailT;

	public IdSearch() {
		super("아이디 찾기");
		setLayout(null);
		nameL = new JLabel("이름: ");
		emailL = new JLabel("Email: ");

		nameT = new JTextField();
		emailT = new JTextField();

		cancelBtn = new JButton("취소");
		confirmNBtn = new JButton("확인");

		nameL.setBounds(60, 30, 80, 20);
		emailL.setBounds(60, 60, 80, 20);
		nameT.setBounds(110, 30, 170, 20);
		emailT.setBounds(110, 60, 170, 20);

		cancelBtn.setBounds(170, 110, 90, 30);
		confirmNBtn.setBounds(75, 110, 90, 30);

		Container c = getContentPane();

		c.add(nameL);
		c.add(emailL);
		c.add(nameT);
		c.add(emailT);

		c.add(cancelBtn);
		c.add(confirmNBtn);

		confirmNBtn.addActionListener(this);
		cancelBtn.addActionListener(this);

		setResizable(false);
		setBounds(750, 250, 350, 175);
		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirmNBtn) {
			int sw = 0;
			RegisterDAO dao = new RegisterDAO();
			ArrayList<LoginDTO> ar = dao.selectAll();
			if (nameT.getText().equals("") || emailT.getText().equals("")) {
				JOptionPane.showMessageDialog(this, "이름 또는 이메일을 입력해 주세요!!");
			} else {
				for (int i = 0; i < ar.size(); i++) {
					if (ar.get(i).getName().equals(nameT.getText()) && ar.get(i).getEmail().equals(emailT.getText())) {
						dispose();
						new IdSearchResult(ar.get(i));
						sw = 1;
						break;
					}
				}
				if (sw == 0)
					JOptionPane.showMessageDialog(this, "일치하는 정보가 존재하지 않습니다!");
			}
		} else if (e.getSource() == cancelBtn) {
			dispose();
		}
	}
}

// 아이디찾기 확인
class IdSearchResult extends JFrame implements ActionListener {
	private JLabel idL;
	private JButton cancelBtn, pwSearchBtn;
	private LoginDTO loginDTO;

	public IdSearchResult(LoginDTO loginDTO) {
		this.loginDTO = loginDTO;
		setLayout(null);

		idL = new JLabel("");

		cancelBtn = new JButton("취소");
		pwSearchBtn = new JButton("PW찾기");
		Font font = new Font("맑은 고딕", Font.PLAIN, 15);

		idL.setText("아이디는 ' " + loginDTO.getId() + " '입니다.");
		idL.setFont(font);
		idL.setBounds(80, 40, 250, 30);

		pwSearchBtn.setBounds(75, 110, 90, 30);
		cancelBtn.setBounds(170, 110, 90, 30);

		Container c = getContentPane();

		c.add(idL);
		c.add(cancelBtn);
		c.add(pwSearchBtn);

		setResizable(false);
		setVisible(true);
		setBounds(750, 250, 350, 175);

		cancelBtn.addActionListener(this);
		pwSearchBtn.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelBtn) {
			setVisible(false);
		} else if (e.getSource() == pwSearchBtn) {
			new PwSearch();
			setVisible(false);
		}
	}
}

class LoginDTO {
	private String name, id, pw, pwConfirm, nickName, email, birth, phoneN;
	private int score, coin;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public String getPwConfirm() {
		return pwConfirm;
	}

	public void setPwConfirm(String pwConfirm) {
		this.pwConfirm = pwConfirm;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBirth() {
		return birth;
	}

	public void setBirth(String birth) {
		this.birth = birth;
	}

	public String getPhoneN() {
		return phoneN;
	}

	public void setPhoneN(String phoneN) {
		this.phoneN = phoneN;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public String toString() {// 주소를 이름으로 바꿔서 보여주는 것
		return nickName;
	}

}
