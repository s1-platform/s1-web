package test;

import org.s1.web.formats.Soap;
import org.s1.web.services.WebOperation;
import org.s1.web.services.WebOperationInput;
import org.s1.web.services.WebOperationOutput;
import org.s1.web.services.formats.FormData;
import org.s1.web.services.formats.JSONData;
import org.s1.web.services.formats.SOAPData;
import org.s1.web.services.formats.TextData;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Grigory Pykhov
 */
public class Test1Service extends WebOperation {

    @Override
    protected WebOperationOutput process(WebOperationInput input) {
        final FormData f = input.asForm();
        //return new TextData("HELLO, "+input.asForm().getFirst("name"));
        /*return new JSONData(new HashMap<String, Object>(){{
            put("name", f.getFirst("name"));
            put("date",new Date());
        }});*/
        return new SOAPData(Soap.fromSOAPString("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cry=\"http://s1-platform.com/crypto\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <cry:SignRequest>\n" +
                "         <type>smev</type>\n" +
                "         <key>test</key>\n" +
                "         <data>123</data>\n" +
                "         <data2></data2>\n" +
                "      </cry:SignRequest>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>", "UTF-8"));
    }
}
