package cookierunrun;

import java.io.BufferedReader;
import java.io.IOException; // InputStreamReader이거를쓰면 나온다.
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;


class LobbyHandler extends Thread {	// 클라이언트의 소켓 쓰레드 부분?? 회사의 지점같은 곳 ChatHandler는 쓰레드를 상속받아서 자체가 쓰레드이다
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket socket; // 서버에서 만들어진 소켓을 받기 위함이다. 소켓하나하나에는 reader와 writer가 있어야 한다.
	private List<LobbyHandler> list;

	public LobbyHandler(Socket socket, List<LobbyHandler> list) throws IOException{
		this.socket = socket;
		this.list = list;

		reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 클라이언트 쪽으로 받는 곳
		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); // 클라이언트로 받는 곳
	} // ChatHandler생성자
	
	public void run(){	
		// 클라이언트로 부터 받기
		// 닉네임 받기
		try{
			String nickName = reader.readLine(); // 한줄씩 읽는다. 닉네임을 읽는거고
			broadcast(nickName + "님이 입장하였습니다."); // 모든 클라이언트에게 입장 메시지를 보내기.
			String line; // 내용을 읽기 위해서
			while(true){
				line = reader.readLine();
				if(line == null || line.equals("exit")){ // 스레드는 한번씩 백그라운드에서 돌기때문에 null 값이 들어 온다
					writer.println("exit");
					writer.flush();

					reader.close(); // 나와 서버와의 연결되있던걸 다 끊어준다.
					writer.close(); // 나와 서버와의 연결되있던걸 다 끊어준다.
					socket.close(); // 나와 서버와의 연결되있던걸 다 끊어준다.
					// 남아있는 클라이언트
					// Arraylist 에서 퇴장한사람을 없애야 한다.
					list.remove(this);

					broadcast(nickName + "님이 퇴장하였습니다.");
					break; // 와일문 나가기
				}//if문의 끝
				// 클라이언트로 보내기	
				broadcast("[" + nickName + "] " + line); 				
			}//while

		}catch(IOException e){
		}
	}
	public void broadcast(String mag){
		for(LobbyHandler handler : list){ // list안에 있는 ChatHandler을 하나씩 꺼내서 하나씩 보낸다.
			handler.writer.println(mag);
			handler.writer.flush();
		}// for
	}
}
