package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PdfSigner.CryptoStandard;
import com.itextpdf.signatures.PrivateKeySignature;

@ManagedBean(name = "pdfMB")
@ViewScoped
public class PdfMB implements Serializable {
	private static final long serialVersionUID = -434036691623610471L;
	public static X509Certificate x509;

	public static void sign(String src, String dest, Certificate[] chain, PrivateKey pk, String digestAlgorithm,
			String provider, CryptoStandard subfilter, String reason, String location) throws Exception {
		// Creating the reader and the signer

		System.out.println(src + "|" + dest);
		PdfFont fonttnw = PdfFontFactory.createFont("SSR56__C.TTF", "CP1251", true); // Поддержка
																						// русека
		ImageData imgdt = ImageDataFactory.create("ktz1.png"); // картиночка
		try {
			PdfDocument pdfDoc = new PdfDocument(new PdfReader(src));
			PdfReader reader = new PdfReader(src);
			PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), false);
			int pages = pdfDoc.getNumberOfPages();
			pdfDoc.close();
			PdfSignatureAppearance appearance = signer.getSignatureAppearance().setReason(reason).setLocation(location)
					.setReuseAppearance(false);
			appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
			// appearance.setSignatureGraphic(imgdt); изображение рядом
			appearance.setImage(imgdt); // изображение фоном
			appearance.setLayer2Text(x509.getIssuerDN() + "\n" + "Дата подписания: " + signer.getSignDate().getTime()
					+ "\n" + "Основание: Утверждено");
			appearance.setLayer2Font(fonttnw);
			Rectangle rect = new Rectangle(2, 2, 150, 150);
			appearance.setPageRect(rect).setPageNumber(pages);
			signer.setFieldName("sig");
			// Creating the signature
			IExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, provider);
			IExternalDigest digest = new BouncyCastleDigest();
			signer.signDetached(digest, pks, chain, null, null, null, 0, subfilter);
			reader.close();
			File forig = new File(src);
			File fsig = new File(dest);
			forig.delete();
			fsig.renameTo(forig);
		} catch (IOException e) {
			throw new IOException(e);
		}

	}

	public static void initkey(String doc, String keystore, char storepass[], String keyalias, char aliaspass[])
			throws Exception {
		// char password[] = "qwe".toCharArray();
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		KeyStore ks = KeyStore.getInstance("PKCS12"); 
		ks.load(new FileInputStream(keystore), storepass);
		// String alias = "pupkinvru";
		PrivateKey pk = (PrivateKey) ks.getKey(keyalias, aliaspass); 
		Certificate[] chain = ks.getCertificateChain(keyalias);
		x509 = (X509Certificate) ks.getCertificate(keyalias);
		System.out.println("|" + x509.getIssuerDN() + "|");
		sign(doc, doc + "(signed)", chain, pk, DigestAlgorithms.SHA256, provider.getName(), CryptoStandard.CMS,
				"Утвержденно", "Подписанно");
	}

	public static void htmlToPdf(String html, String path) throws Exception {	
		ConverterProperties cp = new ConverterProperties();
		FontProvider provider = new DefaultFontProvider(true, true, true);
		cp.setFontProvider(provider);
		cp.setCharset("CP1251");
		PdfWriter writer = new PdfWriter(path);
		//HtmlConverter.convertToPdf(new File("C:/ru.html"), new File("C:/ru1.pdf"), cp);	
		HtmlConverter.convertToPdf(html, writer, cp);
		writer.close();
		
	}

}
