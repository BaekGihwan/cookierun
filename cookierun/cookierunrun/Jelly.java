package cookierunrun;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.JPanel;

/*
 쿠키가 지나가는 길에 JellyDrop으로 젤리들을 올려주면 그 올린 젤리들을 스레드로 연속적으로 옯겨 줌 , 젤리를 먹으면 score가 올라감(작은 젤리와 코인)
 */
public class Jelly extends JPanel implements Runnable {
    // --------------------------필드------------------------
	//이미지 생성
	public Image jelly;
    public Image coin;
    //화면위에 보일 젤리들을 담을 ArrayList
    public static ArrayList<JellyDTO> list;
    //기본 x값 저장
    public int basicX = 720;
    //클래스 생성
    public RunningCookie runningCookie;
    // score 필드 생성
    public static int gameScore = 0;
    public static int coinEat = 0;
    // 기본값 세팅
    public void setting() {
        runningCookie = new RunningCookie();
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
        while (!Game1Client.gameDie) {
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
            if (Game1Client.gameDie)
                break;
        }

    }
}