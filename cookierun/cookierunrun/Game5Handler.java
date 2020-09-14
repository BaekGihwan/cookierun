package cookierunrun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.List;

public class Game5Handler extends Thread {
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket socket;
	private List<Game5Handler> list;
	private String nickName;

	public Game5Handler(Socket socket, List<Game5Handler> list) throws IOException {

		this.socket = socket;
		this.list = list;

		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

	}

	public void run() {
		// 클라이언트로 부터 받기
		// 닉네임 받기
		try {
			nickName = reader.readLine(); // 한줄씩 읽는다. 닉네임을 읽는거고

			String line; // 내용을 읽기 위해서
			while (true) {
				line = reader.readLine();
				//System.out.println("게임 점수 주고받는 핸들러");
				//System.out.println(line);
				if (line == null || line.equals("exit")) {
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
				} // if문의 끝
				// 클라이언트로 보내기
				broadcast(nickName + ":" + line);
			} // while
		} catch (IOException e) {
		}
	}

	public void broadcast(String mag) throws ConcurrentModificationException{
		if (list.size() > 0) {
			for (Game5Handler handler : list) { // list안에 있는 ChatHandler을 하나씩 꺼내서 하나씩 보낸다.
				handler.writer.println(mag);
				handler.writer.flush();
			} // for
		}
	}
}
