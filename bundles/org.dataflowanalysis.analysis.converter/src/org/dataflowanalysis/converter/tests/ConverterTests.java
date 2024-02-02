package org.dataflowanalysis.converter.tests;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;


import org.dataflowanalysis.converter.*;
import org.dataflowanalysis.converter.webdfd.*;

public class ConverterTests {
	
	@Test
	public void test() {
		Main.readWeb("minimal.json");
		Main.readDFD("minimal","test5.json");
		assertTrue(true);
		
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File("minimal.json");
		DFD webBefore = null;
        try {
            webBefore = objectMapper.readValue(file, DFD.class);
            new ProcessJSON().processWeb(file.getName().replaceAll("\\.json.*", ""),webBefore);
        } 
        catch (IOException e) {}
        
        objectMapper = new ObjectMapper();        
		file = new File("test5.json");
		DFD webAfter = null;
        try {
            webAfter = objectMapper.readValue(file, DFD.class);
            new ProcessJSON().processWeb(file.getName().replaceAll("\\.json.*", ""),webAfter);
        } 
        catch (IOException e) {}
                
        webBefore.labelTypes().sort(Comparator.comparing(WebLabelType::id));
        webAfter.labelTypes().sort(Comparator.comparing(WebLabelType::id));
        assertEquals(webBefore.labelTypes(),webAfter.labelTypes());
        
        List<Child> childrenBefore = webBefore.model().children();
        List<Child> childrenAfter = webAfter.model().children();

        childrenBefore.sort(Comparator.comparing(Child::id));
        childrenAfter.sort(Comparator.comparing(Child::id));
                
        List<Child> combined=new ArrayList<>(childrenBefore);
        combined.addAll(childrenAfter);
        for(Child child: combined) {
        	if(child.labels()!=null) {
            	child.labels().sort(Comparator.comparing(WebLabel::labelTypeId).thenComparing(WebLabel::labelTypeValueId));
        	}
        	if(child.ports() != null) {
            	child.ports().sort(Comparator.comparing(Port::id));

        	}
        }
                
        assertEquals(childrenBefore.size(),childrenAfter.size());
        
        for(int i=0;i<childrenBefore.size();i++) {
        	Child childBefore = childrenBefore.get(i);
        	Child childAfter = childrenAfter.get(i);

        	assertEquals(childBefore.type(),childAfter.type());
        	assertEquals(childBefore.id(),childAfter.id());
        	assertEquals(childBefore.text(),childAfter.text());
        	assertEquals(childBefore.children(),childAfter.children());
        	if(childBefore.type().split(":")[0].equals("node")) {
        		assertEquals(childBefore.labels(),childAfter.labels());
        		assertEquals(childBefore.ports().size(),childAfter.ports().size());
        		for(int j=0;j<childBefore.ports().size();j++) {
        			Port portBefore = childBefore.ports().get(j);
        			Port portAfter = childAfter.ports().get(j);
        			assertEquals(portBefore.type(),portAfter.type());
                	assertEquals(portBefore.id(),portAfter.id());
                	assertEquals(portBefore.children(),portAfter.children());
                	if(portBefore.type().equals("port:dfd-output")) {
                		assertEquals(portBefore.behavior(),portAfter.behavior());
                	}
        		}
        	}
        	else if(childBefore.type().split(":")[0].equals("edge")) {
        		assertEquals(childBefore.sourceId(),childAfter.sourceId());
        		assertEquals(childBefore.targetId(),childAfter.targetId());
        		
        	}
        }
	}
}
