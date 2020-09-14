package cookierunrun;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.swing.JPanel;

// 장애물을 연속적으로 움직이는 스레드
public class MovingHurdle extends JPanel implements Runnable {
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
    public RunningCookie runningCookie;
    // 생성자
    public MovingHurdle() {
        runningCookie = new RunningCookie();
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
                    g.drawImage((BackgroundT.round2 ? round2[data.getImageIndex()] : round1[data.getImageIndex()]),
                            data.getX(), data.getY(), 70, 300, this);
                } else {
                    g.drawImage(BackgroundT.round2 ? round2[data.getImageIndex()] : round1[data.getImageIndex()],
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
        while (!Game1Client.gameDie) {
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
            if (Game1Client.gameDie)
                break;
        }
    }

    public ArrayList<HurdleDTO> getHurdleList() {
        return movingHurdle;
    }
}
