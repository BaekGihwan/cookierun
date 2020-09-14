package cookierunrun;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
/*
체력바를 화면에 올려주는 JPanel 클래스. 총 체력은 100으로, 2초에 1씩 체력이 소모가 되고, 
만약 장애물이 부딛혔을 떄는 체력이 15가 닳는다. 
+ 화면 상단 위 곰젤리 획득 개수와 코인획득개수, 스코어가 함께 뜬다. 
*/

public class HpBar extends JPanel implements Runnable {
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
	public EndGame endGame;
	//---------------------메소드-----------------------------------
	public HpBar() {
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
		g.drawImage(HbBar, 130, 38, (MovingHurdle.health * 4), 20, this);
		g.drawImage(HpBar, 90, 20, 450, 50, this);
		g.drawImage(rivalScoreImg, 10,5,80,40,this);
		
		//죽었을 때(체력이 0보다 작을 때)
		if (MovingHurdle.health < 0) {
			Game1Client.gameDie = true;
		}
		// 상단 위에 곰젤리&코인을 카운팅하는 위젯 넣기
		g.drawImage(gomCountBackground, 570, 30, 35, 30, this);
		gomCount.setText(Integer.toString(GomJellyDummy.countGom));
		
		g.drawImage(coinCountBackground, 570, 75, 35, 30, this);
		coinCount.setText(Integer.toString(Jelly.coinEat));
		
		g.drawImage(scoreBackground, 300, 75, 20, 20, this);
		score.setText(df.format(Jelly.gameScore));
	
		competitionL.setText(Game1Client.competitionScore);
	}
	//스레드를 시작해주는 메소드
	public void threadStart() {
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		while (!Game1Client.gameDie) {
			MovingHurdle.health -= 4; // 2초에 1퍼센트 씩 체력 소모
			try {
				Thread.sleep(2000); // 1초에 1씩 닳게 하고 싶으면 1000으로 교환해주기
			} catch (InterruptedException e) {
				return;
			}
			repaint();
			if (Game1Client.gameDie) break;
		}
	}
}
