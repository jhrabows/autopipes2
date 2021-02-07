package org.autopipes.takeout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class TakeoutTest extends TestCase {
   public void testPage() throws Exception{
	   FileOutputStream fos = new FileOutputStream("C:/temp/page.html");
	   OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
	   osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
	   osw.write("<html><body>\n");
	   for(Diameter pd : Diameter.values()){
		   System.out.println(pd);
		   osw.write(pd.getDisplay() + "(" + pd.getMeasure() + ")" + "<br/>\n");
	   }
	   osw.write("</body></html>\n");
	   osw.close();
   }

   private void deserializeRepo(final String name)throws Exception{
		JAXBContext jaxbContext = JAXBContext.newInstance(
				TakeoutInfo.class,
				TakeoutRepository.class,
				Diameter.class,
				TakeoutInfo.Cut.class,
				Vendor.class);

		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		TakeoutRepository repo = (TakeoutRepository)unmarshaller.unmarshal(new File(name));
	    marshaller.marshal(repo, System.out);

   }

   /*
       public void testRepoDeserialization()throws Exception{
	   deserializeRepo("C:/mydev/ap2/takeout/src/main/conf/takeoutrepo.xml");
   }

   public void testDeserialization()throws Exception{
		JAXBContext jaxbContext = JAXBContext.newInstance(
				TakeoutInfo.class,
		        TakeoutRepository.class,
				Diameter.class,
				Root.Info.class,
				TakeoutInfo.Cut.class,
				Vendor.class);

		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		TakeoutRepository root = (TakeoutRepository)unmarshaller.unmarshal(new File("C:/temp/testtakeout.xml"));
        root.initLookup();
		System.out.println(root.getClass());
		TakeoutInfo info =  root.takeoutInfo(Diameter.D1);
		System.out.println(info.getByVendor().get(Vendor.FIRELOCK));
	    marshaller.marshal(root, System.out);

	    TakeoutInfo ti = (TakeoutInfo)unmarshaller.unmarshal(new File("C:/temp/testinfo.xml"));
	    marshaller.marshal(ti, System.out);

	    ti = (TakeoutInfo)unmarshaller.unmarshal(new File("C:/temp/testTakeoutInfo.xml"));
	    marshaller.marshal(ti, System.out);
   }
*/
   private TakeoutInfo createInfo(){
	   TakeoutInfo ret = new TakeoutInfo();
	   ret.setOutlet(Diameter.D1);

	   TakeoutInfo.Cut cut = new TakeoutInfo.Cut();
	   cut.getByAngle().put(Angle.deg0, new BigDecimal(0.1));
	   cut.getByAngle().put(Angle.deg45, new BigDecimal(0.2));
	   cut.getByAttachment().put(Attachment.mechanical, new BigDecimal(.3));
		   cut.getByAngle().put(Angle.deg90, new BigDecimal(0.5));
		TakeoutInfo.Cut cut2 = new TakeoutInfo.Cut();
		cut2.getByAttachment().put(Attachment.mechanical, new BigDecimal(.3));
		cut2.getByAttachment().put(Attachment.welded, new BigDecimal(.5));

		cut2.getByAngle().put(Angle.deg90, new BigDecimal(0.5));

		TreeMap<Diameter, TakeoutInfo.Cut> dcMap = new TreeMap<Diameter, TakeoutInfo.Cut>();
		dcMap.put(Diameter.D1, cut);
		dcMap.put(Diameter.D2, cut2);
		
		TreeMap<Attachment, Diameter> dlMap = new TreeMap<Attachment, Diameter>();
		dlMap.put(Attachment.mechanical, Diameter.D1);
		dlMap.put(Attachment.welded, Diameter.D2);

	   TreeMap<Vendor, BigDecimal> byVendor = new TreeMap<Vendor, BigDecimal>();
	   byVendor.put(Vendor.FIRELOCK, new BigDecimal(10));
	 //  ret.setByVendor(byVendor);
	   TakeoutInfo.GroovedCut gvdcut = new TakeoutInfo.GroovedCut();
	   TakeoutInfo.GroovedCut gvdcut2 = new TakeoutInfo.GroovedCut();
	   gvdcut.setByVendor(byVendor);
	   gvdcut2.getByDiameter().put(Diameter.D1, new BigDecimal(6));
	   ret.getGroovedByAngle().put(Angle.deg90, gvdcut);
	   ret.getGroovedByAngle().put(Angle.deg45, gvdcut2);
	   
	   ret.setByDiameter(dcMap);
		ret.setDrillLimits(dlMap);
		ret.setTargetHole(new BigDecimal(6));
		ret.setMain(true);

	   return ret;
   }

   private TakeoutRepository createTest(){

	   TakeoutInfo info = createInfo();
	   TakeoutRepository takeout = new TakeoutRepository();
		List<TakeoutInfo> list = new ArrayList<TakeoutInfo>();
		list.add(info);
		takeout.setInfoList(list);

	    return takeout;
   }
   private Fitting createFitting(){
	   Fitting ret = new Fitting(Fitting.Type.Coupling,
			   Attachment.grooved , Vendor.FIRELOCK,
			   new Diameter[] {Diameter.D1, Diameter.D1},
			   null);
	   
	   return ret;
   }

   public void testSerialization()throws Exception{
		JAXBContext jaxbContext = JAXBContext.newInstance(
				TakeoutInfo.class,
		        TakeoutRepository.class,
				Diameter.class,
				Attachment.class,
				TakeoutInfo.Cut.class,
				Vendor.class);

		Marshaller marshaller = jaxbContext.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	    marshaller.marshal(createTest(), System.out);
/*
	    TakeoutInfo ti = new TakeoutInfo();
	    TreeMap<Vendor, BigDecimal> map = new TreeMap<Vendor, BigDecimal>();
	    map.put(Vendor.FIRELOCK, new BigDecimal(10));
	    ti.setByVendor(map);
	    marshaller.marshal(ti, System.out);
*/
   }
   public void testFittingSerialization() throws JAXBException{
	   
		JAXBContext jaxbContext = JAXBContext.newInstance(
				Fitting.class);

		Marshaller marshaller = jaxbContext.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(createFitting(), System.out);
   }
   
   private static String TEST_FITTING =
   "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
   +"<ns2:fitting xmlns:ns2=\"http://dwg.autopipes.org\">"
       +"<type>Coupling</type>"
       +"<attachment>grooved</attachment>"
       +"<vendor>FIRELOCK</vendor>"
       +"<diameter>D1</diameter><diameter>D2</diameter>"
   +"</ns2:fitting>";
   
   public void testFittingDeserialization() throws JAXBException, UnsupportedEncodingException{
	   
		JAXBContext jaxbContext = JAXBContext.newInstance(
				Fitting.class);

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		InputStream is = new ByteArrayInputStream(
				TEST_FITTING.getBytes("UTF-8"));
		Object f = unmarshaller.unmarshal(is);
		assertNotNull(f);
		assertTrue(f.getClass() == Fitting.class);

		Marshaller marshaller = jaxbContext.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	    marshaller.marshal(f, System.out);
		assertEquals(2, ((Fitting)f).getDiameterList().size());
  }
}
