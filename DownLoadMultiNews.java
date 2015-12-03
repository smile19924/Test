package test;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.xwtec.mss.work.entity.WikiinfoCustom;
import com.xwtec.mss.work.service.IWikiinfoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring/applicationContext.xml"})
@Transactional
public class DownLoadMultiNews extends AbstractJUnit4SpringContextTests {
	private ConcurrentLinkedQueue<String> notDoneUrls;
	private Set<String> doneUrls;
	static int count=0;
	
	@Autowired
	private IWikiinfoService wikiService;
	
	
	public DownLoadMultiNews() {
		super();
		// TODO Auto-generated constructor stub
	}


	public ConcurrentLinkedQueue<String> getNotDoneUrls() {
		return notDoneUrls;
	}


	public void setNotDoneUrls(ConcurrentLinkedQueue<String> notDoneUrls) {
		this.notDoneUrls = notDoneUrls;
	}


	public Set<String> getDoneUrls() {
		return doneUrls;
	}


	public void setDoneUrls(Set<String> doneUrls) {
		this.doneUrls = doneUrls;
	}


	public void addUrls(Document doc) {
		if(doc==null){
			return;
		}
		Elements links=doc.select("a[href]");
		if(links!=null){
			String urlStr=null;
			for(Element link:links){
				urlStr=link.attr("abs:href");
				if(urlStr!=null&&urlStr.endsWith("html")&&!doneUrls.contains(urlStr)&&!notDoneUrls.contains(urlStr)){
//					if(urlStr.contains("sina.com")||urlStr.contains("163.com")
//							||urlStr.contains("sohu.com")||urlStr.contains("ifeng.com")||urlStr.contains("qq.com")){
						notDoneUrls.add(urlStr);
					//}
				}
			}
		}
	}
	
	public WikiinfoCustom saveContents(Document doc,FileWriter fw) {
		//System.out.println("into save::"+doc.baseUri());
		//if(urlStr.contains("sina.com")||urlStr.contains("163.com")
//				||urlStr.contains("sohu.com")||urlStr.contains("ifeng.com")||urlStr.contains("qq.com")){
		
		//}
		if(doc==null||!doc.baseUri().endsWith("html")){
			return null;
		}
		
		//System.out.println("may save "+doc.baseUri());
		Elements keyWorde=doc.select("meta[name=\"keywords\"]");
		//Elements desce=doc.select("meta[name=\"description\"]");
		String keyWord=null;
		if(keyWorde.size()<1){
//			return;
			keyWord = "";
		} else {
			keyWord = keyWorde.attr("content");
		}
		String host=null;
		try {
			URL url =new URL(doc.baseUri());
			host=url.getHost();
			//System.out.println(host);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			return null;
		}
		if(host==null){
			return null;
		}
		String body=null;
		if(host.contains("sina.com")){
			body=getSinaBody(doc);
		}else if (host.contains("163.com")) {
			body=get163Body(doc);
		}else if(host.contains("sohu.com")){
			body=getSohuBody(doc);
		}else if(host.contains("ifeng.com")){
			body=getIfengBody(doc);
		}else if (host.contains("qq.com")) {
			body=getQQBody(doc);
		}else{
			body="";
		}
		
		
		//Elements para=bodye.select("p");
//		Element first=para.first();
//		if(first.hasAttr("class")){
//			return;
//		}
//		Elements firstSubNodes=first.select("a");
//		if(firstSubNodes.size()>0){
//			return;
//		}
//		Elements front=doc.select("#artibody");
//		if(front.size()>0){
//			body+=front.first().outerHtml();
//		}
//		for(Element p:para){
//			if(p.text().contains("1996-2015 SINA Corporation")){
//				break;
//			}
//			body+=p.outerHtml()+"\n";
//		}
		if(body.length()<1){
			return null;
		}
		//body=bodye.outerHtml();
//		String keyWord=keyWorde.attr("content");
		//String desc=desce.attr("content");
		String title=doc.title();
		
	/*	if(keyWord.length()<1||title.length()<1){
			return;
		}*/
		if(title.length()<1){
			return null;
		}
		try {
			fw.write("\n<url>\n"+doc.baseUri()+"\n<title>\n"+title+"\n<key>\n"+keyWord+"\n<body>\n"+body);
			WikiinfoCustom wiki = new WikiinfoCustom();
			wiki.setTitle(title);
			wiki.setLabelname(keyWord);
			wiki.setDetail(body);
			wiki.setCreatemanid(3l);
			wiki.setMajorcode(2);
			wiki.setSystemcode(201);
			wiki.setModulecode(20102);
			wiki.setStatusid(203);
			System.out.println("saving");
			count++;
			return wiki;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public String getSinaBody(Document doc){
		String body="";
		Elements bodye=doc.select("#artibody");
		if(bodye.size()<1){
			return body;
		}else{
			body+=bodye.first().outerHtml();
			return body;
		}
		
	}
	public String get163Body(Document doc){
		String body="";
		Elements bodye=doc.select("#endText");
		if(bodye.size()<1){
			return body;
		}else{
			body+=bodye.first().outerHtml();
			return body;
		}
	}
	
	public String getSohuBody(Document doc){
		String body="";
		Elements bodye=doc.select("#contentText");
		if(bodye.size()<1){
			return body;
		}else{
			body+=bodye.first().outerHtml();
			return body;
		}
	}
	
	public String getIfengBody(Document doc){
		String body="";
		Elements bodye=doc.select("#artical_real");
		if(bodye.size()<1){
			return body;
		}else{
			body+=bodye.first().outerHtml();
			return body;
		}
	}
	
	public String getQQBody(Document doc){
		String body="";
		Elements bodye=doc.select("#Cnt-Main-Article-QQ");
		if(bodye.size()<1){
			return body;
		}else{
			body+=bodye.first().outerHtml();
			return body;
		}
	}
	public Document getDoc(){
		String urlStr=notDoneUrls.poll();
		doneUrls.add(urlStr);
		if(urlStr==null || urlStr.length()<5){
			return null;
		}
		Document doc=null;
		int tryTimes=0;
		while(tryTimes++<3){			
				try {
					doc=Jsoup.connect(urlStr).get();
					System.out.println(urlStr);
					break;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(urlStr +"::try again");
					try {
						Thread.sleep(new Random().nextInt(2000));
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("url not right");
				}
		}		
		return doc;	
	}
	
	@Test
	public  void test() {
		// TODO Auto-generated method stub
		File file=new File("D:\\data\\d\\obj");
		ConcurrentLinkedQueue<String> notDoneUrls = null;
		Set<String> doneUrls = null;
		if(file.exists()){
			try {
				Files.copy(file.toPath(), new File("D:\\data\\d\\objtemp").toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try (ObjectInputStream ois=new ObjectInputStream(new FileInputStream(file));){
				notDoneUrls=(ConcurrentLinkedQueue<String>) ois.readObject();
				doneUrls=(Set<String>) ois.readObject();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			notDoneUrls=new ConcurrentLinkedQueue<>();
			doneUrls=Collections.synchronizedSet(new HashSet<String>());
		}
		notDoneUrls.add("http://news.sina.com.cn/");
		DownLoadMultiNews dn=new DownLoadMultiNews();
		dn.setDoneUrls(doneUrls);
		dn.setNotDoneUrls(notDoneUrls);
		Document doc;
		FileWriter fw=null;
		ObjectOutputStream oos=null;
		try{
			fw=new FileWriter("D:\\data\\d\\data.txt",true);
			oos=new ObjectOutputStream(new FileOutputStream(file));
			while(count<10000){
				doc=dn.getDoc();
				WikiinfoCustom wiki = dn.saveContents(doc, fw);
				if (wiki != null) {
					wikiService.save(wiki);
				}
				//System.out.println("ready to addUrl");
				dn.addUrls(doc);
//				if(notDoneUrls.size()==0){
//					break;
//				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try{
				fw.flush();
				oos.writeObject(notDoneUrls);
				oos.writeObject(doneUrls);
				oos.flush();
				fw.close();
				oos.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
	}

}

