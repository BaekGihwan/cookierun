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

//일정할 시간마다 곰젤리를 뭉터기로 흩뿌려주는 클래스. 곰젤리를 획득하면 점수가 대폭 상승한다. 
//(젤리개수 변경을 원할 시 setJellyDisplay() 메소드의 i변수에 변동하고 싶은 젤리의 값을 입력하고
//paintComponent 메소드에서도 <프레임 밖을 벗어나면 리스트 지워버리기> 항목에서 개수를 변경해준다)
class GomJellyDummy extends JPanel implements Runnable {
    // --------------------------필드-----------------------------------
    // 패널에 넣을 이미지 호출
    public Image jellyDisappear; // 젤리를 획득했을 떄 교체할 이미지
    public Image gom; // 곰젤리
    // 곰젤리의 위치 정보를 담을 리스트
    public ArrayList<GomJellyDTO> dummy;
    public ArrayList<GomJellyDTO> list;
    // 믈래스 생성
    public GomJellyDTO gomJellyDTO;
    public RunningCookie runningCookie;
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
        runningCookie = new RunningCookie();

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
                            Jelly.gameScore += 1000;
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
        while (!Game1Client.gameDie) {
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
            if (Game1Client.gameDie == true) break;  //확인사살로 한개 더 만들어 둠
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