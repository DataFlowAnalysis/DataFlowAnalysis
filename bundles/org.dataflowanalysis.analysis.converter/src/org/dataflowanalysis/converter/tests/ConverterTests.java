package org.dataflowanalysis.converter.tests;

import org.junit.jupiter.api.*;

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
	@DisplayName("Assure WebDFD=convert(convert(WebDFD))")
	public void webToDfdToWeb() {
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
        
        assertEquals(webBefore,webAfter);
	}
}
