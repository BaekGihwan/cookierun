package cookierunrun;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

class Game5Client extends JFrame implements KeyListener, Runnable {

    // ------------------------필드-----------------------------
    
    public JButton startBtn;
    public Image round1;
    public Image startImg;
    public BackgroundT5 backgroundT;
    public ImageIcon backgroundImg;
    public RunningCookie5 runningCookie;
    public MovingHurdle5 movingHurdle;
    public static HpBar5 hpBar;
    public GomJellyDummy5 gomJellyDummy;
    public Jelly5 jelly;
    public JellyDrop5 jellyDrop;
    public BigGom5 bigGom;
    public Game5Client myFrame;
    public boolean startGame = false;
    public static boolean rivalGameDie = false; 
    public static boolean gameDie = false;
    public JLayeredPane lp;
    public static String competitionScore = "0";
    public static int rivalScore=0;
    public boolean temp1 = false; 
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String nickName;
    private LoginDTO loginDTO;
    private Game5Client gameClient;
    private Thread t;
    
    private int roomNum;

    // -------------------------메소드--------------------------------
    public Game5Client(LoginDTO loginDTO, int roomNum) {
        this.loginDTO = loginDTO;
        this.roomNum =roomNum;
        nickName=loginDTO.getNickName();
        
        jellyDrop = new JellyDrop5();
        // 곰젤리 클래스 호출
        gomJellyDummy = new GomJellyDummy5();
        gomJellyDummy.setLayout(null);
        gomJellyDummy.setBounds(0, 0, 700, 500);
        gomJellyDummy.setting();
        // 일반 젤리 클래스 호출
        jelly = new Jelly5();
        jelly.setLayout(null);
        jelly.setBounds(0, 0, 700, 500);
        jelly.setting();
        // 장애물 클래스 호출
        movingHurdle = new MovingHurdle5();
        movingHurdle.setting();
        movingHurdle.setBounds(0, 0, 700, 500);
        movingHurdle.setLayout(null);
        // 배경 클래스 호출
        backgroundT = new BackgroundT5();
        backgroundT.setLayout(null);
        // 쿠키가 달리는 클래스 호출
        runningCookie = new RunningCookie5();
        runningCookie.setLayout(null);
        runningCookie.setBounds(0, 0, 700, 500);
        // 체력 게이지 관리 클래스 호출
        hpBar = new HpBar5();
        hpBar.setLayout(null);
        hpBar.setBounds(0, 0, 700, 500);

        // 큰 곰젤리 호출
        bigGom = new BigGom5();
        bigGom.setLayout(null);
        bigGom.setBounds(0, 0, 700, 500);
        bigGom.setting();

        // 프레임 위에 올리기
        runningCookie.add(bigGom);
        hpBar.add(runningCookie);
        movingHurdle.add(hpBar);
        jelly.add(movingHurdle);
        gomJellyDummy.add(jelly);
        backgroundT.add(gomJellyDummy);
        add(backgroundT);
        // 닉네임 보내기

        setBounds(200, 300, 700, 500);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE );
        this.setResizable(true);
        this.addKeyListener(this);
        setResizable(false);
    }

    // 클라이언트 서비스
    public void service() {
        String serverIP = "192.168.0.159";
        //String serverIP = "192.168.147.4";
        if (serverIP == null || serverIP.length() == 0) {
            System.out.println("서버IP가 입력되지 않았습니다.");
            System.exit(0);
        }
        try {
            // 소켓생성
            socket = new Socket(serverIP, 8400); // 아이피는 위에있는거고 port 는 고정? 꼭 9500써야하나
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
        System.out.println("게임 서버와 연결 완료!!");
        // 서버로 닉네임 보내기
        writer.println(nickName); 
        writer.flush(); 
        gameStart();
        receiveMsg();  
    }

    @Override
    public void run() {
        allThreadStart();
        while(!t.isInterrupted()) {  
            //게임 끝났을 때
            if(!gameDie) {
                writer.println(Jelly5.gameScore);
                writer.flush();
            } else {
                if (gameDie && temp1 == false) {
                    writer.println("gameDie:"+Jelly5.gameScore);
                    writer.flush();
                    temp1 = true;
                    gameEnd();
                }
                if (gameDie && rivalGameDie) {
                    writer.println("exit");
                    writer.flush();
                }
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
    
    public void receiveMsg() {
        String line;
        while(true) {
            try {
                while (true) {
                    line = reader.readLine();
                    
                    if (line == null || line.equals("exit")) {
                        reader.close(); // 읽기 끊기
                        writer.close(); // 쓰기 끊기
                        socket.close(); // 소켓 끊기
                        t.interrupt();
                        break;
                    } 
                    if (line.contains("gameDie")) {
                        String[] temp = line.split(":");
                        if (!temp[0].equals(nickName)) {
                            rivalGameDie = true; 
                            rivalScore=Integer.parseInt(temp[2]);
                        }
                    } else {
                        String[] temp = line.split(":");
                        if (temp.length == 2) {
                            if (!temp[0].equals(nickName)) {
                                competitionScore = temp[1];
                            }
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    // 게임을 시작하는 메소드
    public void gameStart() {
        startGame = true;
        this.requestFocus();
        runningCookie.setFocusable(true);
        t = new Thread(this);
        t.start();
    }

    // KeyListener 오버라이드
    @Override
    public void keyPressed(KeyEvent e) {
        // 눌렀을 떄
        if (e.getKeyCode() == e.VK_DOWN) {
            if (!runningCookie.jump) { // 점프 중이 아닐 떄
                runningCookie.setSlide(true);
            }
        } else if (e.getKeyCode() == e.VK_UP) {
            if (!runningCookie.slide) { // 슬라이드 중이 아닐 떄
                runningCookie.setJump(true);
                play("src/jump2.wav");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        runningCookie.setSlide(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void play(String fileName) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
            Clip clip = AudioSystem.getClip();
            clip.stop();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
        }
    }

    public void allThreadStart() {
        backgroundT.threadStart();
        hpBar.threadStart();
        runningCookie.threadStart();
        movingHurdle.threadStart();
        gomJellyDummy.threadStart();
        jelly.threadStart();
        jellyDrop.ThreadStart();
        bigGom.threadStart();
    }

    // 게임이 종료되면 나오는 frame창
    public void gameEnd() {
        new EndLoading5(loginDTO, this, roomNum);
    }
}

class EndLoading5 extends JFrame implements Runnable {
    private String nickName;
    private Game5Client game;
    private EndGame5 endGame;
    private LoginDTO loginDTO;
    private int roomNum;

    public EndLoading5(LoginDTO loginDTO, Game5Client game, int roomNum) {
        super("로딩 화면");
        //this.nickName = nickName;
        this.game = game;
        this.loginDTO = loginDTO;
        this.roomNum = roomNum;

        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                Image image = Toolkit.getDefaultToolkit().getImage("C:\\cookierun\\png\\EndLoading.gif");
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                setOpaque(false);
                super.paintComponent(g);
            }
        };
        add("Center", panel);
        JPanel p = new JPanel();
        JLabel label = new JLabel("상대방의 경기종료를 기다리는 중입니다...");
        p.add(label);
        add("South", p);

        setBounds(400, 200, 300, 350);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE );

        Thread th = new Thread(this);
        th.start();
    }

    public void run() {
        while (true) {
            System.out.println(Game5Client.rivalGameDie);  //지우시면 게임 실행이 안됩니다ㅜㅜㅜ 지우지 마세요!!!
            if (Game5Client.rivalGameDie) {
                game.dispose();
                break;
            }
        }
        gameEnd();
    }

    public void gameEnd() {
        endGame = new EndGame5(loginDTO, roomNum);
        endGame.setLayout(null);
        endGame.setBounds(200, 300, 700, 500);
        endGame.setVisible(true);
        dispose();
    }
}
class BackgroundT5 extends JPanel implements Runnable {
    // ------------------------필드----------------------
    public int x1 = 0;
    public int x2 = 700;
    // 1라운드 배경 이미지
    public Image backgroundImg_Round1;
    public Image backgroundImg2_Round1;

    // 2라운드 배경 이미지
    public Image background1;
    // 배경 음악 재생
    public AudioInputStream ais;
    public Clip clip;
    // 라운드 설정
    public static boolean round2 = false;
    // 스레드 시작 시간 측정
    public Long startTime; // 스레드 시작 시간
    public Long endTime; // 스레드 현재 시간
    public int gamingTime; // 스레드 실행 시간
    public Thread t;

    // ---------------------------메소드---------------------
    public BackgroundT5() {
        /*
         * this.setLayout(null); label = new JLabel("0"); label.setFont(new
         * Font("나눔고딕",Font.BOLD,25)); label.setForeground(Color.WHITE);
         * label.setBounds(610,18,100,50); add(label);
         */
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(700, 460);
        Graphics2D g2d = (Graphics2D) g;
        Toolkit t = Toolkit.getDefaultToolkit();

        // 1라운드 배경이미지 설정
        backgroundImg_Round1 = t.getImage("C:\\cookierun\\png\\firstRound_1.JPG");
        backgroundImg2_Round1 = t.getImage("C:\\cookierun\\png\\firstRound_1.JPG");
        // 2라운드 배경 이미지
        background1 = t.getImage("C:\\cookierun\\png\\blackghostback.png");
        // gomCountBackground = t.getImage("src/Jelly/Gom_jelly4.png");
        if (gamingTime == 20 && round2 == false)
            Round2Music();
        // 게임이 시작된 지 60초가 지났을 때 배경 체인지
        if (gamingTime == 20 && round2 == false)
            round2 = true;
        g.drawImage(round2 ? background1 : backgroundImg_Round1, x1, 0, this.getWidth() + 10, this.getHeight(), this);
        g.drawImage(round2 ? background1 : backgroundImg_Round1, x2, 0, this.getWidth() + 10, this.getHeight(), this);

    }

    public void threadStart() {
        t = new Thread(this);
        startTime = System.currentTimeMillis();
        t.start();
    }

    @Override
    public void run() {
        play("C:\\cookierun\\music\\gameMusic.wav");
        while (!Game5Client.gameDie) {
            // 시간을 측정해서 25초 이후엔 자동으로 2라운드 진입
            endTime = System.currentTimeMillis();
            gamingTime = (int) (endTime - startTime) / 1000;
            // 배경이미지 좌표 이동
            x1--;
            x2--;
            if (x1 <= -700) {
                x1 = 700;
            }
            if (x2 <= -700) {
                x2 = 700;
            }
            repaint();
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
            }
            if (Game5Client.gameDie)
                break;
        }
        clip.stop();
    }

    public void play(String fileName) {
        try {
            ais = AudioSystem.getAudioInputStream(new File(fileName));
            clip = AudioSystem.getClip();
            clip.stop();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Round2Music() {
        clip.stop();
        play("C:\\cookierun\\music\\gameMusic2.wav");
    }

}

class RunningCookie5 extends JPanel implements Runnable{
    //-------------------필드----------------------
    public static final int COOKIE_X=80;  //쿠키의 x좌표는 항상 고정
    
    public Image cookieRun1, cookieRun2,cookieRun3,cookieRun4,cookieRun5,cookieRun6,cookieRun7,cookieRun8,cookieRun9,cookieRun10; //올릴 쿠키의 이미지 모두 생성
    public int choice=0; //choice에 따라서 사진이 바뀌게 된다. 
    public boolean slide=false; //슬라이드 하는 동안 true값을 가진다. 
    public boolean jump=false; //점프를 하는 동안 true값을 가진다.
    public static boolean coll=false;  //장애물과 충돌하는 동안 true가 된다. MovingHuddle 클래스에서 값을 변화시켜줘야 하기 때문에 static으로 생성한다. 
    public int jumpY=250; //쿠키의 y값의 좌표를 고정해놓는다.
    
    public boolean jumpUp=true;  //true 값일 댄 jump진행, false일 떈 하강
    //클래스 호출해주는 필드
    public MovingHurdle5 movingHurdle;  
    public HurdleDTO hurdleDTO;  
        
    //변화하는 y값의 좌표를 다른 클래스에서 참조할 수 있도록 static으로 설정
    public static int cookieY;
    public ArrayList <HurdleDTO> list;
    
    //---------------------메소드----------------------
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(700,460);
        setOpaque(false);
        Graphics2D g2d = (Graphics2D) g;
        Toolkit t = Toolkit.getDefaultToolkit();
        
        // 이미지 불러오기
        cookieRun1 = t.getImage("C:\\cookierun\\png\\general_1.gif");
        cookieRun2 = t.getImage("C:\\cookierun\\png\\general_2.gif");
        cookieRun3 = t.getImage("C:\\cookierun\\png\\general_3.gif");
        cookieRun4 = t.getImage("C:\\cookierun\\png\\general_4.gif");
        cookieRun5 = t.getImage("C:\\cookierun\\png\\slide0.png");
        cookieRun6 = t.getImage("C:\\cookierun\\png\\slide1.png");
        cookieRun7 = t.getImage("C:\\cookierun\\png\\jump0.png");
        
        //장애물레 부딪혔을 떄 나오는 이미지
        cookieRun8 = t.getImage("C:\\cookierun\\png\\Char_Hit.png");        
        // 사진 전환하기
        if(choice==0) g.drawImage(cookieRun1,80,250, this);
        else if(choice==1) { g.drawImage(cookieRun2,80,250, this); cookieY=250;}
        else if(choice==2) {g.drawImage(cookieRun3,80,250, this);cookieY=250;}  
        else if(choice==3) { g.drawImage(cookieRun4,80,250, this);cookieY=250;}
        else if(choice==4) {g.drawImage(cookieRun5, 80,310,120,70,this);cookieY=310;}
        else if(choice==5) {g.drawImage(cookieRun6, 80,310,120,70,this); cookieY=310;}
        else if(choice==6) {g.drawImage(cookieRun7, 80,jumpY,120,110, this);cookieY=jumpY ;} 
        else if(choice==7) {g.drawImage(cookieRun8, 80,cookieY,this);}
    }  
    
    //스레드 실행 메소드
    public void threadStart() {
        Thread t = new Thread(this);
        t.start();
    }
    
    //runnabble 오버라이드
    @Override
    public void run() {
        
        while(!Game5Client.gameDie) {
                if(coll==true) {  //장애물과 충돌했을 떄
                    choice=7;
                }else if(slide==false&&jump==false&coll==false){  //그냥 달릴 때
                choice++;
                if (choice>=4) {  //다시 처음으로 돌아가게 설정
                    choice=0;
                }
                try{
                    Thread.sleep(35);
                } catch(InterruptedException e){
                    return;
                }   
            }else if(slide==true&&jump==false&&coll==false) {  //슬라이드를 할 때
                if(choice==4) choice=5;
                else if(choice==5) choice=4;
                else choice=4;
                try{
                    Thread.sleep(35);
                } catch(InterruptedException e){
                    return;
                }   
            } else if(jump==true&&slide==false&&coll==false) { //점프가 클릭되었을 때
                //그림 값 변경
                if(choice<=5) {  //초기 점프사진으로 돌아가기
                    choice=6;
                }  
                //좌표값을 변경
                if(jumpUp) {   //쿠키가 점프할 떄 상승하는 경우
                    jumpY-=5;   
                    if(jumpY<150) jumpUp=false;
                } else if(!jumpUp) {   //쿠키가 하강할 때
                    jumpY+=5;
                    if(jumpY>250&&jumpUp==false) {  //지면에 쿠키가 닿을 때
                        jumpUp=true;
                        jump=false;
                    }
                }
                try{
                    Thread.sleep(20);
                } catch(InterruptedException e){
                }   
            }
            repaint();
            if(Game5Client.gameDie==true) break;
        }
    }
    public void setSlide(boolean slide) {
        this.slide=slide;
    }
    public void setJump(boolean jump) {
        this.jump=jump;
    }
    public void setHurddle() {
        
    }
    public int getCookieY() {
        return cookieY;
    }
    public void setColl(boolean coll) {
        this.coll=coll;
    }
}
class MovingHurdle5 extends JPanel implements Runnable {
    // 1라운드 장애물 이미지 필드
    public Image round1_hurdle1;
    public Image round1_hurdle2;
    public Image round1_hurdle3;
    public Image round1_hurdle4;
    public Image round1_hurdle5;
    public Image round1_hurdle6;;
    public Image[] round1; // 1라운드에서 쓰일 이미지를 넣어놓은 장애물 배열
    // 2라운드 장애물 이미지 필드
    public Image round2_hurdle1;
    public Image round2_hurdle2;
    public Image round2_hurdle3;
    public Image round2_hurdle4;
    public Image round2_hurdle5;
    public Image round2_hurdle6;;
    public Image[] round2; // 1라운드에서 쓰일 이미지를 넣어놓은 장애물 배열
    // 장애물 출몰 시간 설정 필드
    public Long startTime; // 스레드 시작 시간
    public Long endTime; // 스레드 현재 시간
    public int gamingTime; // 스레드 실행 시간
    public int time = 2; // 장애물이 처음 나오기 시작하는 초 단위
    // 체력 설정 필드
    public static int health = 100; // 체력(기본값 :100)
    // 장애물 충돌시간 계산 필드
    public ArrayList<Integer> hurdleTime;
    public boolean hurdleDisplay = false;
    public ArrayList<HurdleDTO> movingHurdle;
    public ArrayList<HurdleDTO> list;
    // 장애물과 부딛힐 때
    public Long collStart;
    public Long collEnd;
    public int collTime;
    // 클래스 선언 필드
    public HurdleDTO hurdleDTO;
    public RunningCookie5 runningCookie;
    // 생성자
    public MovingHurdle5() {
        runningCookie = new RunningCookie5();
    }

    public void setting() {
        hurdleTime = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            hurdleTime.add(time);
            time += (int) (Math.random() * 2) + 1;
            if (time > 200)
                break;
        }
        movingHurdle = new ArrayList<HurdleDTO>();
        list = new ArrayList<HurdleDTO>();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(700, 460);
        setOpaque(false);
        Graphics2D g2d = (Graphics2D) g;
        Toolkit t = Toolkit.getDefaultToolkit();
        // 1라운드 장애물 이미지 설정
        round1_hurdle1 = t.getImage("C:\\cookierun\\png\\ep1_jump1.png");
        round1_hurdle2 = t.getImage("C:\\cookierun\\png\\ep1_jump2.png");
        round1_hurdle3 = t.getImage("C:\\cookierun\\png\\ep1_jump3.png");
        round1_hurdle4 = t.getImage("C:\\cookierun\\png\\ep1_slide1.png");
        round1_hurdle5 = t.getImage("C:\\cookierun\\png\\ep1_slide2.png");
        round1 = new Image[] { round1_hurdle1, round1_hurdle2, round1_hurdle3, round1_hurdle4, round1_hurdle5 };
        // 2라운드 장애물 설정
        round2_hurdle1 = t.getImage("C:\\cookierun\\png\\ep3 jump1.png");
        round2_hurdle2 = t.getImage("C:\\cookierun\\png\\ep3 jump2.png");
        round2_hurdle3 = t.getImage("C:\\cookierun\\png\\ep3 jump3.png");
        round2_hurdle4 = t.getImage("C:\\cookierun\\png\\ep3 slide1.png");
        round2_hurdle5 = t.getImage("C:\\cookierun\\pngep3 slide1.png");
        round2 = new Image[] { round2_hurdle1, round2_hurdle2, round2_hurdle3, round2_hurdle4, round2_hurdle5 };
        // 장애물 이미지를 랜덤으로 설정함
        if (hurdleTime.get(0) == gamingTime) {
            int i = (int) (Math.random() * 5) + 1; // 1~5까지의 난수 발생
            // 초기 셋팅
            if (i >= 1 && i <= 3) {
                hurdleDTO = new HurdleDTO(i - 1);
                hurdleDTO.setX(750);
                hurdleDTO.setY(330);
                movingHurdle.add(hurdleDTO);
                list.add(hurdleDTO);
            } else if (i == 4 || i == 5) {
                hurdleDTO = new HurdleDTO(i - 1);
                hurdleDTO.setX(750);
                hurdleDTO.setY(0);
                movingHurdle.add(hurdleDTO);
                list.add(hurdleDTO);
            }
            hurdleTime.remove(0);
        }
        // 패널에 장애물 그려주기
        if (movingHurdle.size() > 0) {
            for (HurdleDTO data : movingHurdle) {
                if (data.getImageIndex() == 3 || data.getImageIndex() == 4) {
                    g.drawImage((BackgroundT5.round2 ? round2[data.getImageIndex()] : round1[data.getImageIndex()]),
                            data.getX(), data.getY(), 70, 300, this);
                } else {
                    g.drawImage(BackgroundT5.round2 ? round2[data.getImageIndex()] : round1[data.getImageIndex()],
                            data.getX(), data.getY() - 20, 50, 70, this);
                }
            }
        }
        // 장애물과 충돌했을 때
        int cookieX = runningCookie.COOKIE_X;
        int cookieY = runningCookie.getCookieY();

        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {

                if (list.get(i).getX() >= 60 && list.get(i).getX() <= 190) {

                    if (list.get(i).getImageIndex() == 0 || list.get(i).getImageIndex() == 2
                            || list.get(i).getImageIndex() == 1) {
                        // 하단 장애물과 충돌
                        if (list.get(i).getY() <= cookieY + 80 && list.get(i).getY() > cookieY) {
                            runningCookie.setColl(true);
                            collStart = System.currentTimeMillis();
                            list.remove(i);
                            health -= 10; // 충돌 시 체력 하락
                            break;
                        }
                    } else if (list.get(i).getImageIndex() == 3 || list.get(i).getImageIndex() == 4) {
                        // 상단 장애물과 충돌
                        if (list.get(i).getY() + 300 >= cookieY) {
                            runningCookie.setColl(true);
                            collStart = System.currentTimeMillis();
                            list.remove(i);
                            health -= 10; // 충돌 시 체력 하락
                            break;
                        }
                    }
                }
            }
        }
        // 충돌 1초 지나면 이미지 원상복귀
        if (runningCookie.coll == true) {
            collEnd = System.currentTimeMillis();
            collTime = (int) (collEnd - collStart) / 100;
            if (collTime == 2) {
                runningCookie.setColl(false);
                collTime = 0;
            }
        }
        // 프레임 밖을 벗어난 장애물 삭제하기
        for (int i = 0; i < movingHurdle.size(); i++) {
            if (movingHurdle.get(i).getX() < -100)
                movingHurdle.remove(i);
        }
    }
    public void threadStart() {
        Thread t = new Thread(this);
        startTime = System.currentTimeMillis();
        t.start();
    }
    @Override
    public void run() {
        while (!Game5Client.gameDie) {
            // 허들이 정해진 시간마다 출몰함
            endTime = System.currentTimeMillis();
            gamingTime = (int) (endTime - startTime) / 1000;
            // 허들 이미지들의 좌표값을 변경
            // ConcurrentModificationException
            try {
                for (HurdleDTO data : movingHurdle) {
                    data.x--;
                }
            } catch (ConcurrentModificationException e) {
            }
            repaint();
            hurdleDisplay = false;
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
            }
            if (Game5Client.gameDie)
                break;
        }
    }

    public ArrayList<HurdleDTO> getHurdleList() {
        return movingHurdle;
    }
}


class JellyDrop5 implements Runnable {
    public JellyDrop5() {
        Jelly5.list = new ArrayList<JellyDTO>();
    }

    public void ThreadStart() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        while (!Game5Client.gameDie) {
            // 새로운 젤리 셋팅
            if (Jelly5.list.size() == 16)
                Jelly5.list.remove(0);
            JellyDTO jellyDTO = new JellyDTO();
            jellyDTO.setX(720);
            jellyDTO.setY(330);
            Jelly5.list.add(jellyDTO);
            jellyDTO.imageIndex = (int) (Math.random() * 2) + 1; // 1~2의 난수를 생성하여 코인을 올릴지 젤리를 올릴지 결정
            try {
                Thread.sleep(200); // 초단위로 진행됨
            } catch (InterruptedException e) {
            }
            if (Game5Client.gameDie)
                break;
        }
    }
}
class Jelly5 extends JPanel implements Runnable {
    // --------------------------필드------------------------
    //이미지 생성
    public Image jelly;
    public Image coin;
    //화면위에 보일 젤리들을 담을 ArrayList
    public static ArrayList<JellyDTO> list;
    //기본 x값 저장
    public int basicX = 720;
    //클래스 생성
    public RunningCookie5 runningCookie;
    // score 필드 생성
    public static int gameScore = 0;
    public static int coinEat = 0;
    // 기본값 세팅
    public void setting() {
        runningCookie = new RunningCookie5();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(700, 460);
        setOpaque(false);
        Graphics2D g2d = (Graphics2D) g;
        
        // 이미지 생성
        Toolkit t = Toolkit.getDefaultToolkit();
        jelly = t.getImage("C:\\cookierun\\png\\jelly.png");
        coin = t.getImage("C:\\cookierun\\png\\minicoin.png");        
        // list안의 젤리를 패널에 그려주기
        try {
            if (list.size() > 0) {
                for (JellyDTO data : list) {
                    g.drawImage(data.imageIndex == 1 ? jelly : coin, data.getX(), data.getY(), 30, 30, this);
                }
            }
        } catch (Exception e) {
        }
        
        // 젤리 위를 지나가면 젤리가 사라지게 만들기
        int cookieX = runningCookie.COOKIE_X;
        int cookieY = runningCookie.getCookieY();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getX() >= 80 && list.get(i).getX() <= 180) {
                    if (list.get(i).getY() >= cookieY && list.get(i).getY() <= cookieY + 100) {
                        list.get(i).eat = true;
                        if (list.get(i).imageIndex == 2)
                            coinEat++; // 코인 획득
                        gameScore += 95;
                    }
                }
            }
        }
        
        // 먹은 젤리를 리스트에서 없애버리기
        try {
            for (JellyDTO data : list) {
                if (data.eat) {
                    list.remove(data);
                }
            }
        } catch (Exception e) {         
        }
    }
    
    //스레드를 시작시켜주는 스레드
    public void threadStart() {
        Thread t = new Thread(this);
        t.start();
    }
    
    public void run() {
        while (!Game5Client.gameDie) {
            try {
                if (list.size() > 0) {
                    for (JellyDTO data : list) {
                        data.x--;
                    }
                }
            } catch (Exception e) {
            }
            repaint(); // 1초마다 repaint진행함
            try {
                Thread.sleep(3); // 초단위로 진행됨
            } catch (InterruptedException e) {
            }
            if (Game5Client.gameDie)
                break;
        }

    }
}
//작은 젤리 한개의 좌표값을 담고있는 DTO클래스. 

class GomJellyDummy5 extends JPanel implements Runnable {
    // --------------------------필드-----------------------------------
    // 패널에 넣을 이미지 호출
    public Image jellyDisappear; // 젤리를 획득했을 떄 교체할 이미지
    public Image gom; // 곰젤리
    // 곰젤리의 위치 정보를 담을 리스트
    public ArrayList<GomJellyDTO> dummy;
    public ArrayList<GomJellyDTO> list;
    // 믈래스 생성
    public GomJellyDTO gomJellyDTO;
    public RunningCookie5 runningCookie;
    // 좌표값 설정
    public static final int Y1 = 210;
    public static final int Y2 = 250;
    public static final int Y3 = 290;
    public static final int Y4 = 330;
    public static int pointY[] = { Y1, Y2, Y3, Y4 };
    public int pointX = 750;
    public int yIndex = 0;
    // 젤리 출몰 시간대 설정
    public ArrayList<Integer> jellyTime;
    public int time = 1;
    // 스레드 실행 시간 계산
    public Long startTime;
    public Long endTime;
    public int gamingTime;
    // 랜덤 출몰한 젤리 값들 담기
    public ArrayList<ArrayList> appearJelly;
    // 먹은 젤리의 개수 카운트
    public static int countGom = 0;

    // -----------------------------------메소드---------------------------------
    // 기본 셋팅을 해주는 메소드
    public void setting() {
        dummy = new ArrayList<GomJellyDTO>();
        list = new ArrayList<GomJellyDTO>();
        jellyTime = new ArrayList<>();
        appearJelly = new ArrayList<ArrayList>();
        runningCookie = new RunningCookie5();

        // 젤리 출몰 시간대 랜덤 설정
        for (int i = 0; i < 200; i++) {
            jellyTime.add(time);
            time += (int) (Math.random() * 6) + 5; // 5~10초 랜덤 생성
            if (time > 200)
                break;
        }
    }

    // 랜덤 된 시간에 젤리를 패널에 올려주는 메소드
    public void setJellyDisplay() {
        // 젤리 좌표 설정(곰젤리 개수를 조정 할 때 여기 변수의 값들을 조정하기)
        for (int i = 0; i < 40; i += 4) {
            for (int j = i; j < i + 4; j++) {
                gomJellyDTO = new GomJellyDTO();
                gomJellyDTO.setX(pointX);
                gomJellyDTO.setY(pointY[yIndex]);
                yIndex++;
                dummy.add(gomJellyDTO);
            }
            pointX += 50;
            yIndex = 0;
        }
    }
    //패널 위에 이미지를 올려줌
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(700, 460);
        setOpaque(false);
        Graphics2D g2d = (Graphics2D) g;
        Toolkit t = Toolkit.getDefaultToolkit();
        
        // 곰젤리 이미지 생성
        gom = t.getImage("C:\\cookierun\\png\\Gom_jelly2.png");
        jellyDisappear = t.getImage("C:\\cookierun\\png\\afterGom.gif");

        // 곰젤리 Panel위에 올리기
        if (jellyTime.get(0) == gamingTime) {
            setJellyDisplay();
            jellyTime.remove(0);
        }
        // 곰젤리를 쿠키와 닿으면 젤리가 사라지게 만들기
        int cookieX = runningCookie.COOKIE_X;
        int cookieY = runningCookie.getCookieY();
        if (dummy.size() > 0) {
            for (int i = 0; i < dummy.size(); i++) {
                if (dummy.get(i).getX() >= 80 && dummy.get(i).getX() <= 180) { // 쿠키가 서있는 범위
                    if (dummy.get(i).getY() >= cookieY && dummy.get(i).getY() <= cookieY + 100) {
                        if (!dummy.get(i).eat) {
                            dummy.get(i).eat = true;
                            countGom++;
                            Jelly5.gameScore += 1000;
                            play("C:\\cookierun\\music\\jelly1.wav");  //젤리 획득시 나오는 사운드 파일 재생
                        }
                    }
                }
            }
        }
        // 곰젤리 출력하기
        if (dummy.size() > 0) {
            for (GomJellyDTO data2 : dummy) {
                g.drawImage(data2.eat ? jellyDisappear : gom, data2.getX(), data2.getY(), 35, 30, this); // 먹힌 젤리면
                                                                                                         // jellyDisappear 이미지가 나오고
                                                                                                         // , 안먹혔으면 gom 이미지가 나오는 삼항연산자.
            }
        }
        // 프레임 밖을 벗어나면 리스트 지워버리기
        if (dummy.size() > 0) {
            if (dummy.get(39).getX() < -20)
                dummy.clear(); // 잴리 개수 변경 시 이 값도 변경 필요
        }
    }

    // 스레드 스타트 메소드
    public void threadStart() {
        Thread t = new Thread(this);
        startTime = System.currentTimeMillis();
        t.start();
    }

    @Override
    public void run() {
        while (!Game5Client.gameDie) {
            endTime = System.currentTimeMillis();
            gamingTime = (int) (endTime - startTime) / 1000;

            if (dummy.size() > 0) {
                for (GomJellyDTO data2 : dummy) {
                    data2.x--;
                }
            }
            repaint();
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
            }
            if (Game5Client.gameDie == true) break;  //확인사살로 한개 더 만들어 둠
        }
    }
    //음악을 재생해주는 메소드
    public void play(String fileName) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
            Clip clip = AudioSystem.getClip();
            clip.stop();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
        }
    }
}
class BigGom5 extends JPanel implements Runnable {
    // ----------------------필드------------------------
    // 이미지 생성 필드
    public Image bigCoin;
    public Image bigGom;
    // 대형젤리 출몰시간을 ArrayLust안에 랜덤으로 담기
    public ArrayList<Integer> bigTime;
    public ArrayList<BigDTO> bigList; // 젤리 좌표값 담기
    // 클래스 생성
    public BigDTO bigDTO;
    public RunningCookie5 runningCookie;
    // y좌표값 고정, x 시작 좌표 설정
    public static final int BIG_Y = 220;
    public int jellyX = 720;
    // 대형 젤리 생성 시간 조절 필드
    public Long startTime;
    public Long endTime;
    public int gamingTime; // 스레드 실행 시간
    public int time = 5;

    // --------------------------메소드--------------------
    public BigGom5() {
        runningCookie = new RunningCookie5();
    }
    //기본 셋팅을 해주는 메소드로 반드시 호출해주어야 곰젤리와 코인이 나오게 된다. 
    public void setting() {
        // 랜덤 출몰 시간을 저장
        bigDTO = new BigDTO();
        bigTime = new ArrayList<>();
        bigList = new ArrayList<BigDTO>();
        for (int i = 0; i < 200; i++) {
            bigTime.add(time);
            time += (int) (Math.random() * 5) + 1;
            if (time > 200)
                break;
        }
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(700, 460);
        setOpaque(false);
        Graphics2D g2d = (Graphics2D) g;
        Toolkit t = Toolkit.getDefaultToolkit();
        // 곰젤리와 코인 이미지 불러오기
        bigCoin = t.getImage("C:\\cookierun\\png\\Coin.png");
        bigGom = t.getImage("C:\\cookierun\\png\\Gom_jelly4.png");
        // 리스트 안의 시간이랑 플레이 시간이 일치하면 빅 곰젤리 생성
        if (bigTime.get(0) == gamingTime) {
            bigDTO = new BigDTO();
            bigDTO.setX(jellyX); // x시작값 설정
            bigDTO.setY(BIG_Y);
            bigTime.remove(0);
            bigList.add(bigDTO);
        }
        // 리스트 안에 생성된 젤리들 그려주기
        if (bigList.size() > 0) {
            for (BigDTO data : bigList) {
                g.drawImage(data.getRandomIndex() == 1 ? bigGom : bigCoin, data.getX(), BIG_Y, 60, 60, this);
            }
        }
        // 프레임 밖을 벗어나면 젤리 지워버리기
        try {
            if (bigList.size() > 0) {
                for (BigDTO data : bigList) {
                    if (data.getX() < -20)
                        bigList.remove(data);
                }
            }
        } catch (Exception e) {  
        }

        // 쿠키와 부딪히면 젤리가 사라지게 만들기
        int cookieX = runningCookie.COOKIE_X;
        int cookieY = runningCookie.getCookieY();
        if (bigList.size() > 0) {
            for (int i = 0; i < bigList.size(); i++) {
                if (bigList.get(i).getX() >= 80 && bigList.get(i).getX() <= 180) {
                    if (bigList.get(i).getY() >= cookieY && bigList.get(i).getY() <= cookieY + 100) {
                        if (!bigList.get(i).eat) { // 먹지않은 젤리일 때
                            // 젤리를 먹으면 점수를 올려줌
                            bigList.get(i).eat = true;  
                            if (bigList.get(i).getRandomIndex() == 1) GomJellyDummy5.countGom += 5;  //곰젤리를 먹었을 경우 곰젤리 점수를 카운트
                            else Jelly5.coinEat += 5;  //코인을 먹었을 경우 코인 수를 카운트
                            Jelly5.gameScore += 5000;
                            play("src/jelly1.wav");   //곰젤리 획득시 나오는 사운드 파일
                        }
                    }
                }
            }
        }
        try {
            for (BigDTO data : bigList) {
                if (data.eat) {
                    bigList.remove(data);
                }
            }
        } catch (Exception e) {
        }
    }

    // 스레드를 실행하는 메소드
    public void threadStart() {
        Thread t = new Thread(this);
        startTime = System.currentTimeMillis();
        t.start();
    }

    @Override
    public void run() {
        while (!Game5Client.gameDie) {   //게임 종료시까지 플레이
            endTime = System.currentTimeMillis();
            gamingTime = (int) (endTime - startTime) / 1000;
            for (BigDTO data : bigList) {
                data.x--;  //x좌표 한 칸씩 왼쪽으로 이동 
            }
            repaint();
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {}
            if (Game5Client.gameDie)
                break;
        }
    }
    //곰젤리 획득 시 소리 나오게 만드는 메소드
    public void play(String fileName) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
            Clip clip = AudioSystem.getClip();
            clip.stop();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {}
    }
}
class HpBar5 extends JPanel implements Runnable {
    //---------------------필드-----------------------
    // 체력 설정 필드
    public Image HpBar;
    public Image HbBar;
    public int second = 0;
    // 곰젤리 먹은 개수 카운팅
    public Image gomCountBackground; // 배경에 먹은 곰 젤리의 갯수를 세어줄 이미지 넣기
    public JLabel gomCount;
    //먹은 코인 개수 카운팅
    public Image coinCountBackground;
    public JLabel coinCount;
    //전체 스코어 점수 카운팅
    public Image scoreBackground;
    public JLabel score;
    public DecimalFormat df = new DecimalFormat("#,##0");
    
    //0517하린 수정
    // 랭킹 이미지
    //public Image number1, number2, rankBar;
    public JLabel competitionL;
    private Image rivalScoreImg;
    
//게임종료 클래스 호출
    public EndGame5 endGame;
    //---------------------메소드-----------------------------------
    public HpBar5() {
        this.setLayout(null);
        // 화면 상단 위 스코어 설정
        gomCount = new JLabel("0");
        coinCount = new JLabel("0");
        score = new JLabel("0");
        
        competitionL= new JLabel("0");
        gomCount.setFont(new Font("Segoe UI Black", Font.BOLD, 25)); // 글씨체 수정 요망
        coinCount.setFont(new Font("Segoe UI Black", Font.BOLD, 25));
        score.setFont(new Font("Segoe UI Black", Font.BOLD, 15));
        
        competitionL.setFont(new Font("Segoe UI Black", Font.BOLD,15));
        gomCount.setForeground(Color.WHITE);
        coinCount.setForeground(Color.WHITE);
        score.setForeground(Color.WHITE);
        
        competitionL.setForeground(Color.WHITE);
        gomCount.setBounds(630, 18, 100, 50);
        coinCount.setBounds(630, 63, 100, 50);
        score.setBounds(330, 60, 200, 50);
        
        competitionL.setBounds(25,30,200,50);
        add(score);
        add(gomCount);
        add(coinCount);
    
        add(competitionL);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(700, 460);
        setOpaque(false);
        Graphics2D g2d = (Graphics2D) g;
        Toolkit t = Toolkit.getDefaultToolkit();
        
        rivalScoreImg= t.getImage("C:\\cookierun\\png\\rivalScore.png");
        
        // 화면 상단 위젯 이미지 불러오기
        gomCountBackground = t.getImage("C:\\cookierun\\png\\Gom_jelly4.png");
        coinCountBackground = t.getImage("C:\\cookierun\\png\\Coin.png");
        scoreBackground = t.getImage("C:\\cookierun\\png\\jelly.png");
        //number1 = t.getImage("C:\\cookierun\\png\\number1.png");
        
        // 체력 게이지 만들기
        HpBar = t.getImage("C:\\cookierun\\png\\HpBar_wh.png");
        HbBar = t.getImage("C:\\cookierun\\png\\HbBark.gif");

        //만약 죽지 않았을 떄(체력이 0 이상일 때)      
        g.drawImage(HbBar, 130, 38, (MovingHurdle5.health * 4), 20, this);
        g.drawImage(HpBar, 90, 20, 450, 50, this);
        g.drawImage(rivalScoreImg, 10,5,80,40,this);
        
        //죽었을 때(체력이 0보다 작을 때)
        if (MovingHurdle5.health < 0) {
            Game5Client.gameDie = true;
        }
        // 상단 위에 곰젤리&코인을 카운팅하는 위젯 넣기
        g.drawImage(gomCountBackground, 570, 30, 35, 30, this);
        gomCount.setText(Integer.toString(GomJellyDummy5.countGom));
        
        g.drawImage(coinCountBackground, 570, 75, 35, 30, this);
        coinCount.setText(Integer.toString(Jelly5.coinEat));
        
        g.drawImage(scoreBackground, 300, 75, 20, 20, this);
        score.setText(df.format(Jelly5.gameScore));
    
        competitionL.setText(Game5Client.competitionScore);
    }
    //스레드를 시작해주는 메소드
    public void threadStart() {
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        while (!Game5Client.gameDie) {
            MovingHurdle5.health -= 4; // 2초에 1퍼센트 씩 체력 소모
            try {
                Thread.sleep(2000); // 1초에 1씩 닳게 하고 싶으면 1000으로 교환해주기
            } catch (InterruptedException e) {
                return;
            }
            repaint();
            if (Game5Client.gameDie) break;
        }
    }
}
class EndGame5 extends JFrame implements ActionListener{
    
    private LoginDTO loginDTO;
    private  JButton yesBtn;
    private  JLabel gameScoreL;
    private  JLabel score;
    private  JLabel gomScore;
    private  JLabel coinScore;
    private RoomDAO roomDAO;
    
    private  DecimalFormat df = new DecimalFormat("#,##0");
    //패널 선언
    private  JPanel p;
    private  EndBack5 end;
    private int roomNum;
    
    public EndGame5(LoginDTO loginDTO, int roomNum) {
        this.loginDTO = loginDTO;
        this.roomNum = roomNum;
        this.setSize(700, 460);
        this.setLayout(null);
        
        
        //////////////
        p = new JPanel();
        end = new EndBack5();
        p.setOpaque(false);
        p.setLayout(null);
        p.setBounds(0,0,700,500);
        end.setLayout(null);
        end.setBounds(0,0,700,450);
        JPanel bubbleP = new JPanel() {
            private Image win;
            private Image lose;
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                this.setSize(100,100);
                Graphics2D g2d = (Graphics2D) g;
                Toolkit t = Toolkit.getDefaultToolkit();
                win = t.getImage("C:\\cookierun\\png\\Win.png");
                lose = t.getImage("C:\\cookierun\\png\\Lose.png");
                if(Game5Client.rivalScore>Jelly5.gameScore) {
                    g.drawImage(lose,0,0,this.getWidth(),this.getHeight(),this);
                } else {
                    g.drawImage(win,0,0,this.getWidth(),this.getHeight(),this);
                }
            }
        }; 
        bubbleP.setBounds(100,100,100,100);     
        
        yesBtn = new JButton(new ImageIcon("C:\\cookierun\\png\\Gameresult.jpg"));
        yesBtn.setBounds(245,350,200,60);
        yesBtn.addActionListener(this);
        //score라벨 설정
        score = new JLabel();
        score.setFont(new Font("Segoe UI Black",Font.BOLD,30));
        score.setForeground(Color.gray);
        score.setBounds(200,120,300,100);
        score.setText("SCORE");
        score.setHorizontalAlignment(JLabel.CENTER);
        //GameScore라벨 설정
        gameScoreL = new JLabel();
        gameScoreL.setFont(new Font("Segoe UI Black",Font.BOLD,50));
        gameScoreL.setForeground(Color.BLACK);
        gameScoreL.setBounds(200,140,300,100);
        gameScoreL.setText(df.format(Jelly5.gameScore));
        gameScoreL.setHorizontalAlignment(JLabel.CENTER);
        //곰젤리 라벨 설정
        gomScore = new JLabel();
        gomScore.setFont(new Font("Segoe UI Black",Font.BOLD,30));
        gomScore.setForeground(Color.gray);
        gomScore.setBounds(540,205,300,100);
        gomScore.setText(df.format(GomJellyDummy5.countGom));
        //코인 라벨 설정
        coinScore = new JLabel();
        coinScore.setFont(new Font("Segoe UI Black",Font.BOLD,30));
        coinScore.setForeground(Color.gray);
        coinScore.setBounds(540,260,300,100);   
        if(Game5Client.rivalScore>Jelly5.gameScore) {
            coinScore.setText("0");
            Jelly5.coinEat=0;
        } else {
            coinScore.setText(df.format(Jelly5.coinEat));
        }
        
        //패널 위에 올리기
        p.add(gomScore);
        p.add(coinScore);
        p.add(gameScoreL);
        p.add(yesBtn);
        
        //코인&update 실행
        loginDTO.setCoin(loginDTO.getCoin()+Jelly5.coinEat);
        loginDTO.setScore((loginDTO.getScore()<Jelly5.gameScore)?Jelly5.gameScore:loginDTO.getScore());
        
        // 0517하린 추가
        p.add(bubbleP);
        end.add(p);
        add(end);
    }
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==yesBtn) {
            roomDAO = new RoomDAO();
            System.out.println(loginDTO.getCoin());
            System.out.println(loginDTO.getScore());
            roomDAO.updateCoin(loginDTO);
            BackgroundT5.round2=false;          
            Jelly5.gameScore=0;
            Jelly5.coinEat=0;
            Jelly5.gameScore=0;
            Game5Client.gameDie=false;
            MovingHurdle5.health=100;
            Game5Client.rivalGameDie=false;
            Game5Client.competitionScore="0";
            roomDAO.updateEmptyRoom(roomNum);
            //new LobbyClient(loginDTO).service();
            //this.setVisible(false);
            this.dispose();
            new LobbyClient(loginDTO).service();
        }
    }
}
class EndBack5 extends JPanel {
    public Image scoreBoard;
    public void paintComponent(Graphics g) {
        // 이미지 생성
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Toolkit t = Toolkit.getDefaultToolkit();
        scoreBoard = t.getImage("C:\\cookierun\\png\\GameresultBack.jpg");
        g.drawImage(scoreBoard,0,0,this.getWidth(), this.getHeight()-20,this);
        
    }
}
