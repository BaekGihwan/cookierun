package cookierunrun;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;

/*
배경만 스레드로 움직여 주는 클래스. 60초가 지나면 2라운드로 넘어간다. 
배경음악 역시 함꼐 재생되는데 라운드가 넘어가면 음악 역시 바뀐다. 
*/
class BackgroundT extends JPanel implements Runnable {
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
    public BackgroundT() {
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
        while (!Game1Client.gameDie) {
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
            if (Game1Client.gameDie)
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
