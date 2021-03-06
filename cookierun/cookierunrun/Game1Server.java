package cookierunrun;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
//게임서버 생성
public class Game1Server {
	private ServerSocket serverSocket; // 서버 소켓을 열어 주는거다?
	private List<Game1Handler> list; // 인터페이스이다 여러개의 소켓 확인용?
	public Game1Server() {
		try{
			serverSocket = new ServerSocket(8000); // 같은 방안의 포트 번호를 준다.
			System.out.println("게임1 서버 준비완료...."); // 
			list = new ArrayList<Game1Handler>(); // 여기 리스트안에는 핸들러가 담겨야 하기때문에 제너릭을 걸어서 핸들러 타입을 담겠다고 한다.
			while(true){ // 클라이언트를 들어 올때마다 낚아 채야 하기 때문에 while문 돌린다
				Socket socket = serverSocket.accept(); // 들어오는 족족 클라이언트를 낚아챈다.
				Game1Handler handler = new Game1Handler (socket, list); // new하는 순간 쓰레드 생성을 한다 list는 실제로 채팅하는 사람은 서버가 아니고 핸들러가 하기때문에 같이 보내야 한다
				handler.start(); // 그래서 쓰레드 시작을 건다
				list.add(handler); // list안에 핸들러를 넣는다.
			}
		}catch(IOException e){
		}		
	}
	public static void main(String[] args) {
		new Game1Server();
	}
}
