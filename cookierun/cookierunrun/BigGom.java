package cookierunrun;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;

//점프해야 얻을 수 있는 대형 코인과 대형곰젤리 뿌리기(코인과 젤리는 랜덤으로 나오며 획득 스코어는 전부 동일하다. )
public class BigGom extends JPanel implements Runnable {
    // ----------------------필드------------------------
    // 이미지 생성 필드
    public Image bigCoin;
    public Image bigGom;
    // 대형젤리 출몰시간을 ArrayLust안에 랜덤으로 담기
    public ArrayList<Integer> bigTime;
    public ArrayList<BigDTO> bigList; // 젤리 좌표값 담기
    // 클래스 생성
    public BigDTO bigDTO;
    public RunningCookie runningCookie;
    // y좌표값 고정, x 시작 좌표 설정
    public static final int BIG_Y = 220;
    public int jellyX = 720;
    // 대형 젤리 생성 시간 조절 필드
    public Long startTime;
    public Long endTime;
    public int gamingTime; // 스레드 실행 시간
    public int time = 5;

    // --------------------------메소드--------------------
    public BigGom() {
        runningCookie = new RunningCookie();
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
                            if (bigList.get(i).getRandomIndex() == 1) GomJellyDummy.countGom += 5;  //곰젤리를 먹었을 경우 곰젤리 점수를 카운트
                            else Jelly.coinEat += 5;  //코인을 먹었을 경우 코인 수를 카운트
                            Jelly.gameScore += 5000;
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
        while (!Game1Client.gameDie) {   //게임 종료시까지 플레이
            endTime = System.currentTimeMillis();
            gamingTime = (int) (endTime - startTime) / 1000;
            for (BigDTO data : bigList) {
                data.x--;  //x좌표 한 칸씩 왼쪽으로 이동 
            }
            repaint();
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {}
            if (Game1Client.gameDie)
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