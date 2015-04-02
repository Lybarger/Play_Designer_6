package uw.playdesigner6;

//Code from:
//http://www.androidhive.info/2011/11/android-xml-parsing-tutorial/

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class XMLParser {




    // constructor
    public XMLParser() {

    }

    /**
     * Getting XML from URL making HTTP request
     * @param url string
     * */
    public String getXmlFromUrl(String url) {
        String xml = null;

        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // return XML
        return xml;
    }

    /**
     * Getting XML DOM element
     * @param XML string
     * */
    public Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }

        return doc;
    }

    /** Getting node value
     * @param elem element
     */
    public final String getElementValue( Node elem ) {
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.TEXT_NODE  ){
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }
    public Map<String,String> getStage(String XML, int stageNumber){
        final String KEY_STAGE = "stage";
        final String KEY_PLAYER = "player";
        final String KEY_STAGE_NUMBER= "stage_number";
        final String KEY_XY="xy";
        final String KEY_ID="id";
        Map<String, String> result = new HashMap<String, String >();


        Node child;
        Node grandchild;

        Document document = getDomElement(XML);

        //Get list of all STAGES
        NodeList stageList = document.getElementsByTagName(KEY_STAGE);

        int stageCount =stageList.getLength();

        boolean stageRemaining=stageNumber <= stageCount-1;

        //Loop through each STAGE
        if (stageRemaining) {
            //for (int i = 0; i < stageList.getLength(); i++) {
            System.out.println("Current stage: " + String.valueOf(stageNumber));
            //Current STAGE node
            Node currentItem = stageList.item(stageNumber);

            //Current STAGE element
            Element currentElement = (Element) currentItem;

            //Make sure current STAGE element is not empty
            if (currentElement != null) {

                //Determine if current STAGE element has children
                if (currentElement.hasChildNodes()) {

                    //Loop through children within STAGE
                    int j = 0;
                    for (child = currentElement.getFirstChild(); child != null; child = child.getNextSibling()) {

                        j++;
                        Element childElement = (Element) child;
                        String childName = childElement.getNodeName();

                        System.out.println("Child: " + String.valueOf(j) + " " + childName);

                        System.out.println(getValue(childElement, KEY_ID));
                        System.out.println(getValue(childElement, KEY_XY));

                        String idValue=getValue(childElement, KEY_ID);
                        String xyValue=getValue(childElement, KEY_XY);
                        result.put(idValue,xyValue);

                    }
                }
            }
        }

        return result;
    }

    /**
     * Getting node value
     * @param Element node
     * @param key string
     * */
    public String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }
}
