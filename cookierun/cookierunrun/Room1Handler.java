package cookierunrun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.List;

public class Room1Handler extends Thread {
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket socket; // 서버에서 만들어진 소켓을 받기 위함이다. 소켓하나하나에는 reader와 writer가 있어야 한다.
	private List<Room1Handler> list;
	public String nickName;

	public Room1Handler(Socket socket, List<Room1Handler> list) throws IOException {
		this.socket = socket;
		this.list = list;

		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	public void run() {
		// 클라이언트로 부터 받기
		// 닉네임 받기
		try {
			nickName = reader.readLine(); 
			broadcast(nickName + "님이 입장하였습니다."); 
			
			if (list.size() == 2) {
				list.get(1).writer.println(list.get(0).nickName + "님이 입장하였습니다.");
				broadcast("게임을 시작하시려면 READY를 눌러주세요!");
			}
			String line;
			while (true) {
				line = reader.readLine();
				if (line == null || line.equals("exit")) {
					writer.println("exit");
					writer.flush();
					reader.close(); 
					writer.close(); 
					socket.close(); 
					broadcast(nickName + "님이 퇴장하였습니다.");
					list.remove(this);					
					break;
				} // if문의 끝

				//System.out.println(nickName);
				broadcast(nickName + ": " + line); 
				
			} // while
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void broadcast(String mag) throws ConcurrentModificationException{
		if (list.size() > 0) {
			for (Room1Handler handler : list) {
				handler.writer.println(mag);
				handler.writer.flush();
			} // for
		}
	}
}
