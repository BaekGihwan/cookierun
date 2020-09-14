package cookierunrun;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RoomDAO {
	private String driver = "oracle.jdbc.driver.OracleDriver";
	// private String url = "jdbc:oracle:thin:@192.168.0.20:1521:xe";
	private String url = "jdbc:oracle:thin:@localhost:1521:xe";
	private String username = "c##java";
	private String password = "bit";
	private Connection conn;
	private PreparedStatement pstmt;
	private ResultSet rs;
	private RoomDTO roomDTO;

	public RoomDAO() {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void getConnection() {
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<RoomDTO> selectAllRoom() {
		ArrayList<RoomDTO> ar = new ArrayList<RoomDTO>();
		getConnection();
		String sql = "select * from roomTable";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				RoomDTO roomDTO = new RoomDTO();
				roomDTO.setRoomNumber(rs.getInt("roomNumber"));
				roomDTO.setRoomName(rs.getString("roomName"));
				roomDTO.setRoomPw(rs.getString("roomPw"));
				roomDTO.setSecretRoom(rs.getInt("secretRoom"));
				roomDTO.setUser1(rs.getString("user1"));
				roomDTO.setUser2(rs.getString("user2"));
				roomDTO.setUserCount(rs.getInt("userCount"));
				roomDTO.setRoomState(rs.getInt("roomState"));

				ar.add(roomDTO);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			ar = null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ar;
	}

	public void updateRoomInf(RoomDTO roomDTO) {
		getConnection();
		String sql = "update roomtable set roomname=?,roompw=?,secretroom=?,user1=?,user2=?,usercount=?,roomstate=? where roomNumber = ?";

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, roomDTO.getRoomName());
			pstmt.setString(2, roomDTO.getRoomPw());
			pstmt.setInt(3, roomDTO.getSecretRoom());
			pstmt.setString(4, roomDTO.getUser1());
			pstmt.setString(5, roomDTO.getUser2());
			pstmt.setInt(6, roomDTO.getUserCount());
			pstmt.setInt(7, roomDTO.getRoomState());
			pstmt.setInt(8, roomDTO.getRoomNumber());
			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();// 나중에 만든 것 먼저 끊기
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateUser2(String user2, int roomNum) {
		getConnection();
		String sql = "update roomtable set user2 = ?, usercount = 2 where roomNumber = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user2);
			pstmt.setInt(2, roomNum);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();// 나중에 만든 것 먼저 끊기
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 유저 1번이 나갔을때
	public void outUser1(String user1, String user2, int roomNum) {
		getConnection();
		String sql = "";
		if (user2 != null) {
			sql = "update roomtable set user1 = ?, user2 = null, usercount = 1 where roomNumber = ?";
		} else {
			sql = "update roomtable set user1 = ?, user2 = null, usercount = 0 where roomNumber = ?";
		}
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user2);
			pstmt.setInt(2, roomNum);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();// 나중에 만든 것 먼저 끊기
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 유저 2번이 나갔을때
	public void outUser2(int roomNum) {
		getConnection();
		String sql = "update roomtable set user2 = null ,usercount = 1 where roomNumber = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, roomNum);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	// 둘다 나갈때
	public void updateEmptyRoom(int roomNum) {
		getConnection();
		String sql = "update roomtable set roomName='',roomPw='',secretRoom=0, user1='', user2='', userCount=0, roomState=1 where roomNumber=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, roomNum);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();// 나중에 만든 것 먼저 끊기
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 코인과 score 업데이트
	public void updateCoin(LoginDTO loginDTO) {
		System.out.println(loginDTO.getCoin());
		System.out.println(loginDTO.getScore());
		System.out.println(loginDTO.getNickName());

		getConnection();
		String sql = "update gameuser set  score=?,coin=? where nickName = ?";
		// coin score
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, loginDTO.getScore());
			pstmt.setInt(2, loginDTO.getCoin());

			pstmt.setString(3, loginDTO.getNickName());

			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();// 나중에 만든 것 먼저 끊기
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public ArrayList<LoginDTO> tableDesc() {
		ArrayList<LoginDTO> ar = new ArrayList<LoginDTO>();
		getConnection();
		String sql = "select * from gameuser order by score desc"; // 내림차순 정렬
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				LoginDTO loginDTO = new LoginDTO();
				loginDTO.setId(rs.getString("id"));
				loginDTO.setNickName(rs.getString("nickname"));
				loginDTO.setPw(rs.getString("pw"));
				loginDTO.setPwConfirm(rs.getString("pwconfirm"));
				loginDTO.setName(rs.getString("name"));
				loginDTO.setBirth(rs.getString("birth"));
				loginDTO.setPhoneN(rs.getString("phoneN"));
				loginDTO.setEmail(rs.getString("email"));
				loginDTO.setScore(rs.getInt("score"));
				loginDTO.setCoin(rs.getInt("coin"));
				ar.add(loginDTO);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ar = null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ar;
	}

	public void updateGamingState(int roomNum) {
		getConnection();
		String sql = "update roomtable set roomState=3 where roomNumber=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, roomNum);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();// 나중에 만든 것 먼저 끊기
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}