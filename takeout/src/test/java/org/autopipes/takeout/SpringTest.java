package org.autopipes.takeout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.autopipes.takeout.TakeoutInfo.Cut;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/test-context.xml"})
public class SpringTest{
// AbstractDependencyInjectionSpringContextTests{
    private static Logger logger = Logger.getLogger(SpringTest.class);

    public SpringTest(){
//    	super();
//	    setAutowireMode(AbstractDependencyInjectionSpringContextTests.AUTOWIRE_BY_NAME);
    }
    // specify the BeanConfigurationFiles to use for auto-wiring the properties of this class
//	@Override
//	protected String[] getConfigLocations() {
//	        return new String[]{"test-context.xml"};
//	    }

    @Test
	public void testSpringSetup(){
        assertNotNull(root);
        logger.info(root);
        TakeoutInfo ti = root.takeoutInfo(Diameter.D1);
        assertNotNull(ti);
        Diameter dl = ti.getDrillLimit(Attachment.mechanical);
        assertEquals(dl, Diameter.D1);
        BigDecimal val = ti.getGroovedByAngle().get(Angle.deg90).getByVendor().get(Vendor.FIRELOCK);
        assertEquals(val, new BigDecimal(10));
        BigDecimal val2 = ti.getGroovedByAngle().get(Angle.deg45).getByDiameter().get(Diameter.D1);
        assertEquals(val2, new BigDecimal(6));
        Cut cut = ti.getByDiameter().get(Diameter.D1);
        val = cut.getByAttachment().get(Attachment.welded);
        assertEquals(val, new BigDecimal(0.5));
	}

	// injected by Spring
    @Autowired
	private TakeoutRepository root;

	public TakeoutRepository getTakeoutRepo() {
		return root;
	}
	public void setTakeoutRepo(final TakeoutRepository root) {
		this.root = root;
	}

}
