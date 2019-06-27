package ie.tcd.scss.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XMLable {

	public Element toXml(Document doc);
	
}
