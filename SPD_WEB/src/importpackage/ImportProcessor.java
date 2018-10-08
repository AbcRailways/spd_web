package importpackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;



@Stateless
@LocalBean
@Asynchronous
public class ImportProcessor {

	@EJB
	MetadataWorker metadataWork;

	@Resource(mappedName = "jdbc/DB2Dsapp")
	private DataSource dataSource;	


	private Connection conn;

	public volatile boolean isCancelled = false;
	private Path sourceImportPath = Paths.get("C:\\Test\\import");
	

	private String projectId = null;
	private String userId = null;
	private String ext = null;

	@Asynchronous
	public void dirHandler() {

		WatchService service = null;

		try {
			Boolean isFolder = (Boolean) Files.getAttribute(sourceImportPath, "basic:isDirectory",
					LinkOption.NOFOLLOW_LINKS);
			if (!isFolder) {
				throw new IllegalArgumentException("Это не папка : " + sourceImportPath);
			}
		} catch (IOException ioe) {
			System.out.println("Ошибка ввода вывода : " + sourceImportPath);

		}

		System.out.println("Сканирование папки: " + sourceImportPath);

		FileSystem fs = sourceImportPath.getFileSystem();

		try {
			service = fs.newWatchService();

			sourceImportPath.register(service, StandardWatchEventKinds.ENTRY_CREATE);

			for (;;) {				
				WatchKey key = service.take();
				Kind<?> kind = null;
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					kind = watchEvent.kind();
					if (StandardWatchEventKinds.OVERFLOW == kind) {
						continue; 
					} else if (StandardWatchEventKinds.ENTRY_CREATE == kind) {
						Path newPath = ((WatchEvent<Path>) watchEvent).context();					
						try {
							if (newPath.toString().equals("exit.txt")) {								
								isCancelled=true;
								break;
							}
							System.out.println(
									"|----- Найден новый файл : " + newPath + " numKey " + key.toString() + "-----|");
							System.out.println("|----- Обрабатываем -----|");
							System.out.println(sourceImportPath.toString() + "\\" + newPath);
							String ext = FilenameUtils.getExtension(sourceImportPath.toString() + "\\" + newPath);							
							if (ext.equals("pdf")) {
								System.out.println("Файл pdf");
								try {
									/*projectId = metadataWork.readCustomProperty(sourceImportPath.toString() + "/" + newPath, "projid");
									userId = metadataWork.readCustomProperty(sourceImportPath.toString() + "/" + newPath, "userid");*/
									String[] parts = newPath.toString().split("\\.");		
									userId = Arrays.asList(parts).get(0);
									projectId = Arrays.asList(parts).get(1);											
									importDoc(projectId, userId, sourceImportPath.toString(), newPath.toString());									
									projectId = null;
									userId = null;
									ext = null;
									System.out.println("|----- Готово -----|");
								} catch (Exception e) {
									System.out.println("Отсутствует один или несколько аттрибутов!");
									e.printStackTrace();
								}
							} else {
								System.out.println("Это не pdf");
								Files.delete(Paths.get(sourceImportPath.toString() + "\\" + newPath));
							}	
							
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
				
				if (key != null) {
					boolean valid = key.reset();
					if (!valid)
						break; 
								
				}
				if (isCancelled) {
					System.out.println("Закрываю асинхронный цикл");
					break;
				}
				
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} finally {
			try {
				service.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void importDoc(String projid, String userid, String fpath, String fname) {
		String username = null;
		String id_user = null;
		try {
			this.conn = dataSource.getConnection();
			PreparedStatement ps;
			String sql = "select user_id, username from users where username=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, userid);
			ResultSet rs1 = ps.executeQuery();
			while (rs1.next()) {
				username = rs1.getString("USERNAME");
				id_user = rs1.getString("USER_ID");
			}
			rs1.close();
			ps.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}


		String path = "C:\\SPD_WEB\\" + username + "\\";
		Timestamp dt = new Timestamp(System.currentTimeMillis());
		path = path + dt.toString().substring(0, 4) + "\\" + dt.toString().substring(5, 7) + "\\"
				+ dt.toString().substring(8, 10);
		new File(path).mkdirs();
		path = path + "\\" + fname;
		Path from = Paths.get(fpath + "\\" + fname);
		Path to = Paths.get(path);
		try {
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("Не удалось переместить файл");
		}		
		try {			
			this.conn = dataSource.getConnection();
			String sql = "insert into LINK_DOC (USER_ID, NAME_DOC, LINK_DOC, DOC_ID, ID_PROJECT, D_T_CREATE, THEME_ID, TYPE_ID)"
					+ "values(?,?,?,?,?,?,?,?)";			
			try (PreparedStatement ps = conn.prepareStatement(sql);) {
				ps.setInt(1, Integer.parseInt(id_user));
				ps.setString(2, fname);
				ps.setString(3, path);
				ps.setString(4,
						(dt.toString().substring(0, 4) + dt.toString().substring(5, 7) + dt.toString().substring(8, 10)
								+ dt.toString().substring(11, 13) + dt.toString().substring(14, 16)
								+ dt.toString().substring(17, 19)));
				ps.setInt(5, Integer.parseInt(projid));
				ps.setTimestamp(6, dt);	
				ps.setShort(7, (short) 999);
				ps.setShort(8, (short) 999);
				ps.executeUpdate();
				conn.commit();
				ps.close();				
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}

}
