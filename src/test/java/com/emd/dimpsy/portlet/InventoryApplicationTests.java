package com.emd.dimpsy.portlet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.emd.dimpsy.portlet.InventoryApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InventoryApplication.class)
@WebAppConfiguration
public class InventoryApplicationTests {

	@Test
	public void contextLoads() {
	}

}
