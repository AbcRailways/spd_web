package importpackage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;


@Stateless
@LocalBean
public class MetadataWorker {

	public void makeCustomProperty(String pth, String attrbname, String attrbvalue) throws Exception{
		Path path = new File(pth).toPath();
		UserDefinedFileAttributeView udfav = Files.getFileAttributeView(path,
				UserDefinedFileAttributeView.class);
		udfav.write(attrbname,
				Charset.defaultCharset().encode(attrbvalue));			
	}

	//Возвращает значение конкретного пользовательского аттрибута
	public String readCustomProperty(String pth, String attrbname) throws IOException {
		Path path = new File(pth).toPath();
		UserDefinedFileAttributeView udfav = Files.getFileAttributeView(path,
				UserDefinedFileAttributeView.class);
		int size = udfav.size(attrbname);
		ByteBuffer bb = ByteBuffer.allocateDirect(size);
		udfav.read(attrbname, bb);
		bb.flip();		
		return Charset.defaultCharset().decode(bb).toString();		
	}
	
	//Выводит в консоль список все пользовательских аттрибутов и их значений
	public static void listAllCustomProperty(String pth) throws Exception{
		int size = 0;
		ByteBuffer bb = null;
		Path path = new File(pth).toPath();
		UserDefinedFileAttributeView udfav = Files.getFileAttributeView(path,
				UserDefinedFileAttributeView.class);
		List<String> attList = udfav.list();		
		for(int k=0; k<attList.size(); k++) {
			size = udfav.size(attList.get(k));
			bb = ByteBuffer.allocateDirect(size);
			udfav.read(attList.get(k), bb);
			bb.flip();
			 System.out.println(attList.get(k)+" - "+Charset.defaultCharset().decode(bb).toString());
			}
	}
	
	/*public void call() throws Exception{
		makeCustomProperty("C:/encrypted.pdf","projid","123");
		makeCustomProperty("C:/encrypted.pdf","userid","3");		
	}*/
	
}
