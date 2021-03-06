package sprout.clipcon.server.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import sprout.clipcon.server.model.Group;

public class Server {
	private static Server uniqueInstance;
	/** All groups on the server */
	private Map<String, Group> groups = Collections.synchronizedMap(new HashMap<String, Group>());

	public static final String SERVER_ROOT_LOCATION = System.getProperty("user.home") + File.separator + "Desktop"+ File.separator;

	// 업로드 파일을 저장할 위치
	public static final String RECEIVE_LOCATION = SERVER_ROOT_LOCATION + "clipcon_download";

	// change source
	private Server() {
		System.out.println("Construct Server >>>");

		File initReceiveLocation = new File(RECEIVE_LOCATION);

		if (!initReceiveLocation.exists()) {
			initReceiveLocation.mkdir(); // Create Directory
		}
	}

	public static Server getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Server();
		}
		return uniqueInstance;
	}

	/**
	 * 해당 그룹에 사용자 추가
	 * 
	 * @param key
	 *            그룹 고유 키
	 * @return 그룹의 존재 여부. 그룹이 존재하면 true, 그렇지 않으면 false
	 */
	public Group getGroupByPrimaryKey(String key) {

		Set<String> set = groups.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			System.out.print(it.next() + ", ");
		}
		System.out.println();

		Group targetGroup = groups.get(key);
		if (targetGroup != null) {
		}
		return targetGroup;
	}

	/**
	 * 그룹 생성 후 서버의 그룹 목록에 추가
	 * 
	 * @return 생성된 그룹 객체
	 */
	public Group createGroup() {
		String groupKey = generatePrimaryKey(5);
		System.out.println("할당된 그룹 키는 " + groupKey);

		Group newGroup = new Group(groupKey);
		groups.put(groupKey, newGroup);
		return newGroup;
	}

	/** 그룹 안의 모든 사용자가 나가면 그룹 목록에서 그룹 삭제 후 dir 삭제 */
	public void destroyGroup(String groupPrimaryKey) {
		groups.remove(groupPrimaryKey);

		deleteAllFilesInGroupDir(RECEIVE_LOCATION + File.separator + groupPrimaryKey);
	}

	/** Delete all files in group directory */
	public void deleteAllFilesInGroupDir(String parentDirPath) {
		// Get the files in the folder into an array.
		File file = new File(parentDirPath);

		if (file.exists()) {
			File[] tempFile = file.listFiles();

			if (tempFile.length > 0) {
				for (int i = 0; i < tempFile.length; i++) {
					if (tempFile[i].isFile()) {
						tempFile[i].delete();
					}
					else { // Recursive function
						deleteAllFilesInGroupDir(tempFile[i].getPath());
					}
					tempFile[i].delete();
				}
				file.delete();
			}
		}
	}

	public void removeGroup(Group group) {
		Group removeGroup = groups.remove(group.getPrimaryKey());
		if (removeGroup != null) {
			deleteAllFilesInGroupDir(removeGroup.getPrimaryKey());
		}
	}

	/**
	 * 영어 대문자, 소문자, 숫자로 혼합된 문자열 생성.
	 * 
	 * @param length
	 *            생성될 문자열 길이
	 * @return 생성된 문자열
	 */
	private String generatePrimaryKey(int length) {
		StringBuffer temp = new StringBuffer();
		Random rnd = new Random();
		// for (int i = 0; i < length; i++) {
		// int rIndex = rnd.nextInt(2) + 1; // 1 또는 2인 랜덤 정수
		// switch (rIndex) {
		// // case 0:
		// // temp.append((char) ((int) (rnd.nextInt(26)) + 65));
		// // bre;
		// case 1:
		// temp.append((char) ((int) (rnd.nextInt(26)) + 97)); //소문자 알파벳
		// break;
		// case 2:
		// temp.append((rnd.nextInt(10))); //0~9 사이의 랜덤 정수
		// break;
		// }
		// }
		//
		// int rindex = rnd.nextInt(length) + 1; // 1~length 사이의 랜덤 정수

		for (int i = 0; i < 2; i++) {
			temp.append((char) ((int) (rnd.nextInt(26)) + 97)); // 소문자 알파벳
		}
		for (int i = 0; i < length - 2; i++) {
			temp.append((rnd.nextInt(10))); // 0~9 사이의 랜덤 정수
		}

		return temp.toString();
	}

	/** the method for test and debug. */
	public static void subDirList(String source) {
		File dir = new File(source);
		File[] fileList = dir.listFiles();
		try {
			for (int i = 0; i < fileList.length; i++) {
				File file = fileList[i];
				if (file.isFile()) {
					System.out.println("File name = " + file.getPath());
				}
				else if (file.isDirectory()) {
					System.out.println("Dir name = " + file.getPath());
					subDirList(file.getCanonicalPath().toString());
				}
			}
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, a hh:mm:ss");
		System.out.println(sdf.format(date).toString());
	}

	class ServerStatusCheck extends Thread {

		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			String input = null;
			while (true) {
				input = sc.nextLine();
				switch (input) {
				case "g":
					System.out.println("┌──────────────SERVER STATUS");
					System.out.println("├ Group Count: " + groups.size());
					System.out.println("├ Group List...  ");
					Set<String> set = groups.keySet();
					Iterator<String> it = set.iterator();
					while (it.hasNext()) {
						System.out.println("│\t- " + it.next());
					}
					break;

				default:
					Group search = groups.get(input);
					if (search == null) {
						System.out.println("[SERVER] Group named \"" + input + "\" is not found.");
					}
					else {
						List<String> list = search.getUserList();
						System.out.println("┌──────────────GROUP INFO: " + input);
						System.out.println("├ User Count: " + list.size());
						System.out.println("├ User List...  ");
						Iterator<String> listIt = list.iterator();
						while (listIt.hasNext()) {
							System.out.println("│\t- " + listIt.next());
						}
					}
					break;
				}
				System.out.println("└───────────────────────────");
				input = null;
			}
		}
	}
}
