package org.eclipse.jetty.xml;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class XmlAppendableTest
{
    @Test
    public void test() throws Exception
    {
        StringBuilder b = new StringBuilder();
        XmlAppendable out = new XmlAppendable(b);
        Map<String,String> attr = new HashMap<>();
        
        out.open("test");
        
        attr.put("name","attr value");
        attr.put("noval",null);
        attr.put("quotes","'\"");
        
        out.tag("tag");
        out.tag("tag",attr);
        out.tag("tag",attr,"content");
        
        out.open("level1").tag("tag","content").tag("tag","content").close();
        out.open("level1",attr).open("level2").tag("tag","content").tag("tag","content").close().close();
        
        out.close();
        
        String s = b.toString();
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<test>\n  <tag/>\n  <tag quotes=\"&apos;&quot;\" name=\"attr value\" noval=\"\"/>\n  <tag quotes=\"&apos;&quot;\" name=\"attr value\" noval=\"\">content</tag>\n  <level1>\n    <tag>content</tag>\n    <tag>content</tag>\n  </level1>\n  <level1 quotes=\"&apos;&quot;\" name=\"attr value\" noval=\"\">\n    <level2>\n      <tag>content</tag>\n      <tag>content</tag>\n    </level2>\n  </level1>\n</test>\n",s);        
    }
}
